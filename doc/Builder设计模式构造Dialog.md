# Builder设计模式构造Dialog

## 1. 构建者模式

http://www.jianshu.com/p/87288925ee1f

##2. AlertDialog源码阅读

###2.1 基本使用

```java
AlertDialog.Builder builder = new AlertDialog.Builder(this) // 1
        .setIcon(R.drawable.ic_launcher_background)
        .setTitle("Alert Dialog")														// 2
        .setMessage("Hello World");
builder.create()																						// 3
  .show();																									// 4
```

1. 创建Builder对象
2. 设置参数              （选择配件）
3. create()方法         （组装电脑）
4. 显示dialog

###2.1.1 创建 Builder对象

```java
// sdk/sources/android-28/android/app/AlertDialog.java
public Builder(Context context, int themeResId) {
    P = new AlertController.AlertParams(new ContextThemeWrapper(
            context, resolveDialogTheme(context, themeResId)));
}
```

Builder是 AlertDialog的内部类，在它的构造函数里，创建了AlertController.AlertParams对象，猜想，之后的一系列参数设置，都是通过这个P来完成的

###2.1.2 设置参数

```java
public Builder setTitle(CharSequence title) {
    P.mTitle = title;
    return this;
}
```

我们调用Builder的setTitle方法，最后将参数放到了上一步实例化的P中，刚好验证了上一步的猜想。返回this，是可以链式调用的原因。

###2.1.3 create()方法

```java
public AlertDialog create() {
    // Context has already been wrapped with the appropriate theme.
    final AlertDialog dialog = new AlertDialog(P.mContext, 0, false);
    P.apply(dialog.mAlert);
    dialog.setCancelable(P.mCancelable);
    if (P.mCancelable) {
        dialog.setCanceledOnTouchOutside(true);
    }
    dialog.setOnCancelListener(P.mOnCancelListener);
    dialog.setOnDismissListener(P.mOnDismissListener);
    if (P.mOnKeyListener != null) {
        dialog.setOnKeyListener(P.mOnKeyListener);
    }
    return dialog;
}
```

首先实例化 AlertDialog 对象，然后将它的mAlert对象传给P.apply()方法，完成参数的组装。

这个 mAlert是在哪创建的呢？我们看下AlertDialog的构造函数

```java
AlertDialog(Context context, @StyleRes int themeResId, boolean createContextThemeWrapper) {
    super(context, createContextThemeWrapper ? resolveDialogTheme(context, themeResId) : 0,
            createContextThemeWrapper);

    mWindow.alwaysReadCloseOnTouchAttr();
    mAlert = AlertController.create(getContext(), this, getWindow());
}
```

没错，就是这里了。

###2.1.4 apply()

```Java
// sdk/sources/android-28/com/android/internal/app/AlertController.java
public void apply(AlertController dialog) {
    if (mCustomTitleView != null) {
        dialog.setCustomTitle(mCustomTitleView);
    } else {
        if (mTitle != null) {
            dialog.setTitle(mTitle);
        }
        if (mIcon != null) {
            dialog.setIcon(mIcon);
        }
        // 省略...
    }
    // 省略...
    if (mView != null) {
        if (mViewSpacingSpecified) {
            dialog.setView(mView, mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight,
                    mViewSpacingBottom);
        } else {
            dialog.setView(mView);
        }
    } else if (mViewLayoutResId != 0) {
        dialog.setView(mViewLayoutResId);
    }
}
```

##3 Builder设计模式的工作流程

​	添加参数(P) —> 组装参数(apply)  —> 显示

###3.1 主要对象

AlertDialog				电脑对象

AlertDialog.Builder		  规范一系列的组装过程

AlertController 			具体的构造器

AlertController.AlertParams 存放参数，一部分设置参数的功能

##4 基本框架搭建

![dialog](/Users/per4j/Documents/Learn/学习2019/dialog.png)

#### 4.1 创建基本类结构

#####4.1.1 创建AlertDialog及内部类Builder类

```java
public class AlertDialog extends Dialog {

    public static class Builder {

    }
}
```

**注意：** 我们的AlertDialog，模仿系统的，也是继承自Dialog。

#####4.1.2 创建AlertController及内部类AlertParams类

```java 
public class AlertController {

    public static class AlertParams {

    }
}
```

####4.2 模仿系统AlertDialog实现

#####4.2.1 添加Builder构造函数

```java
private final AlertController.AlertParams P;

public Builder(Context context) {
    this(context, R.style.dialog);
}

public Builder(Context context, int themeResId) {
    P = new AlertController.AlertParams(context, themeResId);
}
```

dialog样式，稍后给出。

在第二个构造函数中，我们实例化AlertController的内部类AlertParams，并传入了上下文context和主题id。

#####4.2.2 构造AlertParams构造函数

```java
public final Context mContext;
public final int mThemeResId;

public AlertParams(Context context, int themeResId) {
    this.mContext = context;
    this.mThemeResId = themeResId;
}
```

#####4.3 create及apply方法

#####4.3.1 接着创建最关键的create()方法

```java
public AlertDialog create() {
    // Context has already been wrapped with the appropriate theme.
    final AlertDialog dialog = new AlertDialog(P.mContext, P.mThemeResId);
    P.apply(dialog.mAlert);
    dialog.setCancelable(P.mCancelable);
    if (P.mCancelable) {
        dialog.setCanceledOnTouchOutside(true);
    }
    dialog.setOnCancelListener(P.mOnCancelListener);
    dialog.setOnDismissListener(P.mOnDismissListener);
    if (P.mOnKeyListener != null) {
        dialog.setOnKeyListener(P.mOnKeyListener);
    }
    return dialog;
}

public AlertDialog show() {
    final AlertDialog dialog = create();
    dialog.show();
    return dialog;
}
```

这里直接拷贝系统的create()方法。AlertDialog类是在这里实例化的。参数是通过 P 获取的。

#####4.3.2 添加AlertDialog构造函数

```java
private AlertController mAlert;

public AlertDialog(@NonNull Context context, int themeResId) {
    super(context, themeResId);

    mAlert = new AlertController(this, getWindow());
}
```

在AlertDialog构造函数中，我们创建了AlertController对象，并将当前对象和window传入。接下来看下，AlertController 的构造函数：

```java
private AlertDialog mDialog;
private Window mWindow;

public AlertController(AlertDialog dialog, Window window) {
    this.mDialog = dialog;
    this.mWindow = window;
}

public AlertDialog getDialog() {
    return mDialog;
}

public Window getWindow() {
    return mWindow;
}
```

#####4.3.3 增加apply方法及其他成员变量

```java
public boolean mCancelable = false; // 点击空白是否可以取消
public DialogInterface.OnCancelListener mOnCancelListener; // cancel监听
public DialogInterface.OnDismissListener mOnDismissListener; // dismiss监听
public DialogInterface.OnKeyListener mOnKeyListener; // key监听

/**
 * 绑定我设置参数
 * @param mAlert
 */
public void apply(AlertController mAlert) {

}
```

#### 4.3.4 样式dialog

```xml
<resources>
    <style name="dialog" parent="@android:style/Theme.Dialog">
        <item name="android:windowFrame">@null</item>
        <!-- 边框 -->
        <item name="android:windowIsFloating">true</item>
        <!-- 是否浮在activity上 -->
        <item name="android:windowIsTranslucent">true</item>

        <item name="android:background">@android:color/transparent</item>
        <!-- 背景透明 -->
        <item name="android:backgroundDimEnabled">true</item>
        <!-- 模糊 -->
        <item name="android:windowNoTitle">true</item>
        <!-- 无标题 -->
    </style>
</resources>
```

##5 完善Builder建造者

在 Builder 中添加 设置文字、监听的方法

```java
public Builder setText(int viewId, CharSequence text) {
    P.textArray.put(viewId, text);
    return this;
}

public Builder setOnClickListener(int viewId, View.OnClickListener listener) {
    P.clickArray.put(viewId, listener);
    return this;
}
```

为了支持多控件，我们将viewId存到数组中。

P.textArray 和 P.clickArray 对应 AlertController.AlertParams中的：

```java
// 存放字体文本
public SparseArray<CharSequence> textArray = new SparseArray<>();
public SparseArray<View.OnClickListener> clickArray = new SparseArray<>();
```

到目前，我们的Dialog已初见完成。但是，现在展示，还没有UI显示，那是因为我们的apply方法还没写，只是买了零件，没有组装，电脑当前不能运行啦。

```java
public void apply(AlertController mAlert) {

    // 1. 设置布局  DialogViewHelper

    // 给dialog设置布局

    // 2. 设置文本

    // 3. 设置点击

    // 4. 设置自定义效果
}
```

1. 设置布局

   ```java
   DialogViewHelper viewHelper = null;
   if (mViewLayoutResId != 0) {
       viewHelper = new DialogViewHelper(mContext, mViewLayoutResId);
   }
   
   if (mView != null) {
       viewHelper = new DialogViewHelper();
       viewHelper.setContentView(mView);
   }
   
   if (viewHelper == null) {
       throw new IllegalArgumentException("请设置布局setContentView()");
   }
   
   // 给dialog设置布局
   mAlert.getDialog().setContentView(viewHelper.getContentView());
   ```

   DialogViewHelper 构造函数及设置布局View的方法

   ```java
   private View mContentView = null;
   
   public DialogViewHelper(Context context, int layoutResId) {
       mContentView = LayoutInflater.from(context).inflate(layoutResId, null);
   }
   
   public DialogViewHelper() {
   }
   
   public void setContentView(View view) {
       mContentView = view;
   }
   
   public View getContentView() {
       return mContentView;
   }
   ```

2. 设置文本

   ```java
   final int textSize = textArray.size();
   for (int i = 0; i < textSize; i++) {
       viewHelper.setText(textArray.keyAt(i), textArray.valueAt(i));
   }
   ```

   DialogViewHelper 添加 setText 方法

   ```java
   public void setText(int viewId, CharSequence text) {
       TextView tv = getView(viewId);
       if (tv != null)
           tv.setText(text);
   }
   ```

   getView() 方法稍后给出

3. 设置点击

   ```java
   final int clickSize = clickArray.size();
   for (int i = 0; i < clickSize; i++) {
       viewHelper.setOnClickListener(clickArray.keyAt(i), clickArray.valueAt(i));
   }
   ```

   DialogViewHelper 添加 setOnClickListener方法

   ```java
   public void setOnClickListener(int viewId, View.OnClickListener listener) {
       View view = getView(viewId);
       if (view != null)
           view.setOnClickListener(listener);
   }
   ```

   在DialogViewHelper中添加getView()方法

   ```java
   private SparseArray<WeakReference<View>> mViews = new SparseArray<>();
   public <T extends View> T getView(int viewId) {
       WeakReference<View> weakReference = mViews.get(viewId);
       View view = null;
       if (weakReference != null)
           view = weakReference.get();
   
       if (view == null) {
           view = mContentView.findViewById(viewId);
           if (view != null) 
               mViews.put(viewId, new WeakReference<View>(view));
       }
       return (T) view;
   }
   ```

   

4. 设置自定义效果

   ```java
   Window window = mAlert.getWindow();
   
   window.setGravity(mGravity);
   // 设置动画
   if (mAnimations != 0) {
       window.setWindowAnimations(mAnimations);
   }
   
   // 设置宽度
   WindowManager.LayoutParams params = window.getAttributes();
   params.width = mWidth;
   params.height = mHeight;
   window.setAttributes(params);
   ```

   自定义效果，像宽高、动画，都是通过window对象操作的。

   在Builder中添加对外公开的设置自定义效果的方法：

   ```java
   // 配置万能参数
   public Builder fullWidth() {
       P.mWidth = ViewGroup.LayoutParams.MATCH_PARENT;
       return this;
   }
   
   public Builder fromBottom(boolean animation) {
       if (animation) {
           P.mAnimations = R.style.dialog_from_bottom_anim;
       }
       P.mGravity = Gravity.BOTTOM;
       return this;
   }
   
   public Builder setWidthAndHeight(int width, int height) {
       P.mWidth = width;
       P.mHeight=  height;
       return this;
   }
   
   public Builder addDefaultAnimation() {
       P.mAnimations = R.style.dialog_scale_anim;
       return this;
   }
   
   public Builder setAnimations(int styleAnimation) {
       P.mAnimations = styleAnimation;
       return this;
   }
   ```

##6 其他方法

假如，我们现在要点击按钮时，获取输入的内容，怎么做呢？现在的Dialog还无法支持这样的功能。接下来，实现下这个功能。

```java
final EditText editText = dialog.getView(R.id.editText);
builder.setOnClickListener(R.id.button, new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Toast.makeText(MainActivity.this, 
                editText.getText().toString().trim(), 
                Toast.LENGTH_SHORT).show();
    }
});
```

要想实现以上方法，我们需要在Dialog中添加 getView() 等方法

```java
public void setText(int viewId, CharSequence text) {
    mAlert.setText(viewId, text);
}

public void setOnClickListener(int viewId, View.OnClickListener listener) {
    mAlert.setOnClickListener(viewId, listener);
}

public <T extends View> T getView(int viewId) {
    return mAlert.getView(viewId);
}
```

在这里，我们不直接去处理，而是交给AlertController来处理。

```java
private DialogViewHelper mViewHelper;

public void setViewHelper(DialogViewHelper viewHelper) {
    this.mViewHelper = viewHelper;
}

public void setText(int viewId, CharSequence text) {
    mViewHelper.setText(viewId, text);
}

public void setOnClickListener(int viewId, View.OnClickListener listener) {
    mViewHelper.setOnClickListener(viewId, listener);
}

public <T extends View> T getView(int viewId) {
    return mViewHelper.getView(viewId);
}
```

最后，我们要在哪里对 mViewHelper进行初始化呢？在AlertController.AlertParams的apply方法中，这是我们唯一操作为DialogViewHelper的地方，在这里也持有AlertController的引用，刚好满足我们的需求。

```java
// AlertController.AlertParams#apply()

// 1. 设置布局  DialogViewHelper
DialogViewHelper viewHelper = null;
if (mViewLayoutResId != 0) {
    viewHelper = new DialogViewHelper(mContext, mViewLayoutResId);
}
// 省略...

// 给dialog设置布局
mAlert.getDialog().setContentView(viewHelper.getContentView());

// 设置Controller的辅助类DialogViewHelper
mAlert.setViewHelper(viewHelper);

// 2. 设置文本
```

即此，我们完成了全部功能。





