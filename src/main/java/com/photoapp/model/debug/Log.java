package com.photoapp.model.debug;

public class Log {

    public final static boolean LOG_ENABLED = false;

    public static void e(String tag, String message) {
        if (LOG_ENABLED)
            android.util.Log.e(tag, message);
    }

    public static void d(String tag, String message) {
        if (LOG_ENABLED)
            android.util.Log.d(tag, message);
    }

    public static void i(String tag, String message) {
        if (LOG_ENABLED)
            android.util.Log.i(tag, message);
    }

    public static void v(String tag, String message) {
        if (LOG_ENABLED)
            android.util.Log.v(tag, message);
    }

    public static void w(String tag, String message) {
        if (LOG_ENABLED)
            android.util.Log.w(tag, message);
    }


}
