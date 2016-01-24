package com.photoapp.model.database.DAO;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.photoapp.model.database.PictureFile;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

/**
 * DAO for table "PICTURE_FILES"
 */
public class PictureFileDao extends AbstractDao<PictureFile, Long> {

    public static final String TABLENAME = "PICTURE_FILES";
    private static final int PROPERTY_ID = 0;
    private static final int PROPERTY_FULL_PATH = 1;
    private static final int PROPERTY_FILENAME = 2;
    private static final int PROPERTY_STATUS = 3;
    private static final int PROPERTY_LAT = 4;
    private static final int PROPERTY_LON = 5;
    private static final int PROPERTY_GEONAME = 6;
    private static final int PROPERTY_TIME = 7;
    private static final int PROPERTY_IS_DELETED = 8;

    private static final long IS_DELETED_TRUE = 1;
    private static final long IS_DELETED_FALSE = 0;

    public static class Properties {
        public final static Property Id = new Property(PROPERTY_ID, Long.class, "id", true, "_id");
        public final static Property Full_path_on_device = new Property(PROPERTY_FULL_PATH, String.class, "full_path_on_device", false, "FULL_PATH_ON_DEVICE");
        public final static Property Filename = new Property(PROPERTY_FILENAME, String.class, "filename", false, "FILENAME");
        public final static Property Status = new Property(PROPERTY_STATUS, Integer.class, "status", false, "STATUS");
        public final static Property Lat = new Property(PROPERTY_LAT, Float.class, "lat", false, "LAT");
        public final static Property Lon = new Property(PROPERTY_LON, Float.class, "lon", false, "LON");
        public final static Property Geo_name = new Property(PROPERTY_GEONAME, String.class, "geo_name", false, "GEO_NAME");
        public final static Property Time = new Property(PROPERTY_TIME, Long.class, "time", false, "TIME");
        public final static Property Is_deleted = new Property(PROPERTY_IS_DELETED, Boolean.class, "is_deleted", false, "IS_DELETED");
    }

    ;

    public PictureFileDao(DaoConfig config) {
        super(config);
    }

    public PictureFileDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF EXISTS" : "";
        db.execSQL("CREATE TABLE " + constraint + "\"PICTURE_FILES\"(" +
                "\"_id\" INTEGER PRIMARY KEY," +
                "\"FULL_PATH_ON_DEVICE\" TEXT NOT NULL," +
                "\"FILENAME\" TEXT NOT NULL," +
                "\"STATUS\" INTEGER NOT NULL," +
                "\"LAT\" REAL NOT NULL," +
                "\"LON\" REAL NOT NULL," +
                "\"GEO_NAME\" TEXT," +
                "\"TIME\" INTEGER NOT NULL," +
                "\"IS_DELETED\" INTEGER NOT NULL);");
    }

    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"PICTURE_FILES\"";
        db.execSQL(sql);
    }

    @Override
    protected void bindValues(SQLiteStatement stmt, PictureFile entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(PROPERTY_ID + 1, id);
        }
        stmt.bindString(PROPERTY_FULL_PATH + 1, entity.getFull_path_on_device());
        stmt.bindString(PROPERTY_FILENAME + 1, entity.getFilename());
        stmt.bindLong(PROPERTY_STATUS + 1, (long) entity.getStatus());
        stmt.bindDouble(PROPERTY_LAT + 1, (double) entity.getLat());
        stmt.bindDouble(PROPERTY_LON + 1, (double) entity.getLon());
        String geo_name = entity.getGeo_name();
        if (geo_name != null) {
            stmt.bindString(PROPERTY_GEONAME + 1, geo_name);
        }
        stmt.bindLong(PROPERTY_TIME + 1, entity.getTime());
        if (entity.getIsDeleted()) {
            stmt.bindLong(PROPERTY_IS_DELETED + 1, IS_DELETED_TRUE);
        } else
            stmt.bindLong(PROPERTY_IS_DELETED + 1, IS_DELETED_FALSE);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + PROPERTY_ID) ? null : cursor.getLong(offset + PROPERTY_ID);
    }

    @Override
    public PictureFile readEntity(Cursor cursor, int offset) {
        Boolean isDel;
        if (cursor.isNull(offset + PROPERTY_IS_DELETED))
            isDel = null;
        else if (cursor.getLong(offset + PROPERTY_IS_DELETED) == IS_DELETED_FALSE)
            isDel = false;
        else
            isDel = true;

        PictureFile entity = new PictureFile(
                cursor.isNull(offset + PROPERTY_ID) ? null : cursor.getLong(offset + PROPERTY_ID),
                cursor.getString(offset + PROPERTY_FULL_PATH),
                cursor.getString(offset + PROPERTY_FILENAME),
                cursor.getInt(offset + PROPERTY_STATUS),
                cursor.getDouble(offset + PROPERTY_LAT),
                cursor.getDouble(offset + PROPERTY_LON),
                cursor.isNull(offset + PROPERTY_GEONAME) ? null : cursor.getString(offset + PROPERTY_GEONAME),
                cursor.getLong(offset + PROPERTY_TIME),
                isDel
        );
        return entity;
    }

    @Override
    public void readEntity(Cursor cursor, PictureFile entity, int offset) {
        entity.setId(cursor.isNull(offset + PROPERTY_ID) ? null : cursor.getLong(offset + PROPERTY_ID));
        entity.setFull_path_on_device(cursor.getString(offset + PROPERTY_FULL_PATH));
        entity.setFilename(cursor.getString(offset + PROPERTY_FILENAME));
        entity.setStatus(cursor.getInt(offset + PROPERTY_STATUS));
        entity.setLat(cursor.getDouble(offset + PROPERTY_LAT));
        entity.setLon(cursor.getDouble(offset + PROPERTY_LON));
        entity.setGeo_name(cursor.isNull(offset + PROPERTY_GEONAME) ? null : cursor.getString(offset + PROPERTY_GEONAME));
        entity.setTime(cursor.getLong(offset + PROPERTY_TIME));
        Boolean isDel;
        if (cursor.isNull(offset + PROPERTY_IS_DELETED))
            isDel = null;
        else if (cursor.getLong(offset + PROPERTY_IS_DELETED) == IS_DELETED_FALSE)
            isDel = false;
        else
            isDel = true;
        entity.setIsDeleted(isDel);
    }

    @Override
    protected Long updateKeyAfterInsert(PictureFile entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }

    @Override
    public Long getKey(PictureFile entity) {
        if (entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    protected boolean isEntityUpdateable() {
        return true;
    }


}
