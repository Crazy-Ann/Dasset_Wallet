package com.dasset.wallet.ui.activity.presenter;

import android.app.Activity;
import android.content.Context;
import android.os.Message;

import com.dasset.wallet.R;
import com.dasset.wallet.base.handler.ActivityHandler;
import com.dasset.wallet.components.utils.BundleUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.MessageUtil;
import com.dasset.wallet.components.utils.ThreadPoolUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.core.crypto.mnemonic.MnemonicCode;
import com.dasset.wallet.core.crypto.mnemonic.MnemonicException;
import com.dasset.wallet.core.password.SecureCharSequence;
import com.dasset.wallet.core.wallet.hd.HDAccount;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.PasswordActivity;
import com.dasset.wallet.ui.activity.contract.PasswordContract;
import com.google.common.collect.Lists;

import java.security.SecureRandom;
import java.util.List;

public class PasswordPresenter extends BasePresenterImplement implements PasswordContract.Presenter {

    private PasswordContract.View view;
    private PasswordHandler passwordHandler;
    private String operation;

    public String getOperation() {
        return operation;
    }

    private class PasswordHandler extends ActivityHandler<PasswordActivity> {

        public PasswordHandler(PasswordActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(PasswordActivity activity, Message message) {
            if (activity != null) {
                switch (message.what) {
                    case Constant.StateCode.CREATE_WALLET_SUCCESS:
                        activity.hideLoadingPromptDialog();
                        HDAccount hdAccount = (HDAccount) message.obj;
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

    public PasswordPresenter(Context context, PasswordContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        passwordHandler = new PasswordHandler((PasswordActivity) view);
        operation = BundleUtil.getInstance().getStringData((Activity) view, Constant.BundleKey.WALLET_OPERATION);
    }

    @Override
    public void createWallet(boolean compressed, final String password) {
        view.showPromptDialog(R.string.dialog_prompt_create_wallet, false, false, Constant.RequestCode.DIALOG_PROGRESS_CREATE_WALLET);
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SecureCharSequence secureCharSequence = new SecureCharSequence(password);
                    HDAccount hdAccount = new HDAccount(MnemonicCode.getInstance(), new SecureRandom(), secureCharSequence, null);
                    List<String> words = Lists.newArrayList();
                    words.addAll(hdAccount.getSeedWords(secureCharSequence));
                    secureCharSequence.wipe();
                    for (String word : words) {
                        LogUtil.getInstance().print("word:" + word);
                    }
                    passwordHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.CREATE_WALLET_SUCCESS, hdAccount));
                } catch (MnemonicException.MnemonicLengthException e) {
                    e.printStackTrace();
                    passwordHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.CREATE_WALLET_SUCCESS, e, context.getString(R.string.dialog_prompt_create_wallet_error)));
                }
            }
        });
    }
}
