package com.dapn.andokay.baselibrary.ioc;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
        // 1. 获取类里面所有方法
        Class<?> aClass = object.getClass();
        Method[] methods = aClass.getDeclaredMethods();

        // 2. 获取OnClick里面的value值
        for (Method method : methods) {
            OnClick onClick = method.getAnnotation(OnClick.class);
            if (onClick != null) {
                int[] value = onClick.value();
                for (int viewId : value) {
                    // 3. findViewById 找到 View
                    View view = finder.findViewById(viewId);

                    // 扩展功能
                    CheckNet checkNet = method.getAnnotation(CheckNet.class);
                    boolean isCheckNet = checkNet != null;
                    String toastMsg = "亲，您的网络不太给力 ^ v ^";
                    if (isCheckNet && !TextUtils.isEmpty(checkNet.value())) {
                        toastMsg = checkNet.value();
                    }

                    if (view != null) {
                        // 4. view.setOnClickListener
                        view.setOnClickListener(new DeclaredOnClickListener(object, method, isCheckNet, toastMsg));
                    }
                }
            }
        }
    }

    private static class DeclaredOnClickListener implements View.OnClickListener {

        private Object object;
        private Method method;
        private boolean isCheckNet;
        private String toastMsg;

        DeclaredOnClickListener(Object object, Method method, boolean isCheckNet, String toastMsg) {
            this.object = object;
            this.method = method;
            this.isCheckNet = isCheckNet;
            this.toastMsg = toastMsg;
        }

        @Override
        public void onClick(View v) {

            // 检测网络
            if (isCheckNet) {
                if (!NetUtil.networkAvaible(v.getContext())) {
                    Toast.makeText(v.getContext(), toastMsg, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // 私有、公有都可以访问
            method.setAccessible(true);
            try {
                // 5. 反射执行方法
                method.invoke(object, v);
            } catch (Exception e) {
                // e.printStackTrace();
                try {
                    method.invoke(object, (Object[]) null); // 无参函数走这里
                } catch (Exception e1) {
                    throw new RuntimeException("invoke method error:" +
                            object.getClass().getName() + "#" + method.getName(), e1);
                }
            }
        }
    }
}
