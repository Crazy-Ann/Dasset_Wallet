package com.dasset.wallet.ui;

import android.content.Context;

import com.dasset.wallet.base.permission.Permission;
import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.base.constant.Constant;

import java.util.Arrays;

public abstract class BasePresenterImplement implements BasePresenter {

    protected Context context;

    @Override
    public void initialize() {
    }

    @Override
    public void checkPermission(PermissionCallback permissionCallback, String... permissions) {
        if (context != null) {
            if (permissions == null || permissions.length == 0) {
                permissions = com.dasset.wallet.constant.Constant.PERMISSIONS;
            }
            if (!Permission.getInstance().hasPermission(context, permissions)) {
                Permission.getInstance().with(context)
                        .requestCode(Constant.RequestCode.PERMISSION)
                        .permission(permissions)
                        .callback(permissionCallback)
                        .start();
            } else {
                permissionCallback.onSuccess(Constant.RequestCode.PERMISSION, Arrays.asList(permissions));
            }
        }
    }

    @Override
    public boolean checkGesturePassword(BaseView view) {
        return false;
    }
}
