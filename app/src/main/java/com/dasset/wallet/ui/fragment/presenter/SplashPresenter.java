package com.dasset.wallet.ui.fragment.presenter;

import android.content.Context;

import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.fragment.contract.SplashContract;

public class SplashPresenter extends BasePresenterImplement implements SplashContract.Presenter {

    private SplashContract.View view;

    public SplashPresenter(Context context, SplashContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
    }
}
