package com.dasset.wallet.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dasset.wallet.R;
import com.dasset.wallet.base.sticky.adapter.FixedStickyViewAdapter;
import com.dasset.wallet.base.toolbar.listener.OnLeftIconEventListener;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.GlideUtil;
import com.dasset.wallet.components.utils.InputUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.components.validation.EditTextValidator;
import com.dasset.wallet.components.validation.Validation;
import com.dasset.wallet.components.widget.sticky.LinearLayoutDividerItemDecoration;
import com.dasset.wallet.components.zxing.encode.QRCodeEncode;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.model.validation.AccountNameValidation;
import com.dasset.wallet.model.validation.TransactionPasswordValidation;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.AccountRenameContract;
import com.dasset.wallet.ui.activity.presenter.AccountInfoPresenter;
import com.dasset.wallet.ui.activity.presenter.AccountRenamePresenter;
import com.dasset.wallet.ui.adapter.TransactionRecordAdapter;
import com.dasset.wallet.ui.binder.TransactionRecordBinder;

import java.util.List;

public class AccountRenameActivity extends ActivityViewImplement<AccountRenameContract.Presenter> implements AccountRenameContract.View, View.OnClickListener, OnLeftIconEventListener {

    private AccountRenamePresenter accountRenamePresenter;

    private EditText    etAccountName;
    private ImageButton ibAccountNameEmpty;
    private Button      btnConfirm;

    private EditTextValidator editTextValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_rename);
        getSavedInstanceState(savedInstanceState);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void findViewById() {
        inToolbar = ViewUtil.getInstance().findView(this, R.id.inToolbar);
        etAccountName = ViewUtil.getInstance().findView(this, R.id.etAccountName);
        ibAccountNameEmpty = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.ibAccountNameEmpty, this);
        btnConfirm = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.btnConfirm, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            accountRenamePresenter.checkPermission(new PermissionCallback() {

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
    protected void initialize(Bundle savedInstanceState) {
        initializeToolbar(android.R.color.white, true, R.mipmap.icon_back_black, this, android.R.color.black, getString(R.string.account_rename));
        accountRenamePresenter = new AccountRenamePresenter(this, this);
        accountRenamePresenter.initialize();
        setBasePresenterImplement(accountRenamePresenter);

        editTextValidator = new EditTextValidator();
        editTextValidator.add(new Validation(null, etAccountName, true, ibAccountNameEmpty, new AccountNameValidation()));
        editTextValidator.execute(this, btnConfirm, R.drawable.rectangle_b7b7fa, R.drawable.rectangle_5757ff, R.color.color_d1d1fb, android.R.color.white, null, null, false);
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
                    accountRenamePresenter.checkPermission(new PermissionCallback() {

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
    public void onClick(View v) {
        if (InputUtil.getInstance().isDoubleClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.btnConfirm:
                if (editTextValidator.validate(this)) {
                    accountRenamePresenter.renameAccount(etAccountName.getText().toString());
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

}
