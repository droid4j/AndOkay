package com.dapn.andokay;

import android.app.Application;

import com.dapn.andokay.baselibrary.ExceptionCrashHandler;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/16
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class BaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 设置全局异常捕获类
        ExceptionCrashHandler.getInstance().init(this);
    }
}
