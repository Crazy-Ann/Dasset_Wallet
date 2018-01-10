package com.dasset.wallet.ui.activity.contract;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;

public interface CreateAccountResultContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

    }

    interface Presenter extends BasePresenter {

        void exportAccount(String password);

        void deleteBackupsAccount();

    }
}
