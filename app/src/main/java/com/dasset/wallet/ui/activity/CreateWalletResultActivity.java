package com.dasset.wallet.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dasset.wallet.R;
import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.base.http.model.BaseEntity;
import com.dasset.wallet.base.toolbar.listener.OnLeftIconEventListener;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.model.WalletInfo;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.CreateWalletResultContract;
import com.dasset.wallet.ui.activity.presenter.CreateWalletResultPresenter;
import com.dasset.wallet.ui.dialog.ImagePromptDialog;
import com.dasset.wallet.ui.dialog.PromptDialog;

import java.util.List;

public class CreateWalletResultActivity extends ActivityViewImplement<CreateWalletResultContract.Presenter> implements CreateWalletResultContract.View, View.OnClickListener, OnLeftIconEventListener {

    private CreateWalletResultPresenter createWalletResultPresenter;

    private TextView tvWalletName;
    private TextView tvWalletAddress;
    private Button btnBackupsWallet;
    private Button btnHowToBackups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wallet_result);
        getSavedInstanceState(savedInstanceState);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            createWalletResultPresenter.checkPermission(new PermissionCallback() {
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
        tvWalletName = ViewUtil.getInstance().findView(this, R.id.tvWalletName);
        tvWalletAddress = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.tvWalletAddress, this);
        btnBackupsWallet = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.btnBackupsWallet, this);
        btnHowToBackups = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.btnHowToBackups, this);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        initializeToolbar(android.R.color.white, true, R.mipmap.icon_close_black, this, android.R.color.black, getString(R.string.create_wallet));
        createWalletResultPresenter = new CreateWalletResultPresenter(this, this);
        createWalletResultPresenter.initialize();
        setBasePresenterImplement(createWalletResultPresenter);

        if (BaseApplication.getInstance().getWalletInfo() != null && ((WalletInfo) BaseApplication.getInstance().getWalletInfo()).getHdAccount() != null) {
            tvWalletName.setText(((WalletInfo) BaseApplication.getInstance().getWalletInfo()).getWalletName());
            tvWalletAddress.setText(((WalletInfo) BaseApplication.getInstance().getWalletInfo()).getHdAccount().getFirstAddressFromDb());
        } else {
            tvWalletName.setText(getString(R.string.dialog_prompt_account_info_error));
            tvWalletAddress.setText(R.string.dialog_prompt_account_info_error);
        }
    }

    @Override
    protected void setListener() {

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.RequestCode.NET_WORK_SETTING:
            case Constant.RequestCode.PREMISSION_SETTING:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    createWalletResultPresenter.checkPermission(new PermissionCallback() {

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
                break;
            case Constant.RequestCode.EXPORT_ACCOUNT:
                if (data != null) {
                    //todo
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        PromptDialog.createBuilder(getSupportFragmentManager())
                .setTitle(getString(R.string.dialog_prompt))
                .setPrompt(getString(R.string.prompt_create_account4))
                .setPositiveButtonText(this, R.string.backups)
                .setNegativeButtonText(this, R.string.later)
                .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT)
                .showAllowingStateLoss(this);
    }

    @Override
    public void onPositiveButtonClicked(int requestCode) {
        switch (requestCode) {
            case Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_EXPORT_ACCOUNT");
                //TODO
                break;
            case Constant.RequestCode.DIALOG_PROMPT_SAVE_QRCODE_SUCCESS:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_QRCODE_SAVE_SUCCESS");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_SAVE_QRCODE_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_QRCODE_SAVE_ERROR");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_SHARE_QRCODE_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_QRCODE_SHARE_ERROR");
                break;
            default:
                break;
        }
    }

    @Override
    public void onNegativeButtonClicked(int requestCode) {
        switch (requestCode) {
            case Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_EXPORT_ACCOUNT");
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvWalletAddress:
                createWalletResultPresenter.generateAddresQRCode();
                break;
            case R.id.btnBackupsWallet:
                startActivity(WalletInfoActivity.class);
                break;
            case R.id.btnHowToBackups:
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
    public void showAddressQRCodePromptDialog(byte[] data, String prompt) {
        ImagePromptDialog.createBuilder(getSupportFragmentManager())
                .setTitle(getString(R.string.dialog_prompt))
                .setImage(data)
                .setPrompt(prompt)
                .setPositiveButtonText(this, R.string.dialog_prompt_share)
                .setNegativeButtonText(this, R.string.dialog_prompt_save)
                .setCancelable(true)
                .setCancelableOnTouchOutside(false)
                .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_EXPORT_QRCODE)
                .showAllowingStateLoss(this);
    }

    @Override
    public void onLeftIconEvent() {
        PromptDialog.createBuilder(getSupportFragmentManager())
                .setTitle(getString(R.string.dialog_prompt))
                .setPrompt(getString(R.string.prompt_create_account4))
                .setPositiveButtonText(this, R.string.backups)
                .setNegativeButtonText(this, R.string.later)
                .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT)
                .showAllowingStateLoss(this);
    }
}
