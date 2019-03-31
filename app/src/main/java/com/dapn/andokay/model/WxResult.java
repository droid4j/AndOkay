package com.dapn.andokay.model;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/31
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class WxResult<T> {

    public int curPage;
    public T[] datas;
    public int offset;
    public boolean over;
    public int pageCount;
    public int size;
    public int total;

    // {"curPage":1,"datas":[],"offset":0,"over":true,"pageCount":0,"size":20,"total":0}
}
