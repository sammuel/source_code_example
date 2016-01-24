package com.photoapp;

import android.app.Application;
import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;
import com.photoapp.model.intents.BroadcastReceiverHelper;
import com.photoapp.model.database.DAOHelper;
import com.photoapp.model.internet.InternetHelper;
import com.photoapp.model.location.LocationHelper;
import io.fabric.sdk.android.Fabric;


/**
 * Application class. Used for greenDAO initialization
 */
public class MyApplication extends Application {
    public DAOHelper daoHelper;
    public  LocationHelper locationHelper;
    public  InternetHelper internetHelper;
    public BroadcastReceiverHelper broadcastReceiverHelper;

    public static MyApplication instance;


    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());
        initDAO();

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                initLocation();
                initInternetHelper();
                initBroadcastRecieverHelper();

                return null;
            }
        }.execute();

        instance = this;
    }

    private void initDAO() {
        daoHelper = DAOHelper.getInstance(this);
    }

    private void initLocation() {
        locationHelper = LocationHelper.getInstatce(this);
    }

    private void initInternetHelper() {
        internetHelper = InternetHelper.getInstance(this);
    }

    private void initBroadcastRecieverHelper() {
        broadcastReceiverHelper = new BroadcastReceiverHelper(this);
    }

    public DAOHelper getDaoHelper() {
        return daoHelper;
    }

    public InternetHelper getInternetHelper() {
        return internetHelper;
    }

    public LocationHelper getLocationHelper() {
        return locationHelper;
    }

    public BroadcastReceiverHelper getBroadcastReceiverHelper() {
        return broadcastReceiverHelper;
    }
}