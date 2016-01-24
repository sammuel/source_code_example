package com.photoapp.model.intents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.photoapp.MyApplication;
import com.photoapp.R;
import com.photoapp.controller.dialog.DialogHelper;
import com.photoapp.controller.events.DialogEvent;
import com.photoapp.model.database.DAOHelper;
import com.photoapp.model.events.Bus;
import com.photoapp.model.internet.InternetHelper;

/**
 * Wrapper for init and register broadcast reciever and intentFilter
 */
public class BroadcastReceiverHelper {
    public static final String PARAM = "PARAMETERS";
    public static final String ACTION = "ACT";
    public static final String PARAM_DELETED = "DELETED";
    public static final String PARAM_UPLOADED = "UPLOADED";
    public static final String PARAM_DISCONNECTED = "DISCONNECTED";
    public static final String PARAM_UPLOAD_ERROR = "UPLOAD_ERROR";
    public static final String FILENAME = "FILENAME";
    public static final String FILEPATH = "FILEPATH";


    private Context mContext;

    private static BroadcastReceiver broadcastReceiver;
    private static IntentFilter intentFilter;
    private DAOHelper daoHelper;

    private String filepath;

    public BroadcastReceiverHelper(Context context) {
        mContext = context;
        initReceiver();
    }

    public void registerReceiver() {
        if (broadcastReceiver != null && intentFilter != null)
            mContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void unregisterReceiver() {
        mContext.unregisterReceiver(broadcastReceiver);
    }

    public void initReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String param = intent.getStringExtra(PARAM);
                String filename = intent.getStringExtra(FILENAME);
                if (param.equals(PARAM_DELETED)) {

                    fileWasDeletedBefore(filename);
                } else if (param.equals(PARAM_UPLOAD_ERROR)) {
                    uploadError(filename);
                }
            }
        };
        intentFilter = new IntentFilter(ACTION);
    }


    private void fileWasDeletedBefore(String fileName) {
        Bus.post(new DialogEvent(DialogEvent.DialogType.FILE_WAS_DELETED, fileName));
    }


    private void uploadError(String filename) {
        InternetHelper internetHelper = MyApplication.instance.getInternetHelper();
        boolean connected = internetHelper.isConnected();
        if (!connected) {
            Bus.post(new DialogEvent(DialogEvent.DialogType.INTERNET_DISCONNECTED));
        } else {
            Bus.post(new DialogEvent(DialogEvent.DialogType.UPLOAD_ERROR, filename));
        }
    }
}
