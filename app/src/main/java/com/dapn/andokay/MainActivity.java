package com.dapn.andokay;

import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dapn.andokay.baselibrary.ExceptionCrashHandler;
import com.dapn.andokay.baselibrary.dialog.AlertDialog;
import com.dapn.andokay.baselibrary.fixbug.FixDexManager;
import com.dapn.andokay.baselibrary.http.EngineCallback;
import com.dapn.andokay.baselibrary.http.HttpUtils;
import com.dapn.andokay.baselibrary.http.OkHttpEngine;
import com.dapn.andokay.baselibrary.ioc.CheckNet;
import com.dapn.andokay.baselibrary.ioc.OnClick;
import com.dapn.andokay.baselibrary.ioc.ViewById;
import com.dapn.andokay.baselibrary.ioc.ViewUtils;
import com.dapn.framelibrary.BaseSkinActivity;
import com.dapn.framelibrary.DefaultNavigationBar;

import java.io.File;
import java.io.IOException;

public class MainActivity extends BaseSkinActivity {

    @ViewById(R.id.test_tv)
    private TextView mTestTv;

    @Override
    protected void initData() {

        HttpUtils.with(this)
                .url("http://wanandroid.com/wxarticle/list/405/1/json?k=java")
                .get().execute(new EngineCallback() {
            @Override
            public void onError(Exception e) {
                Log.e("TAG", "onError: " + e.getMessage());
            }

            @Override
            public void onSuccess(String result) {
                Log.e("TAG", "onSuccess: " + result);
            }
        });
//        andfix();

//        fixDexBug();
    }

    private void fixDexBug() {
        File fixFile = new File(Environment.getExternalStorageDirectory() + "fix.dex");

        if (fixFile.exists()) {
            FixDexManager fixDexManager = new FixDexManager(this);
            try {
                fixDexManager.fixDex(fixFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initTitle() {
        DefaultNavigationBar navigationBar = new DefaultNavigationBar
                .Builder(this)
                .setLeftTitle("返回")
                .setTitle("详情页")
                .build();
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_main);
        ViewUtils.inject(this);
    }

    @OnClick(R.id.test_tv)
    public void testClick(View view) {
        startActivity(new Intent(this, TestActivity.class));
    }

    @OnClick(R.id.test2_tv)
//    @CheckNet("网络异常")
    public void testClick2() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setContentView(R.layout.dialog_example)
                .setText(R.id.textView, "Custom Dialog");
        AlertDialog dialog = builder.create();

        final EditText editText = dialog.getView(R.id.editText);
        builder.setOnClickListener(R.id.button, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        editText.getText().toString().trim(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    @OnClick(R.id.test3_tv)
    @CheckNet("")
    public void testClick3() {
        int i = 2 / 2;  // 这种异常，不会crash!!!
        Toast.makeText(this, "onClick2", Toast.LENGTH_SHORT).show();
    }

    private void andfix() {
                /*// 获取上次崩溃文件，上传到服务器
        ExceptionCrashHandler.getInstance().checkAndUploadCrash();

        // 每次启动的时候  去后台获取差分包  fix.apatch     然后修复本地bug
        // 测试，直接获取本地sdcard中的 fix.apatch
        File fixFile = new File(Environment.getExternalStorageDirectory(), "fix.apatch");
        Log.e("TAG", "fixFile" + fixFile.getAbsolutePath());
        if (fixFile.exists()) {
            // 修复bug
            try {
                BaseApp.mPatchManager.addPatch(fixFile.getAbsolutePath());
                Toast.makeText(this, "修复成功", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "修复失败", Toast.LENGTH_SHORT).show();
            }
        }*/
    }
}
