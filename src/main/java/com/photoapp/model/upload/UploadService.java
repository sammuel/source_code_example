package com.photoapp.model.upload;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.photoapp.controller.Constants;
import com.photoapp.controller.dialog.ToastHelper;
import com.photoapp.model.data.StorageHelper;
import com.photoapp.model.database.PictureFile;
import com.photoapp.model.database.DAOHelper;
import com.photoapp.model.intents.BroadcastReceiverHelper;
import com.photoapp.model.internet.InternetHelper;
import com.photoapp.model.location.LocationHelper;
import com.photoapp.model.debug.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Service for uploading photos
 */
public class UploadService extends IntentService {

    private static final String LOG_TAG = UploadService.class.getSimpleName();


    private static final long DELAY_ON_FAIL_IN_MS = 500;

    private boolean isUploading;
    private InternetHelper internetHelper;
    private DAOHelper daoHelper;
    private AWSUploadHelper awsUploadHelper;

    public List<String> failedUploadsList = new ArrayList<String>();


    public static void startService(final Context context) {


        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {

                DAOHelper daoHelper = DAOHelper.getInstance(context);
                InternetHelper internetHelper = InternetHelper.getInstance(context);

                boolean isExistFilesForUploading = daoHelper.IsExistFilesForUploading();
                boolean wifiOnly = daoHelper.getWifiOnlyState();
                boolean canUpload = internetHelper.canUpload(wifiOnly);


                boolean needStartService = canUpload && isExistFilesForUploading;
                Log.w(LOG_TAG, "Try start service is exist files = " +
                                isExistFilesForUploading + " can upload = " + canUpload +
                                " wifi only  = " + wifiOnly + " need start service = " + needStartService
                );


                return needStartService;
            }

            @Override
            protected void onPostExecute(Boolean needStartService) {
                super.onPostExecute(needStartService);

                if (needStartService) {
                    Log.w(LOG_TAG, "Send intent for start service");
                    Intent intent = new Intent(context, UploadService.class);
                    context.startService(intent);

                }
            }
        }.execute();

    }

    public UploadService() {
        super(LOG_TAG);
    }

    public UploadService(String name) {
        super(name);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(LOG_TAG, "service started");
        Context context = getApplicationContext();
        if (internetHelper == null) {
            internetHelper = InternetHelper.getInstance(context);
        }
        if (daoHelper == null) {
            daoHelper = DAOHelper.getInstance(context);
        }

        if (awsUploadHelper == null) {
            awsUploadHelper = new AWSUploadHelper(context);
        }

        synchronized (failedUploadsList) {
            failedUploadsList.clear();
        }


        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        IBinder iBinder = super.onBind(intent);
        Log.w(LOG_TAG, "service binded");
        return iBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w(LOG_TAG, "service handled intent");


        uploadFiles();
    }

    private void uploadFiles() {


        boolean needWork = true;
        while (needWork) {

            boolean uploading = isUploading;
            boolean connected = internetHelper.isConnected();
            boolean wifiOnlyState = daoHelper.getWifiOnlyState();
            boolean wifiConnected = internetHelper.isWifiConnected();

            boolean canUploadWifi = internetHelper.canUploadWifi();
            boolean canUploadCellular = internetHelper.canUploadCellular(wifiOnlyState);
            boolean canUpload = internetHelper.canUpload(wifiOnlyState);

            Log.w(LOG_TAG,
                    "onUpload files"
                            + " isUploading = " + uploading
                            + " conneted = " + connected
                            + " wifionly = " + wifiOnlyState
                            + " wifi connected = " + wifiConnected
                            + " canUpload = " + canUpload
                            + " canUploadWifi = " + canUploadWifi
                            + " canUploadCellular = " + canUploadCellular
            );

            if (!uploading) {
                uploadCycleStarted();
                checkFilesForDelete();
                if (canUpload) {
                    boolean uploadStarted = startUploadFile();
                    while (isUploading) {
                        doThreadSleepAndDelay();
                    }
                    if (!uploadStarted) {
                        needWork = false;
                    }

                } else {
                    needWork = false;
                    uploadCycleFinished();
                }
            } else {
                needWork = false;
            }
            Thread.yield();

        }
    }


    private void checkFilesForDelete() {
        Log.w(LOG_TAG, "checkfilesfordelete");
        daoHelper.deleteUploadedFilesOnDevice();
    }

    private void uploadCycleStarted() {
        Log.w(LOG_TAG, "uploadCycleStarted");
        isUploading = true;

    }

    private void uploadCycleFinished() {
        Log.w(LOG_TAG, "uploadCycleFinished");
        isUploading = false;
    }

    private boolean startUploadFile() {
        boolean uploadStarted;
        PictureFile file = daoHelper.selectPictureFileForUploading();
        if (file != null) {
            String filename = file.getFilename();
            boolean previouslyFailed = isPreviouslyFailed(filename);
            if (previouslyFailed) {
                file = tryFoundNewFileForUpload(filename);

                if (file != null) {
                    uploadStarted = true;
                    beginUpload(file);
                } else {
                    uploadStarted = false;
                    synchronized (failedUploadsList) {
                        failedUploadsList.clear();
                    }
                    uploadCycleFinished();
                }

            } else {
                uploadStarted = true;
                beginUpload(file);
            }

        } else {
            uploadStarted = false;
            uploadCycleFinished();
        }

        return uploadStarted;

    }

    @Nullable
    private PictureFile tryFoundNewFileForUpload(String filename) {
        PictureFile file;
        Log.w(LOG_TAG, "Skip file, which previously failed for upload = " + filename);
        List<PictureFile> pictureFiles = daoHelper.selectPictureFilesForUploading();

        PictureFile newFileForUpload = null;

        for (PictureFile currentFile : pictureFiles) {
            String currentFileFilename = currentFile.getFilename();
            boolean currentPreviouslyFailed = isPreviouslyFailed(currentFileFilename);
            if (!currentPreviouslyFailed) {
                newFileForUpload = currentFile;
                break;
            }
        }
        file = newFileForUpload;
        return file;
    }

    private boolean isPreviouslyFailed(String filename) {
        boolean failed;
        synchronized (failedUploadsList) {
            failed = failedUploadsList.contains(filename);
        }
        return failed;
    }

    private void beginUpload(PictureFile pictureFile) {
        String full_path_on_device = pictureFile.getFull_path_on_device();
        Log.w(LOG_TAG, "try start upload file = " + full_path_on_device);
        File fileFromPath = new File(full_path_on_device);
        boolean exists = fileFromPath.exists();
        if (exists) {
            boolean addressExist = isAddressExist(pictureFile);
            Log.w(LOG_TAG, "file " + full_path_on_device + " EXIST. Try upload it. Address  exist = " + addressExist);

            if (!addressExist) {
                Log.w(LOG_TAG, "file " + full_path_on_device + " don't have geoname, try found it ");
                try {
                    String newGeoname = retrieveGeoName(pictureFile);

                    if (newGeoname != null && !newGeoname.isEmpty()) {
                        renameFile(fileFromPath, pictureFile, newGeoname);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                addressExist = isAddressExist(pictureFile);
                full_path_on_device = pictureFile.getFull_path_on_device();
                Log.w(LOG_TAG, "file " + full_path_on_device + " address retrieved  = " + addressExist);
            }

            if (addressExist) {
                File localFile = new File(full_path_on_device);
                String remoteName = pictureFile.getFilename();
                initUpload(pictureFile, localFile, remoteName);
                Log.w(LOG_TAG, "file " + full_path_on_device + " is READY. START UPLOAD AWS id");
            } else {
                Log.e(LOG_TAG, "file " + full_path_on_device + " don't receive geoname, skip it ");
                String filename = pictureFile.getFilename();
                addInFailed(filename);

                uploadCycleFinished();

            }

        } else {
            Log.e(LOG_TAG, "file " + full_path_on_device + " NOT EXIST. Remove it from database");
            PictureFile deletedFile = daoHelper.getFileFromPath(full_path_on_device);
            if (deletedFile != null) {
                daoHelper.pictureFileDao.delete(deletedFile);
                daoHelper.onPictureFilesUpdated();
            }
            Intent intent = new Intent(BroadcastReceiverHelper.ACTION);
            intent.putExtra(BroadcastReceiverHelper.PARAM, BroadcastReceiverHelper.PARAM_DELETED);
            String name = fileFromPath.getName();
            intent.putExtra(BroadcastReceiverHelper.FILENAME, name);
            sendBroadcast(intent);

            uploadCycleFinished();


        }
    }

    private boolean isAddressExist(PictureFile pictureFile) {
        boolean exist;
        String geoName = pictureFile.getGeo_name();
        exist = geoName != null && !geoName.isEmpty();
        return exist;
    }

    private void renameFile(File oldFile, PictureFile pictureFile, String newGeoname) {

        pictureFile.setGeo_name(newGeoname);

        Double lat = pictureFile.getLat();
        Double lon = pictureFile.getLon();
        String geoname = newGeoname;
        Long time = pictureFile.getTime();


        String oldPath = pictureFile.getFull_path_on_device();
        int indexForSlash = oldPath.lastIndexOf("/");
        String parentPath = oldPath.substring(0, indexForSlash + 1);
        String timestamp = StorageHelper.formatTimeForFilename(time);
        String location = StorageHelper.formatLatLonGeoname(lat, lon, geoname);
        String username = daoHelper.getUserName();
        String newName = StorageHelper.formatFilename(username, location, timestamp);
        String newPath = parentPath + newName;


        pictureFile.setFilename(newName);
        pictureFile.setFull_path_on_device(newPath);

        File newFile = new File(newPath);
        oldFile.renameTo(newFile);

        Log.w(LOG_TAG, "Renamed " + oldPath + " to " + newPath);

        daoHelper.pictureFileDao.update(pictureFile);
        daoHelper.onPictureFilesUpdated();


    }

    private void initUpload(final PictureFile pictureFile, File localFile, String remoteName) {
        String absoluteLocalPath = localFile.getAbsolutePath();
        final long size = localFile.getTotalSpace();

        awsUploadHelper.upload(localFile, remoteName, new UploadStateListener(absoluteLocalPath) {
            @Override
            public void onCompleted(long countBytesTransfered) {
                onUploadCompleted(pictureFile);
                OnUploadStateChanged(pictureFile, ServiceInnerUploadState.COMPLETED, countBytesTransfered, size);
            }

            @Override
            public void onInit(long countBytesTransfered) {
                onUploadInit(pictureFile);
                OnUploadStateChanged(pictureFile, ServiceInnerUploadState.INIT, countBytesTransfered, size);
            }


            @Override
            public void onProgress(long countBytesTransfered) {
                onUploadInProgress(pictureFile);
                OnUploadStateChanged(pictureFile, ServiceInnerUploadState.IN_PROGRESS, countBytesTransfered, size);
            }

            @Override
            public void onFail(long countBytesTransfered) {
                onUploadFailed(pictureFile);
                OnUploadStateChanged(pictureFile, ServiceInnerUploadState.FAILED, countBytesTransfered, size);
            }
        });
    }

    private void OnUploadStateChanged(PictureFile pictureFile, ServiceInnerUploadState state, long countBytesTransfered, long size) {
        if (state == ServiceInnerUploadState.IN_PROGRESS) {
            Log.i(LOG_TAG, pictureFile.getFilename() + " " + state + " " + countBytesTransfered + "/" + size);
        } else {
            Log.w(LOG_TAG, pictureFile.getFilename() + " " + state + " " + countBytesTransfered + "/" + size);
        }

    }


    private void onUploadInit(PictureFile uploadingFile) {

    }

    private String retrieveGeoName(PictureFile pictureFile) throws IOException {
        Log.w(LOG_TAG, "start retrieve geoname for pictureFile " + pictureFile.getFilename());


        String address = null;

        Context context = getApplicationContext();
        LocationHelper locationHelper = LocationHelper.getInstatce(context);
        address = locationHelper.getAddressFromLocationSync(pictureFile.getLat(), pictureFile.getLon());
        Log.w(LOG_TAG, "finish retrieve geoname for pictureFile " + pictureFile.getFilename());
        return address;
    }

    private void onUploadFailed(PictureFile id) {
        setUpUploadingAgain(id);


        Intent intent = new Intent(BroadcastReceiverHelper.ACTION);
        intent.putExtra(BroadcastReceiverHelper.PARAM, BroadcastReceiverHelper.PARAM_UPLOAD_ERROR);
        String name = id.getFilename();
        intent.putExtra(BroadcastReceiverHelper.FILENAME, name);
        sendBroadcast(intent);


        String filename = id.getFilename();

        addInFailed(filename);

        uploadCycleFinished();

    }

    private void addInFailed(String filename) {
        Context applicationContext = getApplicationContext();
        InternetHelper internetHelper = InternetHelper.getInstance(applicationContext);
        DAOHelper instance = DAOHelper.getInstance(applicationContext);
        boolean wifiOnly = instance.getWifiOnlyState();
        if (internetHelper.canUpload(wifiOnly)) {
            synchronized (failedUploadsList) {
                if (!failedUploadsList.contains(filename)) {
                    failedUploadsList.add(filename);
                }
            }
        }
    }

    private void doThreadSleepAndDelay() {
        Thread.yield();
        try {
            Thread.sleep(DELAY_ON_FAIL_IN_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void onUploadInProgress(PictureFile uploadingFile) {

        if (uploadingFile != null) {
            Integer oldStatus = uploadingFile.getStatus();
            if (oldStatus != DAOHelper.FILE_STATUS_UPLOADING && oldStatus != DAOHelper.FILE_STATUS_UPLOADED) {
                Log.w(LOG_TAG, "IN PROGRESS " + uploadingFile.getFilename());
                uploadingFile.setStatus(DAOHelper.FILE_STATUS_UPLOADING);
                daoHelper.pictureFileDao.update(uploadingFile);
                daoHelper.onPictureFilesUpdated();
            }

        }
    }


    private void onUploadCompleted(PictureFile uploadedFile) {


        if (uploadedFile != null) {
            if (Constants.DEBUG) {
                ToastHelper.showToast(getApplicationContext(), "COMPLETED " + uploadedFile.getFilename());
            }
            Log.w(LOG_TAG, "COMPLETED " + uploadedFile.getFilename());
            uploadedFile.setStatus(DAOHelper.FILE_STATUS_UPLOADED);
            uploadedFile.setIsDeleted(false);
            daoHelper.pictureFileDao.update(uploadedFile);
            daoHelper.onPictureFilesUpdated();

        }

        uploadCycleFinished();

    }


    private void setUpUploadingAgain(PictureFile failedFile) {

        if (failedFile != null) {
            String filename = failedFile.getFilename();
            if (Constants.DEBUG) {
                ToastHelper.showToast(getApplicationContext(), "FAILED " + failedFile.getFilename());
            }
            Log.w(LOG_TAG, "FAILED " + filename);
            failedFile.setStatus(DAOHelper.FILE_STATUS_IN_QUEUE);
            daoHelper.pictureFileDao.update(failedFile);
            daoHelper.onPictureFilesUpdated();
        }
    }

}
