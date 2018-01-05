package com.dasset.wallet.ui.activity.presenter;

import android.app.Activity;
import android.content.Context;
import android.os.Message;

import com.dasset.wallet.R;
import com.dasset.wallet.base.handler.ActivityHandler;
import com.dasset.wallet.components.utils.BundleUtil;
import com.dasset.wallet.components.utils.MessageUtil;
import com.dasset.wallet.components.utils.ThreadPoolUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.core.exception.PasswordException;
import com.dasset.wallet.ecc.AccountStorageFactory;
import com.dasset.wallet.model.AccountInfo;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.AccountInfoActivity;
import com.dasset.wallet.ui.activity.contract.AccountInfoContract;

import java.io.IOException;

public class AccountInfoPresenter extends BasePresenterImplement implements AccountInfoContract.Presenter {

    private AccountInfoContract.View view;
    private AccountInfoHandler accountInfoHandler;
    private AccountInfo accountInfo;

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    private class AccountInfoHandler extends ActivityHandler<AccountInfoActivity> {

        public AccountInfoHandler(AccountInfoActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(AccountInfoActivity activity, Message msg) {
            if (activity != null) {
                switch (msg.what) {
                    case Constant.StateCode.EXPORT_ACCOUNT_SUCCESS:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(msg.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT_SUCCESS);
                        break;
                    case Constant.StateCode.EXPORT_ACCOUNT_FAILED:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(msg.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT_FAILED);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public AccountInfoPresenter(Context context, AccountInfoContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            AccountStorageFactory.getInstance().initialize();
            accountInfoHandler = new AccountInfoHandler((AccountInfoActivity) view);
            accountInfo = BundleUtil.getInstance().getParcelableData((Activity) view, Constant.BundleKey.WALLET_ACCOUNT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void exportAccount() {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                if (accountInfo != null) {
                    try {
                        AccountStorageFactory.getInstance().exportAccount(accountInfo.getAddress(), accountInfo.getPassword());
                        accountInfoHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.EXPORT_ACCOUNT_SUCCESS, String.format("备份文件已存储在：%s", AccountStorageFactory.getInstance().getBackupFileDirectory().getAbsolutePath())));
                    } catch (PasswordException | IOException e) {
                        accountInfoHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.EXPORT_ACCOUNT_FAILED, e, context.getString(R.string.dialog_prompt_unknow_error)));
                    }
                } else {
                    accountInfoHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.EXPORT_ACCOUNT_FAILED, context.getString(R.string.dialog_prompt_account_info_error)));
                }
            }
        });
    }

    @Override
    public void renameAccount() {

    }

    @Override
    public void deleteAccount() {

    }
}
