package com.dasset.wallet.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dasset.wallet.R;
import com.dasset.wallet.base.toolbar.listener.OnLeftIconEventListener;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.GlideUtil;
import com.dasset.wallet.components.utils.InputUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ToastUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.components.validation.EditTextValidator;
import com.dasset.wallet.components.validation.Validation;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.model.validation.AccountNameValidation;
import com.dasset.wallet.model.validation.TransactionPasswordValidation;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.CreateAccountContract;
import com.dasset.wallet.ui.activity.presenter.CreateAccountPresenter;
import com.dasset.wallet.ui.dialog.PromptDialog;

import java.util.List;

public class CreateAccountActivity extends ActivityViewImplement<CreateAccountContract.Presenter> implements CreateAccountContract.View, View.OnClickListener, OnLeftIconEventListener {

    private CreateAccountPresenter createAccountPresenter;

    private EditText    etAccountName;
    private ImageButton ibAccountNameEmpty;
    private EditText    etTransactionPassword;
    private ImageButton ibTransactionPasswordDisplay;
    private ImageButton ibTransactionPasswordEmpty;
    private EditText    etConfirmPassword;
    private ImageButton ibConfirmPasswordDisplay;
    private ImageButton ibConfirmPasswordEmpty;
    private Button      btnSubmit;

    private EditTextValidator editTextValidator;
    private boolean           isTransactionPasswordHidden;
    private boolean           isConfirmPasswordHidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            createAccountPresenter.checkPermission(new PermissionCallback() {
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
        etAccountName = ViewUtil.getInstance().findView(this, R.id.etAccountName);
        ibAccountNameEmpty = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.ibAccountNameEmpty, this);
        etTransactionPassword = ViewUtil.getInstance().findView(this, R.id.etTransactionPassword);
        ibTransactionPasswordDisplay = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.ibTransactionPasswordDisplay, this);
        ibTransactionPasswordEmpty = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.ibTransactionPasswordEmpty, this);
        etConfirmPassword = ViewUtil.getInstance().findView(this, R.id.etConfirmPassword);
        ibConfirmPasswordDisplay = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.ibConfirmPasswordDisplay, this);
        ibConfirmPasswordEmpty = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.ibConfirmPasswordEmpty, this);
        btnSubmit = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.btnSubmit, this);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        initializeToolbar(android.R.color.white, true, R.mipmap.icon_back_black, this, android.R.color.black, getString(R.string.create_account));
        createAccountPresenter = new CreateAccountPresenter(this, this);
        createAccountPresenter.initialize();
        setBasePresenterImplement(createAccountPresenter);

        editTextValidator = new EditTextValidator();
        editTextValidator.add(new Validation(null, etAccountName, true, ibAccountNameEmpty, new AccountNameValidation()));
        editTextValidator.add(new Validation(null, etTransactionPassword, true, ibTransactionPasswordEmpty, new TransactionPasswordValidation()));
        editTextValidator.add(new Validation(null, etConfirmPassword, true, ibConfirmPasswordEmpty, new TransactionPasswordValidation()));
        editTextValidator.execute(this, btnSubmit, R.drawable.rectangle_b7b7fa, R.drawable.rectangle_5757ff, R.color.color_d1d1fb, android.R.color.white, null, null, false);
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
            case R.id.ibAccountNameEmpty:
                etAccountName.setText(null);
                break;
            case R.id.ibTransactionPasswordDisplay:
                if (isTransactionPasswordHidden) {
                    etTransactionPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    GlideUtil.getInstance().with(this, R.mipmap.icon_eye_on, null, null, DiskCacheStrategy.NONE, ibTransactionPasswordDisplay);
                } else {
                    etTransactionPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    GlideUtil.getInstance().with(this, R.mipmap.icon_eye_off, null, null, DiskCacheStrategy.NONE, ibTransactionPasswordDisplay);
                }
                isTransactionPasswordHidden = !isTransactionPasswordHidden;
                break;
            case R.id.ibTransactionPasswordEmpty:
                etTransactionPassword.setText(null);
                break;
            case R.id.ibConfirmPasswordDisplay:
                if (isConfirmPasswordHidden) {
                    etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    GlideUtil.getInstance().with(this, R.mipmap.icon_eye_on, null, null, DiskCacheStrategy.NONE, ibTransactionPasswordDisplay);
                } else {
                    etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    GlideUtil.getInstance().with(this, R.mipmap.icon_eye_off, null, null, DiskCacheStrategy.NONE, ibTransactionPasswordDisplay);
                }
                isConfirmPasswordHidden = !isConfirmPasswordHidden;
                break;
            case R.id.ibConfirmPasswordEmpty:
                etConfirmPassword.setText(null);
                break;
            case R.id.btnSubmit:
                if (TextUtils.equals(etConfirmPassword.getText(), etTransactionPassword.getText())) {
                    if (editTextValidator.validate(this)) {
                        PromptDialog.createBuilder(getSupportFragmentManager())
                                .setTitle(getString(R.string.dialog_prompt))
                                .setPrompt(getString(R.string.prompt_create_account3))
                                .setPositiveButtonText(this, R.string.confirm)
                                .setNegativeButtonText(this, R.string.cancel)
                                .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_CREATE_ACCOUNT)
                                .showAllowingStateLoss(this);
                    }
                } else {
                    ToastUtil.getInstance().showToast(this, getString(R.string.prompt_transaction_password2), Toast.LENGTH_SHORT);
                }
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
                    createAccountPresenter.checkPermission(new PermissionCallback() {

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
            case Constant.RequestCode.CREATE_ACCOUNT:
                switch (resultCode) {
                    case Constant.ResultCode.CREATE_ACCOUNT:
                        LogUtil.getInstance().print("create account without backup!");
                        onFinish("startCreateAccountResultActivity");
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPositiveButtonClicked(int requestCode) {
        switch (requestCode) {
            case Constant.RequestCode.DIALOG_PROGRESS_WALLET:
                break;
            case Constant.RequestCode.DIALOG_PROMPT_CREATE_ACCOUNT:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_CREATE_ACCOUNT");
                //TODO
                createAccountPresenter.createAccount(false, etAccountName.getText().toString(), etConfirmPassword.getText().toString());
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
            case Constant.RequestCode.DIALOG_PROMPT_CREATE_ACCOUNT:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_CREATE_ACCOUNT");
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
    public void startCreateAccountResultActivity(Bundle bundle) {
        startActivityForResult(CreateAccountResultActivity.class, Constant.RequestCode.CREATE_ACCOUNT, bundle);
    }

    @Override
    public void onLeftIconEvent() {
        onFinish("onLeftIconEvent");
    }
}
