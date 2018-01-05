package com.dasset.wallet.ui.activity.presenter;

import android.content.Context;

import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.contract.UserProtocolContract;

public class UserProtocolPresenter extends BasePresenterImplement implements UserProtocolContract.Presenter {

    private UserProtocolContract.View view;

    public UserProtocolPresenter(Context context, UserProtocolContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
    }
}
