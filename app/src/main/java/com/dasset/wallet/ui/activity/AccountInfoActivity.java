package com.dasset.wallet.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.dasset.wallet.R;
import com.dasset.wallet.base.sticky.adapter.FixedStickyViewAdapter;
import com.dasset.wallet.base.sticky.listener.OnEventClickListener;
import com.dasset.wallet.base.sticky.listener.OnItemClickListener;
import com.dasset.wallet.base.toolbar.listener.OnLeftIconEventListener;
import com.dasset.wallet.base.toolbar.listener.OnRightIconEventListener;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.ecc.AccountStorageFactory;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.AccountInfoContract;
import com.dasset.wallet.ui.activity.presenter.AccountInfoPresenter;
import com.dasset.wallet.ui.adapter.MainAdapter;
import com.dasset.wallet.ui.binder.MainBinder;

import java.util.List;

public class AccountInfoActivity extends ActivityViewImplement<AccountInfoContract.Presenter> implements AccountInfoContract.View, OnLeftIconEventListener, OnRightIconEventListener, OnItemClickListener, OnEventClickListener/*, SwipeRefreshLayout.OnRefreshListener */ {

    private AccountInfoPresenter accountInfoPresenter;

    private RecyclerView recycleView;
    private FixedStickyViewAdapter fixedStickyViewAdapter;
    private LinearLayoutManager linearLayoutManager;

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
    protected void findViewById() {
        inToolbar = ViewUtil.getInstance().findView(this, R.id.inToolbar);
        recycleView = ViewUtil.getInstance().findView(this, R.id.recycleView);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        initializeToolbar(R.color.color_383856, true, R.mipmap.icon_scan_white, this, R.color.color_383856, false, R.mipmap.icon_title_logo, null, true, R.mipmap.icon_add_white, this);
        accountInfoPresenter = new AccountInfoPresenter(this, this);
        accountInfoPresenter.initialize();
        setBasePresenterImplement(accountInfoPresenter);
        fixedStickyViewAdapter = new MainAdapter(this, new MainBinder(this, recycleView), true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycleView.setHasFixedSize(true);
        recycleView.setHasFixedSize(true);
        recycleView.setLayoutManager(linearLayoutManager);
        recycleView.setAdapter(fixedStickyViewAdapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            accountInfoPresenter.checkPermission(new PermissionCallback() {

                @Override
                public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                    try {
                        fixedStickyViewAdapter.setData(AccountStorageFactory.getInstance().getAccountInfos());
                    } catch (Exception e) {
                        fixedStickyViewAdapter.setData(null);
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                    showPermissionPromptDialog();
                }
            });
        } else {
            try {
                fixedStickyViewAdapter.setData(AccountStorageFactory.getInstance().getAccountInfos());
            } catch (Exception e) {
                fixedStickyViewAdapter.setData(null);
                e.printStackTrace();
            }
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
                    accountInfoPresenter.checkPermission(new PermissionCallback() {

                        @Override
                        public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                            try {
                                fixedStickyViewAdapter.setData(AccountStorageFactory.getInstance().getAccountInfos());
                            } catch (Exception e) {
                                fixedStickyViewAdapter.setData(null);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                            showPermissionPromptDialog();
                        }
                    });
                } else {
                    try {
                        fixedStickyViewAdapter.setData(AccountStorageFactory.getInstance().getAccountInfos());
                    } catch (Exception e) {
                        fixedStickyViewAdapter.setData(null);
                        e.printStackTrace();
                    }
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
            case Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT_SUCCESS:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_EXPORT_ACCOUNT_SUCCESS");
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
    public void OnLeftIconEvent() {

    }

    @Override
    public void OnRightIconEvent() {
        
    }

    @Override
    public void onItemClick(int position) {
        
    }

    @Override
    public void onnEventClick() {
        
    }
}
