package com.dapn.framelibrary.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.Field;

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
    public int insert(T o) {
        /*ContentValues values = new ContentValues();
        values.put("age", person.age);
        values.put("name", person.name);
        mSqLiteDatabase.insert("person", null, values);*/

        return 0;
    }

    // 查询

    // 修改

    // 删除

}
