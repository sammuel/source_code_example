package com.photoapp.view.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.photoapp.MyApplication;
import com.photoapp.R;
import com.photoapp.controller.Constants;
import com.photoapp.controller.button.ButtonHelper;
import com.photoapp.controller.dialog.DialogHelper;
import com.photoapp.controller.dialog.ToastHelper;
import com.photoapp.controller.events.DialogEvent;
import com.photoapp.controller.thread.UIThreadHelper;
import com.photoapp.model.camera.SquareCameraFragment;
import com.photoapp.model.camera.SquareCameraPreview;
import com.photoapp.model.data.StorageHelper;
import com.photoapp.model.database.DAOHelper;
import com.photoapp.model.database.PictureFile;
import com.photoapp.model.debug.Log;
import com.photoapp.model.events.Bus;
import com.photoapp.model.image.BitmapHelper;
import com.photoapp.model.image.ExifHelper;
import com.photoapp.model.internet.InternetHelper;
import com.photoapp.model.location.LocationHelper;

import java.io.File;
import java.io.IOException;

public class CameraFragment extends SquareCameraFragment {
    public static final String LOG_TAG = CameraFragment.class.getSimpleName();

    private static final int NO_LOCATION_RESULT = 0;
    private static final int NO_NETWORK_RESULT = 1;
    private static final int NO_ARRDESS_RESULT = 2;
    private static final int OK_RESULT = 3;
    private static final int NOT_ENOUGH_SPACE = 4;
    private static final int NOT_AVAILABLE_SD_CARD = 5;
    public static final int CAMERA_INIT_DELAY_IN_MS = 2000;
    private ProgressDialog progressDialog;

    public static Fragment newInstance() {
        return new CameraFragment();
    }

    public CameraFragment() {
        this.mPictureMaxSideSize = Constants.PHOTO_MAX_SIDE_SIZE_IN_PX;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InitProgress(view);
        InitButtons(view);
        initLastTakenImagePreview(view);
    }

    private void InitProgress(View view) {
        final CircularProgressView circularProgressView = (CircularProgressView) view.findViewById(R.id.progress_view);
        final FrameLayout layoutCameraParent = (FrameLayout) view.findViewById(R.id.layout_camera_parent);
        final SquareCameraPreview squareCameraPreview = (SquareCameraPreview) view.findViewById(R.id.camera_preview_view);
        final LinearLayout buttonFlash = (LinearLayout) view.findViewById(R.id.button_flash);

        layoutCameraParent.setBackgroundResource(R.color.background_white);
        squareCameraPreview.setVisibility(View.GONE);
        circularProgressView.setVisibility(View.VISIBLE);
        buttonFlash.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                layoutCameraParent.setBackgroundResource(R.color.background_black);
                squareCameraPreview.setVisibility(View.VISIBLE);
                circularProgressView.setVisibility(View.GONE);
                buttonFlash.setVisibility(View.VISIBLE);
            }
        }, CAMERA_INIT_DELAY_IN_MS);
    }

    private void initLastTakenImagePreview(View view) {
        DAOHelper daoHelper = MyApplication.instance.getDaoHelper();

        final PictureFile lastTakenPicture = daoHelper.getLastTakenPicture();
        final DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
        final Context context = getContext();
        ImageView imageSmallPreview = (ImageView) view.findViewById(R.id.image_preview_last_taken);
        imageSmallPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastTakenPicture != null)
                    DialogHelper.showLastTakenDialog(context, lastTakenPicture.getFull_path_on_device(), displaymetrics.widthPixels, displaymetrics.heightPixels);
            }
        });

        if (lastTakenPicture != null) {

            final String lastTakenPicturePath = lastTakenPicture.getFull_path_on_device();
            Resources resources = context.getResources();
            int width = resources.getDimensionPixelSize(R.dimen.image_last_image_size);
            int height = resources.getDimensionPixelSize(R.dimen.image_last_image_size);
            boolean needCheckRotate = true;

            Bitmap smallBitmap=loadBitmap(lastTakenPicturePath,width,height,needCheckRotate);

            if (smallBitmap != null) {
                imageSmallPreview.setImageBitmap(smallBitmap);
            }

        }
    }

    private void InitButtons(View view) {
        final ImageView takePhotoButton = (ImageView) view.findViewById(R.id.button_capture_image);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {

            public long lastClickTime;

            @Override
            public void onClick(View v) {
                takePhotoButton.setEnabled(false);
                long currentTimeInMs = System.currentTimeMillis();
                long diffTime = currentTimeInMs - lastClickTime;
                Log.w(LOG_TAG, "difftime = " + diffTime);
                if (diffTime > ButtonHelper.PREVENT_CLICK_TIME_LONG_IN_MS) {
                    Log.w(LOG_TAG, "start take photo diff time = " + diffTime);
                    Context context = getActivity();
                    showProgress(context);
                    lastClickTime = currentTimeInMs;
                    startTakePicture();
                    ButtonHelper.preventDoubleClickLong(takePhotoButton);
                }

            }
        });
    }

    private void showProgress(Context context) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = DialogHelper.showProgressDialog(context);
        }
    }

    /**
     * Take a picture
     */
    private void startTakePicture() {

        Context context1 = getContext();
        LocationHelper locationHelper = LocationHelper.getInstatce(context1);

        Context context = getActivity();
        boolean locationEnabled = locationHelper.isLocationEnabled();
        if (!locationEnabled) {
            DialogHelper.showDialog(context, R.string.dialog_title_error, R.string.dialog_text_location_service_not_available);
            hideProgress();
            return;
        }

        boolean coordinatesObtained = locationHelper.isCoordinatesObtained();
        if (!coordinatesObtained) {
            DialogHelper.showDialog(context, R.string.dialog_title_error, R.string.dialog_text_location_coordinates_not_obtained);
            hideProgress();
            return;
        }

        boolean isCanTakePicture = isSafeToTakePhoto();
        if (isCanTakePicture) {

            performTakePicture();

        } else

        {
            hideProgress();
            DialogHelper.showDialog(context, R.string.dialog_title_error, R.string.dialog_text_camera_not_available);
            return;
        }

    }

    private Bitmap loadBitmap(String picturePath,int width, int height, boolean checkRotate){
        Bitmap bitmap = null;
        try {
            bitmap = BitmapHelper.loadResizedBitmapByPath(picturePath, width, height, checkRotate);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }


    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void savePicture(final byte[] data, final int rotation) {

        final Context context = getActivity();
        final LocationHelper locationHelper = LocationHelper.getInstatce(context);

        boolean geoCoderAsyncRunned = locationHelper.isGeoCoderAsyncRunned();


        boolean geoCoderAsyncCanceled = false;
        if(geoCoderAsyncRunned) {
            geoCoderAsyncCanceled = true;
            locationHelper.cancelGeoCoderAsync();
        }

        new AsyncTask<Void, Void, Integer>() {


            boolean saved;
            String filename;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();


            }


            @Override
            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);
                if (result == NOT_AVAILABLE_SD_CARD) {
                    Bus.post(new DialogEvent(DialogEvent.DialogType.NO_SD_CARD));


                } else if (result == NOT_ENOUGH_SPACE) {
                    Bus.post(new DialogEvent(DialogEvent.DialogType.NO_ENOUGH_SPACE));
                } else {

                    if (saved) {
                        String fileSavedFormat = context.getString(R.string.toast_file_saved);
                        ToastHelper.showToast(context, String.format(fileSavedFormat, filename));
                    }


                    View view = getView();
                    initLastTakenImagePreview(view);

                    if (result == NO_NETWORK_RESULT) {
                        InternetHelper internetHelper = MyApplication.instance.getInternetHelper();
                        boolean connected = internetHelper.isConnected();
                        boolean mobileConnected = internetHelper.isMobileConnected();
                        if (connected && mobileConnected) {
                            Bus.post(new DialogEvent(DialogEvent.DialogType.DECODER_NOT_AVAILABLE));

                        } else {
                            Bus.post(new DialogEvent(DialogEvent.DialogType.NETWORK_NOT_AVAILABLE));

                        }
                    }
                }
                View view = getView();
                final ImageView takePhotoButton = (ImageView) view.findViewById(R.id.button_capture_image);
                takePhotoButton.setEnabled(true);

                hideProgress();
            }

            @Override
            protected Integer doInBackground(Void... params) {

                Integer result = null;

                Boolean isSDPresent = StorageHelper.isSdCardAvailable();
                if (!isSDPresent) {
                    result = NOT_AVAILABLE_SD_CARD;
                }
                boolean isEnoughSpace = StorageHelper.isEnoughSpace(data);
                if (isEnoughSpace) {
                    result = NOT_ENOUGH_SPACE;
                }

                if (result == null) {
                    String timeStamp = null;

                    long unixTime = System.currentTimeMillis();
                    timeStamp = StorageHelper.formatTimeForFilename(unixTime);


                    String location = null;

                    double lat = locationHelper.getLat();
                    double lon = locationHelper.getLon();
                    String geo = locationHelper.getGeoName();

                    if (geo == null) {
                        location = StorageHelper.formatLatLon(lat, lon);
                        result = NO_NETWORK_RESULT;
                    } else {
                        result = OK_RESULT;
                        location = StorageHelper.formatLatLonGeoname(lat, lon, geo);
                    }

                    DAOHelper daoHelper = MyApplication.instance.getDaoHelper();
                    String user = daoHelper.getUserName();
                    filename = StorageHelper.formatFilename(user, location, timeStamp);
                    File file = StorageHelper.createImageFile(context, filename);


                    try {
                        StorageHelper.saveImageToFile(file, data);
                        saved = true;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        Log.e(LOG_TAG, "Error on save file " + filename + " . Don't add it to database");
                        saved = false;
                    }


                    if (saved) {
                        String absolutePath = file.getAbsolutePath();
                        try {
                            ExifHelper.saveNewExifRotattion(absolutePath, rotation);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(LOG_TAG, "Error on save file EXIF " + filename + " . Don't add it to database");
                            saved = false;
                        }

                        if (saved) {
                            String pictureFileName = filename;

                            int status = DAOHelper.FILE_STATUS_IN_QUEUE;
                            boolean isDeleted = false;
                            daoHelper.addPictureData(absolutePath, pictureFileName, status, lat, lon, geo, unixTime, isDeleted);

                        }

                    }

                }


                return result;
            }
        }.execute();

        if(geoCoderAsyncCanceled) {
            locationHelper.startGeoCoderAsync();
        }
    }

    private void hideProgress() {
        boolean progressNotNull = progressDialog != null;
        if (progressNotNull) {
            boolean progressShowing = progressDialog.isShowing();
            if (progressShowing) {
                progressDialog.dismiss();
            }
        }
    }


}
