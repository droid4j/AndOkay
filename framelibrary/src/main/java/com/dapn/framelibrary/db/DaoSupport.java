package com.dapn.framelibrary.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.dapn.framelibrary.db.query.QuerySupport;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/31
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class DaoSupport<T> implements IDaoSupport<T> {

    private SQLiteDatabase mSqLiteDatabase;
    private Class<T> mClazz;

    // 参与 AppCompatViewInflater.class
    private static final Object[] sPutMethodArgs = new Object[2];
    private static final Map<String, Method> sPutMethods = new ArrayMap();

    private QuerySupport<T> mQuerySupport;

    @Override
    public void init(SQLiteDatabase sqLiteDatabase, Class<T> clazz) {
        this.mSqLiteDatabase = sqLiteDatabase;
        this.mClazz = clazz;

        // 创建表
        /*
        * create table if not exists Person ( id integer primary key autoincrement, name text, age integer);
        * */

        StringBuilder sb = new StringBuilder();

        sb.append("create table if not exists ")
                .append(DaoUtils.getTableName(clazz))
                .append("(id integer primary key autoincrement, ");

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();

            if (field.isSynthetic() || "serialVersionUID".equals(name)) {
                continue;
            }
            String type = field.getType().getSimpleName(); // int String boolean
            // 类型需要进行转换 int --> integer, String --> text

            sb.append(name).append(DaoUtils.getColumnType(type)).append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), ") ");
        String createTableSql = sb.toString();
        Log.w("DaoSupport", createTableSql);
        mSqLiteDatabase.execSQL(createTableSql);
    }

    @Override
    public long insert(T obj) {
        /*ContentValues values = new ContentValues();
        values.put("age", person.age);
        values.put("name", person.name);
        mSqLiteDatabase.insert("person", null, values);*/

        ContentValues values = contentValuesByObj(obj);

        return mSqLiteDatabase.insert(DaoUtils.getTableName(mClazz), null, values);
    }

    @Override
    public void insert(List<T> list) {
        mSqLiteDatabase.beginTransaction();
        for (T t : list) {
            insert(t);
        }
        mSqLiteDatabase.setTransactionSuccessful();
        mSqLiteDatabase.endTransaction();
    }

    // obj 转成 ContentValues
    private ContentValues contentValuesByObj(T obj) {
        ContentValues values = new ContentValues();

        Field[] fields = mClazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String key = field.getName();

                if (field.isSynthetic() || "serialVersionUID".equals(key)) {
                    continue;
                }

                Object value = field.get(obj);

                sPutMethodArgs[0] = key;
                sPutMethodArgs[1] = value;

                // 缓存方法
                String fieldTypeName = field.getType().getName();
                Method putMethod = sPutMethods.get(fieldTypeName);
                if (putMethod == null) {
                    putMethod = ContentValues.class.getDeclaredMethod("put",
                            String.class, value.getClass());
                    sPutMethods.put(fieldTypeName, putMethod);
                }

                // 通过反射执行
//                values.put(key, value);
//                putMethod.invoke(values, key, value);
                putMethod.invoke(values, sPutMethodArgs);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sPutMethodArgs[0] = null;
                sPutMethodArgs[1] = null;
            }
        }
        return values;
    }

    // 查询
    @Override
    public QuerySupport<T> querySupport() {
        if (mQuerySupport == null) {
            mQuerySupport = new QuerySupport<T>(mSqLiteDatabase, mClazz);
        }
        return mQuerySupport;
    }

    // 修改
    @Override
    public int update(T obj, String whereClause, String... whereArgs) {
        ContentValues values = contentValuesByObj(obj);
        return mSqLiteDatabase.update(DaoUtils.getTableName(mClazz), values, whereClause, whereArgs);
    }

    // 删除
    @Override
    public int delete(String whereClause, String... whereArgs) {
        return mSqLiteDatabase.delete(DaoUtils.getTableName(mClazz), whereClause, whereArgs);
    }
}
