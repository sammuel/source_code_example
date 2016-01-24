package com.photoapp.controller.thread;

import android.os.Handler;
import android.os.Looper;

public class UIThreadHelper {

    public static void executeInUIThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
