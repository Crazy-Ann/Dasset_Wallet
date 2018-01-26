package com.dasset.wallet.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dasset.wallet.R;
import com.dasset.wallet.base.toolbar.listener.OnRightTextEventListener;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.GlideUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.components.zxing.encode.QRCodeEncode;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.CreateWalletResultContract;
import com.dasset.wallet.ui.activity.presenter.CreateWalletResultPresenter;
import com.dasset.wallet.ui.dialog.PromptDialog;

import java.util.List;

public class CreateWalletResultActivity extends ActivityViewImplement<CreateWalletResultContract.Presenter> implements CreateWalletResultContract.View, View.OnClickListener, OnRightTextEventListener {

    private CreateWalletResultPresenter createWalletResultPresenter;

    private TextView tvAccountNumber;
    private TextView tvAddress;
    private TextView tvAddressQRCode;
    private ImageView ivAddressQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_result);
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
        tvAccountNumber = ViewUtil.getInstance().findView(this, R.id.tvAccountName);
        tvAddress = ViewUtil.getInstance().findView(this, R.id.tvAddress);
        tvAddressQRCode = ViewUtil.getInstance().findView(this, R.id.tvAddressQRCode);
        ivAddressQRCode = ViewUtil.getInstance().findView(this, R.id.ivAddressQRCode);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        initializeToolbar(R.color.color_383856, android.R.color.white, getString(R.string.create_wallet), android.R.color.white, getString(R.string.done), this);
        createWalletResultPresenter = new CreateWalletResultPresenter(this, this);
        createWalletResultPresenter.initialize();
        setBasePresenterImplement(createWalletResultPresenter);

        if (createWalletResultPresenter.getAccount() != null && !TextUtils.isEmpty(createWalletResultPresenter.getAccount().getAccountName()) && !TextUtils.isEmpty(createWalletResultPresenter.getAccount().getAddress2())) {
            tvAccountNumber.setText(String.format("账户%s", createWalletResultPresenter.getAccount().getAccountName()));
            tvAddressQRCode.setText(String.format("账户%s 二维码", createWalletResultPresenter.getAccount().getAccountName()));
            tvAddress.setText(createWalletResultPresenter.getAccount().getAddress2());
            GlideUtil.getInstance().with(this, QRCodeEncode.createQRCode(createWalletResultPresenter.getAccount().getAddress2(), ViewUtil.getInstance().dp2px(this, 160)), ViewUtil.getInstance().dp2px(this, 120), ViewUtil.getInstance().dp2px(this, 120), DiskCacheStrategy.NONE, ivAddressQRCode);
        } else {
            tvAccountNumber.setText(getString(R.string.dialog_prompt_account_info_error));
            tvAddressQRCode.setText(String.format("账户%s 二维码", Regex.NONE.getRegext()));
            GlideUtil.getInstance().with(this, R.mipmap.ic_launcher_round, ViewUtil.getInstance().dp2px(this, 120), ViewUtil.getInstance().dp2px(this, 120), DiskCacheStrategy.NONE, ivAddressQRCode);
            tvAddress.setText(R.string.dialog_prompt_account_info_error);
        }
    }

    @Override
    protected void setListener() {

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
//                    createWalletResultPresenter.deleteBackupsAccount();
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
            case Constant.RequestCode.DIALOG_PROGRESS_WALLET:
                break;
            case Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_EXPORT_ACCOUNT");
                //TODO
                createWalletResultPresenter.exportAccount(createWalletResultPresenter.getAccount().getPassword());
                break;
            case Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT_SUCCESS:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_EXPORT_ACCOUNT_SUCCESS");
                setResult(Constant.ResultCode.CREATE_ACCOUNT);
                onFinish("onPositiveButtonClicked_DIALOG_PROMPT_EXPORT_ACCOUNT_SUCCESS");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT_FAILED:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_EXPORT_ACCOUNT_FAILED");
                break;
            default:
                break;
        }
    }

    @Override
    public void onNegativeButtonClicked(int requestCode) {
        switch (requestCode) {
            case Constant.RequestCode.DIALOG_PROGRESS_WALLET:
                break;
            case Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_CREATE_ACCOUNT_BACKUPS");
                setResult(Constant.ResultCode.CREATE_ACCOUNT);
                onFinish("onNegativeButtonClicked_DIALOG_PROMPT_CREATE_ACCOUNT_BACKUPS");
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                break;
        }
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void onRightTextEvent() {
        PromptDialog.createBuilder(getSupportFragmentManager())
                .setTitle(getString(R.string.dialog_prompt))
                .setPrompt(getString(R.string.prompt_create_account4))
                .setPositiveButtonText(this, R.string.backups)
                .setNegativeButtonText(this, R.string.later)
                .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT)
                .showAllowingStateLoss(this);
    }
}
