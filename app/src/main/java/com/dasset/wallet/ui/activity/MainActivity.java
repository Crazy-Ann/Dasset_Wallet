package com.dasset.wallet.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.dasset.wallet.R;
import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.base.sticky.adapter.FixedStickyViewAdapter;
import com.dasset.wallet.base.sticky.holder.BaseViewHolder;
import com.dasset.wallet.base.sticky.listener.OnHeaderOrFooterItemClickListener;
import com.dasset.wallet.base.sticky.listener.OnItemClickListener;
import com.dasset.wallet.base.sticky.listener.OnViewClickListener;
import com.dasset.wallet.base.toolbar.listener.OnLeftIconEventListener;
import com.dasset.wallet.base.toolbar.listener.OnRightIconEventListener;
import com.dasset.wallet.components.constant.Regex;
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
import com.dasset.wallet.ui.adapter.MenuAdapter;
import com.dasset.wallet.ui.binder.AccountBinder;
import com.dasset.wallet.ui.dialog.ImagePromptDialog;
import com.dasset.wallet.ui.dialog.PromptDialog;

import java.io.IOException;
import java.util.List;

public class MainActivity extends ActivityViewImplement<MainContract.Presenter> implements MainContract.View, OnLeftIconEventListener, OnRightIconEventListener, OnItemClickListener, OnViewClickListener, OnHeaderOrFooterItemClickListener {

    private MainPresenter mainPresenter;

    private RecyclerView accountRecycleView;
    private RecyclerView menuRecycleView;
    private AccountAdapter accountAdapter;
    private MenuAdapter menuAdapter;
    private LinearLayoutManager linearLayoutManager;
    private AccountBinder accountBinder;

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
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainPresenter.checkPermission(new PermissionCallback() {

                @Override
                public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                    loadAccountData();
                }

                @Override
                public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                    showPermissionPromptDialog();
                }
            });
        } else {
            loadAccountData();
        }
    }

    @Override
    protected void findViewById() {
        inToolbar = ViewUtil.getInstance().findView(this, R.id.inToolbar);
        accountRecycleView = ViewUtil.getInstance().findView(this, R.id.recycleView);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        initializeToolbar(R.color.color_383856, true, R.mipmap.icon_scan_white, this, R.color.color_383856, false, R.mipmap.icon_title_logo, null, true, R.mipmap.icon_add_white, this);
        mainPresenter = new MainPresenter(this, this);
        mainPresenter.initialize();
        setBasePresenterImplement(mainPresenter);
        accountBinder = new AccountBinder(this, accountRecycleView);
        accountAdapter = new AccountAdapter(this, accountBinder, false);
//        menuAdapter = new MenuAdapter(new MenuBinder(this, menuRecycleView));
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        accountRecycleView.setHasFixedSize(true);
        accountRecycleView.setLayoutManager(linearLayoutManager);
        accountRecycleView.setAdapter(accountAdapter);
//        menuRecycleView.setHasFixedSize(true);
//        menuRecycleView.setLayoutManager(linearLayoutManager);
//        menuRecycleView.setAdapter(menuAdapter);
        setAddAccountView();
        LogUtil.getInstance().print(mainPresenter.getMenus());
    }

    @Override
    protected void setListener() {
        accountAdapter.setOnHeaderOrFooterItemClickListener(this);
        accountAdapter.setItemClickListener(this);
        accountBinder.setOnViewClickListener(this);
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
                            loadAccountData();
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                            showPermissionPromptDialog();
                        }
                    });
                } else {
                    loadAccountData();
                }
                break;
            case Constant.RequestCode.CREATE_ACCOUNT:
                loadAccountData();
                break;
            case Constant.RequestCode.QRCODE_RECOGNITION:
                if (data != null) {
                    ToastUtil.getInstance().showToast(this, data.getStringExtra(Constant.BundleKey.QRCODE_RESULT), Toast.LENGTH_SHORT);
                }
                break;
            case Constant.RequestCode.FILE_MANAGER:
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString(Constant.BundleKey.IMPORT_FILE_PATH, mainPresenter.generatorPathFromUri(uri));
                        startActivityForResult(PasswordActivity.class, Constant.RequestCode.PASSWORD_VERIFICATION, bundle);
                    } else {
                        showPromptDialog(R.string.dialog_prompt_import_account_error, false, false, Constant.RequestCode.DIALOG_PROMPT_IMPORT_ACCOUNT_ERROR);
                    }
                }
                break;
            case Constant.RequestCode.PASSWORD_VERIFICATION:
                if (data != null) {
                    mainPresenter.importAccount(data.getStringExtra(Constant.BundleKey.IMPORT_FILE_PATH), data.getStringExtra(Constant.BundleKey.IMPORT_PASSWORD));
                } else {
                    showPromptDialog(R.string.dialog_prompt_import_account_error, false, false, Constant.RequestCode.DIALOG_PROMPT_IMPORT_ACCOUNT_ERROR);
                }
                break;
            case Constant.RequestCode.EXPORT_QRCODE:
                if (data != null) {
                    showPromptDialog(R.string.dialog_prompt_qrcode_share_success, false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_EXPORT_ERROR);
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
            case Constant.RequestCode.DIALOG_PROMPT_IMPORT_ACCOUNT1:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_IMPORT_ACCOUNT1");
                try {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType(Regex.UNLIMITED_DIRECTORY_TYPE.getRegext());
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, Constant.RequestCode.FILE_MANAGER);
                } catch (ActivityNotFoundException e) {
                    showPromptDialog(R.string.dialog_prompt_file_manager_error, false, false, Constant.RequestCode.DIALOG_PROMPT_FILE_MANAGER_ERROR);
                }
                break;
            case Constant.RequestCode.DIALOG_PROMPT_IMPORT_ACCOUNT2:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_IMPORT_ACCOUNT2");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_IMPORT_ACCOUNT_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_IMPORT_ACCOUNT_ERROR");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_FILE_MANAGER_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_FILE_MANAGER_ERROR");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QRCODE_EXPORT:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_QRCODE_EXPORT");
                mainPresenter.share();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QRCODE_EXPORT_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_QRCODE_EXPORT_ERROR");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QRCODE_SAVE_SUCCESS:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_QRCODE_SAVE_SUCCESS");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QRCODE_SAVE_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_QRCODE_SAVE_ERROR");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QRCODE_SHARE_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_QRCODE_SHARE_ERROR");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QRCODE_BITMAP_GET_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_QRCODE_BITMAP_GET_ERROR");
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
            case Constant.RequestCode.DIALOG_PROMPT_IMPORT_ACCOUNT1:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_IMPORT_ACCOUNT");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QRCODE_EXPORT:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_QRCODE_EXPORT");
                mainPresenter.save();
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
        if (accountAdapter != null) {
            if (!accountAdapter.hasFooterView(Constant.RecycleView.FOOTER_VIEW_ID)) {
                accountAdapter.addFooterView(Constant.RecycleView.FOOTER_VIEW_ID,
                                             R.layout.holder_add_account,
                                             FixedStickyViewAdapter.TYPE_FOOTER_VIEW,
                                             R.layout.holder_add_account,
                                             new FixedStickyViewAdapter.FixedViewHoldGenerator() {

                                                 @Override
                                                 public RecyclerView.ViewHolder generate() {
                                                     return new BaseViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.holder_add_account, accountRecycleView, false));
                                                 }
                                             });
            } else {
                accountAdapter.bindDataToHeaderOrFooter(Constant.RecycleView.FOOTER_VIEW_ID, null, FixedStickyViewAdapter.TYPE_FOOTER_VIEW);
            }
        }
    }

    @Override
    public void setMenuView() {

    }

    @Override
    public void showImportAccountPromptDialog() {
        PromptDialog.createBuilder(getSupportFragmentManager())
                .setTitle(getString(R.string.dialog_prompt))
                .setPrompt(getString(R.string.dialog_prompt_import_account1))
                .setPositiveButtonText(this, R.string.dialog_prompt_known)
                .setCancelable(false)
                .setCancelableOnTouchOutside(false)
                .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_IMPORT_ACCOUNT1)
                .showAllowingStateLoss(this);
    }

    @Override
    public void loadAccountData() {
        if (accountAdapter != null) {
            try {
                accountAdapter.setData(AccountStorageFactory.getInstance().getAccountInfos(AccountStorageFactory.getInstance().getKeystoreDirectory()));
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                showPromptDialog(e.getMessage(), false, false, Constant.RequestCode.DIALOG_PROMPT_IMPORT_ACCOUNT_ERROR);
            }
        } else {

        }
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
                .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_QRCODE_EXPORT)
                .showAllowingStateLoss(this);
    }

    @Override
    public void onLeftIconEvent() {
        startActivityForResult(QRCodeRecognitionActivity.class, Constant.RequestCode.QRCODE_RECOGNITION);
    }

    @Override
    public void OnRightIconEvent() {
        //todo
//        showImportAccountPromptDialog();
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(Regex.UNLIMITED_DIRECTORY_TYPE.getRegext());
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, Constant.RequestCode.FILE_MANAGER);
        } catch (ActivityNotFoundException e) {
            showPromptDialog(R.string.dialog_prompt_file_manager_error, false, false, Constant.RequestCode.DIALOG_PROMPT_FILE_MANAGER_ERROR);
        }
    }

    @Override
    public void onHeaderOrFooterItemClick(int id) {
        switch (id) {
            case R.id.llAddAccount:
                startActivityForResult(CreateAccountActivity.class, Constant.RequestCode.CREATE_ACCOUNT);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(int position, View view) {
        try {
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constant.BundleKey.WALLET_ACCOUNT, AccountStorageFactory.getInstance().getAccountInfos(AccountStorageFactory.getInstance().getKeystoreDirectory()).get(position));
            startActivity(AccountInfoActivity.class, bundle);
        } catch (IOException e) {
            e.printStackTrace();
            showPromptDialog(R.string.dialog_prompt_account_info_error, false, false, Constant.RequestCode.DIALOG_PROMPT_ACCOUNT_INFO_ERROR);
        }
    }

    @Override
    public void onViewClick(int position, View view) {
        switch (view.getId()) {
            case R.id.tvAddress:
                LogUtil.getInstance().print("tvAddress," + position);
                break;
            case R.id.ivAddressQRCode:
                mainPresenter.generateAddresQRCode(position);
                break;
            default:
                break;
        }
    }
}
