package com.dapn.andokay.baselibrary.ioc;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Field;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/14
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class ViewUtils {

    public static void inject(Activity activity) {
        inject(new ViewFinder(activity), activity);
    }

    public static void inject(View view) {
        inject(new ViewFinder(view), view);
    }

    public static void inject(View view, Object object) {
        inject(new ViewFinder(view), object);
    }

    // 兼容上面三个方法   object --> 反射需要执行的类
    private static void inject(ViewFinder finder, Object object) {

        injectField(finder, object);
        injectEvent(finder, object);
    }

    /**
     * 注入属性
     */
    private static void injectField(ViewFinder finder, Object object) {
        // 1. 获取类里面所有属性
        Class<?> aClass = object.getClass();
        // 获取所有属性(公有、私有)
        Field[] fields = aClass.getDeclaredFields();

        // 2. 获取ViewById里面value值
        for (Field field : fields) {
            ViewById viewById = field.getAnnotation(ViewById.class);
            if (viewById != null) {
                // 获取注解里面的id
                int value = viewById.value();

                // 3. findViewById
                View view = finder.findViewById(value);
                if (view != null) {
                    // 能够找到所有修饰符
                    field.setAccessible(true);

                    // 4. 动态注入找到的View
                    try {
                        field.set(object, view);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }




    }

    /**
     * 注入方法
     * @param finder
     * @param object
     */
    private static void injectEvent(ViewFinder finder, Object object) {

    }
}
