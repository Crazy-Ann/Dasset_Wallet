package com.dasset.wallet.ui.activity.contract;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;
import com.dasset.wallet.model.TransactionRecords;

public interface AccountRenameContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();


    }

    interface Presenter extends BasePresenter {

        void renameAccount(String accountName);
    }
}
