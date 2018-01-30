package com.dasset.wallet.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.dasset.wallet.R;
import com.dasset.wallet.base.toolbar.listener.OnLeftIconEventListener;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.InputUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.components.validation.EditTextValidator;
import com.dasset.wallet.components.validation.Validation;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.model.validation.WalletNameValidation;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.GenerateWalletContract;
import com.dasset.wallet.ui.activity.presenter.GenerateWalletPresenter;

import java.util.List;

public class GenerateWalletActivity extends ActivityViewImplement<GenerateWalletContract.Presenter> implements GenerateWalletContract.View, View.OnClickListener, OnLeftIconEventListener {

    private GenerateWalletPresenter generateWalletPresenter;
    private EditText etWalletName;
    private ImageButton ibWalletNameEmpty;
    private Button btnCreateWallet;
    private Button btnImportWallet;

    private EditTextValidator editTextValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_wallet);
        getSavedInstanceState(savedInstanceState);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            generateWalletPresenter.checkPermission(new PermissionCallback() {
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
        etWalletName = ViewUtil.getInstance().findView(this, R.id.etWalletName);
        ibWalletNameEmpty = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.ibWalletNameEmpty, this);
        btnCreateWallet = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.btnCreateWallet, this);
        btnImportWallet = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.btnImportWallet, this);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        initializeToolbar(android.R.color.white, true, R.mipmap.icon_close_black, this, android.R.color.black, getString(R.string.create_wallet));
        generateWalletPresenter = new GenerateWalletPresenter(this, this);
        generateWalletPresenter.initialize();
        setBasePresenterImplement(generateWalletPresenter);

        editTextValidator = new EditTextValidator();
        editTextValidator.add(new Validation(null, etWalletName, true, ibWalletNameEmpty, new WalletNameValidation()));
        editTextValidator.execute(this, btnCreateWallet, R.drawable.rectangle_b7b7fa, R.drawable.rectangle_5757ff, R.color.color_d1d1fb, android.R.color.white, null, null, false);
    }

    @Override
    protected void setListener() {

    }

    @Override
    public void onClick(View view) {
        if (InputUtil.getInstance().isDoubleClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.ibWalletNameEmpty:
                etWalletName.setText(null);
                break;
            case R.id.btnCreateWallet:
                if (editTextValidator.validate(this)) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constant.BundleKey.WALLET_OPERATION, Constant.BundleValue.GENERATE_WALLET);
                    bundle.putString(Constant.BundleKey.WALLET_NAME, etWalletName.getText().toString().trim());
                    startPasswordActivity(bundle);
                }
                break;
            case R.id.btnImportWallet:
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
                    generateWalletPresenter.checkPermission(new PermissionCallback() {

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
            default:
                break;
        }
    }

    @Override
    public void onPositiveButtonClicked(int requestCode) {
        switch (requestCode) {
            default:
                break;
        }
    }

    @Override
    public void onNegativeButtonClicked(int requestCode) {
        switch (requestCode) {
            default:
                break;
        }
    }


    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void startPasswordActivity(Bundle bundle) {
        startActivityForResult(PasswordActivity.class, Constant.RequestCode.CREATE_WALLET, bundle);
    }

    @Override
    public void onLeftIconEvent() {
        onFinish("onLeftIconEvent");
    }
}
