package com.dasset.wallet.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dasset.wallet.R;
import com.dasset.wallet.ui.FragmentViewImplement;
import com.dasset.wallet.ui.fragment.contract.SplashContract;
import com.dasset.wallet.ui.fragment.presenter.SplashPresenter;

public class SplashFragment extends FragmentViewImplement<SplashContract.Presenter> implements SplashContract.View {

    private SplashPresenter splashPresenter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.view_splash_item, container, false);
        getSavedInstanceState(savedInstanceState);
        findViewById();
        initialize(savedInstanceState);
        setListener();
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    protected void findViewById() {
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        splashPresenter = new SplashPresenter(getActivity(), this);
        splashPresenter.initialize();
        setBasePresenterImplement(splashPresenter);
    }

    @Override
    protected void setListener() {

    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

}
