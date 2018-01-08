package com.dasset.wallet.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.dasset.wallet.R;
import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.ActivityUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.SharedPreferenceUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.WelcomeContract;
import com.dasset.wallet.ui.activity.presenter.WelcomePresenter;
import com.dasset.wallet.ui.dialog.PromptDialog;

import java.util.List;

public class WelcomeActivity extends ActivityViewImplement<WelcomeContract.Presenter> implements WelcomeContract.View {

    private WelcomePresenter welcomePresenter;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && ViewUtil.getInstance().getNavigationBarStatus(this) != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_welcome);
        getSavedInstanceState(savedInstanceState);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void findViewById() {
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        welcomePresenter = new WelcomePresenter(this, this);
        welcomePresenter.initialize();
        setBasePresenterImplement(welcomePresenter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            welcomePresenter.checkPermission(new PermissionCallback() {

                @Override
                public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                    //TODO
                    if (SharedPreferenceUtil.getInstance().getBoolean(BaseApplication.getInstance(), Constant.Configuration.CONFIGURATION, Context.MODE_PRIVATE, Constant.Configuration.KEY1, false)) {
                        startMainActivity();
                    } else {
                        startSplashActivity();
                    }
                }

                @Override
                public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                    for (String deniedPermission : deniedPermissions) {
                        LogUtil.getInstance().print("deniedPermission:" + deniedPermission);
                    }
                    showPermissionPromptDialog();
                }
            });
        } else {
            //TODO
            if (SharedPreferenceUtil.getInstance().getBoolean(BaseApplication.getInstance(), Constant.Configuration.CONFIGURATION, Context.MODE_PRIVATE, Constant.Configuration.KEY1, false)) {
                startMainActivity();
            } else {
                startSplashActivity();
            }
        }
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.RequestCode.NET_WORK_SETTING:
            case Constant.RequestCode.PREMISSION_SETTING:
            case Constant.RequestCode.INSTALL_APK:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    welcomePresenter.checkPermission(new PermissionCallback() {

                        @Override
                        public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                            //TODO
                            if (SharedPreferenceUtil.getInstance().getBoolean(BaseApplication.getInstance(), Constant.Configuration.CONFIGURATION, Context.MODE_PRIVATE, Constant.Configuration.KEY1, false)) {
                                startMainActivity();
                            } else {
                                startSplashActivity();
                            }
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                            showPermissionPromptDialog();
                        }
                    });
                } else {
                    //TODO
                    if (SharedPreferenceUtil.getInstance().getBoolean(BaseApplication.getInstance(), Constant.Configuration.CONFIGURATION, Context.MODE_PRIVATE, Constant.Configuration.KEY1, false)) {
                        startMainActivity();
                    } else {
                        startSplashActivity();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        PromptDialog.createBuilder(getSupportFragmentManager())
                .setTitle(getString(R.string.dialog_prompt))
                .setPrompt(getString(R.string.prompt_quit))
                .setPositiveButtonText(this, R.string.confirm)
                .setNegativeButtonText(this, R.string.cancel)
                .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_QUIT)
                .showAllowingStateLoss(this);
    }

    @Override
    public void onPositiveButtonClicked(int requestCode) {
        switch (requestCode) {
            case Constant.RequestCode.DIALOG_PROMPT_SET_NET_WORK:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_NET_WORK_ERROR");
                startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), Constant.RequestCode.NET_WORK_SETTING);
                break;
            case Constant.RequestCode.DIALOG_PROMPT_SET_PERMISSION:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_SET_PERMISSION");
                startPermissionSettingActivity();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QUIT:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_QUIT");
                BaseApplication.getInstance().releaseInstance();
                ActivityUtil.removeAll();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNegativeButtonClicked(int requestCode) {
        switch (requestCode) {
            case Constant.RequestCode.DIALOG_PROMPT_SET_NET_WORK:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_SET_NET_WORK");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_SET_PERMISSION:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_SET_PERMISSION");
                refusePermissionSetting();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QUIT:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_QUIT");
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void startMainActivity() {
        startActivity(MainActivity.class);
        onFinish("startMainActivity");
    }

    @Override
    public void startSplashActivity() {
        startActivity(SplashActivity.class);
        onFinish("startSplashActivity");
    }
}
