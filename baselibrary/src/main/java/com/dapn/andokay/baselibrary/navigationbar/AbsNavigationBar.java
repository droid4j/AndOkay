package com.dapn.andokay.baselibrary.navigationbar;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/23
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public abstract class AbsNavigationBar<P extends AbsNavigationBar.Builder.Params> implements INavigationBar {

    private P mParams;
    private View mNavigationView;

    public AbsNavigationBar(P params) {
        this.mParams = params;
        mViews = new SparseArray<>();
        createAndBindView();
    }

    private void createAndBindView() {

        if (mParams.parent == null) {
//            ViewGroup activityRoot = (ViewGroup) ((Activity) mParams.mContext).findViewById(android.R.id.content);
            ViewGroup activityRoot = (ViewGroup) ((Activity) mParams.mContext).getWindow().getDecorView();
            mParams.parent = (ViewGroup) activityRoot.getChildAt(0);
            Log.e("AbsNavigationBar", "parent: " + mParams.parent);
        }

        // 需要处理Activity的源码

        // 1. 创建view
        mNavigationView = LayoutInflater.from(mParams.mContext).inflate(bindLayoutId(), mParams.parent, false);

        // 2. 添加
        mParams.parent.addView(mNavigationView, 0);

        applyView();
    }

    public P getParams() {
        return mParams;
    }

    protected void setText(int viewId, String text) {

        if (!TextUtils.isEmpty(text)) {
            TextView tv = findById(viewId);
            if (tv != null) {
                tv.setText(text);
            }
        }
    }

    protected void setOnClickListener(int viewId, View.OnClickListener listener) {
        View view = findById(viewId);
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    private SparseArray<WeakReference<View>> mViews;
    private <V extends View> V findById(int viewId) {

        WeakReference<View> weakReference = mViews.get(viewId);
        View view = null;
        if (weakReference != null) {
            view = weakReference.get();
        }

        if (view == null) {
            view = mNavigationView.findViewById(viewId);
            if (view != null) {
                mViews.put(viewId, new WeakReference<View>(view));
            }
        }
        return (V) view;
    }

    public static abstract class Builder {

        public Builder(Context context, ViewGroup parent) {

        }

        public abstract AbsNavigationBar build();


        public static class Params {

            public Context mContext;
            public ViewGroup parent;
            public Params(Context context, ViewGroup parent) {
                this.mContext = context;
                this.parent = parent;
            }
        }
    }
}
