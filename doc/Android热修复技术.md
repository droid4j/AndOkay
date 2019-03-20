# Android热修复技术

## 一、Activity启动流程源码分析

当我们从Activity A 中，startActivity() 到 Activity B时，首先，将 classes.dex 解压至 app的缓存目录下。然后去找 ActivityB.class，然后通过类的反射，findClass 找到这个类，然后调用 class 的 newInstance()

```java
@OnClick(R.id.test_tv)
public void testClick(View view) {
    startActivity(new Intent(this, TestActivity.class));
}
```

点击按钮，调用 `startActivity()` 方法，启动 TestActivity。

我们看下 startActivity() 的执行流程

```java
@Override
public void startActivity(Intent intent) {										// 1
    this.startActivity(intent, null);
}

@Override
public void startActivity(Intent intent, @Nullable Bundle options) {		// 2
    if (options != null) {
        startActivityForResult(intent, -1, options);
    } else {
        // Note we want to go through this call for compatibility with
        // applications that may have overridden the method.
        startActivityForResult(intent, -1);
    }
}

public void startActivityForResult(@RequiresPermission Intent intent, 
                                   int requestCode) {		// 3
    startActivityForResult(intent, requestCode, null);
}

public void startActivityForResult(@RequiresPermission Intent intent, int requestCode,
        @Nullable Bundle options) {				// 4
    if (mParent == null) {
        options = transferSpringboardActivityOptions(options);
        Instrumentation.ActivityResult ar =
            mInstrumentation.execStartActivity(				// 5
                this, mMainThread.getApplicationThread(), mToken, this,
                intent, requestCode, options);
				// 省略一大段代码
        cancelInputsAndStartExitTransition(options);
        // TODO Consider clearing/flushing other event sources and events for child windows.
    } else {
        // 省略一大段代码
    }
}
```

我们可以了解到，通过一系列的 startActivity 方法，最后在 startActivityForResult() 方法中，调用了`mInstrumentation.execStartActivity();` 

```java
// sdk/sources/android-24/android/app/Instrumentation.java
public ActivityResult execStartActivity(
        Context who, IBinder contextThread, IBinder token, Activity target,
        Intent intent, int requestCode, Bundle options) {
    IApplicationThread whoThread = (IApplicationThread) contextThread;
    Uri referrer = target != null ? target.onProvideReferrer() : null;
    // 省略一大段代码
    try {
        intent.migrateExtraStreamToClipData();
        intent.prepareToLeaveProcess(who);
        int result = ActivityManagerNative.getDefault()
            .startActivity(whoThread, who.getBasePackageName(), intent,
                    intent.resolveTypeIfNeeded(who.getContentResolver()),
                    token, target != null ? target.mEmbeddedID : null,
                    requestCode, 0, null, options);
        checkStartActivityResult(result, intent);
    } catch (RemoteException e) {
        throw new RuntimeException("Failure from system", e);
    }
    return null;
}
```

从中，我们找到了 `ActivityManagerNative.getDefault().startActivity()` 方法：

```java
// sdk/sources/android-24/android/app/ActivityManagerNative.java
static public IActivityManager getDefault() {
    return gDefault.get();
}
    
private static final Singleton<IActivityManager> gDefault = new Singleton<IActivityManager>() {
    protected IActivityManager create() {
        IBinder b = ServiceManager.getService("activity");
        if (false) {
            Log.v("ActivityManager", "default service binder = " + b);
        }
        IActivityManager am = asInterface(b);
        if (false) {
            Log.v("ActivityManager", "default service = " + am);
        }
        return am;
    }
};
```

到这里，我们发现，其实， `startActivity()` 本身就是一个跨进程访问。



然而，不同的sdk版本，这里略有不同，我们再来看下sdk-28的实现：

```java
// sdk/sources/android-28/android/app/Instrumentation.java
public ActivityResult execStartActivity(
        Context who, IBinder contextThread, IBinder token, Activity target,
        Intent intent, int requestCode, Bundle options) {
    IApplicationThread whoThread = (IApplicationThread) contextThread;
    Uri referrer = target != null ? target.onProvideReferrer() : null;
    // 省略一大段代码
    try {
        intent.migrateExtraStreamToClipData();
        intent.prepareToLeaveProcess(who);
        int result = ActivityManager.getService()
            .startActivity(whoThread, who.getBasePackageName(), intent,
                    intent.resolveTypeIfNeeded(who.getContentResolver()),
                    token, target != null ? target.mEmbeddedID : null,
                    requestCode, 0, null, options);
        checkStartActivityResult(result, intent);
    } catch (RemoteException e) {
        throw new RuntimeException("Failure from system", e);
    }
    return null;
}
```



这里，不再是 调用 `ActivityManagerNative` 的 getService() 方法了，而是 `ActivityManager` 这个类的 getService() 方法。

```java
public static IActivityManager getService() {
    return IActivityManagerSingleton.get();
}

private static final Singleton<IActivityManager> IActivityManagerSingleton =
        new Singleton<IActivityManager>() {
            @Override
            protected IActivityManager create() {
                final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                final IActivityManager am = IActivityManager.Stub.asInterface(b);
                return am;
            }
        };
```

到最后，我们会发现，其实都是通过 `ServiceManager.getService(Context.ACTIVITY_SERVICE);` 获取的 IActivityManager 对象。



然后，回到，ActivityThread中，

```java
// sdk/sources/android-28/android/app/ActivityThread.java
private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
    ActivityInfo aInfo = r.activityInfo;
  	// 省略一大段代码
    ContextImpl appContext = createBaseContextForActivity(r);
    Activity activity = null;
    try {
        java.lang.ClassLoader cl = appContext.getClassLoader();
        activity = mInstrumentation.newActivity(
                cl, component.getClassName(), r.intent);
        StrictMode.incrementExpectedActivityCount(activity.getClass());
        r.intent.setExtrasClassLoader(cl);
        r.intent.prepareToEnterProcess();
        if (r.state != null) {
            r.state.setClassLoader(cl);
        }
    } catch (Exception e) {
       //  省略一大段代码
    }

		// 省略一大段代码
    return activity;
}
```

在这里，调用 `mInstrumentation.newActivity(cl, component.getClassName(), r.intent);` 创建了新的Activity

```java
public Activity newActivity(ClassLoader cl, String className,
        Intent intent)
        throws InstantiationException, IllegalAccessException,
        ClassNotFoundException {
    return (Activity)cl.loadClass(className).newInstance();
}
```

到这里，我们发现，是使用反射，通过classLoader 的 loadClass，找到 class，利用反射实例化对象(Activity)。

接下来，我们再来看下，这个ClassLoader从哪里来的。

在 ActivityThread的performLaunchActivity这个方法，执行 newActivity 之前，通过 appContext.getClassLoader() 创建 ClassLoader :

```java
// sdk/sources/android-28/android/app/ContextImpl.java
@Override
public ClassLoader getClassLoader() {
    return mClassLoader != null ? mClassLoader : (mPackageInfo != null ? mPackageInfo.getClassLoader() : ClassLoader.getSystemClassLoader());
}
```

第一次创建，肯定都会空，最后走到 ClassLoader.getSystemClassLoader()

```java
// sdk/sources/android-28/java/lang/ClassLoader.java
@CallerSensitive
public static ClassLoader getSystemClassLoader() {
    return SystemClassLoader.loader;
}

static private class SystemClassLoader {
    public static ClassLoader loader = ClassLoader.createSystemClassLoader();
}

private static ClassLoader createSystemClassLoader() {
    String classPath = System.getProperty("java.class.path", ".");
    String librarySearchPath = System.getProperty("java.library.path", "");
		// 省略注释

    // TODO Make this a java.net.URLClassLoader once we have those?
    return new PathClassLoader(classPath, librarySearchPath, BootClassLoader.getInstance());
}
```

一路下来，我们发现了 PathClassLoader

```java
public class PathClassLoader extends BaseDexClassLoader {
  	// 省略。。。

    public PathClassLoader(String dexPath, String librarySearchPath, ClassLoader parent) {
        super((String)null, (File)null, (String)null, (ClassLoader)null);
        throw new RuntimeException("Stub!");
    }
}
```

到这里，我们知道，Android系统是通知PathClassLoader去加载类的，那么loadClass在哪里呢？

没错，是父类，ClassLoader中的方法。

```java
public Class<?> loadClass(String name) throws ClassNotFoundException {
    return loadClass(name, false);
}

protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      // First, check if the class has already been loaded
      Class<?> c = findLoadedClass(name);
      if (c == null) {
          try {
              if (parent != null) {
                  c = parent.loadClass(name, false);
              } else {
                  c = findBootstrapClassOrNull(name);
              }
          } catch (ClassNotFoundException e) {
              // ClassNotFoundException thrown if class not found
              // from the non-null parent class loader
          }

          if (c == null) {
              // If still not found, then invoke findClass in order
              // to find the class.
              c = findClass(name);
          }
      }
      return c;
}
```



## 二、类的加载机制源码分析

BasePathClassLoader -> DexPathList -> findClass()

[DexPathList.java](http://androidxref.com/5.0.0_r2/xref/libcore/dalvik/src/main/java/dalvik/system/DexPathList.java)

```java
public Class findClass(String name, List<Throwable> suppressed) {
    for (Element element : dexElements) {
        DexFile dex = element.dexFile;

        if (dex != null) {
            Class clazz = dex.loadClassBinaryName(name, definingContext, suppressed);
            if (clazz != null) {
                return clazz;
            }
        }
    }
    if (dexElementsSuppressedExceptions != null) {
        suppressed.addAll(Arrays.asList(dexElementsSuppressedExceptions));
    }
    return null;
}
```

通过遍历 dexElements，找到类后，立即返回。

![findClass](/Users/per4j/Documents/Learn/学习2019/findclass.png)

我们知道了，系统是如何查找一个类的，那么，该怎么利用系统的这套机制，实现我们自己的热修复呢？

在上面，我们知道，在DexpathList的findClass()中遍历dexElements，找到后返回的。那么，我们是不是可以在dexElements上动下手脚，让系统先加载我们自己的类，通过这种方式，实现bug修复呢？

## 三、反射获取ClassLoader中的dexElements

```java
public class FixDexManager {

    private Context mContext;

    private File mDexDir;

    public FixDexManager(Context context) {
        this.mContext = context;
        // 获取应用可以访问的odex目录
        mDexDir = context.getDir("odex", Context.MODE_PRIVATE);
    }

    /**
     * 修复dex包
     */
    public void fixDex(String path) throws Exception {
        // 1. 先获取已经运行的 DexElement
        ClassLoader applicationClassLoader = mContext.getClassLoader();

        Object dexElements = getDexElementsByClassLoader(applicationClassLoader);

        // 2. 获取下载好的补丁 dexElement
        // 2.1 移到系统能够访问的 dex目录下
        File srcFile = new File(path);
        if (!srcFile.exists()) {
            throw new FileNotFoundException(path);
        }

        File targetFile = new File(mDexDir, srcFile.getName());
        if (targetFile.exists()) { // 已经加载的，直接返回
            return;
        }
        // 2.2 ClassLoader读取fixDex路径

        // 3. 把补丁 dexElement 插到 已经运行的DexElement 之前
    }

    /**
     * 从 classLoader中获取DexElement
     */
    private Object getDexElementsByClassLoader(ClassLoader classLoader) throws Exception {
        // 1. 先获取 pathList
        Field pathListField = classLoader.getClass().getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object pathList = pathListField.get(classLoader);

        // 2. 获取pathList中的dexElements
        Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        return dexElementsField.get(pathList);
    }
}
```

## 四、合并修复dex补丁

上一层，FixDexManager.fixDex() 如果dex文件已经加载了，就返回，下面实现加载部分。

```java
/**
 * 修复dex包
 */
public void fixDex(String path) throws Exception {
    // 省略。。。
    File targetFile = new File(mDexDir, srcFile.getName());
    if (targetFile.exists()) { // 已经加载的，直接返回
        return;
    }
  
    copyFile(srcFile, destFile);

    // 2.2 ClassLoader读取fixDex路径
    // 为什么要加入集合？   一启动可能就要修复 BaseApplication
    List<File> fixDexFiles = new ArrayList<>();
    fixDexFiles.add(destFile);

    File optimizedDirectory = new File(mDexDir, "odex");
    if (!optimizedDirectory.exists()) {
        optimizedDirectory.mkdirs();
    }

    // 修复
    for (File dexFile : fixDexFiles) {
        // dexPath  dex路径
        // optimizedDirectory 解压的路径
        // libraryPath .so文件位置
        // parent 父classloader
        ClassLoader fixDexClassLoader = new BaseDexClassLoader(
                dexFile.getAbsolutePath(), optimizedDirectory,
          			null, applicationClassLoader);
        Object fixDexElements = getDexElementsByClassLoader(fixDexClassLoader);

        // 3. 把补丁 dexElement 插到 已经运行的DexElement 之前
        // applicationClassLoader 数组 合并 fixDexElements 数组
        // 3.1 合并完成
        applicationDexElements = combineArray(fixDexElements, applicationDexElements);

        // 3.2 把合并的数组注入到原来的类中 applicationClassLoader
        injectDexElements(applicationClassLoader, applicationDexElements);
    }
}

/**
 * 把 dexElements 注入到 classLoader 中
 * @param classLoader
 * @param dexElements
 */
private void injectDexElements(ClassLoader classLoader, Object dexElements) throws Exception {
    // 1. 先获取 pathList
    Field pathListField = classLoader.getClass().getDeclaredField("pathList");
    pathListField.setAccessible(true);
    Object pathList = pathListField.get(classLoader);

    // 2. 获取pathList中的dexElements
    Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
    dexElementsField.setAccessible(true);
    dexElementsField.set(pathList, dexElements);
}
```

## 五、打补丁修复测试

在设备上安装有bug的apk，然后，修复bug，编译出新包，解压新包，拿到 dex文件，放到指定目录，启动应用，查看bug是否被修复。

##六、注意事项

可以学习 AndFix，打差分包的方法，减少 dex包的大小。