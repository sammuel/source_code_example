package com.photoapp.controller.keyboard;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyBoardHelper {

    public static void show(Context context) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (!imm.isActive()) {

            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY); // show
        }
    }

    public static void hide(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = activity.getCurrentFocus();
        if (v == null)
            return;

        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    public static void hide(Context context, View focused) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = focused;
        if (v == null)
            return;

        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}