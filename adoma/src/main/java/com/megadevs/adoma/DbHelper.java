package com.megadevs.adoma;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DbHelper {
    private DaoSession daoSession;
    private DaoMaster daoMaster;

    public static String getDatabaseName() {
        return "adoma.db";
    }

    @Inject
    public DbHelper(Context context) {
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(context, getDatabaseName(), null);
        final SQLiteDatabase db = devOpenHelper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public DaoMaster getDaoMaster() {
        return daoMaster;
    }
}
