package com.dasset.wallet.ui.activity.contract;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;

public interface WalletInfoContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();
        
    }

    interface Presenter extends BasePresenter {

        void deleteWallet();
    }
}
