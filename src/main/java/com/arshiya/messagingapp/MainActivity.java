package com.arshiya.messagingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.arshiya.messagingapp.model.SmsModel;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private static final String  TAG = MainActivity.class.getSimpleName();
    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create a RealmConfiguration which is to locate Realm file in package's "files" directory.
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).build();
        // Get a Realm instance for this thread
        mRealm = Realm.getInstance(realmConfig);
        init();

        print();
    }

    private void print() {
        RealmResults<SmsModel> results = mRealm.where(SmsModel.class).findAllSorted("DATE");

        int size = results.size(), i = 0;

        for (i = 0; i < size; i++){
            Log.d(TAG, "id : " + results.get(i).getID());
            Log.d(TAG, "thread id : " + results.get(i).getThreadId());
            Log.d(TAG, "Address : " + results.get(i).getAddress());
            Log.d(TAG, "body : " + results.get(i).getBody());
            Log.d(TAG, "date : " + results.get(i).getDate());
        }

    }

    private void init() {
        //check if first run
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);

        if (!sharedPreferences.getBoolean("first_run", false)){
            //read all the existing messages from Telephony.Sms content provider
            initializeSmsRealmClass();

        }
    }

    private void initializeSmsRealmClass() {
        try (Cursor c = getContentResolver().query(Telephony.Sms.CONTENT_URI,
                null, null, null, "_id DESC")) {

            assert c != null;
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                mRealm.beginTransaction();
                SmsModel smsModel = mRealm.createObject(SmsModel.class);

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

                mRealm.commitTransaction();

            }
        }
    }

}
