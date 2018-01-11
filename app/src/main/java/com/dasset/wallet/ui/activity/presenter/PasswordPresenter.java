package com.dasset.wallet.ui.activity.presenter;

import android.app.Activity;
import android.content.Context;

import com.dasset.wallet.components.utils.BundleUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.contract.PasswordContract;

public class PasswordPresenter extends BasePresenterImplement implements PasswordContract.Presenter {

    private PasswordContract.View view;

    private String path;

    public String getPath() {
        return path;
    }

    public PasswordPresenter(Context context, PasswordContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        path = BundleUtil.getInstance().getStringData((Activity) view, Constant.BundleKey.IMPORT_FILE_PATH);
    }
}
