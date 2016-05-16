package com.arshiya.messagingapp;

import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.arshiya.messagingapp.model.SmsModel;
import com.arshiya.messagingapp.receivers.SmsDeliveredReceiver;
import com.arshiya.messagingapp.receivers.SmsSentReceiver;

import org.w3c.dom.Text;

import io.realm.Realm;

public class NewMessage extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = NewMessage.class.getSimpleName();
    private ContactsAdapter mContactsAdapter;
    private Uri mUri;
    private String[] mProjection ;
    private EditText mToAddress;
    private ListView mContactsListView;
    private EditText mMessage;
    private Realm mRealm;
    private String mAddress;
    private long mThread_ID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(getResources().getString(R.string.neew_message));

        mUri  = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        mProjection   = new String[] {BaseColumns._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};


        ImageButton send = (ImageButton) findViewById(R.id.send_sms);
        assert send != null;
        send.setOnClickListener(this);

        mMessage = (EditText) findViewById(R.id.message);

        mContactsListView = (ListView) findViewById(R.id.contacts_list_view);
        assert mContactsListView != null;
        mContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getAdapter().getItem(position);

                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) + ";";
                mAddress = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                updateToAddress(name, name.length());

                mContactsListView.setVisibility(View.GONE);

            }
        });


        mContactsAdapter = new ContactsAdapter(this, null, true);

        assert mContactsListView != null;
        mContactsListView.setAdapter(mContactsAdapter);

        mToAddress = (EditText) findViewById(R.id.to_address);
        mToAddress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mContactsListView.setVisibility(View.VISIBLE);
                return false;
            }
        });

        mContactsListView.setVisibility(View.GONE);

        mToAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "new text : " + s.toString());

                int len = s.length();

                if (len > 2 && (';' == s.charAt(len - 2))) {
                    Toast.makeText(NewMessage.this, "Please choose valid and one contact", Toast.LENGTH_SHORT).show();
                    mToAddress.setText("");
                    mAddress = null;
                } else {
                    updateListView(s.toString());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        mRealm = Realm.getDefaultInstance();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mRealm.isClosed()){
            mRealm = Realm.getDefaultInstance();

        }

        mRealm = Realm.getDefaultInstance();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.removeAllChangeListeners();
        if (!mRealm.isClosed()){
            mRealm.close();
            mRealm = null;
        }
    }

    private void updateToAddress(String toAddress, int selection) {
        mToAddress.setText(toAddress);
        mToAddress.setSelection(selection);
    }

    private void updateListView(String s) {
        String searchString = "%" + s + "%";
        Log.d(TAG, "search string : " + searchString);

        Cursor cursor = getContentResolver().query(mUri,
                mProjection,
                "( " + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+ " LIKE ? ) OR ( " + ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ? )",
                new String[] {searchString},
                null);

        mContactsAdapter.swapCursor(cursor);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
            case R.id.send_sms:
                onSendSmsClick();
                break;

            default:
                Log.d(TAG, "onClick - unknown");
        }
    }

    private void onSendSmsClick() {
        if (null == mAddress){

            if (!isAValidNumber(mToAddress.getText().toString())){
                return;
            } else {
                mAddress = mToAddress.getText().toString();
            }
        }

        //get Thread id
        SmsModel smsModel= mRealm.where(SmsModel.class).equalTo("ADDRESS", mAddress).findFirst();

        if (null == smsModel) {
            smsModel = new SmsModel();
            mThread_ID = smsModel.getOrCreateThreadId(mAddress);
        } else {
            mThread_ID = smsModel.getThreadId();
        }
        Log.d(TAG, "thread id  : " + mThread_ID);

        String message = mMessage.getText().toString();
        Log.d(TAG, "message : " + message);

        if (0 < message.length()) {
            sendMessage(mAddress, message);
        }
    }

    private boolean isAValidNumber(String address) {
        return PhoneNumberUtils.isGlobalPhoneNumber(address);
    }

    private void sendMessage(String destinationAddress, String message) {
        long nextId =  mRealm.where(SmsModel.class).max("_ID").longValue();
        Log.d(TAG, "next id : " + nextId);
        Intent sentIntent = new Intent(Constants.SENT + nextId);
        sentIntent.putExtra("id", nextId);
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                sentIntent, 0);

        Intent deliveredIntent = new Intent(Constants.DELIVERED + nextId);
        deliveredIntent.putExtra("id", nextId);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                deliveredIntent, 0);

        registerSmsSentReceiver(nextId);
        registerSmsDeliveredReceiver(nextId);

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(destinationAddress, null, message, sentPI, deliveredPI);
        addMessageToRealm(destinationAddress, message);

        //launch Conversation Activty
        launchConversationActivity();
    }

    private void launchConversationActivity() {
        Intent converssationIntent = new Intent(this, Conversation.class);
        converssationIntent.putExtra("thread_id", mThread_ID);
        converssationIntent.putExtra("title", mAddress);

        startActivity(converssationIntent);

        this.finish();
    }

    private void registerSmsDeliveredReceiver(long nextId) {
        registerReceiver(new SmsDeliveredReceiver(), new IntentFilter(Constants.DELIVERED + nextId));
    }

    private void registerSmsSentReceiver(long nextId) {
        registerReceiver(new SmsSentReceiver(), new IntentFilter(Constants.SENT + nextId));

    }

    private void addMessageToRealm(String address, String body){
        Log.d(TAG, "results size before adding :" + mRealm.where(SmsModel.class).findAll().size());
        Log.d(TAG, "Thread id : " + mThread_ID);

        try (Cursor c = getContentResolver().query(Telephony.Sms.Sent.CONTENT_URI,
                null, Telephony.Sms.ADDRESS + "=?", new String[]{ address}, Telephony.Sms.DATE_SENT + " DESC")) {


            mRealm.beginTransaction();
            SmsModel smsModel = mRealm.createObject(SmsModel.class);

            smsModel.setId();
            smsModel.setThreadId(mThread_ID);
            smsModel.setMessagesType(Constants.MESSAGE_TYPE_SENT);
            smsModel.setAddress(address);
            smsModel.setDateSent(System.currentTimeMillis());
            smsModel.setDate(System.currentTimeMillis());
            smsModel.setSubject(null);
            smsModel.setBody(body);

            if (null != c &&  0 > c.getCount()) {
                smsModel.setServiceCenter(c.getString(c.getColumnIndex(Telephony.Sms.SERVICE_CENTER)));
                smsModel.setSubscriptionId(c.getString(c.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)));
                smsModel.setErrorCode(c.getInt(c.getColumnIndex(Telephony.Sms.ERROR_CODE)));
                smsModel.setCreator(c.getString(c.getColumnIndex(Telephony.Sms.CREATOR)));
            }
            mRealm.commitTransaction();
            Log.d(TAG, "results size after adding :" + mRealm.where(SmsModel.class).findAll().size());

            c.close();

        }

    }

    public class ContactsAdapter extends CursorAdapter{

        private final String TAG = CursorAdapter.class.getSimpleName();

        public ContactsAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.contact_search_item, parent, false);

            ViewHolder viewHolder = new ViewHolder();

            viewHolder.mContactName = (TextView) view.findViewById(R.id.contact_name);
            viewHolder.mContactNumber= (TextView) view.findViewById(R.id.contact_number);

            view.setTag(viewHolder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();

            String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            viewHolder.mContactName.setText(name);
            viewHolder.mContactNumber.setText(number);

        }

        private class ViewHolder{

            TextView mContactName;
            TextView mContactNumber;
        }
    }
}
