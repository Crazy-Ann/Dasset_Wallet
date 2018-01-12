package com.dasset.wallet.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dasset.wallet.R;
import com.dasset.wallet.base.sticky.adapter.FixedStickyViewAdapter;
import com.dasset.wallet.base.sticky.listener.OnItemClickListener;
import com.dasset.wallet.base.toolbar.listener.OnLeftIconEventListener;
import com.dasset.wallet.base.toolbar.listener.OnRightIconEventListener;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.GlideUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ToastUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.components.widget.sticky.LinearLayoutDividerItemDecoration;
import com.dasset.wallet.components.zxing.encode.QRCodeEncode;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.core.ecc.Account;
import com.dasset.wallet.model.TransactionRecord;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.AccountInfoContract;
import com.dasset.wallet.ui.activity.presenter.AccountInfoPresenter;
import com.dasset.wallet.ui.adapter.TransactionRecordAdapter;
import com.dasset.wallet.ui.binder.TransactionRecordBinder;
import com.dasset.wallet.ui.dialog.ImagePromptDialog;
import com.dasset.wallet.ui.dialog.PromptDialog;

import java.util.List;

public class AccountInfoActivity extends ActivityViewImplement<AccountInfoContract.Presenter> implements AccountInfoContract.View, View.OnClickListener, OnLeftIconEventListener, OnRightIconEventListener, SwipeRefreshLayout.OnRefreshListener, OnItemClickListener {

    private AccountInfoPresenter accountInfoPresenter;

    private LinearLayout llAddress;
    private TextView tvAddress;
    private ImageView ivAddressQRCode;
    private TextView tvAmount;
    private LinearLayout llTransactionRecord;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recycleView;
    private FixedStickyViewAdapter fixedStickyViewAdapter;
    private LinearLayoutManager linearLayoutManager;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);
        getSavedInstanceState(savedInstanceState);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            accountInfoPresenter.checkPermission(new PermissionCallback() {

                @Override
                public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                    loadTransactionRecords();
                }

                @Override
                public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                    showPermissionPromptDialog();
                }
            });
        } else {
            loadTransactionRecords();
        }
    }

    @Override
    protected void findViewById() {
        inToolbar = ViewUtil.getInstance().findView(this, R.id.inToolbar);
        llAddress = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.llAddress, this);
        tvAddress = ViewUtil.getInstance().findView(this, R.id.tvAddress);
        ivAddressQRCode = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.ivAddressQRCode, this);
        tvAmount = ViewUtil.getInstance().findView(this, R.id.tvAmount);
        llTransactionRecord = ViewUtil.getInstance().findView(this, R.id.llTransactionRecord);
        swipeRefreshLayout = ViewUtil.getInstance().findView(this, R.id.swipeRefreshLayout);
        recycleView = ViewUtil.getInstance().findView(this, R.id.recycleView);
        tvEmpty = ViewUtil.getInstance().findView(this, R.id.tvEmpty);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        accountInfoPresenter = new AccountInfoPresenter(this, this);
        accountInfoPresenter.initialize();
        setBasePresenterImplement(accountInfoPresenter);
        if (accountInfoPresenter.getAccountInfo() != null && !TextUtils.isEmpty(accountInfoPresenter.getAccountInfo().getAccountName()) && !TextUtils.isEmpty(accountInfoPresenter.getAccountInfo().getAddress2())) {
            initializeToolbar(R.color.color_383856, true, R.mipmap.icon_back_white, this, android.R.color.white, accountInfoPresenter.getAccountInfo().getAccountName(), true, R.mipmap.icon_more, this);
            tvAddress.setText(accountInfoPresenter.getAccountInfo().getAddress2());
            GlideUtil.getInstance().with(this, QRCodeEncode.createQRCode(accountInfoPresenter.getAccountInfo().getAddress2(), ViewUtil.getInstance().dp2px(this, 160)), ViewUtil.getInstance().dp2px(this, 120), ViewUtil.getInstance().dp2px(this, 120), DiskCacheStrategy.NONE, ivAddressQRCode);
            //todo
            tvAmount.setText(String.format("余额  ¥ %s", 1000.00));
        } else {
            initializeToolbar(R.color.color_383856, true, R.mipmap.icon_back_white, this, android.R.color.white, getString(R.string.dialog_prompt_account_info_error), true, R.mipmap.icon_more, this);
            tvAddress.setText(getString(R.string.dialog_prompt_account_info_error));
            GlideUtil.getInstance().with(this, R.mipmap.ic_launcher_round, ViewUtil.getInstance().dp2px(this, 120), ViewUtil.getInstance().dp2px(this, 120), DiskCacheStrategy.NONE, ivAddressQRCode);
            tvAmount.setText(String.format("余额  ¥ %s", 0.00));
        }

        fixedStickyViewAdapter = new TransactionRecordAdapter(new TransactionRecordBinder(this, recycleView));
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycleView.setHasFixedSize(true);
        recycleView.setLayoutManager(linearLayoutManager);
        recycleView.addItemDecoration(new LinearLayoutDividerItemDecoration(getResources().getColor(R.color.color_e4e4e4), 2, LinearLayoutManager.VERTICAL));
        recycleView.setAdapter(fixedStickyViewAdapter);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_c9c9c9);
        swipeRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
    }

    @Override
    protected void setListener() {
        fixedStickyViewAdapter.setItemClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.RequestCode.NET_WORK_SETTING:
            case Constant.RequestCode.PREMISSION_SETTING:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    accountInfoPresenter.checkPermission(new PermissionCallback() {

                        @Override
                        public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                            loadTransactionRecords();
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                            showPermissionPromptDialog();
                        }
                    });
                } else {
                    loadTransactionRecords();
                }
            case Constant.RequestCode.ACCOUNT_RENAME:
                if (data != null) {
                    initializeToolbar(R.color.color_383856, true, R.mipmap.icon_back_white, this, android.R.color.white, ((Account) data.getParcelableExtra(Constant.BundleKey.WALLET_ACCOUNT)).getAccountName(), true, R.mipmap.icon_more, this);
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
            case Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT_SUCCESS:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_EXPORT_ACCOUNT_SUCCESS");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT_FAILED:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_EXPORT_ACCOUNT_FAILED");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_DELETE_ACCOUNT:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_DELETE_ACCOUNT");
                accountInfoPresenter.deleteAccount();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_DELETE_ACCOUNT_SUCCESS:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_DELETE_ACCOUNT_SUCCESS");
                onFinish("onPositiveButtonClicked_DIALOG_PROMPT_DELETE_ACCOUNT_SUCCESS");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_DELETE_ACCOUNT_FAILED:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_DIALOG_PROMPT_DELETE_ACCOUNT_FAILED");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QRCODE_EXPORT:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_QRCODE_EXPORT");
                accountInfoPresenter.share();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QRCODE_EXPORT_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_QRCODE_EXPORT_ERROR");
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
            case Constant.RequestCode.DIALOG_PROMPT_DELETE_ACCOUNT:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_DELETE_ACCOUNT");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QRCODE_EXPORT:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_QRCODE_EXPORT");
                accountInfoPresenter.save();
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
    public void setSwipeRefreshLayout(boolean isRefresh) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(isRefresh);
        }
    }

    @Override
    public void showDeleteAccountPromptDialog() {
        PromptDialog.createBuilder(getSupportFragmentManager())
                .setTitle(getString(R.string.dialog_prompt))
                .setPrompt(getString(R.string.dialog_prompt_delete_account))
                .setPositiveButtonText(this, R.string.confirm)
                .setNegativeButtonText(this, R.string.cancel)
                .setCancelable(false)
                .setCancelableOnTouchOutside(false)
                .setRequestCode(Constant.RequestCode.DIALOG_PROMPT_DELETE_ACCOUNT)
                .showAllowingStateLoss(this);
    }

    @Override
    public void loadTransactionRecords() {
        if (accountInfoPresenter.getTransactionRecords() != null) {
            List<TransactionRecord> transactionRecords = accountInfoPresenter.getTransactionRecords().getTransactionRecords();
            if (transactionRecords != null && transactionRecords.size() > 0) {
                ViewUtil.getInstance().setViewGone(tvEmpty);
                ViewUtil.getInstance().setViewVisible(llTransactionRecord);
                fixedStickyViewAdapter.setData(transactionRecords);
            } else {
                ViewUtil.getInstance().setViewGone(llTransactionRecord);
                ViewUtil.getInstance().setViewVisible(tvEmpty);
            }
        } else {
            ViewUtil.getInstance().setViewGone(llTransactionRecord);
            ViewUtil.getInstance().setViewVisible(tvEmpty);
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
        onFinish("onLeftIconEvent");
    }

    @Override
    public void OnRightIconEvent() {
        accountInfoPresenter.renameAccount();
    }

    @Override
    public void onRefresh() {
        //todo
    }

    @Override
    public void onItemClick(int position, View view) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llAddress:
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, tvAddress.getText().toString().trim()));
                if (clipboardManager.hasPrimaryClip()) {
                    ToastUtil.getInstance().showToast(this, R.string.account_info_copy, Toast.LENGTH_SHORT);
                }
                break;
            case R.id.ivAddressQRCode:
                accountInfoPresenter.generateAddresQRCode();
                break;
            default:
                break;
        }
    }
}
