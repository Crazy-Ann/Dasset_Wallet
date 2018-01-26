package com.dasset.wallet.ui.activity.contract;

import android.os.Bundle;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;

public interface GenerateWalletContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void startPasswordActivity(Bundle bundle);

    }

    interface Presenter extends BasePresenter {

    }
}
