package com.photoapp.model.data;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;

import com.photoapp.model.debug.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class StorageHelper {


    private static final String IMAGE_SUFFIX = "jpg";
    private static final String IMAGE_DATE_PATTERN = "dd.MM.yyyy_HH.mm.ss.SSS";
    private static final String IMAGE_FILE_PATTERN = "%s_%s_%s.%s";
    private static final String LAT_LON_PATTERN = "%s_%s";
    private static final String LAT_LON_GEONAME_PATTERN = "%s_%s_%s";
    private static final String LOG_TAG = StorageHelper.class.getCanonicalName();

    public static boolean isEnoughSpace(byte data[]) {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long freeSpace = stat.getBlockSize() * stat.getFreeBlocks();
        return freeSpace <= data.length * 2;
    }

    public static boolean isSdCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    public static File createImageFile(Context context, String imageFileName) {


        File storageDirectory = calculateDirectoryForFiles(context);
        String path = storageDirectory.getPath();
        String imagePath = path + File.separator + imageFileName;
        File image = new File(imagePath);
        Log.w(LOG_TAG, "Create image in " + imagePath);

        return image;
    }

    public static String formatFilename(String userName, String location, String timeStamp) {
        return String.format(IMAGE_FILE_PATTERN, userName, location, timeStamp, IMAGE_SUFFIX);
    }

    private static File calculateDirectoryForFiles(Context context) {
        File externalFilesDir = context.getExternalFilesDir(null);
        if (!externalFilesDir.exists()) {
            externalFilesDir.mkdir();
        }
        return externalFilesDir;
    }

    @NonNull
    public static String formatTimeForFilename(long time) {
        String format = new SimpleDateFormat(IMAGE_DATE_PATTERN).format(time);
        return format;
    }

    @NonNull
    public static String formatLatLonGeoname(double lat, double lon, String geo) {
        String result;
        if (geo != null) {
            result = String.format(LAT_LON_GEONAME_PATTERN, geo, lat, lon);
        } else {
            result = formatLatLon(lat, lon);
        }

        return result;

    }

    @NonNull
    public static String formatLatLon(double lat, double lon) {
        String result;
        result = String.format(LAT_LON_PATTERN, lat, lon);
        return result;
    }

    public static void saveImageToFile(File file, byte[] data) throws IOException {

            FileOutputStream fos;
            fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
            fos.close();

    }

    public static void deleteFileOnDevice(final String filePath) {

        Log.w(LOG_TAG, "delete file on device" + filePath);
        File file = new File(filePath);
        file.delete();

    }

}
