package com.dapn.framelibrary.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/31
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class DaoSupportFactory<T> {

    private static DaoSupportFactory mFactory;

    // 持有外部数据库引用
    SQLiteDatabase mSqliteDatabase;
    private DaoSupportFactory() {

        // 把数据库放到内存卡里   判断是否有存储卡，6.0动态判断权限
        File dbPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "andokay" + File.separator + "database");

        if (!dbPath.exists()) {
            dbPath.mkdirs();
        }
        File dbFile = new File(dbPath, "andokay.db");

        // 打开或创建一个数据库
        mSqliteDatabase = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
    }

    public static DaoSupportFactory getFactory() {
        if (mFactory == null) {
            synchronized (DaoSupportFactory.class) {
                if (mFactory == null) {
                    mFactory = new DaoSupportFactory();
                }
            }
        }
        return mFactory;
    }

    public IDaoSupport getDao(Class<T> clazz) {
        IDaoSupport<T> daoSupport = new DaoSupport<>();
        daoSupport.init(mSqliteDatabase, clazz);
        return daoSupport;
    }
}
