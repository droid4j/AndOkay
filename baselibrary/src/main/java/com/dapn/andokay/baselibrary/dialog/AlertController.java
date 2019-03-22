package com.dapn.andokay.baselibrary.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/03/21
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class AlertController {

    private AlertDialog mDialog;
    private Window mWindow;
    private DialogViewHelper mViewHelper;

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

    public static class AlertParams {

        public final Context mContext;
        public final int mThemeResId;
        public boolean mCancelable = true; // 点击空白是否可以取消
        public DialogInterface.OnCancelListener mOnCancelListener; // cancel监听
        public DialogInterface.OnDismissListener mOnDismissListener; // dismiss监听
        public DialogInterface.OnKeyListener mOnKeyListener; // key监听
        public View mView;           // 布局
        public int mViewLayoutResId; // 布局id

        // 存放字体文本
        public SparseArray<CharSequence> textArray = new SparseArray<>();

        public SparseArray<View.OnClickListener> clickArray = new SparseArray<>();
        public int mWidth = ViewGroup.LayoutParams.WRAP_CONTENT; // 宽度
        public int mAnimations = 0; // 动画
        public int mGravity = Gravity.CENTER; // 位置
        public int mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;

        public AlertParams(Context context, int themeResId) {
            this.mContext = context;
            this.mThemeResId = themeResId;
        }

        /**
         * 绑定我设置参数
         * @param mAlert
         */
        public void apply(AlertController mAlert) {

            // 1. 设置布局  DialogViewHelper
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

            // 设置Controller的辅助类DialogViewHelper
            mAlert.setViewHelper(viewHelper);

            // 2. 设置文本
            final int textSize = textArray.size();
            for (int i = 0; i < textSize; i++) {
                viewHelper.setText(textArray.keyAt(i), textArray.valueAt(i));
            }

            // 3. 设置点击
            final int clickSize = clickArray.size();
            for (int i = 0; i < clickSize; i++) {
                viewHelper.setOnClickListener(clickArray.keyAt(i), clickArray.valueAt(i));
            }

            // 4. 设置自定义效果
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


        }
    }
}
