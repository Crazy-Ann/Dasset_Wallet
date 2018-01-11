package com.dasset.wallet.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.dasset.wallet.R;
import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.ActivityUtil;
import com.dasset.wallet.components.utils.ApplicationUtil;
import com.dasset.wallet.components.utils.FileProviderUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.net.task.listener.OnDialogInstallListner;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.WelcomeContract;
import com.dasset.wallet.ui.activity.presenter.WelcomePresenter;
import com.dasset.wallet.ui.dialog.DownloadDialog;
import com.dasset.wallet.ui.dialog.PromptDialog;

import java.io.File;
import java.util.List;

public class WelcomeActivity extends ActivityViewImplement<WelcomeContract.Presenter> implements WelcomeContract.View, OnDialogInstallListner {

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
                    welcomePresenter.getVersion();
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
            welcomePresenter.getVersion();
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
                            welcomePresenter.getVersion();
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                            showPermissionPromptDialog();
                        }
                    });
                } else {
                    welcomePresenter.getVersion();
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
            case Constant.RequestCode.DIALOG_PROMPT_GET_VERSION_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_GET_VERSION_ERROR");
                onFinish("DIALOG_PROMPT_GET_VERSION_ERROR");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_VERSION_UPDATE:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_VERSION_UPDATE");
                welcomePresenter.download();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_DOWNLOAD_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_DOWNLOAD_ERROR");
                welcomePresenter.checkForceUpdate();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_INSTALL:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_INSTALL");
                if (!TextUtils.isEmpty(welcomePresenter.getPath())) {
                    ApplicationUtil.getInstance().chmod(Regex.PERMISSION.getRegext(), welcomePresenter.getPath());
                    startActivityForResult(Intent.ACTION_VIEW, FileProviderUtil.getInstance().generateUri(this, new File(welcomePresenter.getPath())),
                                           (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ? Intent.FLAG_GRANT_READ_URI_PERMISSION : -1,
                                           Regex.FILE_HEADER_TYPE.getRegext(),
                                           Constant.RequestCode.INSTALL_APK);
                } else {
                    showPromptDialog(R.string.dialog_prompt_install_error, true, false, Constant.RequestCode.DIALOG_PROMPT_INSTALL_ERROR);
                }
                break;
            case Constant.RequestCode.DIALOG_PROMPT_INSTALL_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_INSTALL_FAILED");
                welcomePresenter.checkForceUpdate();
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
            case Constant.RequestCode.DIALOG_PROMPT_VERSION_UPDATE:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_VERSION_UPDATE");
                welcomePresenter.checkForceUpdate();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_DOWNLOAD:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_DOWNLOAD");
                welcomePresenter.checkForceUpdate();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_INSTALL:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_INSTALL");
                welcomePresenter.checkForceUpdate();
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
    public void showVersionUpdatePromptDialog(String prompt) {
        PromptDialog.createBuilder(getSupportFragmentManager())
                .setTitle(getString(R.string.dialog_prompt))
                .setPrompt(prompt)
                .setPositiveButtonText(this, R.string.download)
                .setNegativeButtonText(this, R.string.cancel)
                .setCancelable(false)
                .setCancelableOnTouchOutside(false)
                .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_VERSION_UPDATE)
                .showAllowingStateLoss(this);
    }

    @Override
    public void showDownloadPromptDialog(String url, File file) {
        DownloadDialog.createBuilder(getSupportFragmentManager())
                .setTitle(getString(R.string.dialog_prompt))
                .setPrompt(getString(R.string.dialog_prompt_download))
                .setUrl(url)
                .setFile(file)
                .setNegativeButtonText(this, R.string.cancel)
                .setCancelable(false)
                .setCancelableOnTouchOutside(false)
                .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_DOWNLOAD)
                .showAllowingStateLoss(this);
    }

    @Override
    public void startMainActivity() {
        startActivity(MainActivity.class);
        onFinish("startMainActivity");
    }

    @Override
    public void startSplashActivity(Bundle bundle) {
        startActivity(SplashActivity.class, bundle);
        onFinish("startSplashActivity");
    }

    @Override
    public void onDialogInstall(String path) {
        welcomePresenter.setPath(path);
        PromptDialog.createBuilder(getSupportFragmentManager())
                .setTitle(getString(R.string.dialog_prompt))
                .setPrompt(getString(R.string.dialog_prompt_install))
                .setPositiveButtonText(this, R.string.install)
                .setNegativeButtonText(this, R.string.cancel)
                .setCancelable(false)
                .setCancelableOnTouchOutside(false)
                .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_INSTALL)
                .showAllowingStateLoss(this);
    }
}
