1.创建xxx.jks签名密钥

2.打包旧版apk(有bug的)

3.打包新版apk(修复bug的)

4.使用apkpatch工具生成差分包

5.测试

5.1 手机上安装旧apk，复现bug

5.2 将fix.patch包push到相关目录(如：adb push out/fix.apatch /storage/emulated/0/)

6 patch包的内部分析

6.1 修改patch包的后锥为zip包，使用解压缩工具，解压

```
classes.dex
META-INF
		-- CERT.RSA				// 加密文件
		-- CERT.SF
		-- MANIFEST.MF
		-- PATCH.MF				// 差分包中包含哪些类
```

PATCH.MF 文件：

```
Manifest-Version: 1.0
Patch-Name: new
Created-Time: 17 Mar 2019 12:58:21 GMT
From-File: new.apk
To-File: old.apk
Patch-Classes: com.dapn.andokay.MainActivity_CF			// 生成新的类
Created-By: 1.0 (ApkPatch)
```



6.2 反编译 classes.dex

下载 apktool-jdgui 工具

执行 dex2jar 命令：

./dex2jar-0.0.9.15/dex2jar.sh ~/Desktop/fix/classes.dex 

执行后，会在 .dex 对应的目录下，生成 classes_dex2jar.jar 包

使用  JD-GUI 打开 classes_dex2jar.jar

```java
public class MainActivity_CF extends Activity implements View.OnClickListener {
  
    @MethodReplace(clazz="com.dapn.andokay.MainActivity", method="onClick")
    public void onClick(View paramView) {
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append("onClick ");
        localStringBuilder.append(2);
        Toast.makeText(this, localStringBuilder.toString(), 0).show();
    }
}
```

注意，以上代码，有删减，只保留关键方法，详情的，请自行操作，查看。

