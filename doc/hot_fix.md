# Android热修复

##1､收集崩溃信息上传服务器

把崩溃信息保存到内存卡中，等上线之后，将内存卡中的崩溃信息上传到服务器。

###1.1 创建异常捕获类

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

###1.2 在Application中使用

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

###1.3 模拟异常

```java
@OnClick(R.id.test_tv)
public void testClick(View view) {
    mTestTv.setText("@OnClick");
    int i = 2 / 0;
    Toast.makeText(this, "onClick", Toast.LENGTH_SHORT).show();
}
```

###1.4 查看logcat

    03-16 23:47:27.051 31537-31537/com.dapn.andokay E/ExceptionCrashHandler: 报异常了
    
        --------- beginning of crash
    03-16 23:47:27.052 31537-31537/com.dapn.andokay E/AndroidRuntime: FATAL EXCEPTION: main

不仅可以看到我们打印的log，还看到了异常抛出的异常

### 1.5 完善uncaughtException方法

异常捕获我们需要做到以下几点：

1. 获取异常手机信息，方便定位
2. 获取异常信息，在保持不打扰用户的前提下，完成bug修复
3. 不能打断系统行为，也就是说，我们捕获异常，不能影响系统默认行为

具体实现，如下：

```java
@Override
public void uncaughtException(Thread t, Throwable ex) {
    // 全局异常
    Log.e(TAG, "报异常了");

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
```

具体实现见：[ExceptionCrashHandler.java](./baselibrary/src/main/java/com/dapn/andokay/baselibrary/ExceptionCrashHandler.java)

## 2､阿里开源的解决方案

AndFix下载地址： https://github.com/alibaba/AndFix

### 2.1 接入

#### 2.1.1 添加依赖

```groovy
dependencies {
	compile 'com.alipay.euler:andfix:0.5.0@aar'
}
```

#### 2.1.2 初始化

在自定义的Application中，添加如下代码：

```java
@Override
public void onCreate() {
    super.onCreate();
    // 设置全局异常捕获类
    ExceptionCrashHandler.getInstance().init(this);

    mPatchManager = new PatchManager(this);			// 1
    mPatchManager.init(getVersionCode()+"");		// 2
    mPatchManager.loadPatch();									// 3
}

//获取版本号
private int getVersionCode() {
    PackageManager packageManager = getPackageManager();
    PackageInfo packageInfo = null;
    try {
        packageInfo = packageManager.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
    } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
    }
    return packageInfo.versionCode;
}
```

1. 初始化阿里的热修复
2. 初始化当前应用版本
3. 加载之前的差分包

#### 2.1.3 检测并加载差分包

```java
@Override
protected void initData() {
    // 获取上次崩溃文件，上传到服务器
    ExceptionCrashHandler.getInstance().checkAndUploadCrash();

    // 每次启动的时候  去后台获取差分包  fix.apatch     然后修复本地bug
    // 测试，直接获取本地sdcard中的 fix.apatch
    File fixFile = new File(Environment.getExternalStorageDirectory(), "fix.apatch");
    if (fixFile.exists()) {
        try {
            BaseApp.mPatchManager.addPatch(fixFile.getAbsolutePath());
            Toast.makeText(this, "修复成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "修复失败", Toast.LENGTH_SHORT).show();
        }
    }
}
```

#### 2.2 AndFix使用

**注意：在打包之前，一定要添加权限，否则，你死都不知道怎么死的！〜**

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

1. 创建应用签名

2. 打包有bug的apk   —> old.apk

3. 打包修复bug后的apk —> new.apk

4. 使用apkpatch工具，打差分包

   ./apkpatch.sh -f new.apk -t old.apk -o out -k AndOkay.jks -p 123456 -a key0 -e 123456

执行完这个命令，如果成功生成差分包，终端会打印如下信息：

```java
add modified Method:V  testClick(Landroid/view/View;)  in Class:Lcom/dapn/andokay/MainActivity;
```

就是说，为哪个类的哪个方法生成新的方法

这个工具位于：./AndFix/tools/apkpatch-1.0.3

打包工具，各参数含义：

```java
-f 没有bug的新版包
-t 有bug的旧版包 
-o 生成的补丁文件所在文件夹 
-k 签名密钥 
-p 签名密钥密码 
-a 签名密钥别名 
-e 签名别名密码
```

执行完该命令后，会在 out 目录下生成差分包:

```java
new-6379e0ea392f8bb72d4a5e3480dee484.apatch
```

####2.3 AndFix工具包及反编译

[apkpatch工具使用及反编译](./apkpatch工具使用及反编译.md)

####2.4 apatch热修复原理
[apatch热修复原理](./apatch热修复原理.md)

## 3､自己的实现方式
[Android热修复技术](./Android热修复技术.md)

