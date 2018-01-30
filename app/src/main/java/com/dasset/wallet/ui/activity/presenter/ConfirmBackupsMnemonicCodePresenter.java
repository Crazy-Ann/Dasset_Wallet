package com.dasset.wallet.ui.activity.presenter;

import android.content.Context;

import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.contract.ConfirmBackupsMnemonicCodeContract;

public class ConfirmBackupsMnemonicCodePresenter extends BasePresenterImplement implements ConfirmBackupsMnemonicCodeContract.Presenter {

    private ConfirmBackupsMnemonicCodeContract.View view;

    public ConfirmBackupsMnemonicCodePresenter(Context context, ConfirmBackupsMnemonicCodeContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
    }
}
