package com.dapn.andokay.baselibrary;

import android.content.Context;
import android.util.Log;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/16
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class ExceptionCrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "ExceptionCrashHandler";

    // 获取系统默认的
    private Thread.UncaughtExceptionHandler defaultExceptionHandler;
    private static ExceptionCrashHandler sInstance;

    private ExceptionCrashHandler() {

    }

    public static ExceptionCrashHandler getInstance() {
        if (sInstance == null) {
            synchronized (ExceptionCrashHandler.class) {
                if (sInstance == null) {
                    sInstance = new ExceptionCrashHandler();
                }
            }
        }
        return sInstance;
    }

    // 获取应用信息
    private Context context;

    public void init(Context context) {
        this.context = context;
        // 设置全局的异常类为本类
        Thread.currentThread().setUncaughtExceptionHandler(this);


        defaultExceptionHandler = Thread.currentThread().getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread t, Throwable ex) {
        // 全局异常
        Log.e(TAG, "报异常了");

        // 写入本地文件 ex 当前版本

        // 1. 崩溃的详情信息

        // 2. 应用信息 包名 版本号

        // 3. 手机信息

        // 4. 保存当前文件，等应用再次启动再上传（上传问题不在这里处理）

        // 让系统默认处理
        defaultExceptionHandler.uncaughtException(t, ex);
    }
}
