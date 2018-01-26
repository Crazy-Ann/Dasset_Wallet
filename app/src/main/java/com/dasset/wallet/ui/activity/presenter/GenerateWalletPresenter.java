package com.dasset.wallet.ui.activity.presenter;

import android.content.Context;

import com.dasset.wallet.core.contant.MnemonicDictionary;
import com.dasset.wallet.core.crypto.mnemonic.MnemonicCode;
import com.dasset.wallet.core.crypto.mnemonic.listener.OnMnemonicDictionaryResourcelistener;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.contract.GenerateWalletContract;

import java.io.InputStream;

public class GenerateWalletPresenter extends BasePresenterImplement implements GenerateWalletContract.Presenter, OnMnemonicDictionaryResourcelistener {

    private GenerateWalletContract.View view;

    public GenerateWalletPresenter(Context context, GenerateWalletContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        MnemonicCode.getInstance().setOnMnemonicDictionaryResourcelistener(this);
        MnemonicCode.getInstance().setMnemonicDictionary(MnemonicDictionary.US);
    }

    @Override
    public InputStream onMnemonicDictionaryResource(MnemonicDictionary mnemonicDictionary) {
        switch (mnemonicDictionary) {
            case US:
                return context.getResources().openRawResource(com.dasset.wallet.core.R.raw.en_us);
            case CN:
                return context.getResources().openRawResource(com.dasset.wallet.core.R.raw.zh_cn);
            case TW:
                return context.getResources().openRawResource(com.dasset.wallet.core.R.raw.zh_tw);
            default:
                return context.getResources().openRawResource(com.dasset.wallet.core.R.raw.en_us);
        }
    }
}
