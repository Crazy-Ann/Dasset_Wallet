package com.dasset.wallet.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dasset.wallet.R;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.model.Test;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.TestContract;
import com.dasset.wallet.ui.activity.presenter.TestPresenter;

public class TestActivity extends ActivityViewImplement<TestContract.Presenter> implements TestContract.View, View.OnClickListener {

    private TestPresenter testPresenter;

    private EditText etData;
    private Button btnTest;
    private TextView tvPrivateKey;
    private TextView tvPublicKey;
    private TextView tvAddress;
    private TextView tvSignature;
    private TextView tvSignatureResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void findViewById() {
        etData = ViewUtil.getInstance().findView(this, R.id.etData);
        btnTest = ViewUtil.getInstance().findViewAttachOnclick(this, R.id.btnTest, this);
        tvPrivateKey = ViewUtil.getInstance().findView(this, R.id.tvPrivateKey);
        tvPublicKey = ViewUtil.getInstance().findView(this, R.id.tvPublicKey);
        tvAddress = ViewUtil.getInstance().findView(this, R.id.tvAddress);
        tvSignature = ViewUtil.getInstance().findView(this, R.id.tvSignature);
        tvSignatureResult = ViewUtil.getInstance().findView(this, R.id.tvSignatureResult);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        testPresenter = new TestPresenter(this, this);
        testPresenter.initialize();
    }

    @Override
    protected void setListener() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnTest:
                testPresenter.test(etData.getText().toString());
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
    public void showTestData(Test eccTest) {
        tvPrivateKey.setText(String.format("Private Key: %s", eccTest.getPrivateKey()));
        tvPublicKey.setText(String.format("Public Key: %s", eccTest.getPublicKey()));
        tvAddress.setText(String.format("Address: %s", eccTest.getAddress()));
        tvSignature.setText(String.format("Signature: %s", eccTest.getSignature()));
        tvSignatureResult.setText(String.format("Signature Result: %s", eccTest.getSignatureResult()));
    }
}
