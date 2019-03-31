package com.dapn.framelibrary.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/31
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public interface IDaoSupport<T> {

    void init(SQLiteDatabase sqLiteDatabase, Class<T> clazz);

    long insert(T t);

    void insert(List<T> list);

    List<T> query();

    int delete(String whereClause, String[] whereArgs);

    int update(T obj, String whereClause, String... whereArgs);
}
