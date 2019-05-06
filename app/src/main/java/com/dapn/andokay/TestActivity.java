package com.dapn.andokay;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dapn.andokay.baselibrary.ExceptionCrashHandler;
import com.dapn.andokay.baselibrary.ioc.CheckNet;
import com.dapn.andokay.baselibrary.ioc.OnClick;
import com.dapn.andokay.baselibrary.ioc.ViewById;
import com.dapn.andokay.baselibrary.ioc.ViewUtils;
import com.dapn.framelibrary.BaseSkinActivity;

import java.io.File;
import java.io.IOException;

public class TestActivity extends BaseSkinActivity {

    @ViewById(R.id.test_tv)
    private TextView mTestTv;

    @Override
    protected void initData() {

        // 获取上次崩溃文件，上传到服务器
//        ExceptionCrashHandler.getInstance().checkAndUploadCrash();
//
//        // 每次启动的时候  去后台获取差分包  fix.apatch     然后修复本地bug
//        // 测试，直接获取本地sdcard中的 fix.apatch
//        File fixFile = new File(Environment.getExternalStorageDirectory(), "fix.apatch");
//        Log.e("TAG", "fixFile" + fixFile.getAbsolutePath());
//        if (fixFile.exists()) {
//            // 修复bug
//            try {
//                BaseApp.mPatchManager.addPatch(fixFile.getAbsolutePath());
//                Toast.makeText(this, "修复成功", Toast.LENGTH_SHORT).show();
//            } catch (IOException e) {
//                e.printStackTrace();
//                Toast.makeText(this, "修复失败", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    @Override
    protected void initView() {

        getResources().getColor(R.color.colorAccent);

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
