package com.dasset.wallet.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dasset.wallet.R;
import com.dasset.wallet.base.toolbar.listener.OnLeftIconEventListener;
import com.dasset.wallet.base.toolbar.listener.OnRightTextEventListener;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.WalletInfoContract;
import com.dasset.wallet.ui.activity.presenter.WalletInfoPresenter;
import com.dasset.wallet.ui.dialog.PromptDialog;

import java.util.List;

public class WalletInfoActivity extends ActivityViewImplement<WalletInfoContract.Presenter> implements WalletInfoContract.View, View.OnClickListener, OnLeftIconEventListener, OnRightTextEventListener {

    private WalletInfoPresenter walletInfoPresenter;
    private TextView tvWalletName1;
    private TextView tvWalleAddress;
    private TextView tvWalletName2;
    private Button btnBackupsMnemonicCode;
    private Button btnDeleteWallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_info);
        getSavedInstanceState(savedInstanceState);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            walletInfoPresenter.checkPermission(new PermissionCallback() {

                @Override
                public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                    //TODO
                }

                @Override
                public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                    showPermissionPromptDialog();
                }
            });
        } else {
            //TODO
        }
    }

    @Override
    protected void findViewById() {
        inToolbar = ViewUtil.getInstance().findView(this, R.id.inToolbar);
        tvWalletName1 = ViewUtil.getInstance().findView(this, R.id.tvWalletName1);
        tvWalleAddress = ViewUtil.getInstance().findView(this, R.id.tvWalleAddress);
        tvWalletName2 = ViewUtil.getInstance().findView(this, R.id.tvWalletName2);
        btnBackupsMnemonicCode = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.btnBackupsMnemonicCode, this);
        btnDeleteWallet = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.btnDeleteWallet, this);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        walletInfoPresenter = new WalletInfoPresenter(this, this);
        walletInfoPresenter.initialize();
        initializeToolbar(android.R.color.white, true, R.mipmap.icon_back_black, this, android.R.color.black, false, walletInfoPresenter.getWalletInfo().getWalletName(), null, true, android.R.color.black, getString(R.string.toolbar_save), this);
        setBasePresenterImplement(walletInfoPresenter);

        if (walletInfoPresenter.getWalletInfo() != null && walletInfoPresenter.getWalletInfo().getHdAccount() != null) {
            tvWalletName1.setText(walletInfoPresenter.getWalletInfo().getWalletName());
            tvWalleAddress.setText(walletInfoPresenter.getWalletInfo().getHdAccount().getFirstAddressFromDb());
            tvWalletName2.setText(walletInfoPresenter.getWalletInfo().getWalletName());
        } else {
            tvWalletName1.setText(getString(R.string.dialog_prompt_account_info_error));
            tvWalleAddress.setText(R.string.dialog_prompt_account_info_error);
            tvWalletName2.setText(getString(R.string.dialog_prompt_account_info_error));
        }
    }

    @Override
    protected void setListener() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBackupsMnemonicCode:
                Bundle bundle = new Bundle();
                bundle.putParcelable(Constant.BundleKey.WALLET_INFO, walletInfoPresenter.getWalletInfo());
                startActivity(BackupsMnemonicCodeActivity.class, bundle);
                break;
            case R.id.btnDeleteWallet:
                PromptDialog.createBuilder(getSupportFragmentManager())
                        .setTitle(getString(R.string.dialog_prompt))
                        .setPrompt(getString(R.string.dialog_prompt_delete_wallet))
                        .setPositiveButtonText(this, R.string.confirm)
                        .setNegativeButtonText(this, R.string.cancel)
                        .setCancelable(false)
                        .setCancelableOnTouchOutside(false)
                        .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_DELETE_WALLET)
                        .showAllowingStateLoss(this);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.RequestCode.NET_WORK_SETTING:
            case Constant.RequestCode.PREMISSION_SETTING:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    walletInfoPresenter.checkPermission(new PermissionCallback() {

                        @Override
                        public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                            //TODO
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                            showPermissionPromptDialog();
                        }
                    });
                } else {
                    //TODO
                }
            default:
                break;
        }
    }

    @Override
    public void onPositiveButtonClicked(int requestCode) {
        switch (requestCode) {
            case Constant.RequestCode.DIALOG_PROMPT_SET_NET_WORK:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_NET_WORK_ERROR");
                startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), Constant.RequestCode.NET_WORK_SETTING);
                break;
            case Constant.RequestCode.DIALOG_PROMPT_SET_PERMISSION:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_SET_PERMISSION");
                startPermissionSettingActivity();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_DELETE_WALLET:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_DELETE_WALLET");
                walletInfoPresenter.deleteWallet();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNegativeButtonClicked(int requestCode) {
        switch (requestCode) {
            case Constant.RequestCode.DIALOG_PROMPT_SET_NET_WORK:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_SET_NET_WORK");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_SET_PERMISSION:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_SET_PERMISSION");
                refusePermissionSetting();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_DELETE_WALLET:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_DELETE_WALLET");
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void onLeftIconEvent() {
        onFinish("onLeftIconEvent");
    }

    @Override
    public void onRightTextEvent() {
        //TODO
    }
}
