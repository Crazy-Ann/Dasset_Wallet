package com.dasset.wallet.ui.activity;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dasset.wallet.R;
import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.base.http.model.BaseEntity;
import com.dasset.wallet.base.toolbar.listener.OnLeftIconEventListener;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.model.WalletInfo;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.BackupsMnemonicCodeContract;
import com.dasset.wallet.ui.activity.presenter.BackupsMnemonicCodePresenter;

public class BackupsMnemonicCodeActivity extends ActivityViewImplement<BackupsMnemonicCodeContract.Presenter> implements BackupsMnemonicCodeContract.View, OnLeftIconEventListener, View.OnClickListener {

    private BackupsMnemonicCodePresenter backupsMnemonicCodePresenter;

    private TextView tvBackupsMnemonicCode;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backups_mnemonic_code);
        getSavedInstanceState(savedInstanceState);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void findViewById() {
        inToolbar = ViewUtil.getInstance().findView(this, R.id.inToolbar);
        tvBackupsMnemonicCode = ViewUtil.getInstance().findView(this, R.id.tvBackupsMnemonicCode);
        btnNext = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.btnNext, this);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        initializeToolbar(android.R.color.white, true, R.mipmap.icon_back_black, this, android.R.color.black, getString(R.string.backups_mnemonic_code));
        backupsMnemonicCodePresenter = new BackupsMnemonicCodePresenter(this, this);
        backupsMnemonicCodePresenter.initialize();
        setBasePresenterImplement(backupsMnemonicCodePresenter);

        if (BaseApplication.getInstance().getWalletInfo() != null && ((WalletInfo) BaseApplication.getInstance().getWalletInfo()).getMnemonicCodes() != null) {
            StringBuffer stringBuffer = new StringBuffer();
            for (String mnemonicCodes : ((WalletInfo) BaseApplication.getInstance().getWalletInfo()).getMnemonicCodes()) {
                stringBuffer.append(String.format("%s  ", mnemonicCodes));
            }
            tvBackupsMnemonicCode.setText(stringBuffer.toString());
            LogUtil.getInstance().print(String.format("mnemonicCodes:%s", stringBuffer.toString()));
        }
    }

    @Override
    protected void setListener() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnNext:
                startActivity(ConfirmBackupsMnemonicCodeActivity.class);
                break;
            default:
                break;
        }
    }

    @Override
    protected void getSavedInstanceState(Bundle savedInstanceState) {
        super.getSavedInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            BaseApplication.getInstance().setWalletInfo((BaseEntity) savedInstanceState.getParcelable(Constant.BundleKey.WALLET_INFO));
        }
    }

    @Override
    protected void setSavedInstanceState(Bundle savedInstanceState) {
        super.setSavedInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            savedInstanceState.putParcelable(Constant.BundleKey.WALLET_INFO, BaseApplication.getInstance().getWalletInfo());
        }
    }

    @Override
    public void onLeftIconEvent() {

    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void onPositiveButtonClicked(int requestCode) {

    }

    @Override
    public void onNegativeButtonClicked(int requestCode) {

    }
}
