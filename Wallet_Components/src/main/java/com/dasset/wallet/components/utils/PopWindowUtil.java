package com.dasset.wallet.components.utils;

import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import com.dasset.wallet.components.constant.Constant;

public class PopWindowUtil {

    private        PopupWindow   popupWindow;
    private static PopWindowUtil popWindowUtil;

    private PopWindowUtil() {
        // cannot be instantiated
    }

    public static synchronized PopWindowUtil getInstance() {
        if (popWindowUtil == null) {
            popWindowUtil = new PopWindowUtil();
        }
        return popWindowUtil;
    }

    public static void releaseInstance() {
        if (popWindowUtil != null) {
            popWindowUtil = null;
        }
    }

    public void showPopWindow(View anchorView, View contentView, int width, int height, boolean focusable, int gravity) {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
        popupWindow = new PopupWindow(contentView, width, height, focusable);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        switch (gravity) {
            case Constant.View.POP_WINDOW_RIGHT:
                int windowPosition[] = calculatePopWindowPos(anchorView, contentView);
                windowPosition[0] -= 20;
                popupWindow.showAtLocation(anchorView, Gravity.TOP | Gravity.START, windowPosition[0], windowPosition[1]);
                break;
            case Constant.View.POP_WINDOW_CENTER:
                popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
                break;
            default:
                popupWindow.showAsDropDown(anchorView);
                break;
        }
    }

    private int[] calculatePopWindowPos(View anchorView, View contentView) {
        int windowPosition[] = new int[2];
        int anchorLocation[] = new int[2];
        anchorView.getLocationOnScreen(anchorLocation);
        int anchorHeight = anchorView.getHeight();
        int screenHeight = anchorView.getResources().getDisplayMetrics().heightPixels;
        int screenWidth  = anchorView.getResources().getDisplayMetrics().widthPixels;
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int windowHeight = contentView.getMeasuredHeight();
        int windowWidth  = contentView.getMeasuredWidth();
        if ((screenHeight - anchorLocation[1] - anchorHeight < windowHeight)) {
            windowPosition[0] = screenWidth - windowWidth;
            windowPosition[1] = anchorLocation[1] - windowHeight;
        } else {
            windowPosition[0] = screenWidth - windowWidth;
            windowPosition[1] = anchorLocation[1] + anchorHeight;
        }
        return windowPosition;
    }
}
