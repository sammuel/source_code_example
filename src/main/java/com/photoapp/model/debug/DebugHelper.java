package com.photoapp.model.debug;

import android.os.Build;

public class DebugHelper {

    private static boolean isEmulator = false;

    public static boolean isEmulator() {
        return isEmulator;
    }

    public static boolean isDevice() {
        return !isEmulator;
    }

    static {
        isEmulator = Build.FINGERPRINT.startsWith("generic");
    }
}
