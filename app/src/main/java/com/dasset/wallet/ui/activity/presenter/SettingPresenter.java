package com.dasset.wallet.ui.activity.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.dasset.wallet.R;
import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.base.http.model.BaseEntity;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.ApplicationUtil;
import com.dasset.wallet.components.utils.IOUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.NetworkUtil;
import com.dasset.wallet.components.utils.SharedPreferenceUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.model.Version;
import com.dasset.wallet.net.WalletApi;
import com.dasset.wallet.net.listener.ApiResponse;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.SettingActivity;
import com.dasset.wallet.ui.activity.contract.SettingContract;

import java.io.File;
import java.io.IOException;

public class SettingPresenter extends BasePresenterImplement implements SettingContract.Presenter {

    private SettingContract.View view;
    private Version              version;
    private String               path;

    public SettingPresenter(Context context, SettingContract.View view) {
        this.context = context;
        this.view = view;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void getVersion() {
        WalletApi.getInstance().getVersion(context, view, SharedPreferenceUtil.getInstance().getString(context, com.dasset.wallet.components.constant.Constant.ShreadPreference.FILE_CONFIGURATION, Context.MODE_PRIVATE, com.dasset.wallet.components.constant.Constant.ShreadPreference.PAGE_SRC_SIGN, Regex.NONE.getRegext()), new ApiResponse() {

            @Override
            public void success(BaseEntity baseEntity) {
                version = (Version) baseEntity;
                if (version != null) {
                    BaseApplication.getInstance().setEncryptKey(version.getApk());
                    if (ApplicationUtil.getInstance().getVersionCode(context) < Integer.valueOf(version.getClientVersion())) {
                        view.showVersionUpdatePromptDialog(version.getUpdateMessage());
                    } else {
                        view.showPromptDialog(R.string.dialog_prompt_get_version_success, true, false, Constant.RequestCode.DIALOG_PROMPT_GET_VERSION_SUCCESS);
                    }
                } else {

                }
            }

            @Override
            public void failed(BaseEntity entity) {
                
            }
        });
    }

    @Override
    public void checkForceUpdate() {
        if (version != null) {
            if (ApplicationUtil.getInstance().getVersionCode(context) < Integer.valueOf(version.getLowestClientVersion())) {
                ((SettingActivity) view).onFinish("onPositiveButtonClicked_DIALOG_PROMPT_VERSION_UPDATE");
            }
        } else {
            view.showPromptDialog(R.string.dialog_prompt_get_version_error, true, false, Constant.RequestCode.DIALOG_PROMPT_GET_VERSION_ERROR);
        }
    }

    @Override
    public void download() {
        if (NetworkUtil.getInstance().isInternetConnecting(context)) {
            try {
                if (version != null) {
                    File file = new File(IOUtil.getInstance().getExternalStorageDirectory(Constant.FilePath.APK_CACHE), Constant.APK_NAME);
                    LogUtil.getInstance().print(String.format("file:%s", file.getAbsolutePath()));
                    String url = version.getDownloadUrl();
                    if (file.exists() && file.delete() && file.createNewFile()) {
                        if (!TextUtils.isEmpty(url)) {
                            view.showDownloadPromptDialog(url, file);
                        } else {
                            view.showPromptDialog(R.string.dialog_prompt_download_error, true, false, Constant.RequestCode.DIALOG_PROMPT_DOWNLOAD_ERROR);
                        }
                    } else {
                        if (!TextUtils.isEmpty(url)) {
                            view.showDownloadPromptDialog(url, file);
                        } else {
                            view.showPromptDialog(R.string.dialog_prompt_download_error, true, false, Constant.RequestCode.DIALOG_PROMPT_DOWNLOAD_ERROR);
                        }
                    }
                } else {
                    view.showPromptDialog(R.string.dialog_prompt_download_error, true, false, Constant.RequestCode.DIALOG_PROMPT_DOWNLOAD_ERROR);
                }
            } catch (IOException e) {
                e.printStackTrace();
                view.showPromptDialog(e.getMessage(), true, false, Constant.RequestCode.DIALOG_PROMPT_DOWNLOAD_ERROR);
            }
        } else {
            view.showNetWorkPromptDialog();
        }
    }
}
