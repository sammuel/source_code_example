package com.photoapp.controller.dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.photoapp.R;
import com.photoapp.model.image.BitmapHelper;

import java.io.IOException;

/**
 * Helper for work with dialog from Material Design library.
 */
public abstract class DialogHelper {


    public static void showDialog(Context context, String title, String msg) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(msg)
                .positiveText(context.getString(R.string.dialog_button_ok))
                .show();
    }

    public static void showDialog(Context context, int titleId, int msgId) {
        new MaterialDialog.Builder(context)
                .title(context.getString(titleId))
                .content(context.getString(msgId))
                .positiveText(context.getString(R.string.dialog_button_ok))
                .show();
    }

    public static void showDialog(Context context, int titleId, int msgId, MaterialDialog.SingleButtonCallback onPositiveCallback) {
        new MaterialDialog.Builder(context)
                .title(context.getString(titleId))
                .content(context.getString(msgId))
                .positiveText(context.getString(R.string.dialog_button_ok))
                .onPositive(onPositiveCallback)
                .show();
    }

    public static void showDialog(Context context, String title, String msg, MaterialDialog.SingleButtonCallback onPositiveCallback) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(msg)
                .positiveText(context.getString(R.string.dialog_button_ok))
                .onPositive(onPositiveCallback)
                .show();
    }

    public static ProgressDialog showProgressDialog(Context context) {
        String title = context.getString(R.string.dialog_title_loading_progress);
        CharSequence text = context.getString(R.string.dialog_text_please_wait_progress);
        ProgressDialog show = ProgressDialog.show(context, title,
                text, true);
        return show;
    }

    public static void showLastTakenDialog(Context context, String picturePath, int imgWidth, int imgHeight){
        final MaterialDialog dialog= new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_last_taken_image, false)
                .title(context.getString(R.string.dialog_title_last_taken_image))
                .build();

        final ImageView imageView=(ImageView)dialog.getCustomView().findViewById(R.id.image_last_taken);
        final Button buttonOk=(Button)dialog.getCustomView().findViewById(R.id.button_ok_dialog);
        buttonOk.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.hide();
                    }
                }
        );
        buttonOk.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonOk.setBackgroundColor(v.getResources().getColor(R.color.md_btn_selected));
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    buttonOk.setBackgroundColor(v.getResources().getColor(R.color.md_divider_white));
                }
                return false;
            }
        });
        Bitmap lastImageBitmap=null;
        int actualWidth=imgWidth-2*context.getResources().getDimensionPixelSize(R.dimen.image_last_image_margin);
        try {
            lastImageBitmap = BitmapHelper.loadResizedBitmapByPath(
                    picturePath,
                    actualWidth,
                    actualWidth,
                    true);
        }catch (IOException ex){
            ex.printStackTrace();
        }
        if(lastImageBitmap!=null) {
            imageView.setImageBitmap(lastImageBitmap);
            dialog.show();
        }
    }



}
