package com.dasset.wallet.ui.activity.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;

import com.dasset.wallet.R;
import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.ApplicationUtil;
import com.dasset.wallet.components.utils.IOUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.NetworkUtil;
import com.dasset.wallet.components.utils.SharedPreferenceUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.core.contant.MnemonicDictionary;
import com.dasset.wallet.core.crypto.mnemonic.MnemonicCode;
import com.dasset.wallet.core.crypto.mnemonic.listener.OnMnemonicDictionaryResourcelistener;
import com.dasset.wallet.model.Version;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.WelcomeActivity;
import com.dasset.wallet.ui.activity.contract.WelcomeContract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class WelcomePresenter extends BasePresenterImplement implements WelcomeContract.Presenter {

    private WelcomeContract.View view;
    private Version              version;
    private String               path;

    public WelcomePresenter(Context context, WelcomeContract.View view) {
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
        view.startMainActivity();
//        WalletApi.getInstance().getVersion(context, view, SharedPreferenceUtil.getInstance().getString(context, Constant.Configuration.CONFIGURATION, Context.MODE_PRIVATE, Constant.Configuration.KEY1, Regex.NONE.getRegext()), new ApiResponse() {
//
//            @Override
//            public void success(BaseEntity baseEntity) {
//                version = (Version) baseEntity;
//                if (version != null) {
//                    BaseApplication.getInstance().setEncryptKey(version.getApk());
//                    if (ApplicationUtil.getInstance().getVersionCode(context) < Integer.valueOf(version.getClientVersion())) {
//                        view.showVersionUpdatePromptDialog(version.getUpdateMessage());
//                    } else {
//                        checkPageSignature();
//                    }
//                } else {
//                    view.showPromptDialog(R.string.dialog_prompt_get_version_error, true, false, Constant.RequestCode.DIALOG_PROMPT_GET_VERSION_ERROR);
//                }
//            }
//
//            @Override
//            public void failed(BaseEntity entity) {
//                
//            }
//        });
    }

    @Override
    public void checkForceUpdate() {
        if (version != null) {
            if (ApplicationUtil.getInstance().getVersionCode(context) < Integer.valueOf(version.getLowestClientVersion())) {
                ((WelcomeActivity) view).onFinish("onPositiveButtonClicked_DIALOG_PROMPT_VERSION_UPDATE");
            } else {
                checkPageSignature();
            }
        } else {
            view.showPromptDialog(R.string.dialog_prompt_get_version_error, true, false, Constant.RequestCode.DIALOG_PROMPT_GET_VERSION_ERROR);
        }
    }

    @Override
    public void checkPageSignature() {
        if (TextUtils.equals(version.getPageSrcSign(), SharedPreferenceUtil.getInstance().getString(context, Constant.Configuration.CONFIGURATION, Context.MODE_PRIVATE, Constant.Configuration.KEY1, Regex.NONE.getRegext()))) {
            view.startMainActivity();
        } else {
            if (version.getPagesList() != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(Constant.BundleKey.PAGE_LIST, (ArrayList<? extends Parcelable>) version.getPagesList());
                view.startSplashActivity(bundle);
            }
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
