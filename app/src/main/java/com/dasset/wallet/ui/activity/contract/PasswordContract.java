package com.dasset.wallet.ui.activity.contract;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;

public interface PasswordContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void startCreateWalletResultActivity();
    }

    interface Presenter extends BasePresenter {

        void createWallet(boolean compressed, String password);
    }
}
