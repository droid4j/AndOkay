package com.dapn.andokay.logprint.sdk;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/04/23
 *     desc   :
 *     version: 1.0
 * </pre>
 */
@Keep
public final class AutoLogPrint {

    private final String TAG = this.getClass().getSimpleName();

    public static final String SDK_VERSION = "1.0.0";
    private static AutoLogPrint INSTANCE;
    private static final Object mLock = new Object();
    private static JSONObject sAppInfo;
    private static JSONObject sUserInfo;
    private static JSONObject dateInfo;

    private Context ctx;

     @Keep
    @SuppressWarnings("UnusedReturnValue")
    public static AutoLogPrint init(@NonNull Application application) {
        synchronized (mLock) {
            if (null == INSTANCE) {
                INSTANCE = new AutoLogPrint(application);
            }
            return INSTANCE;
        }
    }

    public static void start(final Context ctx) {


    }

        @Keep
    public static AutoLogPrint getInstance() {
        return INSTANCE;
    }

    private AutoLogPrint(Application application) {
        sAppInfo = AutoLogPrintInternal.getAppInfo(application.getApplicationContext());
        dateInfo = new JSONObject();
        sUserInfo = new JSONObject();
        ctx = application.getApplicationContext();
    }

    /**
     * 用户信息
     *
     * @param system_id
     * @param system_type 1-老师
     */
    public void addUserInfo(@NonNull String system_id, @NonNull int system_type) {
        try {
            sUserInfo.put("system_id", system_id);
            sUserInfo.put("system_type", system_type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Track 事件
     *
     * @param eventName  String 事件名称
     * @param properties JSONObject 事件属性
     */
    @Keep
    public void track(@NonNull final String eventName, @Nullable JSONObject properties) {
        try {
            // 公共参数
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("event", eventName);
            jsonObject.put("app_info", sAppInfo); // app_info(必选)
            jsonObject.put("user_info", sUserInfo); // user_info(必选)
            jsonObject.put("log_type", 3); // 日志类型：3 教师pad


//            JSONObject sendProperties = new JSONObject(mDeviceInfo);

            if (properties != null) {
                AutoLogPrintInternal.mergeJSONObject(properties, jsonObject);
            }

//            jsonObject.put("properties", sendProperties);

            dateInfo.remove("date_time");
            dateInfo.put("date_time", System.currentTimeMillis());
            jsonObject.put("date_info", dateInfo); // date_info(必选)

            Log.d(TAG, jsonObject.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
