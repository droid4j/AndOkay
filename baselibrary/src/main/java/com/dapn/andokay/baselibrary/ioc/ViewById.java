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
//    ElementType.FIELD         属性
//    ElementType.Type          类上
//    ElementType.CONSTRUCTOR   构造函数上
@Target(ElementType.FIELD)
// RetentionPolicy 什么时候生效
//      RetentionPolicy.CLASS   编译时
//      RetentionPolicy.RUNTIME 运行时
//      RetentionPolicy.SOURCE  源码时
@Retention(RetentionPolicy.CLASS)
public @interface ViewById {

    // --> @ViewById(R.id.xxx)
    int value();
}
