package com.dasset.wallet.ui.activity;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dasset.wallet.R;
import com.dasset.wallet.base.toolbar.listener.OnLeftIconEventListener;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.ConfirmBackupsMnemonicCodeContract;
import com.dasset.wallet.ui.activity.presenter.ConfirmBackupsMnemonicCodePresenter;

public class ConfirmBackupsMnemonicCodeActivity extends ActivityViewImplement<ConfirmBackupsMnemonicCodeContract.Presenter> implements ConfirmBackupsMnemonicCodeContract.View, OnLeftIconEventListener, View.OnClickListener {

    private ConfirmBackupsMnemonicCodePresenter confirmBackupsMnemonicCodePresenter;

    private TextView tvBackupsMnemonicCode1;
    private TextView tvBackupsMnemonicCode2;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_backups_mnemonic_code);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void findViewById() {
        inToolbar = ViewUtil.getInstance().findView(this, R.id.inToolbar);
        tvBackupsMnemonicCode1 = ViewUtil.getInstance().findView(this, R.id.tvBackupsMnemonicCode1);
        tvBackupsMnemonicCode2 = ViewUtil.getInstance().findView(this, R.id.tvBackupsMnemonicCode2);
        btnNext = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.btnNext, this);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        initializeToolbar(android.R.color.white, true, R.mipmap.icon_back_black, this, android.R.color.black, getString(R.string.backups_mnemonic_code));
        confirmBackupsMnemonicCodePresenter = new ConfirmBackupsMnemonicCodePresenter(this, this);
        confirmBackupsMnemonicCodePresenter.initialize();
        setBasePresenterImplement(confirmBackupsMnemonicCodePresenter);

        if (confirmBackupsMnemonicCodePresenter.getWalletInfo() != null && confirmBackupsMnemonicCodePresenter.getWalletInfo().getMnemonicCodes() != null) {
            StringBuffer stringBuffer = new StringBuffer();
            for (String mnemonicCodes : confirmBackupsMnemonicCodePresenter.getWalletInfo().getMnemonicCodes()) {
                stringBuffer.append(String.format("%s  ", mnemonicCodes));
            }
            tvBackupsMnemonicCode2.setText(stringBuffer.toString());
            LogUtil.getInstance().print(String.format("mnemonicCodes:%s" ,stringBuffer.toString()));
        }
    }

    @Override
    protected void setListener() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnNext:
                Bundle bundle = new Bundle();
                bundle.putParcelable(Constant.BundleKey.WALLET_INFO, confirmBackupsMnemonicCodePresenter.getWalletInfo());
                startActivity(ConfirmBackupsMnemonicCodeActivity.class, bundle);
                break;
            default:
                break;
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
