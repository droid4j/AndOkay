apatch热修复原理

## 1 初始化 AndFix

```java
// 初始化阿里的热修复
mPatchManager = new PatchManager(this);
// 初始化当前应用版本
mPatchManager.init(getVersionCode());
// 加载之前的差分包
mPatchManager.loadPatch();
```

## 2 加载差分包

```java
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
}
```

### 2.1 查看addPatch实现

```java
// AndFix	PatchManager.java
	/**
	 * add patch at runtime
	 */
	public void addPatch(String path) throws IOException {
		File src = new File(path);
		File dest = new File(mPatchDir, src.getName());
		if(!src.exists()){
			throw new FileNotFoundException(path);
		}
		if (dest.exists()) {
			Log.d(TAG, "patch [" + path + "] has be loaded.");
			return;
		}
		FileUtil.copyFile(src, dest);// copy to patch's directory
		Patch patch = addPatch(dest);
		if (patch != null) {
			loadPatch(patch);						// 1
		}
	}
```

接着查看 `注释1` 的方法

```java
// AndFix	PatchManager.java
/**
 * load specific patch
 */
private void loadPatch(Patch patch) {
  Set<String> patchNames = patch.getPatchNames();
  ClassLoader cl;
  List<String> classes;
  for (String patchName : patchNames) {
    if (mLoaders.containsKey("*")) {
      cl = mContext.getClassLoader();
    } else {
      cl = mLoaders.get(patchName);
    }
    if (cl != null) {       // 1
      classes = patch.getClasses(patchName);
      mAndFixManager.fix(patch.getFile(), cl, classes);
    }
  }
}
```

`注释1` classLoader不为空，调用 `mAndFixManager.fix()` 去修复。

```java
// Andfix    AndFixManager.java
/**
 * fix
 * 
 * @param file atch file
 * @param classLoader classloader of class that will be fixed
 * @param classes lasses will be fixed
 */
public synchronized void fix(File file, ClassLoader classLoader,
    List<String> classes) {
  if (!mSupport) {					// 1
    return;
  }

  if (!mSecurityChecker.verifyApk(file)) {// security check fail			// 2
    return;
  }

  try {
    File optfile = new File(mOptDir, file.getName());
    boolean saveFingerprint = true;
    if (optfile.exists()) {
      // need to verify fingerprint when the optimize file exist,
      // prevent someone attack on jailbreak device with
      // Vulnerability-Parasyte.
      // btw:exaggerated android Vulnerability-Parasyte
      // http://secauo.com/Exaggerated-Android-Vulnerability-Parasyte.html
      if (mSecurityChecker.verifyOpt(optfile)) {
        saveFingerprint = false;
      } else if (!optfile.delete()) {
        return;
      }
    }

    final DexFile dexFile = DexFile.loadDex(file.getAbsolutePath(),
        optfile.getAbsolutePath(), Context.MODE_PRIVATE);				// 3

    if (saveFingerprint) {
      mSecurityChecker.saveOptSig(optfile);
    }

    ClassLoader patchClassLoader = new ClassLoader(classLoader) {		// 4
      @Override
      protected Class<?> findClass(String className)
          throws ClassNotFoundException {
        Class<?> clazz = dexFile.loadClass(className, this);
        if (clazz == null
            && className.startsWith("com.alipay.euler.andfix")) {
          return Class.forName(className);// annotation’s class
                          // not found
        }
        if (clazz == null) {
          throw new ClassNotFoundException(className);
        }
        return clazz;
      }
    };
    Enumeration<String> entrys = dexFile.entries();
    Class<?> clazz = null;
    while (entrys.hasMoreElements()) {					// 5
      String entry = entrys.nextElement();
      if (classes != null && !classes.contains(entry)) {
        continue;// skip, not need fix
      }
      clazz = dexFile.loadClass(entry, patchClassLoader);
      if (clazz != null) {
        fixClass(clazz, classLoader);					// 6
      }
    }
  } catch (IOException e) {
    Log.e(TAG, "pacth", e);
  }
}
```

1. support: 1.阿里的YunOS不支持；2.Andfix必须初始化成功；3.支持的版本：from android 2.3 to android 7.0
2. 校验apk是否合法
3. 加载dex文件
4. 获取patch的classLoader
5. 查找需要修复的class文件
6. 修复有bug的类

修复含有 `MethodReplace` 注解的方法

```java
/**
 * fix class
 * 
 * @param clazz class
 */
private void fixClass(Class<?> clazz, ClassLoader classLoader) {
  Method[] methods = clazz.getDeclaredMethods();
  MethodReplace methodReplace;
  String clz;
  String meth;
  for (Method method : methods) {
    methodReplace = method.getAnnotation(MethodReplace.class);
    if (methodReplace == null)
      continue;
    clz = methodReplace.clazz();
    meth = methodReplace.method();
    if (!isEmpty(clz) && !isEmpty(meth)) {
      replaceMethod(classLoader, clz, meth, method);			// 1
    }
  }
}
```

1. 找到要替换的方法了

```java
	/**
	 * replace method
	 * 
	 * @param classLoader classloader
	 * @param clz class
	 * @param meth name of target method 
	 * @param method source method
	 */
	private void replaceMethod(ClassLoader classLoader, String clz,
			String meth, Method method) {
		try {
			String key = clz + "@" + classLoader.toString();
			Class<?> clazz = mFixedClass.get(key);
			if (clazz == null) {// class not load
				Class<?> clzz = classLoader.loadClass(clz);
				// initialize target class
				clazz = AndFix.initTargetClass(clzz);
			}
			if (clazz != null) {// initialize class OK
				mFixedClass.put(key, clazz);
				Method src = clazz.getDeclaredMethod(meth,
						method.getParameterTypes());
				AndFix.addReplaceMethod(src, method);
			}
		} catch (Exception e) {
			Log.e(TAG, "replaceMethod", e);
		}
	}
```

如果要修复的class未加载，先加载，然后再调用AndFix.addReplaceMethod()方法去替换

```java
// Andfix  AndFix.java
/**
 * replace method's body
 * 
 * @param src source method
 * @param dest target method
 */
public static void addReplaceMethod(Method src, Method dest) {
  try {
    replaceMethod(src, dest);
    initFields(dest.getDeclaringClass());
  } catch (Throwable e) {
    Log.e(TAG, "addReplaceMethod", e);
  }
}

private static native void replaceMethod(Method dest, Method src);
```

到最后，我们发现，AndFix是通过jni的方式进行修复的。

```java
static {
  try {
    Runtime.getRuntime().loadLibrary("andfix");
  } catch (Throwable e) {
    Log.e(TAG, "loadLibrary", e);
  }
}
```

加载.so库

andfix.so的实现在： AndFix-master/jni/andfix.cpp

```cpp
static void replaceMethod(JNIEnv* env, jclass clazz, jobject src,
		jobject dest) {
	if (isArt) {
		art_replaceMethod(env, src, dest);
	} else {
		dalvik_replaceMethod(env, src, dest);
	}
}
```

isArt表示系统架构，4.4以后是art架构，4.4之前是dalvik架构

```java

extern void __attribute__ ((visibility ("hidden"))) dalvik_replaceMethod(
		JNIEnv* env, jobject src, jobject dest) {
	jobject clazz = env->CallObjectMethod(dest, jClassMethod);
	ClassObject* clz = (ClassObject*) dvmDecodeIndirectRef_fnPtr(
			dvmThreadSelf_fnPtr(), clazz);
	clz->status = CLASS_INITIALIZED;

	Method* meth = (Method*) env->FromReflectedMethod(src);
	Method* target = (Method*) env->FromReflectedMethod(dest);
	LOGD("dalvikMethod: %s", meth->name);

//	meth->clazz = target->clazz;
	meth->accessFlags |= ACC_PUBLIC;
	meth->methodIndex = target->methodIndex;
	meth->jniArgInfo = target->jniArgInfo;
	meth->registersSize = target->registersSize;
	meth->outsSize = target->outsSize;
	meth->insSize = target->insSize;

	meth->prototype = target->prototype;
	meth->insns = target->insns;
	meth->nativeFunc = target->nativeFunc;
}
```

总结：错误的方法指向修复方法的地址