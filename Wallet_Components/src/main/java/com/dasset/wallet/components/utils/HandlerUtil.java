package com.dasset.wallet.components.utils;


import android.os.Handler;
import android.os.Looper;

public class HandlerUtil {

    private static Handler handler;

    private HandlerUtil() {
    }

    public static Handler getMainThreadHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    public static void runOnMainThread(Runnable runnable) {
        if(Looper.myLooper() == Looper.getMainLooper()){
            runnable.run();
        }else{
            getMainThreadHandler().post(runnable);
        }
    }
}
