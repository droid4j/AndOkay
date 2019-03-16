# Android热修复

1､收集崩溃信息上传服务器

把崩溃信息保存到内存卡中，等上线之后，将内存卡中的崩溃信息上传到服务器。

1.1 创建异常捕获类

```java
public class ExceptionCrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "ExceptionCrashHandler";

    private Thread.UncaughtExceptionHandler defaultExceptionHandler;		// 1
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

    private Context context;																			// 2

    public void init(Context context) {
        this.context = context;
        Thread.currentThread().setUncaughtExceptionHandler(this);	// 3

        defaultExceptionHandler = Thread.currentThread().getDefaultUncaughtExceptionHandler();			// 4
    }

    @Override
    public void uncaughtException(Thread t, Throwable ex) {				// 5
        Log.e(TAG, "报异常了");
        // 写入本地文件 ex 当前版本

        // 1. 崩溃的详情信息

        // 2. 应用信息 包名 版本号

        // 3. 手机信息

        // 4. 保存当前文件，等应用再次启动再上传（上传问题不在这里处理

        defaultExceptionHandler.uncaughtException(t, ex);					// 6
    }
}
```

1. 获取系统默认的
2. 获取应用信息
3. 设置全局的异常类为本类
4. 获取默认的异常捕获类
5. 全局异常，都会走这里！！！
6. 让系统默认处理，我们只取异常，不要打断系统的处理

1.2 在Application中使用

```java
public class BaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ExceptionCrashHandler.getInstance().init(this);		// 1
    }
}
```

1. 设置全局异常捕获类

1.3 模拟异常

```java
@OnClick(R.id.test_tv)
public void testClick(View view) {
    mTestTv.setText("@OnClick");
    int i = 2 / 0;
    Toast.makeText(this, "onClick", Toast.LENGTH_SHORT).show();
}
```

1.4 查看logcat

    03-16 23:47:27.051 31537-31537/com.dapn.andokay E/ExceptionCrashHandler: 报异常了
    
        --------- beginning of crash
    03-16 23:47:27.052 31537-31537/com.dapn.andokay E/AndroidRuntime: FATAL EXCEPTION: main

不仅可以看到我们打印的log，还看到了异常抛出的异常


