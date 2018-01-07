package com.dasset.wallet.ui.activity.contract;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;
import com.dasset.wallet.model.TransactionRecords;

public interface AccountInfoContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void setSwipeRefreshLayout(boolean isRefresh);

        void showDeleteAccountPromptDialog();


    }

    interface Presenter extends BasePresenter {

        TransactionRecords getTransactionRecords();

        void exportAccount();

        void renameAccount();

        void deleteAccount();
    }
}
