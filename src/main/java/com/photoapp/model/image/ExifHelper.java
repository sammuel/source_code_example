package com.photoapp.model.image;

import android.media.ExifInterface;

import com.photoapp.model.debug.Log;

import java.io.IOException;

/**
 * Created by admin on 24.10.15.
 */
public class ExifHelper {

    private static final String LOG_TAG = ExifHelper.class.getCanonicalName();

    public static int getExifOrientation(String lastTakenPicturePath) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(lastTakenPicturePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
    }

    public static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        } else if (exifOrientation == ExifInterface.ORIENTATION_NORMAL) {

        }
        return 0;
    }

    public static void saveNewExifRotattion(String absolutePath, int rotation) throws IOException {



            ExifInterface exif = new ExifInterface(absolutePath);
            int newOrient = -1;

            switch (rotation) {
                case 90:
                    newOrient = ExifInterface.ORIENTATION_ROTATE_90;
                    break;
                case 180:
                    newOrient = ExifInterface.ORIENTATION_ROTATE_180;
                    break;
                case 270:
                    newOrient = ExifInterface.ORIENTATION_ROTATE_270;
                    break;
                case 0:
                    newOrient = ExifInterface.ORIENTATION_NORMAL;
                    break;
            }
            Log.w(LOG_TAG, String.format("write exif " + newOrient));
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, "" + newOrient);
            exif.saveAttributes();


    }

}
