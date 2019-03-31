package com.dapn.framelibrary;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.dapn.andokay.baselibrary.navigationbar.AbsNavigationBar;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/23
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class DefaultNavigationBar extends AbsNavigationBar<DefaultNavigationBar.Builder.DefaultParams> {

    public DefaultNavigationBar(Builder.DefaultParams params) {
        super(params);
    }

    @Override
    public int bindLayoutId() {
        return R.layout.layout_navigation;
    }

    @Override
    public void applyView() {

        setText(R.id.left_tv, getParams().mLeftTitle);
        setText(R.id.title_tv, getParams().mTitle);
        setOnClickListener(R.id.left_tv, getParams().mLeftOnClickListener);
        setOnClickListener(R.id.right_tv, getParams().mRightOnClickListener);

    }

    public static class Builder extends AbsNavigationBar.Builder {

        DefaultParams P;

        public Builder(Context context, ViewGroup parent) {
            super(context, parent);
            P = new DefaultParams(context, parent);
        }

        public Builder(Context context) {
            super(context, null);
            P = new DefaultParams(context, null);
        }

        @Override
        public DefaultNavigationBar build() {
            DefaultNavigationBar navigationBar = new DefaultNavigationBar(P);
            return navigationBar;
        }

        // 1. 设置所有效果

        public Builder setLeftTitle(String title) {
            P.mLeftTitle = title;
            return this;
        }

        public Builder setTitle(String title) {
            P.mTitle = title;
            return this;
        }

        public Builder setRightTitle(String title) {
            P.mRightTitle = title;
            return this;
        }

        public Builder setRightIcon(int icon) {
            P.resRightId = icon;
            return this;
        }

        public Builder setLeftOnClickListener(View.OnClickListener listener) {
            P.mLeftOnClickListener = listener;
            return this;
        }

        public Builder setRightOnClikListener(View.OnClickListener listener) {
            P.mRightOnClickListener = listener;
            return this;
        }

        public static class DefaultParams extends AbsNavigationBar.Builder.Params {

            public String mLeftTitle;
            public String mTitle;
            public String mRightTitle;
            public int resRightId;
            public View.OnClickListener mRightOnClickListener;
            public View.OnClickListener mLeftOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity) mContext).finish();
                }
            };

            // 2. 放所有效果
            DefaultParams(Context context, ViewGroup parent) {
                super(context, parent);
            }
        }
    }
}
