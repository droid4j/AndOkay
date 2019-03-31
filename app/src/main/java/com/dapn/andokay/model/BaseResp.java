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
public class BaseResp<T> {

    public T data;
    public int errorCode;
    public String errorMsg;


    // {"data":{"curPage":1,"datas":[],"offset":0,"over":true,"pageCount":0,"size":20,"total":0},"errorCode":0,"errorMsg":""}
}
