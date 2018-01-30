package com.dasset.wallet.ui.activity.presenter;

import android.content.Context;

import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.contract.BackupsMnemonicCodeContract;

public class BackupsMnemonicCodePresenter extends BasePresenterImplement implements BackupsMnemonicCodeContract.Presenter {

    private BackupsMnemonicCodeContract.View view;

    public BackupsMnemonicCodePresenter(Context context, BackupsMnemonicCodeContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
    }
}
