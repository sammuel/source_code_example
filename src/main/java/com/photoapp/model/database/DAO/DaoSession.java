package com.photoapp.model.database.DAO;

import android.database.sqlite.SQLiteDatabase;

import com.photoapp.model.database.PictureFile;
import com.photoapp.model.database.User;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;


/**
 *
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig userDaoConfig;
    private final DaoConfig pictureFileDaoConfig;

    private final UserDao userDao;
    private final PictureFileDao pictureFileDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        userDaoConfig = daoConfigMap.get(UserDao.class).clone();
        userDaoConfig.initIdentityScope(type);

        pictureFileDaoConfig = daoConfigMap.get(PictureFileDao.class).clone();
        pictureFileDaoConfig.initIdentityScope(type);

        userDao = new UserDao(userDaoConfig, this);
        pictureFileDao = new PictureFileDao(pictureFileDaoConfig, this);

        registerDao(User.class, userDao);
        registerDao(PictureFile.class, pictureFileDao);
    }

    public void clear() {
        userDaoConfig.getIdentityScope().clear();
        pictureFileDaoConfig.getIdentityScope().clear();
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public PictureFileDao getPictureFileDao() {
        return pictureFileDao;
    }

}