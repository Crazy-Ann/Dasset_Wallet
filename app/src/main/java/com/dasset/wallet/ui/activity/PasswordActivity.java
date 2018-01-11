package com.dasset.wallet.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dasset.wallet.R;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.GlideUtil;
import com.dasset.wallet.components.utils.InputUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.components.validation.EditTextValidator;
import com.dasset.wallet.components.validation.Validation;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.model.validation.TransactionPasswordValidation;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.PasswordContract;
import com.dasset.wallet.ui.activity.presenter.PasswordPresenter;

import java.util.List;

public class PasswordActivity extends ActivityViewImplement<PasswordContract.Presenter> implements PasswordContract.View, View.OnClickListener {

    private PasswordPresenter passwordPresenter;

    private EditText etTransactionPassword;
    private ImageButton ibTransactionPasswordDisplay;
    private ImageButton ibTransactionPasswordEmpty;
    private Button btnImport;

    private EditTextValidator editTextValidator;
    private boolean isTransactionPasswordHidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        getSavedInstanceState(savedInstanceState);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            passwordPresenter.checkPermission(new PermissionCallback() {

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
        etTransactionPassword = ViewUtil.getInstance().findView(this, R.id.etTransactionPassword);
        ibTransactionPasswordDisplay = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.ibTransactionPasswordDisplay, this);
        ibTransactionPasswordEmpty = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.ibTransactionPasswordEmpty, this);
        btnImport = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.btnImport, this);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        passwordPresenter = new PasswordPresenter(this, this);
        passwordPresenter.initialize();
        setBasePresenterImplement(passwordPresenter);

        editTextValidator = new EditTextValidator();
        editTextValidator.add(new Validation(null, etTransactionPassword, true, ibTransactionPasswordEmpty, new TransactionPasswordValidation()));
        editTextValidator.execute(this, btnImport, R.drawable.rectangle_b7b7fa, R.drawable.rectangle_5757ff, R.color.color_d1d1fb, android.R.color.white, null, null, false);
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
            case R.id.btnImport:
                if (editTextValidator.validate(this)) {
                    Intent intent = new Intent();
                    intent.putExtra(Constant.BundleKey.IMPORT_PASSWORD, etTransactionPassword.getText().toString().trim());
                    intent.putExtra(Constant.BundleKey.IMPORT_FILE_PATH, passwordPresenter.getPath());
                    setResult(Constant.ResultCode.PASSWORD_VERIFICATION, intent);
                    onFinish("btnImport");
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
                    passwordPresenter.checkPermission(new PermissionCallback() {
                        @Override
                        public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                            //TODO
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                            showPermissionPromptDialog();
                        }
                    });
                }
                break;
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
            default:
                break;
        }
    }

    @Override
    public boolean isActive() {
        return false;
    }

}
