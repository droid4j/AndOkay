package com.dapn.andokay;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dapn.andokay.baselibrary.ioc.CheckNet;
import com.dapn.andokay.baselibrary.ioc.OnClick;
import com.dapn.andokay.baselibrary.ioc.ViewById;
import com.dapn.andokay.baselibrary.ioc.ViewUtils;
import com.dapn.framelibrary.BaseSkinActivity;

public class MainActivity extends BaseSkinActivity {

    @ViewById(R.id.test_tv)
    private TextView mTestTv;

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initTitle() {

    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_main);
        ViewUtils.inject(this);
    }

    @OnClick(R.id.test_tv)
    public void testClick(View view) {
        mTestTv.setText("@OnClick");
        int i = 2 / 0;
        Toast.makeText(this, "onClick", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.test2_tv)
    @CheckNet("网络异常")
    public void testClick2() {
        int i = 2 / 1;  // 这种异常，不会crash!!!
        Toast.makeText(this, "onClick2", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.test3_tv)
    @CheckNet("")
    public void testClick3() {
        int i = 2 / 2;  // 这种异常，不会crash!!!
        Toast.makeText(this, "onClick2", Toast.LENGTH_SHORT).show();
    }
}
