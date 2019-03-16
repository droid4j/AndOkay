package com.dapn.andokay.baselibrary.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/16
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置布局layout
        setContentView();

        // 初始化头部
        initTitle();

        // 初始化界面
        initView();

        // 初始化数据
        initData();
    }

    protected abstract void initData();

    protected abstract void initView();

    protected abstract void initTitle();

    protected abstract void setContentView();

    protected void startActivity(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    protected <T extends View> T findById(@IdRes int viewId) {
        return (T) findViewById(viewId);
    }
}
