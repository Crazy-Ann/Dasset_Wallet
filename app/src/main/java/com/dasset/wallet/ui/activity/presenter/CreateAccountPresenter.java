package com.dasset.wallet.ui.activity.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import com.dasset.wallet.R;
import com.dasset.wallet.base.handler.ActivityHandler;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.DeviceUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.MessageUtil;
import com.dasset.wallet.components.utils.ThreadPoolUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.core.wallet.Account;
import com.dasset.wallet.core.wallet.ECKeyPairFactory;
import com.dasset.wallet.ecc.AccountStorageFactory;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.CreateAccountActivity;
import com.dasset.wallet.ui.activity.contract.CreateAccountContract;

import org.spongycastle.util.encoders.Hex;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CreateAccountPresenter extends BasePresenterImplement implements CreateAccountContract.Presenter {

    private CreateAccountContract.View view;
    private CreateAccountHandler createAccountHandler;

    private class CreateAccountHandler extends ActivityHandler<CreateAccountActivity> {

        public CreateAccountHandler(CreateAccountActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(CreateAccountActivity activity, Message message) {
            if (activity != null) {
                switch (message.what) {
                    case Constant.StateCode.ECKEYPAIR_GENERATE_SUCCESS:
                        activity.hideLoadingPromptDialog();
                        Account account = (Account) message.obj;
                        LogUtil.getInstance().print(account.toString());
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(Constant.BundleKey.WALLET_ACCOUNT, account);
                        activity.startCreateAccountResultActivity(bundle);
                        break;
                    case Constant.StateCode.ECKEYPAIR_GENERATE_FAILED:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_CREATE_ACCOUNT_ERROR);
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
                    ECKeyPairFactory ecKeyPairFactory = ECKeyPairFactory.generateECKeyPair(compressed);
                    DateFormat dateFormat = new SimpleDateFormat(Regex.UTC_DATE_FORMAT_ALL.getRegext());
                    dateFormat.setTimeZone(TimeZone.getTimeZone(Regex.UTC.getRegext()));
                    String timestamp = Regex.UTC.getRegext() + Regex.DOUBLE_MINUS.getRegext() + dateFormat.format(new Date());
                    Account account = AccountStorageFactory.getInstance().createAccount(DeviceUtil.getInstance().getDeviceId(context), timestamp, Regex.AES_128_ECB.getRegext(), accountName, Hex.toHexString(ecKeyPairFactory.getPrivateKey().toByteArray()), password, timestamp, false);
                    createAccountHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.ECKEYPAIR_GENERATE_SUCCESS, account));
                } catch (Exception e) {
                    e.printStackTrace();
                    createAccountHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.ECKEYPAIR_GENERATE_FAILED, e, context.getString(R.string.dialog_prompt_unknow_error)));
                }
            }
        });
    }
}
