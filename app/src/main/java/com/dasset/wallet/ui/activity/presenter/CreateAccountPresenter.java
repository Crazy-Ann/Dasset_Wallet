package com.dasset.wallet.ui.activity.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import com.dasset.wallet.R;
import com.dasset.wallet.base.handler.ActivityHandler;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.MessageUtil;
import com.dasset.wallet.components.utils.ThreadPoolUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.core.ecc.Account;
import com.dasset.wallet.core.ecc.AddressFactory;
import com.dasset.wallet.core.ecc.ECKeyPairFactory;
import com.dasset.wallet.ecc.AccountStorageFactory;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.CreateAccountActivity;
import com.dasset.wallet.ui.activity.contract.CreateAccountContract;

import org.spongycastle.util.encoders.Hex;

public class CreateAccountPresenter extends BasePresenterImplement implements CreateAccountContract.Presenter {

    private CreateAccountContract.View view;
    private CreateAccountHandler       createAccountHandler;

    private class CreateAccountHandler extends ActivityHandler<CreateAccountActivity> {

        public CreateAccountHandler(CreateAccountActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(CreateAccountActivity activity, Message msg) {
            if (activity != null) {
                switch (msg.what) {
                    case Constant.StateCode.GENERATE_ECKEYPAIR_SUCCESS:
                        activity.hideLoadingPromptDialog();
                        Account account = (Account) msg.obj;
                        LogUtil.getInstance().print(account.toString());
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(Constant.BundleKey.WALLET_ACCOUNT, account);
                        view.startCreateAccountResultActivity(bundle);
                        break;
                    case Constant.StateCode.GENERATE_ECKEYPAIR_FAILED:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(msg.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_CREATE_ACCOUNT_ERROR);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public CreateAccountPresenter(Context context, CreateAccountContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        createAccountHandler = new CreateAccountHandler((CreateAccountActivity) view);
    }

    @Override
    public void createAccount(final boolean compressed, final String accountName, final String password) {
        view.showLoadingPromptDialog(R.string.dialog_prompt_create_account6, Constant.RequestCode.DIALOG_PROGRESS_CREATE_ACCOUNT);
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ECKeyPairFactory keyPair = ECKeyPairFactory.generateECKeyPair(compressed);
                    Account          account = AccountStorageFactory.getInstance().createAccount(accountName, Hex.toHexString(keyPair.getPrivateKey().toByteArray()), Hex.toHexString(keyPair.getPublicKey()), AddressFactory.generatorAddress(keyPair.getPublicKey(), com.dasset.wallet.core.ecc.Constant.AddressType.HYC), password);
                    createAccountHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.GENERATE_ECKEYPAIR_SUCCESS, account));
                } catch (Exception e) {
                    e.printStackTrace();
                    createAccountHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.GENERATE_ECKEYPAIR_FAILED, e, context.getString(R.string.dialog_prompt_unknow_error)));
                }
            }
        });
    }
}
