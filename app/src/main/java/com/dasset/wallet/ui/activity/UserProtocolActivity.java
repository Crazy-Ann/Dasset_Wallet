package com.dasset.wallet.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.dasset.wallet.R;
import com.dasset.wallet.base.toolbar.listener.OnLeftIconEventListener;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.InputUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.UserProtocolContract;
import com.dasset.wallet.ui.activity.presenter.UserProtocolPresenter;

import java.util.List;

public class UserProtocolActivity extends ActivityViewImplement<UserProtocolContract.Presenter> implements UserProtocolContract.View, View.OnClickListener, OnLeftIconEventListener {

    private UserProtocolPresenter userProtocolPresenter;

    private WebView wbUserProtocol;
    private Button  btnUserProtocol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_protocol);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            userProtocolPresenter.checkPermission(new PermissionCallback() {
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
        wbUserProtocol = ViewUtil.getInstance().findView(this, R.id.wbUserProtocol);
        btnUserProtocol = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.btnUserProtocol, this);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        initializeToolbar(R.color.color_5757ff, true, R.mipmap.ic_launcher, this, android.R.color.white, getString(R.string.user_protocol));
        userProtocolPresenter = new UserProtocolPresenter(this, this);
        userProtocolPresenter.initialize();
        setBasePresenterImplement(userProtocolPresenter);
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
            case R.id.btnUserProtocol:
                startActivity(CreateAccountActivity.class);
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
    public void onLeftIconEvent() {
        onFinish("onLeftIconEvent");
    }
}
