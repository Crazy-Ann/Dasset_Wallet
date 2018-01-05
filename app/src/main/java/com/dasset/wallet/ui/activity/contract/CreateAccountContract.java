package com.dasset.wallet.ui.activity.contract;

import android.os.Bundle;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;

public interface CreateAccountContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void startCreateAccountResultActivity(Bundle bundle);

    }

    interface Presenter extends BasePresenter {

        void createAccount(boolean compressed, String accountName, String password);
    }
}
