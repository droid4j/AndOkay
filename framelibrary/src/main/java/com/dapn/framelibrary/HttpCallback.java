package com.dapn.framelibrary;

import android.content.Context;

import com.dapn.andokay.baselibrary.http.EngineCallback;
import com.dapn.andokay.baselibrary.http.HttpUtils;
import com.google.gson.Gson;

import java.util.Map;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/31
 *     desc   : 处理与业务相关的逻辑
 *     version: 1.0
 * </pre>
 */
public abstract class HttpCallback<T> implements EngineCallback {

    @Override
    public void onPreExecute(Context context, Map<String, Object> params) {

        params.put("k", "java");

        onPreExecute();
    }

    public void onPreExecute() {

    }

    @Override
    public void onSuccess(String result) {
        Gson gson = new Gson();
        T obj = (T) gson.fromJson(result, HttpUtils.analysisClazzInfo(this));

        onSuccess(obj);
    }

    // 返回可直接操作的对象
    public abstract void onSuccess(T result);
}
