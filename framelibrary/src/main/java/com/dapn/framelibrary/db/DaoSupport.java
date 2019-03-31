package com.dapn.framelibrary.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.ArrayMap;
import android.util.Log;

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
    public List<T> query() {

        Cursor cursor = mSqLiteDatabase.query(DaoUtils.getTableName(mClazz), null, null, null, null, null, null, null);

        return cursorToLit(cursor);
    }

    private List<T> cursorToLit(Cursor cursor) {

        List<T> list = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    T newInstance = mClazz.newInstance(); // 这里调用的是无参构造函数
                    Field[] fields = mClazz.getDeclaredFields();
                    for (Field field : fields) {
                        // 遍历属性
                        field.setAccessible(true);
                        String name = field.getName();
                        // 获取角标
                        int index = cursor.getColumnIndex(name);
                        if (index == -1) {
                            continue;
                        }
                        // 通过反射，获取游标的方法
                        Method cursorMethod = cursorMethod(field.getType());
                        if (cursorMethod != null) {
                            Object value = cursorMethod.invoke(cursor, index);
                            if (value == null) {
                                continue;
                            }

                            // 处理一些特殊的部分
                            if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                                if ("0".equals(String.valueOf(value))) {
                                    value = false;
                                } else if ("1".equals(String.valueOf(value))) {
                                    value = true;
                                }
                            } else if (field.getType() == char.class || field.getType() == Character.class) {
                                value = ((String) value).charAt(0);
                            } else if (field.getType() == Date.class) {
                                long date = (long) value;
                                if (date <= 0) {
                                    value = null;
                                } else {
                                    value = new Date(date);
                                }
                            }
                            field.set(newInstance, value);
                        }
                    }
                    list.add(newInstance);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private Method cursorMethod(Class<?> type) throws NoSuchMethodException {
        String methodName = getColumnMethodName(type);
        Method method = Cursor.class.getMethod(methodName, int.class);
        return method;
    }

    private String getColumnMethodName(Class<?> fieldType) {
        String typeName;
        if (fieldType.isPrimitive()) {
            typeName = DaoUtils.capitalize(fieldType.getName());
        } else {
            typeName = fieldType.getSimpleName();
        }
        String methodName = "get" + typeName;
        if ("getBoolean".equals(methodName)) {
            methodName = "getInt";
        } else if ("getChar".equals(methodName) || "getCharacter".equals(methodName)) {
            methodName = "getString";
        } else if ("getDate".equals(methodName)) {
            methodName = "getLong";
        } else if ("getInteger".equals(methodName)) {
            methodName = "getInt";
        }
        return methodName;
    }


    // 修改
    @Override
    public int update(T obj, String whereClause, String... whereArgs) {
        ContentValues values = contentValuesByObj(obj);
        return mSqLiteDatabase.update(DaoUtils.getTableName(mClazz), values, whereClause, whereArgs);
    }

    // 删除
    @Override
    public int delete(String whereClause, String[] whereArgs) {
        return mSqLiteDatabase.delete(DaoUtils.getTableName(mClazz), whereClause, whereArgs);
    }
}
