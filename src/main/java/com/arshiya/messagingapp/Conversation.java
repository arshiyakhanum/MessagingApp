package com.arshiya.messagingapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.arshiya.messagingapp.adapters.RealmRecyclerViewAdapter;
import com.arshiya.messagingapp.model.SmsModel;
import com.arshiya.messagingapp.receivers.SmsDeliveredReceiver;
import com.arshiya.messagingapp.receivers.SmsSentReceiver;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class Conversation extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = Conversation.class.getSimpleName();
    private Realm mRealm;
    private RecyclerView mRecyclerView;
    private IndividualConversationAdapter mAdapter;
    private EditText mMessage;
    private ImageButton mSend;
    private long mThread_ID;
    private SmsManager mSmsManager;
    private static long mNextId;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        mThread_ID = intent.getLongExtra("thread_id", -99);
        String title = intent.getStringExtra("title");

        if (0 < title.length()) {
            //get display name from contacts
            setTitle(getDisplayName(title));
        }

        if (0 > mThread_ID) {
            Log.d(TAG, "empty");
            //todo show error
            finish();
        }

        mRealm = Realm.getDefaultInstance();

        //get phone number
        SmsModel smsModel = mRealm.where(SmsModel.class).equalTo("THREAD_ID", mThread_ID).findFirst();

        String destinationAddress = smsModel.getAddress();

        if (!PhoneNumberUtils.isGlobalPhoneNumber(destinationAddress)) {
            //hide compose message layout
            findViewById(R.id.compose_msg_holder).setVisibility(View.GONE);
        }
        disablefullScreenMode();


        mNextId = mRealm.where(SmsModel.class).max("_ID").longValue();

        RealmResults<SmsModel> results = mRealm.where(SmsModel.class).equalTo("THREAD_ID", mThread_ID).findAllSorted("DATE", Sort.ASCENDING);

        mRecyclerView = (RecyclerView) findViewById(R.id.individual_conversation_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        assert mRecyclerView != null;
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new IndividualConversationAdapter(results, true);

        initRecyclerView(results);

        mMessage = (EditText) findViewById(R.id.message);
        mSend = (ImageButton) findViewById(R.id.send_sms);

        assert mSend != null;
        mSend.setOnClickListener(this);

        mSmsManager = SmsManager.getDefault();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private String getDisplayName(String title) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(title));
        Cursor cursor = getContentResolver().query(uri,
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},
                null,
                null,
                null);

        if (cursor != null && 0 > cursor.getCount()) {
            cursor.moveToFirst();
            title = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            cursor.close();
        }
        return title;
    }

    private void disablefullScreenMode() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void initRecyclerView(RealmResults<SmsModel> results) {
        mRecyclerView.setAdapter(mAdapter);
        scrollToLastItem(mRecyclerView, mAdapter);


        results.addChangeListener(new RealmChangeListener<RealmResults<SmsModel>>() {
            @Override
            public void onChange(RealmResults<SmsModel> element) {
                mAdapter.updateRealmResults(element, element.size());
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mRealm.isClosed()) {
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
        if (!mRealm.isClosed()) {
            mRealm.close();
            mRealm = null;
        }
    }

    private void scrollToLastItem(RecyclerView recyclerView, IndividualConversationAdapter adapter) {
        recyclerView.smoothScrollToPosition(adapter.getItemCount());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.send_sms:
                handleSendClick();
                break;

            default:
                Log.d(TAG, "On click :unknown button");
        }
    }

    private void handleSendClick() {
        //get phone number
        SmsModel smsModel = mRealm.where(SmsModel.class).equalTo("THREAD_ID", mThread_ID).findFirst();

        String destinationAddress = smsModel.getAddress();

        Log.d(TAG, "number : " + destinationAddress);

        String message = mMessage.getText().toString();
        Log.d(TAG, "message : " + message);

        if (0 < message.length()) {
            sendMessage(destinationAddress, message);
        }
    }

    private void sendMessage(String destinationAddress, String message) {
        mNextId++;
        Log.d(TAG, "next id : " + mNextId);
        Intent sentIntent = new Intent(Constants.SENT + mNextId);
        sentIntent.putExtra("id", mNextId);
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                sentIntent, 0);

        Intent deliveredIntent = new Intent(Constants.DELIVERED + mNextId);
        deliveredIntent.putExtra("id", mNextId);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                deliveredIntent, 0);

        registerSmsSentReceiver();
        registerSmsDeliveredReceiver();

        mSmsManager.sendTextMessage(destinationAddress, null, message, sentPI, deliveredPI);
        addMessageToRealm(destinationAddress, message);

        updateUI();
    }

    private void registerSmsDeliveredReceiver() {
        registerReceiver(new SmsDeliveredReceiver(), new IntentFilter(Constants.DELIVERED + mNextId));
    }

    private void registerSmsSentReceiver() {
        registerReceiver(new SmsSentReceiver(), new IntentFilter(Constants.SENT + mNextId));

    }

    private void addMessageToRealm(String address, String body) {
        Log.d(TAG, "results size before adding :" + mRealm.where(SmsModel.class).findAll().size());
        Log.d(TAG, "Thread id : " + mThread_ID);

        try (Cursor c = getContentResolver().query(Telephony.Sms.Sent.CONTENT_URI,
                null, Telephony.Sms.ADDRESS + "=?", new String[]{address}, Telephony.Sms.DATE_SENT + " DESC")) {

            assert c != null;
            c.moveToPosition(0);

            Log.d(TAG, "cursor : " + c.toString());


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
            smsModel.setServiceCenter(c.getString(c.getColumnIndex(Telephony.Sms.SERVICE_CENTER)));
            smsModel.setSubscriptionId(c.getString(c.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)));
            smsModel.setErrorCode(c.getInt(c.getColumnIndex(Telephony.Sms.ERROR_CODE)));
            smsModel.setCreator(c.getString(c.getColumnIndex(Telephony.Sms.CREATOR)));

            mRealm.commitTransaction();
            Log.d(TAG, "results size after adding :" + mRealm.where(SmsModel.class).findAll().size());

            c.close();

        }

    }

    private void updateUI() {
        mMessage.setText("");
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Conversation Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.arshiya.messagingapp/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Conversation Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.arshiya.messagingapp/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


    public class IndividualConversationAdapter extends RealmRecyclerViewAdapter<SmsModel, IndividualConversationAdapter.ViewHolder> {

        private RealmResults<SmsModel> realmResults;
        private static final int TYPE_INBOX = 1;
        private static final int TYPE_SENT = 2;
        private Utils utils;

        public IndividualConversationAdapter(RealmResults<SmsModel> realmResults, boolean automaticUpdate) {
            super(realmResults, automaticUpdate, 0);
            this.realmResults = realmResults;
            utils = new Utils();
        }

        @Override
        public void updateRealmResults(RealmResults<SmsModel> queryResults, int maxDepth) {
            super.updateRealmResults(queryResults, maxDepth);
            scrollToLastItem(mRecyclerView, mAdapter);
            notifyDataSetChanged();

        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView message;
            TextView date;

            public ViewHolder(View itemView) {
                super(itemView);

                message = (TextView) itemView.findViewById(R.id.message);
                date = (TextView) itemView.findViewById(R.id.date);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;

            switch (viewType) {
                case TYPE_INBOX:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_item_received, parent, false);
                    break;

                case TYPE_SENT:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_item_sent, parent, false);
                    break;

                default:
                    Log.e(TAG, "unknown type : " + viewType);
            }

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String body = realmResults.get(position).getBody();
            long dateInMillis = 0;

            dateInMillis = realmResults.get(position).getDate();

            Date date = new Date(dateInMillis);

            holder.message.setText(body);
            holder.date.setText(utils.getDate(date));
        }

        @Override
        public int getItemViewType(int position) {
            return realmResults.get(position).getMessagesType();
        }

        @Override
        public int getItemCount() {
            return realmResults.size();
        }
    }
}
