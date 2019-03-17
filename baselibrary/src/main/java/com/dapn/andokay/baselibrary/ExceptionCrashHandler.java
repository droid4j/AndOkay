package com.dapn.andokay.baselibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/16
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class ExceptionCrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "ExceptionCrashHandler";

    // 获取系统默认的
    private Thread.UncaughtExceptionHandler defaultExceptionHandler;
    private static ExceptionCrashHandler sInstance;

    private ExceptionCrashHandler() {

    }

    public static ExceptionCrashHandler getInstance() {
        if (sInstance == null) {
            synchronized (ExceptionCrashHandler.class) {
                if (sInstance == null) {
                    sInstance = new ExceptionCrashHandler();
                }
            }
        }
        return sInstance;
    }

    // 获取应用信息
    private Context context;

    public void init(Context context) {
        this.context = context;
        // 设置全局的异常类为本类
        Thread.currentThread().setUncaughtExceptionHandler(this);


        defaultExceptionHandler = Thread.currentThread().getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread t, Throwable ex) {
        // 全局异常
        Log.e(TAG, "报异常了");

        // 写入本地文件 ex 当前版本

        // 1 获取信息
        // 1.1 崩溃的详情信息
        // 1.2 手机信息
        // 1.3 版本号
        // 2 保存当前文件，等应用再次启动再上传（上传问题不在这里处理）
        String crashFileName = saveInfo2SDCard(ex);

        Log.e(TAG, "异常信息已存入文件：" + crashFileName);

        // 3. 缓存崩溃日志文件
        cacheCrashFile(crashFileName);

        // 让系统默认处理
        defaultExceptionHandler.uncaughtException(t, ex);
    }

    /**
     * 缓存崩溃日志文件
     * @param fileName
     */
    private void cacheCrashFile(String fileName) {
        SharedPreferences sp = context.getSharedPreferences("crash", Context.MODE_PRIVATE);
        sp.edit().putString("CRASH_FILE_NAME", fileName).commit();
    }

    /**
     * 获取崩溃文件
     * @return
     */
    public File getCrashFile() {
        String cacheFileName = context.getSharedPreferences("crash", Context.MODE_PRIVATE)
                .getString("CRASH_FILE_NAME", "");

        return new File(cacheFileName);
    }

    public void checkAndUploadCrash() {
        File crashFile = ExceptionCrashHandler.getInstance().getCrashFile();
        if (crashFile.exists()) {
            Log.e(TAG, "检测到有缓存的异常文件!!");
            // 上传至服务器
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(crashFile));
                char[] buffer = new char[1024];
                int len = 0;
                StringBuilder result = new StringBuilder();
                while ((len = inputStreamReader.read(buffer)) != -1) {
                    result.append(new String(buffer, 0, len));
                }

                Log.e("MainActivity", result.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存获得的 软件信息，设备信息和错误信息到sd卡上
     * @param ex
     * @return
     */
    private String saveInfo2SDCard(Throwable ex) {
        String fileName = null;
        StringBuffer buffer = new StringBuffer();

        // 1. 拼手机信息
        for (Map.Entry<String, String> entry : obtainsSimpleInfo(context).entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            buffer.append(key).append("=").append(value).append("\n");
        }

        // 2. 拼异常信息
        buffer.append(obtainExceptionInfo(ex));

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(context.getFilesDir() + File.separator + "crash" + File.separator);

            // 先删除之前的异常信息
            if (dir.exists()) {
                deleteDir(dir);
            }

            // 再重新创建文件
            if (!dir.exists()) {
                dir.mkdir();
            }

            try {
                fileName = dir.toString() + File.separator + getAssignTime("yyyy_MM_dd_HH_mm") + ".txt";
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(buffer.toString().getBytes());
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }

    private String getAssignTime(String pattern) {
        DateFormat df = new SimpleDateFormat(pattern);
        long timeMillis = System.currentTimeMillis();
        return df.format(timeMillis);
    }

    private HashMap<String, String> obtainsSimpleInfo(Context context) {
        HashMap<String, String> map = new HashMap<>();
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        map.put("versionName", packageInfo.versionName);
        map.put("versionCode", packageInfo.versionCode+"");
        map.put("MODEL", Build.MODEL);
        map.put("SDK_INT", Build.VERSION.SDK_INT+"");
        map.put("PRODUCT", Build.PRODUCT);
        map.put("MOBILE_INFO", getMobileInfo());

        return map;
    }

    /**
     * 获取手机信息
     * @return
     */
    private String getMobileInfo() {
        StringBuffer buffer = new StringBuffer();
        try {
            // 利用反射获取 Build 类的所有属性
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                String value = field.get(null).toString();
                if (TextUtils.isEmpty(value) || "unknown".equals(value)) { // 忽略 value为空或 unknown的值
                    continue;
                }
                buffer.append(name).append("=").append(value);
                buffer.append("\n");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    /**
     * 获取系统未捕获的异常信息
     * @param throwable
     * @return
     */
    private String obtainExceptionInfo(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.close();
        return stringWriter.toString();
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            // 递归删除目录中的子目录
            File[] children = dir.listFiles();
            for (File child : children) {
                child.delete(); // 存在即删除
            }
        }
        return true;
    }
}
