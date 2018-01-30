package com.dasset.wallet.ui.activity;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import com.dasset.wallet.ui.activity.contract.ConfirmBackupsMnemonicCodeContract;
import com.dasset.wallet.ui.activity.presenter.ConfirmBackupsMnemonicCodePresenter;
import com.dasset.wallet.ui.dialog.PromptDialog;
import com.google.common.collect.Lists;
import com.yjt.tag.TagAdapter;
import com.yjt.tag.layout.FlowLayout;
import com.yjt.tag.layout.TagFlowLayout;
import com.yjt.tag.listener.OnTagSelectedPositionListener;
import com.yjt.tag.listener.OnTagUnselectedPositionListener;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ConfirmBackupsMnemonicCodeActivity extends ActivityViewImplement<ConfirmBackupsMnemonicCodeContract.Presenter> implements ConfirmBackupsMnemonicCodeContract.View, OnLeftIconEventListener, View.OnClickListener, OnTagSelectedPositionListener, OnTagUnselectedPositionListener {

    private ConfirmBackupsMnemonicCodePresenter confirmBackupsMnemonicCodePresenter;

    private TagFlowLayout tflBackupsMnemonicCode1;
    private TagFlowLayout tflBackupsMnemonicCode2;
    private TagAdapter<String> tagAdapter1;
    private TagAdapter<String> tagAdapter2;
    private Button btnConfirm;

    private List<String> unverifiedBackupsMnemonicCodes;
    private List<String> backupsMnemonicCodes;
    private List<String> shuffleBackupsMnemonicCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_backups_mnemonic_code);
        getSavedInstanceState(savedInstanceState);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            confirmBackupsMnemonicCodePresenter.checkPermission(new PermissionCallback() {

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
        tflBackupsMnemonicCode1 = ViewUtil.getInstance().findView(this, R.id.tflBackupsMnemonicCode1);
        tflBackupsMnemonicCode2 = ViewUtil.getInstance().findView(this, R.id.tflBackupsMnemonicCode2);
        btnConfirm = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.btnConfirm, this);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        initializeToolbar(android.R.color.white, true, R.mipmap.icon_back_black, this, android.R.color.black, getString(R.string.backups_mnemonic_code));
        confirmBackupsMnemonicCodePresenter = new ConfirmBackupsMnemonicCodePresenter(this, this);
        confirmBackupsMnemonicCodePresenter.initialize();
        setBasePresenterImplement(confirmBackupsMnemonicCodePresenter);

        if (BaseApplication.getInstance().getWalletInfo() != null && ((WalletInfo) BaseApplication.getInstance().getWalletInfo()).getMnemonicCodes() != null) {
            unverifiedBackupsMnemonicCodes = Lists.newArrayList();
            backupsMnemonicCodes = ((WalletInfo) BaseApplication.getInstance().getWalletInfo()).getMnemonicCodes();
            shuffleBackupsMnemonicCodes = ((WalletInfo) BaseApplication.getInstance().getWalletInfo()).getShuffleMnemonicCodes();
            tagAdapter2 = new TagAdapter<String>(shuffleBackupsMnemonicCodes) {
                @Override
                public View getView(FlowLayout parent, int position, String s) {
                    TextView textView = (TextView) LayoutInflater.from(ConfirmBackupsMnemonicCodeActivity.this).inflate(R.layout.view_tag2, tflBackupsMnemonicCode2, false);
                    textView.setText(s);
                    return textView;
                }
            };
            tflBackupsMnemonicCode2.setAdapter(tagAdapter2);
        }
    }

    @Override
    protected void setListener() {
        tagAdapter2.setOnTagSelectedPositionListener(this);
        tagAdapter2.setOnTagUnselectedPositionListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConfirm:
                for (String backupsMnemonicCode : backupsMnemonicCodes.toArray(new String[backupsMnemonicCodes.size()])) {
                    LogUtil.getInstance().print(String.format("backupsMnemonicCode:%s", backupsMnemonicCode));
                }
                for (String unverifiedBackupsMnemonicCode : unverifiedBackupsMnemonicCodes.toArray(new String[unverifiedBackupsMnemonicCodes.size()])) {
                    LogUtil.getInstance().print(String.format("unverifiedBackupsMnemonicCode:%s", unverifiedBackupsMnemonicCode));
                }
                if (Arrays.deepEquals(backupsMnemonicCodes.toArray(new String[backupsMnemonicCodes.size()]), unverifiedBackupsMnemonicCodes.toArray(new String[unverifiedBackupsMnemonicCodes.size()]))) {
                    startActivity(MainActivity.class);
                    onFinish("btnConfirm");
                } else {
                    PromptDialog.createBuilder(getSupportFragmentManager())
                            .setTitle(getString(R.string.dialog_prompt))
                            .setPrompt(getString(R.string.dialog_prompt_verify_backups_mnemonic_code_error))
                            .setPositiveButtonText(this, R.string.dialog_prompt_known)
                            .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_VERIFY_BACKUPS_MNEMONIC_CODE_ERROR)
                            .showAllowingStateLoss(this);
                }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.RequestCode.NET_WORK_SETTING:
            case Constant.RequestCode.PREMISSION_SETTING:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    confirmBackupsMnemonicCodePresenter.checkPermission(new PermissionCallback() {
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
    public void onLeftIconEvent() {
        onFinish("onLeftIconEvent");
    }

    @Override
    public boolean isActive() {
        return false;
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
            case Constant.RequestCode.DIALOG_PROMPT_VERIFY_BACKUPS_MNEMONIC_CODE_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_VERIFY_BACKUPS_MNEMONIC_CODE_ERROR");
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
    public void onTagSelectedPosition(int position, View view) {
        LogUtil.getInstance().print(String.format("selected position:%s", position));
        if (unverifiedBackupsMnemonicCodes != null) {
            unverifiedBackupsMnemonicCodes.add(shuffleBackupsMnemonicCodes.get(position));
            tagAdapter1 = new TagAdapter<String>(unverifiedBackupsMnemonicCodes) {
                @Override
                public View getView(FlowLayout parent, int position, String s) {
                    TextView textView = (TextView) LayoutInflater.from(ConfirmBackupsMnemonicCodeActivity.this).inflate(R.layout.view_tag1, tflBackupsMnemonicCode1, false);
                    textView.setText(s);
                    return textView;
                }
            };
            tflBackupsMnemonicCode1.setAdapter(tagAdapter1);
        }
    }

    @Override
    public void onTagUnselectedPosition(int position, View view) {
        LogUtil.getInstance().print(String.format("unselected position:%s", position));
        if (unverifiedBackupsMnemonicCodes != null && shuffleBackupsMnemonicCodes != null) {
            for (Iterator iterator = unverifiedBackupsMnemonicCodes.iterator(); iterator.hasNext(); ) {
                String mnemonicCode = String.valueOf(iterator.next());
                if (TextUtils.equals(shuffleBackupsMnemonicCodes.get(position), mnemonicCode)) {
                    iterator.remove();
                }
            }
            tagAdapter1 = new TagAdapter<String>(unverifiedBackupsMnemonicCodes) {
                @Override
                public View getView(FlowLayout parent, int position, String s) {
                    TextView textView = (TextView) LayoutInflater.from(ConfirmBackupsMnemonicCodeActivity.this).inflate(R.layout.view_tag1, tflBackupsMnemonicCode1, false);
                    textView.setText(s);
                    return textView;
                }
            };
            tflBackupsMnemonicCode1.setAdapter(tagAdapter1);
        }
    }
}
