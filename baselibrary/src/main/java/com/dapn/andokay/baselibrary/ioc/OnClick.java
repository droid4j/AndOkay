package com.dapn.andokay.baselibrary.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/14
 *     desc   : View注解的Annotation
 *     version: 1.0
 * </pre>
 */
// ElementType 代表 Annotation位置
@Target(ElementType.METHOD)
// RetentionPolicy 什么时候生效
@Retention(RetentionPolicy.RUNTIME)
public @interface OnClick {

    // --> @OnClick(R.id.xxx)
    int[] value();
}
