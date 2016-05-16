package com.arshiya.messagingapp.model;


import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;

import com.arshiya.messagingapp.MyApplication;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by arshiya on 7/5/16.
 */
public class SmsModel extends RealmObject{

    private  static final String TAG = SmsModel.class.getSimpleName();

    public int  MESSAGE_TYPE;

    @PrimaryKey
    public long _ID;

    public int COUNT;

    @Index
    public long THREAD_ID;
    @Index
    public String ADDRESS;
    public long DATE;
    public long DATE_SENT;
    public int READ;
    public int SEEN;

    public int STATUS;


    @Index
    public String SUBJECT;
    @Index
    public String BODY;
    //todo add PERSON, get name and number
//    public ContactsContract.CommonDataKinds.Phone PERSON;

    public String SERVICE_CENTER;

    //    public boolean LOCKED; // not required now
    public String SUBSCRIPTION_ID;
    public int ERROR_CODE;
    public String CREATOR;


    public void addSms(int messageType,String address, long date, long dateSent, int read, int seen, int status, String subject,
                       String body, String serviceCenter, String subscriptionId, int errorCode, String creator, long threadId){
        //get unique id
        THREAD_ID = threadId;
        MESSAGE_TYPE = messageType;
        ADDRESS = address;
        DATE = date;
        DATE_SENT = dateSent;
        READ = read;
        SEEN = seen;
        STATUS = status;
        SUBJECT = subject;
        BODY = body;
        SERVICE_CENTER = serviceCenter;
        SUBSCRIPTION_ID = subscriptionId;
        ERROR_CODE = errorCode;
        CREATOR = creator;
    }

    public void setId(){
        _ID = MyApplication.nextKey();
    }

    public long getID(){
        return _ID;
    }

    public long getThreadId(){
        return THREAD_ID;
    }

    public int getMessagesType(){
        return MESSAGE_TYPE;
    }

    public String getAddress(){
        return ADDRESS;
    }

    public long getDate(){
        return DATE;
    }

    public long getDateSent(){
        return DATE_SENT;
    }

    public int getRead(){
        return READ;
    }

    public int getSeen(){
        return SEEN;
    }

    public int getStatus(){
        return STATUS;
    }

    public String getSubject(){
        return SUBJECT;
    }

    public String getBody(){
        return BODY;
    }

    public String getServiceCenter(){
        return SERVICE_CENTER;
    }

    public String getSubscriptionId(){
        return SUBSCRIPTION_ID;
    }

    public int getErrorCode(){
        return ERROR_CODE;
    }

    public String getCreator(){
        return CREATOR;
    }


    public void setThreadId(long threadId){
        THREAD_ID = threadId;
    }

    public void setMessagesType(int type){
        MESSAGE_TYPE = type;
    }

    public void setAddress(String address){
        ADDRESS = address;
    }

    public void setDate(long date){
        DATE = date;
    }

    public void setDateSent(long dateSent){
        DATE_SENT = dateSent;
    }

    public void setRead(int read){
        READ = read;
    }

    public void setSeen(int seen){
        SEEN = seen;
    }

    public void setStatus(int status){
        STATUS = status;
    }

    public void setSubject(String subject){
        SUBJECT = subject;
    }

    public void setBody(String body){
        BODY = body;
    }

    public void setServiceCenter(String center){
        SERVICE_CENTER = center;
    }

    public void setSubscriptionId(String id){
        SUBSCRIPTION_ID = id;
    }

    public void setErrorCode(int errorCode){
        ERROR_CODE = errorCode;
    }

    public void setCreator(String creator){
        CREATOR = creator;;
    }

    public long getOrCreateThreadId(String address){
        Realm realm = Realm.getDefaultInstance();

        SmsModel model = realm.where(SmsModel.class).equalTo("ADDRESS", address).findFirst();
        long threadId = 0;

        if (null == model){
            threadId = realm.where(SmsModel.class).max("THREAD_ID").longValue() + 1;
            Log.d(TAG, "created new thread_id : " + threadId);
        } else {
            threadId = model.getThreadId();
            Log.d(TAG, "existing thread_id : " + threadId);
        }
        return threadId;
    }

//    public class Conversations extends SmsModel{
//
//        private Conversations(){
//
//        }
//
//        public String snippet;
//        public long messageCount;
//
//        public void setSnippet(String snippet){
//            this.snippet = snippet;
//
//        }
//
//
//        public String getSnippet(){
//            return snippet;
//        }
//
//        public void setMessageCount(Context context){
//            RealmConfiguration configuration = new RealmConfiguration.Builder(context).build();
//            Realm realm = Realm.getInstance(configuration);
//            // get the thread_id
//            realm.where(SmsModel.class).
//        }
//        public long getMessageCount(){
//            //set message count
//
//        }
//
//
//
//
//    }
}
