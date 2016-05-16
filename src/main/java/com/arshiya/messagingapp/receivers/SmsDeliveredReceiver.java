package com.arshiya.messagingapp.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.arshiya.messagingapp.Constants;
import com.arshiya.messagingapp.MyApplication;
import com.arshiya.messagingapp.model.SmsModel;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by arshiya on 13/5/16.
 */
public class SmsDeliveredReceiver extends BroadcastReceiver {

    private static final String TAG = SmsDeliveredReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra("id", -1L);

        if (-1L == id){
            return;
        }
        Log.d(TAG, "message id : " + id);

        switch (getResultCode()) {
            case Activity.RESULT_OK:
                onSuccess(id);
                Toast.makeText(context, "SMS delivered",
                        Toast.LENGTH_SHORT).show();
                break;
            case Activity.RESULT_CANCELED:
                onFailure(id);
                Toast.makeText(context, "SMS not delivered",
                        Toast.LENGTH_SHORT).show();
                break;
        }

        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }


    private void onSuccess(long id) {
        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();

        SmsModel smsModel = realm.where(SmsModel.class).equalTo("_ID", id).findFirst();
        smsModel.setDate(System.currentTimeMillis());
        smsModel.setStatus(Constants.COMPLETE);
        realm.commitTransaction();

    }

    private void onFailure(long id){
        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();

        SmsModel smsModel = realm.where(SmsModel.class).equalTo("_ID", id).findFirst();
        smsModel.setDate(System.currentTimeMillis());
        smsModel.setStatus(Constants.FAILED);
        realm.commitTransaction();
    }
}



