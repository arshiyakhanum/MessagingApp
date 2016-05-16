package com.arshiya.messagingapp.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
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
public class SmsSentReceiver extends BroadcastReceiver {

    private static final String TAG = SmsSentReceiver.class.getSimpleName();

    public SmsSentReceiver(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra("id", -1L);

        if (-1L == id){
            return;
        }
        Log.d(TAG, "message id : " + id);

        switch (getResultCode())
        {
            case Activity.RESULT_OK:
                onSuccess(id);
                Toast.makeText(context, "SMS sent", Toast.LENGTH_SHORT).show();
                break;

            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                onFailure(id, SmsManager.RESULT_ERROR_GENERIC_FAILURE);
                Toast.makeText(context, "Generic failure", Toast.LENGTH_SHORT).show();
                break;

            case SmsManager.RESULT_ERROR_NO_SERVICE:
                onFailure(id, SmsManager.RESULT_ERROR_NO_SERVICE);
                Toast.makeText(context, "No service", Toast.LENGTH_SHORT).show();
                break;

            case SmsManager.RESULT_ERROR_NULL_PDU:
                onFailure(id, SmsManager.RESULT_ERROR_NULL_PDU);
                Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show();
                break;

            case SmsManager.RESULT_ERROR_RADIO_OFF:
                onFailure(id, SmsManager.RESULT_ERROR_RADIO_OFF);
                Toast.makeText(context, "Radio off", Toast.LENGTH_SHORT).show();
                break;
        }

        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);

    }

    private void onSuccess(long id) {
        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();

        SmsModel smsModel = realm.where(SmsModel.class).equalTo("_ID", id).findFirst();
        smsModel.setDateSent(System.currentTimeMillis());
        smsModel.setStatus(Constants.COMPLETE);
        realm.commitTransaction();

    }

    private void onFailure(long id, int errorCode){
        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();

        SmsModel smsModel = realm.where(SmsModel.class).equalTo("_ID", id).findFirst();
        smsModel.setDateSent(System.currentTimeMillis());
        smsModel.setStatus(Constants.FAILED);
        smsModel.setErrorCode(errorCode);
        realm.commitTransaction();
    }
}


