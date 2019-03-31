package com.dapn.andokay.baselibrary.http;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/31
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public interface EngineCallback {

    void onError(Exception e);

    void onSuccess(String result);
}
