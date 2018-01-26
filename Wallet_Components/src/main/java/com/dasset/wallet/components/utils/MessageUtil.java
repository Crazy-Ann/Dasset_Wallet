package com.dasset.wallet.components.utils;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

public final class MessageUtil {

    private MessageUtil() {
        // cannot be instantiated
    }

    public static Message getMessage(int state) {
        Message message = Message.obtain();
        message.what = state;
        return message;
    }

    public static Message getMessage(int state, Bundle bundle) {
        Message message = Message.obtain();
        message.setData(bundle);
        message.what = state;
        return message;
    }

    public static Message getMessage(int state, Object obj) {
        Message message = Message.obtain();
        message.obj = obj;
        message.what = state;
        return message;
    }

    public static Message getMessage(int state, String param) {
        Message message = Message.obtain();
        message.what = state;
        message.obj = param;
        return message;
    }


    public static Message getErrorMessage(int state, Exception e, String error) {
        Message message = Message.obtain();
        message.what = state;
        if (!TextUtils.isEmpty(e.getMessage())) {
            message.obj = e.getMessage();
        } else {
            message.obj = error;
        }
        return message;
    }
}
