package com.dapn.andokay;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.alipay.euler.andfix.patch.PatchManager;
import com.dapn.andokay.baselibrary.http.HttpUtils;
import com.dapn.framelibrary.http.OkHttpEngine;

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

    public static PatchManager mPatchManager;

    @Override
    public void onCreate() {
        super.onCreate();

        HttpUtils.init(new OkHttpEngine());
        // 设置全局异常捕获类
//        ExceptionCrashHandler.getInstance().init(this);
//
//        // 初始化阿里的热修复
//        mPatchManager = new PatchManager(this);
//        // 初始化当前应用版本
//        mPatchManager.init(getVersionCode());
//        // 加载之前的差分包
//        mPatchManager.loadPatch();

//        try {
//            FixDexManager fixDexManager = new FixDexManager(this);
//            // 加载所有修复的dex包
//            fixDexManager.loadFixDex();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    private String getVersionCode() {
        PackageManager packageManager = getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo.versionName;
    }
}
