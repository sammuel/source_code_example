package com.photoapp.model.internet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.github.pwittchen.networkevents.library.BusWrapper;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.NetworkEvents;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.photoapp.model.debug.CrashlyticsHelper;
import com.photoapp.model.debug.Log;
import com.photoapp.model.events.Bus;
import com.photoapp.model.upload.UploadService;

/**
 *
 */
public class InternetHelper {

    private static final String LOG_TAG = InternetHelper.class.getSimpleName();

    private static InternetHelper instance;
    private Context mContext;

    private NetworkEvents networkEvents;


    public static InternetHelper getInstance(Context context) {
        if (instance == null) {
            instance = new InternetHelper(context);
        }
        return instance;
    }

    private InternetHelper(Context context) {
        mContext = context;

        networkEvents = new NetworkEvents(context, getGreenRobotBusWrapper())
                .enableInternetCheck();
        Bus.register(this);
    }

    public void onEvent(ConnectivityChanged event) {
        ConnectivityStatus connectivityStatus = event.getConnectivityStatus();
        Log.w(LOG_TAG, "On internet changed = " + connectivityStatus);
        switch (connectivityStatus) {
            case WIFI_CONNECTED_HAS_INTERNET:
                onWifiConnected();
                break;

            case MOBILE_CONNECTED:
                onMobileConnected();
                break;

            case WIFI_CONNECTED:
                break;
            case UNKNOWN:
            case WIFI_CONNECTED_HAS_NO_INTERNET:
            case OFFLINE:
                onNoConnection();
                break;
        }
    }

    private void onNoConnection() {
        Log.w(LOG_TAG, "OnNoConnection");
        CrashlyticsHelper.setInternetExist(false);
    }

    private void onMobileConnected() {
        OnConnectionExist();
    }

    private void OnConnectionExist() {
        Log.w(LOG_TAG, "OnConnectionExist");
        UploadService.startService(mContext);
        CrashlyticsHelper.setInternetExist(true);
    }

    private void onWifiConnected() {
        OnConnectionExist();
    }

    public boolean isConnected() {
        return isWifiConnected() | isMobileConnected();
    }

    public boolean isWifiConnected() {

        boolean isWifiConnected = false;

        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connManager != null) {
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if(mWifi != null) {
                isWifiConnected = mWifi.isConnected();
            }
        }

        return isWifiConnected;
    }

    public boolean isMobileConnected() {
        boolean isCellularConnected = false;

        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connManager != null) {
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if(mWifi != null) {
                isCellularConnected = mWifi.isConnected();
            }
        }


        return isCellularConnected;
    }

    public void register() {

        Log.w(LOG_TAG, "start listen NETWORK status");
        networkEvents.register();
    }

    public void unregister() {
        Log.w(LOG_TAG, "stop listen NETWORK status");
        networkEvents.unregister();
    }


    private BusWrapper getGreenRobotBusWrapper() {
        return new BusWrapper() {
            @Override
            public void register(Object object) {
                Bus.register(object);
            }

            @Override
            public void unregister(Object object) {
                Bus.unregister(object);
            }

            @Override
            public void post(Object event) {
                Bus.post(event);
            }
        };
    }

    public boolean canUploadWifi() {
        return isWifiConnected();
    }

    public boolean canUploadCellular(boolean wifiOnlyState) {
        return isMobileConnected() && !wifiOnlyState;
    }

    public boolean canUpload(boolean wifiOnlyState) {
        return  canUploadWifi() || canUploadCellular(wifiOnlyState);
    }


}

