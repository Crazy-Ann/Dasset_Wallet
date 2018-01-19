package com.dasset.wallet.base.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.dasset.wallet.base.BuildConfig;
import com.dasset.wallet.components.constant.Constant;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.http.Configuration;
import com.dasset.wallet.components.http.CustomHttpClient;
import com.dasset.wallet.components.http.model.Parameter;
import com.dasset.wallet.components.utils.ActivityUtil;
import com.dasset.wallet.components.utils.ApplicationUtil;
import com.dasset.wallet.components.utils.BundleUtil;
import com.dasset.wallet.components.utils.DeviceUtil;
import com.dasset.wallet.components.utils.GlideUtil;
import com.dasset.wallet.components.utils.InputUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.NetworkUtil;
import com.dasset.wallet.components.utils.ReflectUtil;
import com.dasset.wallet.components.utils.SecurityUtil;
import com.dasset.wallet.components.utils.SharedPreferenceUtil;
import com.dasset.wallet.components.utils.SnackBarUtil;
import com.dasset.wallet.components.utils.StrictModeUtil;
import com.dasset.wallet.components.utils.ToastUtil;
import com.dasset.wallet.components.utils.TypefaceUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.google.common.collect.Lists;

import io.objectbox.android.AndroidObjectBrowser;
import okhttp3.Headers;
import okhttp3.Interceptor;

public class BaseApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

    private static BaseApplication application;
    private String encryptKey = Regex.NONE.getRegext();

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        if (!TextUtils.isEmpty(encryptKey)) {
            this.encryptKey = encryptKey;
        }
    }

    public static BaseApplication getInstance() {
        return application;
    }

    public void releaseInstance() {
        CustomHttpClient.releaseInstance();
        BundleUtil.releaseInstance();
        DeviceUtil.releaseInstance();
        InputUtil.releaseInstance();
        LogUtil.releaseInstance();
        ToastUtil.releaseInstance();
        NetworkUtil.releaseInstance();
        ReflectUtil.releaseInstance();
        SecurityUtil.releaseInstance();
        SharedPreferenceUtil.releaseInstance();
        SnackBarUtil.releaseInstance();
        StrictModeUtil.releaseInstance();
        NetworkUtil.releaseInstance();
        TypefaceUtil.releaseInstance();
        ViewUtil.releaseInstance();
        ApplicationUtil.releaseInstance();
        application = null;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.getInstance().print(this.getClass().getSimpleName() + " onCreate() invoked!!");
        application = this;
        registerActivityLifecycleCallbacks(this);
        CustomHttpClient.getInstance().initialize(new Configuration.Builder()
                                                          .setParameters(Lists.<Parameter>newArrayList())
                                                          .setHeaders(new Headers.Builder().build())
                                                          .setTimeout(Constant.HttpTask.REQUEST_TIME_OUT_PERIOD)
                                                          .setInterceptors(Lists.<Interceptor>newArrayList())
                                                          .setDebug(true).build());
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        LogUtil.getInstance().print(this.getClass().getSimpleName() + " onLowMemory() invoked!!");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        LogUtil.getInstance().print(this.getClass().getSimpleName() + " onTrimMemory() invoked!!" + level);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        LogUtil.getInstance().print(activity.getClass().getSimpleName() + " onActivityCreated() invoked!!");
        LogUtil.getInstance().print(activity.getClass().getSimpleName() + " taskId:" + activity.getTaskId());
        ActivityUtil.add(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        LogUtil.getInstance().print(activity.getClass().getSimpleName() + " onActivityStarted() invoked!!");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        LogUtil.getInstance().print(activity.getClass().getSimpleName() + " onActivityResumed() invoked!!");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        LogUtil.getInstance().print(activity.getClass().getSimpleName() + " onActivityPaused() invoked!!");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        LogUtil.getInstance().print(activity.getClass().getSimpleName() + " onActivityStopped() invoked!!");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        LogUtil.getInstance().print(activity.getClass().getSimpleName() + " onActivitySaveInstanceState() invoked!!");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        LogUtil.getInstance().print(activity.getClass().getSimpleName() + " onActivityDestroyed() invoked!!");
        GlideUtil.getInstance().clearMemory(this);
//        ActivityUtil.remove(activity);
    }
}
