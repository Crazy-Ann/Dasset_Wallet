package com.dasset.wallet.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.dasset.wallet.R;
import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.base.toolbar.listener.OnLeftIconEventListener;
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
import com.dasset.wallet.ui.activity.contract.SettingContract;
import com.dasset.wallet.ui.activity.presenter.SettingPresenter;
import com.dasset.wallet.ui.dialog.DownloadDialog;
import com.dasset.wallet.ui.dialog.PromptDialog;

import java.io.File;
import java.util.List;

public class SettingActivity extends ActivityViewImplement<SettingContract.Presenter> implements SettingContract.View, View.OnClickListener, OnDialogInstallListner, OnLeftIconEventListener {

    private SettingPresenter settingPresenter;

    private TextView tvUpdate;
    private TextView tvUserProtocol;
    private TextView tvAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSavedInstanceState(savedInstanceState);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void findViewById() {
        inToolbar = ViewUtil.getInstance().findView(this, R.id.inToolbar);
        tvUpdate = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.tvUpdate, this);
        tvUserProtocol = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.tvUserProtocol, this);
        tvAbout = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.tvAbout, this);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        initializeToolbar(android.R.color.white, true, R.mipmap.icon_back_black, this, android.R.color.black, getString(R.string.account_setting));
        settingPresenter = new SettingPresenter(this, this);
        settingPresenter.initialize();
        setBasePresenterImplement(settingPresenter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            settingPresenter.checkPermission(new PermissionCallback() {

                @Override
                public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                    //TODO
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
        }
    }

    @Override
    protected void setListener() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvUpdate:
                settingPresenter.getVersion();
                break;
            case R.id.tvUserProtocol:
                startActivity(UserProtocolActivity.class);
                break;
            case R.id.tvAbout:
                PromptDialog.createBuilder(getSupportFragmentManager())
                        .setTitle(getString(R.string.dialog_prompt))
                        .setPrompt(getString(R.string.prompt_quit))
                        .setPositiveButtonText(this, R.string.confirm)
                        .setNegativeButtonText(this, R.string.cancel)
                        .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_QUIT)
                        .showAllowingStateLoss(this);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.RequestCode.NET_WORK_SETTING:
            case Constant.RequestCode.PREMISSION_SETTING:
            case Constant.RequestCode.INSTALL_APK:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    settingPresenter.checkPermission(new PermissionCallback() {

                        @Override
                        public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                            //TODO
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                            showPermissionPromptDialog();
                        }
                    });
                } else {
                    //TODO
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
                settingPresenter.download();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_GET_VERSION_SUCCESS:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_GET_VERSION_SUCCESS");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_DOWNLOAD_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_DOWNLOAD_ERROR");
                settingPresenter.checkForceUpdate();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_INSTALL:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_INSTALL");
                if (!TextUtils.isEmpty(settingPresenter.getPath())) {
                    ApplicationUtil.getInstance().chmod(Regex.PERMISSION.getRegext(), settingPresenter.getPath());
                    startActivityForResult(Intent.ACTION_VIEW, FileProviderUtil.getInstance().generateUri(this, new File(settingPresenter.getPath())),
                                           (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ? Intent.FLAG_GRANT_READ_URI_PERMISSION : -1,
                                           Regex.FILE_HEADER_TYPE.getRegext(),
                                           Constant.RequestCode.INSTALL_APK);
                } else {
                    showPromptDialog(R.string.dialog_prompt_install_error, true, false, Constant.RequestCode.DIALOG_PROMPT_INSTALL_ERROR);
                }
                break;
            case Constant.RequestCode.DIALOG_PROMPT_INSTALL_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_INSTALL_FAILED");
                settingPresenter.checkForceUpdate();
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
                settingPresenter.checkForceUpdate();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_DOWNLOAD:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_DOWNLOAD");
                settingPresenter.checkForceUpdate();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_INSTALL:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_INSTALL");
                settingPresenter.checkForceUpdate();
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
    public void showAbountPromptDialog(String prompt) {

    }

    @Override
    public void onDialogInstall(String path) {
        settingPresenter.setPath(path);
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

    @Override
    public void onLeftIconEvent() {
        onFinish("onLeftIconEvent");
    }
}
