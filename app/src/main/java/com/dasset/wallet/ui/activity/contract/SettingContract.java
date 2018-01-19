package com.dasset.wallet.ui.activity.contract;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;

import java.io.File;

public interface SettingContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void showVersionUpdatePromptDialog(String prompt);

        void showDownloadPromptDialog(String url, File file);

        void showAbountPromptDialog(String prompt);
    }

    interface Presenter extends BasePresenter {

        void getVersion();

        void checkForceUpdate();

        void download();
    }
}
