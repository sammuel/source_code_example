package com.photoapp.controller.activity;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.DialogAction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.photoapp.MyApplication;
import com.photoapp.R;
import com.photoapp.controller.events.DialogEvent;
import com.photoapp.controller.thread.UIThreadHelper;
import com.photoapp.model.debug.Log;
import com.photoapp.model.events.Bus;
import com.photoapp.model.database.DAOHelper;
import com.photoapp.controller.events.SignUpEvent;
import com.photoapp.controller.dialog.DialogHelper;
import com.photoapp.controller.fragments.FragmentHelper;
import com.photoapp.model.upload.UploadService;
import com.photoapp.view.fragments.SignUpFragment;
import com.photoapp.view.fragments.TabsFragment;


public class MainActivity extends AppCompatActivity {

    private static final int LAYOUT_MAIN_ID = R.id.mainFrLayout;
    private static final String LOG_TAG = MainActivity.class.getCanonicalName();


    private FragmentHelper fragmentHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bus.register(this);
        final DAOHelper daoHelper = MyApplication.instance.getDaoHelper();


        boolean anim = false;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentHelper = new FragmentHelper(fragmentManager);

        if (daoHelper.retrieveUser() == null) {
            showSignUpFragment(anim);
        } else {
            showTabFragment(anim);
        }

        AsyncTask<Void, Void, Void> execute = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                daoHelper.checkNotUploadedFiles();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                UploadService.startService(MainActivity.this);
            }
        }.execute();


    }

    boolean internetListenerRegistred;
    boolean locationListenerRegistred;
    boolean broadcastListenerRegistred;

    @Override
    protected void onResume() {
        super.onResume();
        internetListenerRegistred = false;
        locationListenerRegistred = false;
        broadcastListenerRegistred = false;


        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    MyApplication.instance.internetHelper.register();
                    internetListenerRegistred = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "error on start internet listener");
                }

                try {

                    MyApplication.instance.locationHelper.startLocation();
                    locationListenerRegistred = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "error on start location listener");
                }

                try {
                    MyApplication.instance.broadcastReceiverHelper.registerReceiver();
                    broadcastListenerRegistred = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "error on start broadcast listener");
                }

                return null;
            }
        }.execute();

        UploadService.startService(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (broadcastListenerRegistred) {
                MyApplication.instance.broadcastReceiverHelper.unregisterReceiver();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "error on pause broadcast listener");
        }
        try {
            if (locationListenerRegistred) {
                MyApplication.instance.locationHelper.stopLocation();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "error on pause location listener");
        }
        try {
            if (internetListenerRegistred) {
                MyApplication.instance.internetHelper.unregister();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "error on pause internet listener");
        }

        internetListenerRegistred = false;
        locationListenerRegistred = false;
        broadcastListenerRegistred = false;
    }


    public void onEvent(SignUpEvent event) {
        boolean anim = true;
        showTabFragment(anim);
    }

    public void onEvent(final DialogEvent event) {

        final Context mContext = this;
        UIThreadHelper.executeInUIThread(new Runnable() {
            @Override
            public void run() {
                DialogEvent.DialogType dialogType = event.dialogType;
                Log.w(LOG_TAG, "show dialog " + dialogType);


                switch (dialogType) {
                    case NO_LOCATION_SERVICES:
                        DialogHelper.showDialog(
                                mContext,
                                R.string.dialog_title_error,
                                R.string.dialog_text_location_service_not_available,
                                new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        mContext.startActivity(intent);
                                    }
                                });
                        break;
                    case FILE_WAS_DELETED:
                        String deleteFileFormat = mContext.getString(R.string.dialog_text_file_deleted);
                        String string = mContext.getString(R.string.dialog_title_error);
                        DialogHelper.showDialog(mContext, string, String.format(deleteFileFormat, event.filename));
                        break;
                    case UPLOAD_ERROR:
                        String formatErrorUploadFile = getString(R.string.dialog_text_upload_error);
                        String dialog_title_error = getString(R.string.dialog_title_error);
                        DialogHelper.showDialog(mContext, dialog_title_error, String.format(formatErrorUploadFile, event.filename));
                        break;
                    case INTERNET_DISCONNECTED:
                        DialogHelper.showDialog(mContext, R.string.dialog_title_error, R.string.dialog_text_internet_disconnected);
                        break;
                    case NO_SD_CARD:
                        DialogHelper.showDialog(mContext, R.string.dialog_title_error, R.string.dialog_text_no_sd_card);
                        break;
                    case NO_ENOUGH_SPACE:
                        DialogHelper.showDialog(mContext, R.string.dialog_title_error, R.string.dialog_text_not_enough_memory);
                        break;
                    case DECODER_NOT_AVAILABLE:
                        // Don't show this dialog
//                        DialogHelper.showDialog(mContext, R.string.dialog_title_warning, R.string.dialog_text_address_decoder_not_available);
                        break;
                    case NETWORK_NOT_AVAILABLE:
                        // Don't show this dialog
//                        DialogHelper.showDialog(mContext, R.string.dialog_title_warning, R.string.dialog_text_address_decoder_not_available);
                        break;
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        Bus.unregister(this);
        super.onDestroy();
    }


    public void showSignUpFragment(boolean anim) {

        SignUpFragment signUpFragmentFragment = SignUpFragment.newInstance();


        int layoutId = LAYOUT_MAIN_ID;
        fragmentHelper.changeFragment(layoutId, signUpFragmentFragment, anim);
    }

    public void showTabFragment(boolean anim) {

        TabsFragment tabsFragmentFragment = TabsFragment.newInstance();

        int layoutId = LAYOUT_MAIN_ID;
        fragmentHelper.changeFragment(layoutId, tabsFragmentFragment, anim);
    }
}
