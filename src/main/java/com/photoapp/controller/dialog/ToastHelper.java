package com.photoapp.controller.dialog;

import android.content.Context;
import android.widget.Toast;

/**
 * Wrapper for toast
 */
public class ToastHelper {
    public static void showToast(Context context, int idMsg) {
        Toast.makeText(context, context.getString(idMsg), Toast.LENGTH_SHORT)
                .show();
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                .show();
    }
}
