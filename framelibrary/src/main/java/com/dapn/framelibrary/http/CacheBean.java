package com.dapn.framelibrary.http;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/04/01
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class CacheBean {

    private String key;
    private String value;

    public CacheBean() {

    }

    public CacheBean(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
