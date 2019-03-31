package com.dapn.andokay.baselibrary.http;

import android.content.Context;
import android.util.ArrayMap;

import java.util.HashMap;
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
public class HttpUtils {

    // 链式调用
    private String mUrl;

    private int mType = GET_TYPE;
    private static final int GET_TYPE = 0x001;
    private static final int POST_TYPE = 0x002;

    private Map<String, Object> mParams;

    private Context mContext;

    private HttpUtils(Context context) {
        this.mContext = context;
        mParams = new HashMap<>();
    }

    public static HttpUtils with(Context context) {
        return new HttpUtils(context);
    }

    public HttpUtils url(String url) {
        mUrl = url;
        return this;
    }

    // 请求方式
    public HttpUtils post() {
        mType = POST_TYPE;
        return this;
    }

    public HttpUtils get() {
        mType = GET_TYPE;
        return this;
    }

    // 添加参数
    public HttpUtils addParam(String key, Object value) {
        mParams.put(key, value);
        return this;
    }

    public HttpUtils addParams(Map<String, Object> params) {
        mParams.putAll(params);
        return this;
    }

    // 添加回调
    public void execute(EngineCallback callback) {
        if (callback == null) {
            callback = EngineCallback.DEFAULT_CALL_BACK;
        }

        // 判断执行方法
        if (mType == GET_TYPE) {
            get(mUrl, mParams, callback);
        } else if (mType == POST_TYPE) {
            post(mUrl, mParams, callback);
        }
    }

    public void execute() {
        execute(null);
    }

    // 默认使用 OkHttpEngine 引擎
    private static IHttpEngine sHttpEngine = new OkHttpEngine();

    // 在application中初始化引擎
    public static void init(IHttpEngine httpEngine) {
        sHttpEngine = httpEngine;
    }

    public void exchangeEngine(IHttpEngine httpEngine) {
        sHttpEngine = httpEngine;
    }

    private void get(String url, Map<String, Object> params, EngineCallback callback) {
        sHttpEngine.get(url, params, callback);
    }

    private void post(String url, Map<String, Object> params, EngineCallback callback) {
        sHttpEngine.post(url, params, callback);
    }
}
