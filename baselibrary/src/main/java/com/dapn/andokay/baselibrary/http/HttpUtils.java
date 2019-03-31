package com.dapn.andokay.baselibrary.http;

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
public class HttpUtils implements IHttpEngine {

    // 默认使用 OkHttpEngine 引擎
    private static IHttpEngine sHttpEngine = new OkHttpEngine();

    // 在application中初始化引擎
    public static void init(IHttpEngine httpEngine) {
        sHttpEngine = httpEngine;
    }

    public void exchangeEngine(IHttpEngine httpEngine) {
        sHttpEngine = httpEngine;
    }

    @Override
    public void get(String url, Map<String, Object> params, EngineCallback callback) {
        sHttpEngine.get(url, params, callback);
    }

    @Override
    public void post(String url, Map<String, Object> params, EngineCallback callback) {
        sHttpEngine.post(url, params, callback);
    }
}
