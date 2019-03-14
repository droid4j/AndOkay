package com.dapn.andokay;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dapn.andokay.baselibrary.ioc.OnClick;
import com.dapn.andokay.baselibrary.ioc.ViewById;
import com.dapn.andokay.baselibrary.ioc.ViewUtils;

public class MainActivity extends AppCompatActivity {

    @ViewById(R.id.test_tv)
    private TextView mTestTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewUtils.inject(this);
    }

    @OnClick(R.id.test_tv)
    public void testClick(View view) {
        mTestTv.setText("@OnClick");
        Toast.makeText(this, "onClick", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.test2_tv)
    public void testClick2() {
        int i = 2 / 0;  // 这种异常，不会crash!!!
        Toast.makeText(this, "onClick2", Toast.LENGTH_SHORT).show();
    }
}
