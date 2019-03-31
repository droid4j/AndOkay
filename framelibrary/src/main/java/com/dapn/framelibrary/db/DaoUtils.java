package com.dapn.framelibrary.db;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/31
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class DaoUtils {

    private DaoUtils() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static String getTableName(Class<?> clazz) {
        return clazz.getSimpleName();
    }

    public static String getColumnType(String type) {
        String value = null;
        if (type.equals("String")) {
            value = " text";
        } else if (type.equals("int")) {
            value = " integer";
        } else if (type.equals("boolean")) {
            value = " boolean";
        } else if (type.equals("float")) {
            value = " float";
        } else if (type.equals("double")) {
            value = " double";
        } else if (type.equals("char")) {
            value = " varchar";
        } else if (type.equals("long")) {
            value = " long";
        }
        return value;
    }
}
