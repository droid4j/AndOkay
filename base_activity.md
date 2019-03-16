# 模板设计模式构建BaseActivity


## 一、模板设计模式定义

所谓模板，就是在父类中定义某算法的某些步骤。使这些步骤的具体实现延迟到子类中。

## 二、使用场景

1. 多个子类有公有的方法，并且基本流程相同。

2. 可以将重要、复杂的算法的核心设计为模板方法，周边相关细节由子类实现。

3. 重构时，模板方法是一个常用手段，把相同的代码抽取到父类中，然后通过勾子函数约束其行为。

###2.1 基本框架

* AbstractClass: 抽象类，定义一套算法框架

* ConcreteClass: 具体的实现类，可以自定义一些算法

###2.2 设计模式相关git

https://github.com/simple-android-framework-exchange/android_design_patterns_analysis

## 三、Android设计模式源码阅读

###3.1 AsyncTask
###3.2 View

## 四、使用模板设计模式创建BaseActivity