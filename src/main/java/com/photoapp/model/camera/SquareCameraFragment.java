package com.photoapp.model.camera;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.photoapp.R;
import com.photoapp.controller.dialog.DialogHelper;
import com.photoapp.model.debug.Log;

import java.io.IOException;
import java.util.List;

public abstract class SquareCameraFragment extends Fragment implements SurfaceHolder.Callback, Camera.PictureCallback {


    public static final String LOG_TAG = SquareCameraFragment.class.getSimpleName();
    public static final String CAMERA_ID_KEY = "camera_id";
    public static final String CAMERA_FLASH_KEY = "flash_mode";
    public static final String IMAGE_INFO = "image_info";
    private static final int SIZE_NOT_EXIST = -1;

    private int mCameraID;
    private String mFlashMode;
    private Camera mCamera;
    private SquareCameraPreview mPreviewView;
    private SurfaceHolder mSurfaceHolder;

    private ImageParameters mImageParameters;

    private CameraOrientationListener mOrientationListener;
    private boolean mIsSafeToTakePhoto;
    protected int mPictureMaxSideSize;

    public boolean isSafeToTakePhoto() {
        return mIsSafeToTakePhoto;
    }

    public SquareCameraFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOrientationListener = new CameraOrientationListener(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore your state here because a double rotation with this fragment
        // in the backstack will cause improper state restoration
        // onCreate() -> onSavedInstanceState() instead of going through onCreateView()
        if (savedInstanceState == null) {
            mCameraID = getBackCameraID();
            mFlashMode = CameraSettingPreferences.getCameraFlashMode(getActivity());
            mImageParameters = new ImageParameters();
        } else {
            mCameraID = savedInstanceState.getInt(CAMERA_ID_KEY);
            mFlashMode = savedInstanceState.getString(CAMERA_FLASH_KEY);
            mImageParameters = savedInstanceState.getParcelable(IMAGE_INFO);
        }

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mOrientationListener.enable();

        mPreviewView = (SquareCameraPreview) view.findViewById(R.id.camera_preview_view);
        mPreviewView.getHolder().addCallback(SquareCameraFragment.this);

        mImageParameters.mIsPortrait =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (savedInstanceState == null) {
            ViewTreeObserver observer = mPreviewView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageParameters.mPreviewWidth = mPreviewView.getWidth();
                    mImageParameters.mPreviewHeight = mPreviewView.getHeight();

                    mImageParameters.mCoverWidth = mImageParameters.mCoverHeight
                            = mImageParameters.calculateCoverWidthHeight();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mPreviewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mPreviewView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        } else {

        }

        final View changeCameraFlashModeBtn = view.findViewById(R.id.button_flash);
        changeCameraFlashModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_AUTO)) {
                    mFlashMode = Camera.Parameters.FLASH_MODE_ON;
                    CameraSettingPreferences.saveCameraFlashMode(getActivity(), mFlashMode);
                } else if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_ON)) {
                    mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
                    CameraSettingPreferences.saveCameraFlashMode(getActivity(), mFlashMode);
                } else if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_OFF)) {
                    mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
                    CameraSettingPreferences.saveCameraFlashMode(getActivity(), mFlashMode);
                }

                setupFlashMode();
                setupCamera();
            }
        });
        setupFlashMode();

    }

    private void setupFlashMode() {
        View view = getView();
        if (view == null) return;

        final ImageView autoFlashIcon = (ImageView) view.findViewById(R.id.flash_icon);

        if (Camera.Parameters.FLASH_MODE_AUTO.equalsIgnoreCase(mFlashMode)) {
            autoFlashIcon.setImageResource(R.drawable.ic_flash_auto_white_24dp);
        } else if (Camera.Parameters.FLASH_MODE_ON.equalsIgnoreCase(mFlashMode)) {
            autoFlashIcon.setImageResource(R.drawable.ic_flash_on_white_24dp);
        } else if (Camera.Parameters.FLASH_MODE_OFF.equalsIgnoreCase(mFlashMode)) {
            autoFlashIcon.setImageResource(R.drawable.ic_flash_off_white_24dp);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//      Log.d(LOG_TAG, "onSaveInstanceState");
        outState.putInt(CAMERA_ID_KEY, mCameraID);
        outState.putString(CAMERA_FLASH_KEY, mFlashMode);
        outState.putParcelable(IMAGE_INFO, mImageParameters);
        super.onSaveInstanceState(outState);
    }

    private void getCamera(int cameraID) {
        try {
            mCamera = Camera.open(cameraID);
            mPreviewView.setCamera(mCamera);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Can't open camera with id " + cameraID);
//            e.printStackTrace();
        }
    }

    /**
     * Restart the camera preview
     */
    private void restartPreview() {
        if (mCamera != null) {
            stopCameraPreview();
            mCamera.release();
            mCamera = null;
        }

        getCamera(mCameraID);
        startCameraPreview();
    }

    /**
     * Start the camera preview
     */
    private void startCameraPreview() {
        if (mCamera != null) {
            determineDisplayOrientation();
            setupCamera();

            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();

                setSafeToTakePhoto(true);
                setCameraFocusReady(true);
            } catch (IOException e) {
                Log.d(LOG_TAG, "Can't start camera preview due to IOException " + e);
                e.printStackTrace();
            }
        } else {
            Context context = getActivity();
            DialogHelper.showDialog(context, R.string.dialog_title_error, R.string.dialog_text_camera_not_available);
        }
    }

    /**
     * Stop the camera preview
     */
    private void stopCameraPreview() {
        setSafeToTakePhoto(false);
        setCameraFocusReady(false);

        // Nulls out callbacks, stops face detection
        mCamera.stopPreview();
        mPreviewView.setCamera(null);
    }

    public void setSafeToTakePhoto(final boolean isSafeToTakePhoto) {
        mIsSafeToTakePhoto = isSafeToTakePhoto;
    }

    private void setCameraFocusReady(final boolean isFocusReady) {
        if (this.mPreviewView != null) {
            mPreviewView.setIsFocusReady(isFocusReady);
        }
    }

    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly
     */
    private void determineDisplayOrientation() {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);

        // Clockwise rotation needed to align the window display to the natural position
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: {
                degrees = 0;
                break;
            }
            case Surface.ROTATION_90: {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180: {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270: {
                degrees = 270;
                break;
            }
        }

        int displayOrientation;

        // CameraInfo.Orientation is the angle relative to the natural position of the device
        // in clockwise rotation (angle that is rotated clockwise from the natural position)
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            // Orientation is angle of rotation when facing the camera for
            // the camera image to match the natural orientation of the device
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        mImageParameters.mDisplayOrientation = displayOrientation;
        mImageParameters.mLayoutOrientation = degrees;

        mCamera.setDisplayOrientation(mImageParameters.mDisplayOrientation);
    }

    /**
     * Setup the camera parameters
     */
    private void setupCamera() {
        // Never keep a global parameters
        Camera.Parameters parameters = mCamera.getParameters();

        Size bestPreviewSize = determineBestPreviewSize(parameters);
        Size bestPictureSize = determineBestPictureSize(parameters);

        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
//        parameters.setPreviewSize(bestPictureSize.width, bestPictureSize.height);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);


        // Set continuous picture focus, if it's supported
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        final View changeCameraFlashModeBtn = getView().findViewById(R.id.button_flash);
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(mFlashMode)) {
            parameters.setFlashMode(mFlashMode);
            changeCameraFlashModeBtn.setVisibility(View.VISIBLE);
        } else {
            changeCameraFlashModeBtn.setVisibility(View.INVISIBLE);
        }

        // Lock in the changes
        mCamera.setParameters(parameters);
    }

    private Size determineBestPreviewSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPreviewSizes());
    }

    private Size determineBestPictureSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPictureSizes(), mPictureMaxSideSize);
    }

    private Size determineBestSize(List<Size> sizes) {
        return determineBestSize(sizes, SIZE_NOT_EXIST);
    }

    private Size determineBestSize(List<Size> sizes, int maxSideSize) {
        Size bestSize = null;
        Size size;
        int numOfSizes = sizes.size();
        for (int i = 0; i < numOfSizes; i++) {
            size = sizes.get(i);
            int x = (size.width / 4);
            int y = (size.height / 3);
            boolean isDesireRatio = x == y;
            boolean isBetterSize = (bestSize == null) || size.width > bestSize.width;

            if (maxSideSize != SIZE_NOT_EXIST) {
                if (isDesireRatio && isBetterSize) {
                    int maxSide = maxSideSize;
                    if (size.width < maxSide && size.height < maxSide) {
                        bestSize = size;
                    }
                }
            } else {
                if (isDesireRatio && isBetterSize) {
                    bestSize = size;
                }
            }
        }

        if (bestSize == null) {
            Log.d(LOG_TAG, "cannot find the best camera size");
            return sizes.get(sizes.size() - 1);
        }

        return bestSize;
    }

    private int getBackCameraID() {
        return CameraInfo.CAMERA_FACING_BACK;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCamera == null && mSurfaceHolder != null) {
            restartPreview();
        }

        if (mOrientationListener != null) {
            mOrientationListener.enable();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mOrientationListener.disable();
    }


    @Override
    public void onStop() {


        // stop the preview
        if (mCamera != null) {
            stopCameraPreview();
            mCamera.release();
            mCamera = null;
        }

        super.onStop();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;

        getCamera(mCameraID);
        startCameraPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // The surface is destroyed with the visibility of the SurfaceView is set to View.Invisible
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case 1:
                Uri imageUri = data.getData();
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * A picture has been taken
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        int rotation = getPhotoRotation();


        ImageParameters imageParameters = mImageParameters.createCopy();
        imageParameters.mIsPortrait =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        savePicture(data, rotation);

        restartPreview();
        setSafeToTakePhoto(true);

    }

    protected abstract void savePicture(byte[] data, int rotation);

    private int getPhotoRotation() {
        int rotation;
        int orientation = mOrientationListener.getRememberedNormalOrientation();
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(mCameraID, info);

        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {
            rotation = (info.orientation + orientation) % 360;
        }

        return rotation;
    }

    /**
     * When orientation changes, onOrientationChanged(int) of the listener will be called
     */
    private static class CameraOrientationListener extends OrientationEventListener {

        private int mCurrentNormalizedOrientation;
        private int mRememberedNormalOrientation;

        public CameraOrientationListener(Context context) {
            super(context, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation);
            }
        }

        /**
         * @param degrees Amount of clockwise rotation from the device's natural position
         * @return Normalized degrees to just 0, 90, 180, 270
         */
        private int normalize(int degrees) {
            if (degrees > 315 || degrees <= 45) {
                return 0;
            }

            if (degrees > 45 && degrees <= 135) {
                return 90;
            }

            if (degrees > 135 && degrees <= 225) {
                return 180;
            }

            if (degrees > 225 && degrees <= 315) {
                return 270;
            }

            throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
        }

        public void rememberOrientation() {
            mRememberedNormalOrientation = mCurrentNormalizedOrientation;
        }

        public int getRememberedNormalOrientation() {
            rememberOrientation();
            return mRememberedNormalOrientation;
        }
    }


    /**
     * Take a picture
     */
    public boolean performTakePicture() {

        boolean pictureTaken = false;

        if (mIsSafeToTakePhoto) {
            setSafeToTakePhoto(false);

            mOrientationListener.rememberOrientation();

            // Shutter callback occurs after the image is captured. This can
            // be used to trigger a sound to let the user know that image is taken
            Camera.ShutterCallback shutterCallback = null;

            // Raw callback occurs when the raw image data is available
            Camera.PictureCallback raw = null;

            // postView callback occurs when a scaled, fully processed
            // postView image is available.
            Camera.PictureCallback postView = null;

            Camera.Parameters parameters = mCamera.getParameters();


            Size bestPictureSize = determineBestPictureSize(parameters);


            parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);

            mCamera.setParameters(parameters);
            // jpeg callback occurs when the compressed image is available
            mCamera.takePicture(shutterCallback, raw, postView, this);

            pictureTaken = true;
        }

        return pictureTaken;
    }

}
