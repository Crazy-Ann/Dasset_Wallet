package com.dasset.wallet.ui.activity.presenter;

import android.content.Context;

import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.contract.WelcomeContract;

public class WelcomePresenter extends BasePresenterImplement implements WelcomeContract.Presenter {

    private WelcomeContract.View view;

    public WelcomePresenter(Context context, WelcomeContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
    }
}
