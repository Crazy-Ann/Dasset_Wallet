package com.dasset.wallet.ui.activity.presenter;

import android.content.Context;

import com.dasset.wallet.ecc.AccountStorageFactory;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.contract.MainContract;

public class MainPresenter extends BasePresenterImplement implements MainContract.Presenter {

    private MainContract.View view;

    public MainPresenter(Context context, MainContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            AccountStorageFactory.getInstance().initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
