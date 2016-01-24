package com.photoapp.model.camera;

import android.content.Context;
import android.support.annotation.NonNull;

import com.photoapp.MyApplication;
import com.photoapp.model.database.DAOHelper;

/**
 * Created by desmond on 4/10/15.
 */
public class CameraSettingPreferences {


    public static void saveCameraFlashMode(@NonNull final Context context, @NonNull final String cameraFlashMode) {

        MyApplication.instance.getDaoHelper().setFlashState(cameraFlashMode);
    }

    public static String getCameraFlashMode(@NonNull final Context context) {

        DAOHelper daoHelper = MyApplication.instance.getDaoHelper();
        String flashState = daoHelper.getFlashState();
        return flashState;
    }
}
