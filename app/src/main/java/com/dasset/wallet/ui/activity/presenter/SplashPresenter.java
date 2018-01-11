package com.dasset.wallet.ui.activity.presenter;

import android.app.Activity;
import android.content.Context;

import com.dasset.wallet.components.utils.BundleUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.model.Page;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.contract.SplashContract;

import java.util.List;

public class SplashPresenter extends BasePresenterImplement implements SplashContract.Presenter {

    private SplashContract.View view;
    private List<Page> pages;

    public List<Page> getPages() {
        return pages;
    }

    public SplashPresenter(Context context, SplashContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        pages = BundleUtil.getInstance().getParcelableArrayListData((Activity) view, Constant.BundleKey.PAGE_LIST);
    }
}
