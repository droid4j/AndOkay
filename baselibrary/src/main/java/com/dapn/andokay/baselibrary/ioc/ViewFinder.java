package com.dapn.andokay.baselibrary.ioc;

import android.app.Activity;
import android.view.View; /**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/14
 *     desc   : View.findViewById辅助类
 *     version: 1.0
 * </pre>
 */
public class ViewFinder {

    private Activity activity;
    private View view;

    ViewFinder(Activity activity) {
        this.activity = activity;
    }

    ViewFinder(View view) {
        this.view = view;
    }

    public View findViewById(int viewId) {
        return activity != null ? activity.findViewById(viewId) : view.findViewById(viewId);
    }
}
