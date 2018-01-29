package com.dasset.wallet.ui.activity.presenter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import com.dasset.wallet.R;
import com.dasset.wallet.base.handler.ActivityHandler;
import com.dasset.wallet.components.utils.BundleUtil;
import com.dasset.wallet.components.utils.MessageUtil;
import com.dasset.wallet.components.utils.ThreadPoolUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.core.crypto.mnemonic.MnemonicCode;
import com.dasset.wallet.core.crypto.mnemonic.MnemonicException;
import com.dasset.wallet.core.password.SecureCharSequence;
import com.dasset.wallet.core.wallet.hd.HDAccount;
import com.dasset.wallet.model.WalletInfo;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.ConfirmPasswordActivity;
import com.dasset.wallet.ui.activity.contract.ConfirmPasswordContract;

import java.security.SecureRandom;

public class ConfirmPasswordPresenter extends BasePresenterImplement implements ConfirmPasswordContract.Presenter {

    private ConfirmPasswordContract.View view;
    private PasswordHandler passwordHandler;
    private String password;

    public String getPassword() {
        return password;
    }

    private class PasswordHandler extends ActivityHandler<ConfirmPasswordActivity> {

        public PasswordHandler(ConfirmPasswordActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(ConfirmPasswordActivity activity, Message message) {
            if (activity != null) {
                switch (message.what) {
                    case Constant.StateCode.CREATE_WALLET_SUCCESS:
                        activity.hideLoadingPromptDialog();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(Constant.BundleKey.WALLET_INFO, (WalletInfo) message.obj);
                        view.startGenerateWalletResultActivity(bundle);
                        break;
                    case Constant.StateCode.CREATE_WALLET_FAILED:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_CREATE_WALLET_ERROR);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public ConfirmPasswordPresenter(Context context, ConfirmPasswordContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        passwordHandler = new PasswordHandler((ConfirmPasswordActivity) view);
        password = BundleUtil.getInstance().getStringData((Activity) view, Constant.BundleKey.WALLET_PASSWORD);
    }

    @Override
    public void createWallet(boolean compressed, final String password) {
        view.showLoadingPromptDialog(R.string.dialog_prompt_create_wallet, Constant.RequestCode.DIALOG_PROGRESS_CREATE_WALLET);
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    WalletInfo walletInfo = new WalletInfo();
                    SecureCharSequence secureCharSequence = new SecureCharSequence(password);
                    HDAccount hdAccount = new HDAccount(MnemonicCode.getInstance(), new SecureRandom(), secureCharSequence, null);
                    walletInfo.setWalletName(BundleUtil.getInstance().getStringData((Activity) view, Constant.BundleKey.WALLET_NAME));
                    walletInfo.setHdAccount(hdAccount);
                    walletInfo.setMnemonicCodes(hdAccount.getSeedWords(secureCharSequence));
                    secureCharSequence.wipe();
                    passwordHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.CREATE_WALLET_SUCCESS, walletInfo));
                } catch (MnemonicException.MnemonicLengthException e) {
                    e.printStackTrace();
                    passwordHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.CREATE_WALLET_FAILED, e, context.getString(R.string.dialog_prompt_create_wallet_error)));
                }
            }
        });
    }
}
