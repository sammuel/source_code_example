package com.photoapp.model.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import com.photoapp.model.debug.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapHelper {


    private static final String LOG_TAG = BitmapHelper.class.getCanonicalName();

    public static Bitmap loadResizedBitmapByPath(String path, int maxWidth, int maxHeight, boolean needCheckExifOrientation) throws IOException {
        Bitmap result;

        BitmapFactory.Options options = new BitmapFactory.Options();

        Bitmap scaledBitmap = scaleBitmap(path,maxWidth, maxHeight, options);

        int outWidth = options.outWidth;
        int outHeight = options.outHeight;

        if (!(outWidth <= maxWidth && outHeight <= maxHeight)) {

            float ratio = (float) outWidth / (float) outHeight;

            int resizedWidth;
            int resizedHeight;

            if (ratio < 1) {
                resizedHeight = maxHeight;
                resizedWidth = (int) ((float) resizedHeight * ratio);
            } else {
                resizedWidth = maxWidth;
                resizedHeight = (int) ((float) resizedWidth / ratio);
            }

            result = Bitmap.createScaledBitmap(scaledBitmap, resizedWidth, resizedHeight, true);

            if (result != scaledBitmap) {
                scaledBitmap.recycle();
            }

        } else {

            result = scaledBitmap;
        }

        if(needCheckExifOrientation) {
            int orient = ExifHelper.getExifOrientation(path);

            try {
                if (result != null) {
                    result = BitmapHelper.checkBitmapRotation(result, orient);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return result;
    }

    private static Bitmap scaleBitmap(String path,int width, int height, BitmapFactory.Options options) throws IOException {

        options.inSampleSize = calculateScale(path, width, height);
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        InputStream inputStream = new FileInputStream(path);

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

        inputStream.close();

        return bitmap;
    }


    private static int calculateScale(String path, int maxWidth, int maxHeight) throws IOException {
        InputStream inputStream = new FileInputStream(path);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        int origWidth = options.outWidth;
        int origHeight = options.outHeight;

        int scale;

        if (origWidth > origHeight) {
            scale = Math.round((float) origHeight / (float) maxWidth);
        } else {
            scale = Math.round((float) origWidth / (float) maxHeight);
        }

        return scale;
    }


    public static Bitmap checkBitmapRotation(Bitmap bitmap, int rotation) throws IOException {


        int rotationInDegrees = ExifHelper.exifToDegrees(rotation);

        Bitmap rotatedBitmap = null;
        if (rotationInDegrees != 0) {
            rotatedBitmap = rotateBitmap(bitmap, rotationInDegrees);
            bitmap.recycle();

        } else {
            rotatedBitmap = bitmap;
        }
        Log.w(LOG_TAG, String.format("rotate Exif orientation = %d converted in degrees %d", rotation, rotationInDegrees));

        return rotatedBitmap;
    }




    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.setRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(),
                source.getHeight(), matrix, true);
    }


    public static Bitmap decodeSampledBitmapFromByteArray(byte[] data, int offset, int length, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, offset, length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, offset, length, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
