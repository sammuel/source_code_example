package com.photoapp.model.database;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;

import com.photoapp.model.data.StorageHelper;
import com.photoapp.model.events.Bus;
import com.photoapp.controller.events.PictureFilesUpdatedEvent;
import com.photoapp.controller.events.PictureTakenEvent;
import com.photoapp.controller.events.SignUpEvent;
import com.photoapp.model.database.DAO.DaoMaster;
import com.photoapp.model.database.DAO.DaoSession;
import com.photoapp.model.database.DAO.PictureFileDao;
import com.photoapp.model.database.DAO.UserDao;
import com.photoapp.model.debug.Log;
import com.photoapp.model.upload.UploadService;

import java.util.Calendar;
import java.util.List;

import de.greenrobot.dao.query.Query;

/**
 * Class-helper for work with database
 */
public class DAOHelper {

    private static final boolean WIFI_ONLY_ON = true;
    private static final boolean WIFI_ONLY_OFF = false;
    private static final String FLASH_OFF = Camera.Parameters.FLASH_MODE_OFF;
    private static final String FLASH_ON = Camera.Parameters.FLASH_MODE_ON;
    private static final String FLASH_AUTO = Camera.Parameters.FLASH_MODE_AUTO;


    private static final String DEFAULT_FLASH = FLASH_AUTO;
    private static final boolean DEFAULT_WIFI_ONLY = WIFI_ONLY_OFF;


    public static final int FILE_STATUS_IN_QUEUE = 0;
    public static final int FILE_STATUS_UPLOADING = 1;
    public static final int FILE_STATUS_UPLOADED = 2;
    private static final String LOG_TAG = DAOHelper.class.getCanonicalName();
    public static final String PHOTOAPP_DB = "photoapp-db";
    private static final Object DESC_ORDER_BY = " DESC ";

    private static DAOHelper instance;
    private Context mContext;

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private UserDao userDao;
    public PictureFileDao pictureFileDao;

    public User currentUser;

    public static DAOHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DAOHelper(context);
        }
        return instance;
    }

    private DAOHelper(Context context) {
        Bus.register(this);
        initDAO(context);
        mContext = context;
    }

    public void initDAO(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, PHOTOAPP_DB, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        userDao = daoSession.getUserDao();
        pictureFileDao = daoSession.getPictureFileDao();
    }

    public void onEvent(PictureTakenEvent event) {
        onPictureFilesUpdated();
    }

    public void onEvent(SignUpEvent event) {
        boolean wifiOnlyState = DEFAULT_WIFI_ONLY;
        String flashState = DEFAULT_FLASH;
        String userName = event.getUserName();
        User user = new User((long) 1, userName, wifiOnlyState, flashState);
        long id = userDao.insert(user);
        user.setId(id);
        currentUser = user;
    }

    public void addPictureData(String path, String fileName, int status, double lat, double lon, String geoName, long time, boolean isDeleted) {
        pictureFileDao.insert(new PictureFile(null, path, fileName, status, lat, lon, geoName, time, isDeleted));


        onPictureFilesUpdated();
        UploadService.startService(mContext);
    }

    public User retrieveUser() {
        User user = userDao.queryBuilder().limit(1).unique();
        currentUser = user;
        return user;
    }

    public String getUserName() {
        String name = null;
        if (currentUser != null) {
            name = currentUser.getName();
        }
        return name;
    }

    public void setFlashState(String state) {
        currentUser.setFlashState(state);
        userDao.update(currentUser);
    }

    public void setWifiOnlyState(final boolean state) {
        currentUser.setWifi_only_upload(state);
        userDao.update(currentUser);
    }

    public String getFlashState() {
        String flashState = DEFAULT_FLASH;
        if (currentUser != null) {
            flashState = currentUser.getFlashState();
        }

        return flashState;
    }

    public boolean getWifiOnlyState() {
        Boolean wifi_only_upload = DEFAULT_WIFI_ONLY;
        if (currentUser != null) {
            wifi_only_upload = currentUser.isWifi_only_upload();
        }

        return wifi_only_upload;
    }

    public void checkNotUploadedFiles() {
        List<PictureFile> files = pictureFileDao
                .queryBuilder()
                .where(PictureFileDao.Properties.Status.eq(FILE_STATUS_UPLOADING))
                .build()
                .list();

        if(files.size() > 0 ) {
            for (PictureFile pictureFile : files) {
                pictureFile.setStatus(FILE_STATUS_IN_QUEUE);
                pictureFileDao.update(pictureFile);
            }
            onPictureFilesUpdated();
        }
    }

    public PictureFile selectPictureFileForUploading() {
        PictureFile unique = pictureFileDao.queryBuilder()
                .where(PictureFileDao.Properties.Status.eq(FILE_STATUS_IN_QUEUE))
                .orderAsc(PictureFileDao.Properties.Time)
                .limit(1)
                .unique();

        return unique;
    }

    public List<PictureFile> selectPictureFilesForUploading() {
        List<PictureFile> list = pictureFileDao.queryBuilder()
                .where(PictureFileDao.Properties.Status.eq(FILE_STATUS_IN_QUEUE))
                .orderAsc(PictureFileDao.Properties.Time)

                .list();

        return list;
    }

    public PictureFile getFileFromPath(String path) {
        Query<PictureFile> query = pictureFileDao
                .queryBuilder()
                .where(PictureFileDao.Properties.Full_path_on_device.eq(path))
                .limit(1)
                .build();
        if (query.list().size() > 0) {
            return query.list().get(0);
        } else
            return null;
    }


    public PictureFile getLastTakenPicture() {

        PictureFile pf = pictureFileDao.queryBuilder().orderDesc(PictureFileDao.Properties.Time).limit(1).build().unique();
        if (pf != null) {
            Log.w(LOG_TAG, "Last taken image retrived = " + pf.getFilename());
        }

        return pf;
    }

    public void deleteUploadedFilesOnDevice() {
        List<PictureFile> uploadedFiles = pictureFileDao
                .queryBuilder()
                .where(PictureFileDao.Properties.Is_deleted.eq(false), PictureFileDao.Properties.Status.eq(FILE_STATUS_UPLOADED))
                .build()
                .list();

        if (uploadedFiles.size() > 0) {
            PictureFile lastTakenPicture = getLastTakenPicture();
            String lastTakenPictureFilename = lastTakenPicture.getFilename();
            for (PictureFile file : uploadedFiles) {

                String currentFilename = file.getFilename();
                boolean isLastTaken = currentFilename.equals(lastTakenPictureFilename);


                Log.w(LOG_TAG, "Try delete file " + currentFilename + " is Last = " + isLastTaken
                                + " status " + file.getStatus()
                                + "isdeleted " + file.getIsDeleted()
                );
                if (!isLastTaken) {
                    String full_path_on_device = file.getFull_path_on_device();
                    StorageHelper.deleteFileOnDevice(full_path_on_device);
                    file.setIsDeleted(true);
                    pictureFileDao.update(file);
                }

            }
        }
    }


    public void onPictureFilesUpdated() {
        Bus.post(new PictureFilesUpdatedEvent());
    }

    public int getTodayTaken() {
        long timeInMillis = calculateMillisecondForCurrentDayStart();
        int count;
        count = (int) pictureFileDao.queryBuilder().where(PictureFileDao.Properties.Time.ge(timeInMillis)).count();
        return count;
    }

    private long calculateMillisecondForCurrentDayStart() {
        Calendar calendar = Calendar.getInstance();


        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }


    public int getTodayUploaded() {
        long timeInMillis = calculateMillisecondForCurrentDayStart();
        int count;
        count = (int) pictureFileDao.queryBuilder().where(
                PictureFileDao.Properties.Time.ge(timeInMillis),
                PictureFileDao.Properties.Status.eq(FILE_STATUS_UPLOADED)
        ).count();
        return count;
    }

    public int getAllTimeTaken() {
        int count = (int) pictureFileDao.count();
        return count;
    }


    public int getAllTimeUploaded() {
        int count;

        count = (int) pictureFileDao.queryBuilder().where(PictureFileDao.Properties.Status.eq(FILE_STATUS_UPLOADED)).count();
        return count;
    }

    public Cursor createUploadListCursor() {

        Cursor c;

        String[] columns = {
                PictureFileDao.Properties.Id.columnName,
                PictureFileDao.Properties.Filename.columnName,
                PictureFileDao.Properties.Status.columnName};
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;

        String orderBy = PictureFileDao.Properties.Time.columnName + DESC_ORDER_BY;

        c = db.query(PictureFileDao.TABLENAME, columns, selection, selectionArgs, groupBy, having, orderBy);

        return c;
    }

    public boolean IsExistFilesForUploading() {
        boolean isExist;

        long count = pictureFileDao.queryBuilder().where(PictureFileDao.Properties.Status.notEq(FILE_STATUS_UPLOADED)).count();
        isExist = count > 0;

        return isExist;
    }
}
