package com.dasset.wallet.components.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public final class ToastUtil {

    private Toast toast;

    private static ToastUtil toastUtil;

    private ToastUtil() {
        // cannot be instantiated
    }

    public static synchronized ToastUtil getInstance() {
        if (toastUtil == null) {
            toastUtil = new ToastUtil();
        }
        return toastUtil;
    }

    public static void releaseInstance() {
        if (toastUtil != null) {
            toastUtil = null;
        }
    }

    public void showToast(Context context, CharSequence message, int duration) {
        if (toast == null) {
            toast = Toast.makeText(context, message, duration);
        } else {
            toast.setText(message);
        }
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void showToast(Context context, int resId, int duration) {
        if (toast == null) {
            toast = Toast.makeText(context, resId, duration);
        } else {
            toast.setText(resId);
        }
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void hideToast() {
        if (toast != null) {
            toast.cancel();
        }
    }
}
