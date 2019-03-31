package com.dapn.andokay.model;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/31
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class Person {

    public int age;
    public String name;
    public boolean flag;

    // mClazz.newInstance();
    public Person() {

    }

    public Person(int age, String name, boolean flag) {
        this.age = age;
        this.name = name;
        this.flag = flag;
    }
}
