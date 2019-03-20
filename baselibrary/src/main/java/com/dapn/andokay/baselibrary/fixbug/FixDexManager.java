package com.dapn.andokay.baselibrary.fixbug;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.BaseDexClassLoader;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/20
 *     desc   :
 *     version: 1.0
 * </pre>
 */
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
     * @param path
     */
    public void fixDex(String path) throws Exception {


        // 2. 获取下载好的补丁 dexElement
        // 2.1 移到系统能够访问的 dex目录下
        File srcFile = new File(path);
        if (!srcFile.exists()) {
            throw new FileNotFoundException(path);
        }

        File destFile = new File(mDexDir, srcFile.getName());
        if (destFile.exists()) { // 已经加载的，直接返回
            return;
        }


        copyFile(srcFile, destFile);

        // 2.2 ClassLoader读取fixDex路径
        // 为什么要加入集合？   一启动可能就要修复 BaseApplication
        List<File> fixDexFiles = new ArrayList<>();
        fixDexFiles.add(destFile);

        fixDexFiles(fixDexFiles);
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

    /**
     * 从 classLoader中获取DexElement
     * @param classLoader
     * @return
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

    /**
     *
     * copy file
     *
     * from andifx/FileUtil.java
     *
     * @param src
     *            source file
     * @param dest
     *            target file
     * @throws IOException
     */
    public static void copyFile(File src, File dest) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            if (!dest.exists()) {
                dest.createNewFile();
            }
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dest).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; k++) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }

        return result;
    }

    /**
     * 加载全部的修复包
     */
    public void loadFixDex() throws Exception {
        File[] dexFiles = mDexDir.listFiles();

        List<File> fixDexFiles = new ArrayList<>();

        for (File fixDexFile : dexFiles) {
            if (fixDexFile.getName().endsWith(".dex")) {
                fixDexFiles.add(fixDexFile);
            }
        }

        fixDexFiles(fixDexFiles);
    }

    /**
     * 修复dex
     * @param fixDexFiles
     */
    private void fixDexFiles(List<File> fixDexFiles) throws Exception {
        // 1. 先获取已经运行的 DexElement
        ClassLoader applicationClassLoader = mContext.getClassLoader();

        Object applicationDexElements = getDexElementsByClassLoader(applicationClassLoader);


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
                    dexFile.getAbsolutePath(),
                    optimizedDirectory,
                    null,
                    applicationClassLoader
            );
            Object fixDexElements = getDexElementsByClassLoader(fixDexClassLoader);

            // 3. 把补丁 dexElement 插到 已经运行的DexElement 之前

            // applicationClassLoader 数组 合并 fixDexElements 数组
            // 3.1 合并完成
            applicationDexElements = combineArray(fixDexElements, applicationDexElements);

            // 3.2 把合并的数组注入到原来的类中 applicationClassLoader
            injectDexElements(applicationClassLoader, applicationDexElements);
        }
    }
}
