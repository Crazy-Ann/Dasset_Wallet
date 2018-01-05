package com.dasset.wallet.ui.activity.contract;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;

public interface SplashContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void startMainActivity();
    }

    interface Presenter extends BasePresenter {

    }
}
