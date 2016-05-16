package com.arshiya.messagingapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.arshiya.messagingapp.Constants;
import com.arshiya.messagingapp.MyApplication;
import com.arshiya.messagingapp.model.SmsModel;
import com.arshiya.messagingapp.services.MessageNotificationService;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;


/**
 * Created by arshiya on 8/5/16.
 */
public class IncomingSmsReceiver extends BroadcastReceiver {

    private static final String TAG = IncomingSmsReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        try{
            if (null != bundle){
                final Object[] pdus = (Object[]) bundle.get("pdus");

                Realm realm = Realm.getDefaultInstance();

                assert pdus != null;
                for (Object pdu : pdus) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdu);


                    long date = System.currentTimeMillis();
                    long dateSent = currentMessage.getTimestampMillis();
                    int type = Constants.MESSAGE_TYPE_INBOX;
                    int status = Constants.NONE;

                    String address = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();

                    //start service to post notification
                    startPostNotificationService(context,address, message, dateSent);

                    //todo delete - start
                    RealmResults<SmsModel> results = realm.where(SmsModel.class).findAll();

                    Log.d(TAG, "size :" + results.size());
                    results.addChangeListener(new RealmChangeListener<RealmResults<SmsModel>>() {
                        @Override
                        public void onChange(RealmResults<SmsModel> element) {
                            Log.d(TAG, "changed size :" + element.size());

                        }
                    });

                    //todo delete - end

                    Log.i(TAG, "senderNum: " + address + "; message: " + message);

                    realm.beginTransaction();

                    SmsModel model = realm.createObject(SmsModel.class);

                    String serviceCenter = currentMessage.getServiceCenterAddress();
//                    String subscriptionId = currentMessage.get
                    String subject = currentMessage.getPseudoSubject();
//                    String creator

                    model.setId();
                    model.setThreadId(model.getOrCreateThreadId(address));
                    model.setDate(date);
                    model.setDateSent(dateSent);
                    model.setAddress(address);
                    model.setBody(message);
                    model.setMessagesType(type);
                    model.setStatus(status);
                    model.setServiceCenter(serviceCenter);

                    realm.commitTransaction();

                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);

        }
    }

    private void startPostNotificationService(Context context, String address, String message, long dateSent) {
        Intent notifyIntent = new Intent(context, MessageNotificationService.class);
        notifyIntent.putExtra("address", address);
        notifyIntent.putExtra("body", message);
        notifyIntent.putExtra("date_sent", dateSent);

        context.startService(notifyIntent);

    }
}



