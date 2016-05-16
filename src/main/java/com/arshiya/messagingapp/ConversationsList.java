package com.arshiya.messagingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.opengl.Visibility;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arshiya.messagingapp.adapters.RealmRecyclerViewAdapter;
import com.arshiya.messagingapp.adapters.SearchResultsAdapter;
import com.arshiya.messagingapp.model.SmsModel;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class ConversationsList extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    private static final String TAG = ConversationsList.class.getSimpleName();
    private Realm mRealm;
    private RecyclerView mRecyclerView;
    private ConversationsRecyclerViewAdapter mAdapter;
    private LinearLayout mSearchViewHolder;
    private Button mSearch;
    private Button mClose;
    private EditText mSearchInput;
    private LinearLayout mSearchResultHolder;
    private RecyclerView mSearchResultRecyclerView;
    private SearchResultsAdapter mSearchResultAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newMessageIntent = new Intent(ConversationsList.this, NewMessage.class);
                startActivity(newMessageIntent);
            }
        });

        mSearch = (Button) findViewById(R.id.search);
        mSearchViewHolder = (LinearLayout) findViewById(R.id.search_view_holder);
        mClose = (Button) findViewById(R.id.close);
        mSearchInput = (EditText) findViewById(R.id.search_input_box);
        mSearchResultRecyclerView = (RecyclerView) findViewById(R.id.message_search_result_list);
        mSearchResultHolder = (LinearLayout) findViewById(R.id.message_search_result_holder);

        mSearch.setOnClickListener(this);
        mClose.setOnClickListener(this);

        mSearchInput.addTextChangedListener(this);

        mRealm = Realm.getDefaultInstance();

        init();

        //todo check if default sms app
//        checkIfDefaultMessageApp();

        print();

        mRecyclerView = (RecyclerView ) findViewById(R.id.realm_recycler_view);

        RealmResults<SmsModel> results = mRealm
                .where(SmsModel.class)
                .findAllSorted("DATE", Sort.DESCENDING)
                .distinct("THREAD_ID");

        Log.d(TAG, "results : " + results.size());

        mAdapter = new ConversationsRecyclerViewAdapter(results);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);

        assert mRecyclerView != null;
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);

        results.addChangeListener(new RealmChangeListener<RealmResults<SmsModel>>() {
            @Override
            public void onChange(RealmResults<SmsModel> element) {
                mAdapter.updateRealmResults(element, element.size());
            }
        });

        //search result adapter
        RecyclerView.LayoutManager searchManager = new LinearLayoutManager(this);

        mSearchResultRecyclerView.setLayoutManager(searchManager);
        mSearchResultAdapter = new SearchResultsAdapter(null, new SearchResultsAdapter.OnItemClickListenerRecyclerView() {
            @Override
            public void onItemClick(View item, RealmResults<SmsModel> realmResults) {
                int pos = mSearchResultRecyclerView.getChildLayoutPosition(item);

                long threadId = realmResults.get(pos).getThreadId();
                String body = realmResults.get(pos).getBody();

                Intent intent = new Intent(ConversationsList.this, Conversation.class);
                intent.putExtra("thread_id", threadId);
                intent.putExtra("title", realmResults.get(pos).getAddress());

                Log.d(TAG, threadId + " - " + "body : " + body);

                startActivity(intent);            }
        });

        mSearchResultRecyclerView.setAdapter(mSearchResultAdapter);

        mSearch.setVisibility(View.VISIBLE);
        updateSearchUI(false);
    }

    private void updateSearchUI(boolean visible) {
        int visibility;

        if (visible){
            visibility = View.VISIBLE;
        } else {
            visibility = View.GONE;
        }
        mSearchViewHolder.setVisibility(visibility);
        mSearchResultHolder.setVisibility(visibility);
        mSearchInput.setText("");
    }


//    private void checkIfDefaultMessageApp() {
//        final String myPackageName = getPackageName();
//        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
//            //todo add dialog to prompt user to make this app deafult
//            Log.d(TAG, "not default app " );
//            // App is not default.
////            // Show the "not currently set as the default SMS app" interface
////            View viewGroup = findViewById(R.id.not_default_app);
////            viewGroup.setVisibility(View.VISIBLE);
////
////            // Set up a button that allows the user to change the default SMS app
////            Button button = (Button) findViewById(R.id.change_default_app);
////            button.setOnClickListener(new View.OnClickListener() {
////                public void onClick(View v) {
//            Intent intent =
//                    new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
//            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
//                    myPackageName);
////                    startActivity(intent);
////                }
////            });
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.removeAllChangeListeners();
        if (!mRealm.isClosed()){
            mRealm.close();
        }
    }

    private void print() {
        RealmResults<SmsModel> results = mRealm.where(SmsModel.class).findAllSorted("_ID");

        int size = results.size(), i = 0;

        for (i = 0; i < size; i++){
            Log.d(TAG, "id : " + results.get(i).getID());
            Log.d(TAG, "thread id : " + results.get(i).getThreadId());
            Log.d(TAG, "Address : " + results.get(i).getAddress());
            Log.d(TAG, "body : " + results.get(i).getBody());
            Log.d(TAG, "date : " + new Date(results.get(i).getDate()));
            Log.d(TAG, "date sent :  " + new Date(results.get(i).getDateSent()));

        }

    }

    private void init() {
        //check if first run
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);

        if (!sharedPreferences.getBoolean("first_run", false)){
            //read all the existing messages from Telephony.Sms content provider
            initializeSmsRealmClass();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("first_run",true);
            editor.apply();

        }
    }

    private void initializeSmsRealmClass() {
        try (Cursor c = getContentResolver().query(Telephony.Sms.CONTENT_URI,
                null, null, null, "_id DESC")) {

            assert c != null;
            c.moveToPosition(-1);

            while (c.moveToNext()) {
                SmsModel smsModel = new SmsModel();

                mRealm.beginTransaction();

                smsModel.setId();
                smsModel.setThreadId(c.getLong(c.getColumnIndex(Telephony.Sms.THREAD_ID)));
                smsModel.setMessagesType(c.getInt(c.getColumnIndex(Telephony.Sms.TYPE)));
                smsModel.setAddress(c.getString(c.getColumnIndex(Telephony.Sms.ADDRESS)));
                smsModel.setDate(c.getLong(c.getColumnIndex(Telephony.Sms.DATE)));
                smsModel.setDateSent(c.getLong(c.getColumnIndex(Telephony.Sms.DATE_SENT)));
                smsModel.setRead(c.getInt(c.getColumnIndex(Telephony.Sms.READ)));
                smsModel.setSeen(Integer.parseInt(c.getString(c.getColumnIndex(Telephony.Sms.SEEN))));
                smsModel.setStatus(c.getInt(c.getColumnIndex(Telephony.Sms.STATUS)));
                smsModel.setSubject(c.getString(c.getColumnIndex(Telephony.Sms.SUBJECT)));
                smsModel.setBody(c.getString(c.getColumnIndex(Telephony.Sms.BODY)));
                smsModel.setServiceCenter(c.getString(c.getColumnIndex(Telephony.Sms.SERVICE_CENTER)));
//                Log.d(TAG, "LOCKED : " + c.getString(c.getColumnIndex(Telephony.Sms.LOCKED)));
                smsModel.setSubscriptionId(c.getString(c.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)));
                smsModel.setErrorCode(c.getInt(c.getColumnIndex(Telephony.Sms.ERROR_CODE)));
                smsModel.setCreator(c.getString(c.getColumnIndex(Telephony.Sms.CREATOR)));

                mRealm.copyToRealm(smsModel);
                mRealm.commitTransaction();

            }

        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
            case R.id.search:
                updateSearchUI(true);
                mSearch.setVisibility(View.GONE);
                break;

            case R.id.close:
                updateSearchUI(false);
                mSearch.setVisibility(View.VISIBLE);
                break;

            default:
                Log.e(TAG, "onclick : unknown");
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //get the text and search for text in Realm
        RealmResults<SmsModel> searchResults = getSearchResult(s.toString());
        mSearchResultAdapter.updateRealmResults(searchResults, 0);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private RealmResults<SmsModel> getSearchResult(String s) {
        RealmResults<SmsModel> results = null;
        if (!s.equalsIgnoreCase("")){
            results = mRealm.where(SmsModel.class)
                    .contains("ADDRESS", s)
                    .or()
                    .contains("BODY", s)
                    .findAllSorted("DATE", Sort.ASCENDING);
        }

        if (null == results || 0 >= results.size()){
            mSearchResultRecyclerView.setVisibility(View.GONE);
        } else {
            mSearchResultRecyclerView.setVisibility(View.VISIBLE);
        }

        return results;
    }


    public class ConversationsRecyclerViewAdapter extends RealmRecyclerViewAdapter<SmsModel, ConversationsRecyclerViewAdapter.ViewHolder> implements View.OnClickListener {

        private RealmResults<SmsModel> realmResults;
        private Utils utils;

        public ConversationsRecyclerViewAdapter(RealmResults<SmsModel> realmResults){
            super(realmResults, true, 0);

            this.realmResults = realmResults;
            utils = new Utils();
        }


        @Override
        public void updateRealmResults(RealmResults<SmsModel> queryResults, int maxDepth) {
            super.updateRealmResults(queryResults, maxDepth);
            realmResults = queryResults;
            notifyDataSetChanged();

        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.conversations_list_item, parent, false);

            view.setOnClickListener(this);
            return new ViewHolder(view);        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            SmsModel smsModel = realmResults.get(position);

            holder.snippet.setText(smsModel.getBody());
            holder.address.setText(smsModel.getAddress());

            Date date = new Date(smsModel.getDate());
            holder.date.setText(utils.getDate(date));
        }

        @Override
        public int getItemCount() {
            int size = 0;
            if (null != realmResults){
                size = realmResults.size();
            }
            return size;
        }

        @Override
        public void onClick(View v) {
            int pos = mRecyclerView.getChildLayoutPosition(v);

            long threadId = realmResults.get(pos).getThreadId();
            String body = realmResults.get(pos).getBody();

            Intent intent = new Intent(ConversationsList.this, Conversation.class);
            intent.putExtra("thread_id", threadId);
            intent.putExtra("title", realmResults.get(pos).getAddress());

            Log.d(TAG, threadId + " - " + "body : " + body);

            startActivity(intent);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView address;
            TextView snippet;
            TextView date;

            public ViewHolder(View itemView) {
                super(itemView);

                address = (TextView) itemView.findViewById(R.id.address);
                snippet = (TextView) itemView.findViewById(R.id.snippet);
                date = (TextView) itemView.findViewById(R.id.date);
            }
        }
    }

}
