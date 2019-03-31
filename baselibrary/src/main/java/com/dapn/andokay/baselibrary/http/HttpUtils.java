package com.dapn.andokay.baselibrary.http;

import android.content.Context;
import android.util.ArrayMap;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
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
    private boolean isCache;

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

    public HttpUtils isCache(boolean isCache) {
        this.isCache = isCache;
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

        callback.onPreExecute(mContext, mParams);

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
    private static IHttpEngine sHttpEngine = null;

    // 在application中初始化引擎
    public static void init(IHttpEngine httpEngine) {
        sHttpEngine = httpEngine;
    }

    public void exchangeEngine(IHttpEngine httpEngine) {
        sHttpEngine = httpEngine;
    }

    private void get(String url, Map<String, Object> params, EngineCallback callback) {
        sHttpEngine.get(isCache, mContext, url, params, callback);
    }

    private void post(String url, Map<String, Object> params, EngineCallback callback) {
        sHttpEngine.post(isCache, mContext, url, params, callback);
    }

    // 拼接参数
    public static String joinParams(String url, Map<String, Object> params) {
        if (params == null || params.size() <= 0) {
            return url;
        }

        StringBuilder stringBuilder = new StringBuilder(url);
        if (!url.contains("?")) {
            stringBuilder.append("?");
        } else {
            if (!url.endsWith("?")) {
                stringBuilder.append("&");
            }
        }

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        return stringBuilder.toString();
    }

    // 解析一个类上面的class信息
    public static Type analysisClazzInfo(Object object) {
        Type superclass = object.getClass().getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return parameterized.getActualTypeArguments()[0];
    }

//    public static Type canonicalize(Type type) {
//        if (type instanceof Class) {
//            Class<?> c = (Class<?>) type;
//            return c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c;
//
//        } else if (type instanceof ParameterizedType) {
//            ParameterizedType p = (ParameterizedType) type;
//            return new ParameterizedTypeImpl(p.getOwnerType(),
//                    p.getRawType(), p.getActualTypeArguments());
//
//        } else if (type instanceof GenericArrayType) {
//            GenericArrayType g = (GenericArrayType) type;
//            return new GenericArrayTypeImpl(g.getGenericComponentType());
//
//        } else if (type instanceof WildcardType) {
//            WildcardType w = (WildcardType) type;
//            return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());
//
//        } else {
//            // type is either serializable as-is or unsupported
//            return type;
//        }
//    }
}
