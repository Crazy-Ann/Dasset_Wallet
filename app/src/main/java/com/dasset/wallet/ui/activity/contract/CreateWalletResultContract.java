package com.dasset.wallet.ui.activity.contract;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;

public interface CreateWalletResultContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void showAddressQRCodePromptDialog(byte[] data, String prompt);
        
    }

    interface Presenter extends BasePresenter {

        void generateAddresQRCode();

        void save();

        void share();

        void backupsWallet();
    }
}
