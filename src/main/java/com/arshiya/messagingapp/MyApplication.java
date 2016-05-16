package com.arshiya.messagingapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.arshiya.messagingapp.model.SmsModel;

import java.util.concurrent.atomic.AtomicLong;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by arshiya on 7/5/16.
 */
public class MyApplication extends Application {

    private static final String TAG = MyApplication.class.getSimpleName();
    private static AtomicLong primaryKeyValue;

    @Override
    public void onCreate() {
        super.onCreate();
        primaryKeyValue = new AtomicLong(0);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfig);
        initialize();
    }

    public void initialize() {
        Log.d(TAG, "initializing primaryKey ...");
        Realm realm = Realm.getDefaultInstance();
        if (null != realm.where(SmsModel.class).max("_ID")){
            primaryKeyValue = new AtomicLong( realm.where(SmsModel.class).max("_ID").longValue());
        }
        realm.close();
    }

    // Automatically create next key
    public static long nextKey() {
        return primaryKeyValue.incrementAndGet();
    }
}
