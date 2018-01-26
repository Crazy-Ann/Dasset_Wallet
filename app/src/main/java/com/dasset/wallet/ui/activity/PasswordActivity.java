package com.dasset.wallet.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.dasset.wallet.R;
import com.dasset.wallet.base.toolbar.listener.OnLeftIconEventListener;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.PasswordContract;
import com.dasset.wallet.ui.activity.presenter.PasswordPresenter;
import com.yjt.keyboard.DynamicKeyBoardView;
import com.yjt.keyboard.listener.OnKeyboardListener;
import com.yjt.password.PasswordView;
import com.yjt.password.constant.PasswordType;
import com.yjt.password.listener.OnPasswordChangedListener;

import java.util.List;

public class PasswordActivity extends ActivityViewImplement<PasswordContract.Presenter> implements PasswordContract.View, OnLeftIconEventListener, OnPasswordChangedListener, OnKeyboardListener {

    private PasswordPresenter passwordPresenter;
    private PasswordView pvPassowrd;
    private DynamicKeyBoardView dkbvPassword;

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
        dkbvPassword.shuffleKeyboard();
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
        inToolbar = ViewUtil.getInstance().findView(this, R.id.inToolbar);
        pvPassowrd = ViewUtil.getInstance().findView(this, R.id.pvPassowrd);
        dkbvPassword = ViewUtil.getInstance().findView(this, R.id.dkbvPassword);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        initializeToolbar(android.R.color.white, true, R.mipmap.icon_back_black, this, android.R.color.black, getString(R.string.transaction_password));
        passwordPresenter = new PasswordPresenter(this, this);
        passwordPresenter.initialize();
        pvPassowrd.setPasswordType(PasswordType.NUMBER);
        pvPassowrd.setHasAddTextChangedListener(false);
        setBasePresenterImplement(passwordPresenter);
    }

    @Override
    protected void setListener() {
        pvPassowrd.setOnPasswordChangedListener(this);
        dkbvPassword.setOnKeyboardListener(this);
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

    @Override
    public void startCreateWalletResultActivity() {
        startActivityForResult(CreateWalletResultActivity.class, Constant.RequestCode.CREATE_WALLET);
        onFinish("startCreateWalletResultActivity");
    }

    @Override
    public void onLeftIconEvent() {
        onFinish("onLeftIconEvent");
    }

    @Override
    public void onPasswordChange(String password) {
        LogUtil.getInstance().print("onPasswordChange:" + password);
    }

    @Override
    public void onInputFinish(String password) {
        //TODO
    }

    @Override
    public void onInsert(String data) {
        pvPassowrd.setPassword(pvPassowrd.getPassword() + data);
    }

    @Override
    public void onDelete() {
        pvPassowrd.onPasswordDelete();
    }
}
