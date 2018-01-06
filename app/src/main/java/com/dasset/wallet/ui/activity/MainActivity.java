package com.dasset.wallet.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.dasset.wallet.R;
import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.base.sticky.adapter.FixedStickyViewAdapter;
import com.dasset.wallet.base.sticky.holder.BaseViewHolder;
import com.dasset.wallet.base.sticky.listener.OnEventClickListener;
import com.dasset.wallet.base.sticky.listener.OnItemClickListener;
import com.dasset.wallet.base.toolbar.listener.OnLeftIconEventListener;
import com.dasset.wallet.base.toolbar.listener.OnRightIconEventListener;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.ActivityUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ToastUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.ecc.AccountStorageFactory;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.MainContract;
import com.dasset.wallet.ui.activity.presenter.MainPresenter;
import com.dasset.wallet.ui.adapter.AccountAdapter;
import com.dasset.wallet.ui.binder.AccountBinder;
import com.dasset.wallet.ui.dialog.PromptDialog;

import java.util.List;

public class MainActivity extends ActivityViewImplement<MainContract.Presenter> implements MainContract.View, OnLeftIconEventListener, OnRightIconEventListener, OnItemClickListener, OnEventClickListener {

    private MainPresenter mainPresenter;

    private RecyclerView           recycleView;
    private FixedStickyViewAdapter fixedStickyViewAdapter;
    private LinearLayoutManager    linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSavedInstanceState(savedInstanceState);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void findViewById() {
        inToolbar = ViewUtil.getInstance().findView(this, R.id.inToolbar);
        recycleView = ViewUtil.getInstance().findView(this, R.id.recycleView);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        initializeToolbar(R.color.color_383856, true, R.mipmap.icon_scan_white, this, R.color.color_383856, false, R.mipmap.icon_title_logo, null, true, R.mipmap.icon_add_white, this);
        mainPresenter = new MainPresenter(this, this);
        mainPresenter.initialize();
        setBasePresenterImplement(mainPresenter);
        fixedStickyViewAdapter = new AccountAdapter(this, new AccountBinder(this, recycleView), false);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycleView.setHasFixedSize(true);
        recycleView.setLayoutManager(linearLayoutManager);
        recycleView.setAdapter(fixedStickyViewAdapter);
        setAddAccountView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainPresenter.checkPermission(new PermissionCallback() {

                @Override
                public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                    fixedStickyViewAdapter.setData(AccountStorageFactory.getInstance().getAccountInfos());
                }

                @Override
                public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                    showPermissionPromptDialog();
                }
            });
        } else {
            fixedStickyViewAdapter.setData(AccountStorageFactory.getInstance().getAccountInfos());
        }
    }

    @Override
    protected void setListener() {
        fixedStickyViewAdapter.setOnItemClickListener(this);
        fixedStickyViewAdapter.setOnEventClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.RequestCode.NET_WORK_SETTING:
            case Constant.RequestCode.PREMISSION_SETTING:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mainPresenter.checkPermission(new PermissionCallback() {

                        @Override
                        public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                            fixedStickyViewAdapter.setData(AccountStorageFactory.getInstance().getAccountInfos());
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                            showPermissionPromptDialog();
                        }
                    });
                } else {
                    fixedStickyViewAdapter.setData(AccountStorageFactory.getInstance().getAccountInfos());
                }
                break;
            case Constant.RequestCode.CREATE_ACCOUNT:
                fixedStickyViewAdapter.setData(AccountStorageFactory.getInstance().getAccountInfos());
                break;
            case Constant.RequestCode.QRCODE_RECOGNITION:
                if (data != null) {
                    ToastUtil.getInstance().showToast(this, data.getStringExtra(Constant.BundleKey.QRCODE_RESULT), Toast.LENGTH_SHORT);
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
                .setPrompt(getString(R.string.prompt_quit))
                .setPositiveButtonText(this, R.string.confirm)
                .setNegativeButtonText(this, R.string.cancel)
                .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_QUIT)
                .showAllowingStateLoss(this);
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
            case Constant.RequestCode.DIALOG_PROMPT_QUIT:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_QUIT");
                BaseApplication.getInstance().releaseInstance();
                ActivityUtil.removeAll();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_IMPORT_ACCOUNT:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_IMPORT_ACCOUNT");
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
            case Constant.RequestCode.DIALOG_PROMPT_QUIT:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_QUIT");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_IMPORT_ACCOUNT:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_IMPORT_ACCOUNT");
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
    public void setAddAccountView() {
        if (fixedStickyViewAdapter != null) {
            if (!fixedStickyViewAdapter.hasFooterView(Constant.RecycleView.FOOTER_VIEW_ID)) {
                fixedStickyViewAdapter.addFooterView(Constant.RecycleView.FOOTER_VIEW_ID,
                                                     R.layout.holder_add_account,
                                                     FixedStickyViewAdapter.TYPE_FOOTER_VIEW,
                                                     R.layout.holder_add_account,
                                                     new FixedStickyViewAdapter.FixedViewHoldGenerator() {

                                                         @Override
                                                         public RecyclerView.ViewHolder generate() {
                                                             return new BaseViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.holder_add_account, recycleView, false));
                                                         }
                                                     });
            } else {
                fixedStickyViewAdapter.bindDataToHeaderOrFooter(Constant.RecycleView.FOOTER_VIEW_ID, null, FixedStickyViewAdapter.TYPE_FOOTER_VIEW);
            }
        }
    }

    @Override
    public void showImportAccountPromptDialog() {
        PromptDialog.createBuilder(getSupportFragmentManager())
                .setTitle(getString(R.string.dialog_prompt))
                .setPrompt(getString(R.string.dialog_prompt_import_account))
                .setPositiveButtonText(this, R.string.dialog_prompt_known)
                .setCancelable(false)
                .setCancelableOnTouchOutside(false)
                .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_IMPORT_ACCOUNT)
                .showAllowingStateLoss(this);
    }

    @Override
    public void onLeftIconEvent() {
        startActivityForResult(QRCodeRecognitionActivity.class, Constant.RequestCode.QRCODE_RECOGNITION);
    }

    @Override
    public void OnRightIconEvent() {

    }

    @Override
    public void onItemClick(int position) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constant.BundleKey.WALLET_ACCOUNT, AccountStorageFactory.getInstance().getAccountInfos().get(position));
        startActivity(AccountInfoActivity.class, bundle);
    }

    @Override
    public void onnEventClick() {
        startActivityForResult(CreateAccountActivity.class, Constant.RequestCode.CREATE_ACCOUNT);
    }
}
