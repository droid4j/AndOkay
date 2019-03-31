package com.dapn.andokay.baselibrary.http;

import android.content.Context;

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
public interface EngineCallback {

    // 执行前的回调方法
    void onPreExecute(Context context, Map<String, Object> params);

    void onError(Exception e);

    void onSuccess(String result);

    // 默认的
    public final EngineCallback DEFAULT_CALL_BACK = new EngineCallback() {

        @Override
        public void onPreExecute(Context context, Map<String, Object> params) {

        }

        @Override
        public void onError(Exception e) {

        }

        @Override
        public void onSuccess(String result) {

        }
    };
}
