package com.dasset.wallet.base.presenter;


import com.dasset.wallet.base.view.BaseView;
import com.dasset.wallet.components.permission.listener.PermissionCallback;

public interface BasePresenter {

    void initialize();

    void checkPermission(PermissionCallback permissionCallback, String... permissions);

    boolean checkGesturePassword(BaseView view);
}
