package com.dasset.wallet.ui.activity.presenter;

import android.app.Activity;
import android.content.Context;

import com.dasset.wallet.components.utils.BundleUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.model.WalletInfo;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.contract.ConfirmBackupsMnemonicCodeContract;

public class ConfirmBackupsMnemonicCodePresenter extends BasePresenterImplement implements ConfirmBackupsMnemonicCodeContract.Presenter {

    private ConfirmBackupsMnemonicCodeContract.View view;
    
    private WalletInfo walletInfo;

    public WalletInfo getWalletInfo() {
        return walletInfo;
    }

    public ConfirmBackupsMnemonicCodePresenter(Context context, ConfirmBackupsMnemonicCodeContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        walletInfo = BundleUtil.getInstance().getParcelableData((Activity) view, Constant.BundleKey.WALLET_INFO);
    }
    

}
