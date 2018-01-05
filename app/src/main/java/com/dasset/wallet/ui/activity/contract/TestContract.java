package com.dasset.wallet.ui.activity.contract;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;
import com.dasset.wallet.model.Test;

public interface TestContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void showTestData(Test eccTest);
    }

    interface Presenter extends BasePresenter {

        void getSecurityProviders();

        void test(String data);
    }
}
