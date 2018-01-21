package com.dasset.wallet.ui.activity.contract;

import android.os.Bundle;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;
import com.dasset.wallet.core.crypto.mnemonic.listener.OnMnemonicDictionaryResourcelistener;

import java.io.File;

public interface WelcomeContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void showVersionUpdatePromptDialog(String prompt);

        void showDownloadPromptDialog(String url, File file);

        void startMainActivity();

        void startSplashActivity(Bundle bundle);
    }

    interface Presenter extends BasePresenter {

        void getVersion();

        void checkForceUpdate();

        void checkPageSignature();

        void download();
    }
}
