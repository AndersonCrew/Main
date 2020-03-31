package com.crewcloud.crewmain.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class SoftKeyboardDetectorView extends View {
    private boolean mShownKeyboard;
    private OnShownKeyboardListener mOnShownSoftKeyboard;
    private OnHiddenKeyboardListener mOnHiddenSoftKeyboard;

    public SoftKeyboardDetectorView(Context context) {
        this(context, null);
    }

    public SoftKeyboardDetectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int w2, int h2) {
        Activity activity = (Activity)getContext();
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

        int statusBarHeight = rect.top;
        int screenHeight = activity.getWindowManager().getDefaultDisplay().getHeight();
        int diffHeight = (screenHeight - statusBarHeight) - h;

        if (diffHeight > 100 && !mShownKeyboard) {
            mShownKeyboard = true;
            onShownSoftKeyboard();
        } else if (diffHeight < 100 && mShownKeyboard) {
            mShownKeyboard = false;
            onHiddenSoftKeyboard();
        }

        super.onSizeChanged(w, h, w2, h2);
    }

    public void onHiddenSoftKeyboard() {
        if (mOnHiddenSoftKeyboard != null) {
            mOnHiddenSoftKeyboard.onHiddenSoftKeyboard();
        }
    }

    public void onShownSoftKeyboard() {
        if (mOnShownSoftKeyboard != null) {
            mOnShownSoftKeyboard.onShowSoftKeyboard();
        }
    }

    public void setOnShownKeyboard(OnShownKeyboardListener listener) {
        mOnShownSoftKeyboard = listener;
    }

    public void setOnHiddenKeyboard(OnHiddenKeyboardListener listener) {
        mOnHiddenSoftKeyboard = listener;
    }

    public interface OnShownKeyboardListener {
        void onShowSoftKeyboard();
    }

    public interface OnHiddenKeyboardListener {
        void onHiddenSoftKeyboard();
    }
}