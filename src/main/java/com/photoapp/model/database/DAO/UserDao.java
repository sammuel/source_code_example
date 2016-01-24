package com.photoapp.model.database.DAO;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.photoapp.model.database.User;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

/**
 * Entity mapped to table "USER".
 */
public class UserDao extends AbstractDao<User, Long> {
    public static final String TABLENAME = "USER";
    private static final int PROPERTY_ID = 0;
    private static final int PROPERTY_NAME = 1;
    private static final int PROPERTY_WIFI_ONLY = 2;
    private static final int PROPERTY_FLASH_STATE = 3;

    private static final long WIFI_ONLY_TRUE = 1;
    private static final long WIFI_ONLY_FALSE = 0;

    public static class Properties {
        public final static Property Id = new Property(PROPERTY_ID, Long.class, "id", true, "_id");
        public final static Property Name = new Property(PROPERTY_NAME, String.class, "name", false, "NAME");
        public final static Property Wifi_only_upload = new Property(PROPERTY_WIFI_ONLY, Boolean.class, "wifi_only_upload", false, "WIFI_ONLY_UPLOAD");
        public final static Property Flash_state = new Property(PROPERTY_FLASH_STATE, String.class, "flash_state", false, "FLASH_STATE");
    }

    public UserDao(DaoConfig config) {
        super(config);
    }

    public UserDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS" : "";
        db.execSQL("CREATE TABLE " + constraint + "\"USER\"(" +
                "\"_id\" INTEGER PRIMARY KEY ," +
                "\"NAME\" TEXT NOT NULL," +
                "\"WIFI_ONLY_UPLOAD\" INTEGER NOT NULL," +
                "\"FLASH_STATE\" TEXT NOT NULL);");
    }

    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"USER\"";
        db.execSQL(sql);
    }

    @Override
    protected void bindValues(SQLiteStatement stmt, User entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(PROPERTY_ID + 1, id);
        }
        stmt.bindString(PROPERTY_NAME + 1, entity.getName());

        if (entity.isWifi_only_upload()) {
            stmt.bindLong(PROPERTY_WIFI_ONLY + 1, WIFI_ONLY_TRUE); //SQLite does not have a separate Boolean storage class.
            // Instead, Boolean values are stored as integers 0 (false) and 1 (true).
        } else
            stmt.bindLong(PROPERTY_WIFI_ONLY + 1, WIFI_ONLY_FALSE);

        stmt.bindString(PROPERTY_FLASH_STATE + 1, entity.getFlashState());
    }


    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + PROPERTY_ID) ? null : cursor.getLong(offset + PROPERTY_ID);
    }


    @Override
    public User readEntity(Cursor cursor, int offset) {

        Long id = cursor.isNull(offset + PROPERTY_ID) ? null : cursor.getLong(offset + 0);
        String name = cursor.isNull(offset + PROPERTY_NAME) ? null : cursor.getString(offset + 1);
        Boolean wifi_only;
        if (cursor.isNull(offset + PROPERTY_WIFI_ONLY)) {
            wifi_only = null;
        } else if (cursor.getInt(offset + PROPERTY_WIFI_ONLY) == 0) {
            wifi_only = false;
        } else
            wifi_only = true;
        String flashState = cursor.isNull(offset + PROPERTY_FLASH_STATE) ? null : cursor.getString(offset + PROPERTY_FLASH_STATE);

        User entity = new User(id, name, wifi_only, flashState);
        return entity;
    }


    @Override
    public void readEntity(Cursor cursor, User entity, int offset) {
        entity.setId(cursor.isNull(offset + PROPERTY_ID) ? null : cursor.getLong(offset + PROPERTY_ID));
        entity.setName(cursor.getString(offset + PROPERTY_NAME));
        Boolean wifi_only;
        if (cursor.isNull(offset + PROPERTY_WIFI_ONLY))
            wifi_only = null;
        else if (cursor.getLong(offset + PROPERTY_WIFI_ONLY) == WIFI_ONLY_FALSE)
            wifi_only = false;
        else
            wifi_only = true;
        entity.setWifi_only_upload(wifi_only);
        entity.setFlashState(cursor.isNull(offset + PROPERTY_FLASH_STATE) ? null : cursor.getString(offset + PROPERTY_FLASH_STATE));

    }


    @Override
    protected Long updateKeyAfterInsert(User entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }

    @Override
    public Long getKey(User entity) {
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
