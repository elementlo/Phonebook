package com.weikuo.elemenzhang.phonebookwk.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.weikuo.elemenzhang.phonebookwk.bean.RawContact;

import java.sql.SQLException;

/**
 * Created by elemenzhang on 2017/6/28.
 */

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String TABLE_NAME = "sqlite-contact.db";

    private Dao<RawContact, Integer> userDao;

    private DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase database,
                         ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, RawContact.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database,
                          ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, RawContact.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onDelete() {
        try {
            TableUtils.dropTable(getConnectionSource(), RawContact.class, true);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void reCreateTable(){
        try {
            TableUtils.createTable(getConnectionSource(),RawContact.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static DatabaseHelper instance;

    /**
     * 单例获取该Helper
     *
     * @param context
     * @return
     */
    public static synchronized DatabaseHelper getHelper(Context context) {
        context = context.getApplicationContext();
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null)
                    instance = new DatabaseHelper(context);
            }
        }

        return instance;
    }

    public Dao<RawContact, Integer> getUserDao() throws SQLException {
        if (userDao == null) {
            userDao = getDao(RawContact.class);
        }
        return userDao;
    }

    /**
     * 释放资源
     */
    @Override
    public void close() {
        super.close();
        userDao = null;
    }
}
