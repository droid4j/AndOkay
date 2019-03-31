package com.dapn.andokay.baselibrary.http;

import java.util.Map;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/31
 *     desc   : 引擎的规范
 *     version: 1.0
 * </pre>
 */
public interface IHttpEngine {

    // get 请求
    void get(String url, Map<String, Object> params, EngineCallback callback);

    // post 请求
    void post(String url, Map<String, Object> params, EngineCallback callback);

    // 下载文件

    // 上传文件

    // https 添加证书
}
