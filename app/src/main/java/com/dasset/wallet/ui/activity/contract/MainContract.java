package com.dasset.wallet.ui.activity.contract;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;

public interface MainContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void setAddAccountView();

        void showImportAccountPromptDialog();
    }

    interface Presenter extends BasePresenter {

    }
}
