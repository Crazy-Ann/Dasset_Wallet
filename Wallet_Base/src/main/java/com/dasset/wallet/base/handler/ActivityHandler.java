package com.dasset.wallet.base.handler;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.SoftReference;

public abstract class ActivityHandler<T extends Activity> extends Handler {

    private final SoftReference<T> activities;

    public ActivityHandler(T activity) {
        activities = new SoftReference<>(activity);
    }

    @Override
    public void handleMessage(Message message) {
        super.handleMessage(message);
        T activity = activities.get();
        if (activity == null) {
            return;
        }
        handleMessage(activity, message);
    }

    protected abstract void handleMessage(T activity, Message message);
}
