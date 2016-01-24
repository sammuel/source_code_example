package com.photoapp.controller.button;

import android.os.Handler;
import android.widget.ImageView;

public class ButtonHelper {

    public static final int PREVENT_CLICK_TIME_IN_MS = 1000;
    public static final int PREVENT_CLICK_TIME_MIDDLE_IN_MS = 2000;
    public static final int PREVENT_CLICK_TIME_LONG_IN_MS = 3000;

    public static void preventDoubleClick(final ImageView button) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(true);
            }
        }, PREVENT_CLICK_TIME_IN_MS);
        button.setEnabled(false);
    }

    public static void preventDoubleClickLong(final ImageView button) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(true);
            }
        }, PREVENT_CLICK_TIME_LONG_IN_MS);
        button.setEnabled(false);
    }
}
