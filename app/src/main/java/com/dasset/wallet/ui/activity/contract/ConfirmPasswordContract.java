package com.dasset.wallet.ui.activity.contract;

import android.os.Bundle;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;

public interface ConfirmPasswordContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void startGenerateWalletResultActivity(Bundle bundle);
    }

    interface Presenter extends BasePresenter {

        void createWallet(boolean compressed, String password);
    }
}
