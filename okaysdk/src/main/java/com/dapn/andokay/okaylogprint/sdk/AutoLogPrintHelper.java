package com.dapn.andokay.okaylogprint.sdk;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/04/23
 *     desc   : 在 Plugin 中使用本类实现自动埋点
 *     version: 1.0
 * </pre>
 */
public class AutoLogPrintHelper {

    public static void addItemEvent(@NonNull View view) {
        try {
            // 事件独享参数
            JSONObject jsonObject = new JSONObject();
            JSONObject clickInfo = new JSONObject();
            clickInfo.put("click_id", AutoLogPrintInternal.getViewId(view));
            clickInfo.put("click_pos", -1);
            clickInfo.put("desc", AutoLogPrintInternal.getElementContent(view));
            jsonObject.put("click_info", clickInfo); // click_info

            JSONObject screenInfo = new JSONObject();
            Activity activity = AutoLogPrintInternal.getActivityFromView(view);
            if (activity != null) {
                screenInfo.put("screen_cur", activity.getClass().getCanonicalName());
            } else {
                screenInfo.put("screen_cur", "UNKNOWN");
            }
            screenInfo.put("screen_path", "");
            screenInfo.put("screen_mode", 1);

            jsonObject.put("screen_info", screenInfo); // screen_info（必选）
            AutoLogPrint.getInstance().track("$viewItemClick", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void addEvent(@NonNull View view) {
        try {
            // 事件独享参数
            JSONObject jsonObject = new JSONObject();
            JSONObject clickInfo = new JSONObject();
            clickInfo.put("click_id", AutoLogPrintInternal.getViewId(view));
            clickInfo.put("click_pos", -1);
            clickInfo.put("desc", AutoLogPrintInternal.getElementContent(view));
            jsonObject.put("click_info", clickInfo); // click_info

            JSONObject screenInfo = new JSONObject();
            Activity activity = AutoLogPrintInternal.getActivityFromView(view);
            if (activity != null) {
                screenInfo.put("screen_cur", activity.getClass().getCanonicalName());
            } else {
                screenInfo.put("screen_cur", "UNKNOWN");
            }
            screenInfo.put("screen_path", "");
            screenInfo.put("screen_mode", 1);

            jsonObject.put("screen_info", screenInfo); // screen_info（必选）
            AutoLogPrint.getInstance().track("$viewClick", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
