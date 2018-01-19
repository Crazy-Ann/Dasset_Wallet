package com.dasset.wallet.ui.activity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.dasset.wallet.R;
import com.dasset.wallet.base.handler.ActivityHandler;
import com.dasset.wallet.components.utils.BundleUtil;
import com.dasset.wallet.components.utils.MessageUtil;
import com.dasset.wallet.components.utils.ThreadPoolUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.core.wallet.Account;
import com.dasset.wallet.ecc.AccountStorageFactory;
import com.dasset.wallet.model.AccountInfo;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.AccountRenameActivity;
import com.dasset.wallet.ui.activity.contract.AccountRenameContract;

import java.io.IOException;

public class AccountRenamePresenter extends BasePresenterImplement implements AccountRenameContract.Presenter {

    private AccountRenameContract.View view;
    private AccountRenameHandler accountRenameHandler;
    private AccountInfo accountInfo;

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    private class AccountRenameHandler extends ActivityHandler<AccountRenameActivity> {

        public AccountRenameHandler(AccountRenameActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(AccountRenameActivity activity, Message message) {
            if (activity != null) {
                switch (message.what) {
                    case Constant.StateCode.ACCOUNT_RENAME_SUCCESS:
                        activity.hideLoadingPromptDialog();
                        Account account = (Account) message.obj;
                        Intent intent = new Intent();
                        intent.putExtra(Constant.BundleKey.WALLET_ACCOUNT, account);
                        activity.setResult(Constant.ResultCode.ACCOUNT_RENAME, intent);
                        activity.onFinish("ACCOUNT_RENAME_SUCCESS");
                        break;
                    case Constant.StateCode.ACCOUNT_RENAME_FAILED:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT_FAILED);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public AccountRenamePresenter(Context context, AccountRenameContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            AccountStorageFactory.getInstance().initialize();
            accountRenameHandler = new AccountRenameHandler((AccountRenameActivity) view);
            accountInfo = BundleUtil.getInstance().getParcelableData((Activity) view, Constant.BundleKey.WALLET_ACCOUNT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void renameAccount(final String accountName) {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (accountInfo != null) {
                        Account account = AccountStorageFactory.getInstance().renameAccount(accountInfo.getAddress2(), accountName);
                        if (account != null) {
                            accountRenameHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.ACCOUNT_RENAME_SUCCESS, account));
                        } else {
                            accountRenameHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.ACCOUNT_RENAME_FAILED, context.getString(R.string.dialog_prompt_account_info_error)));
                        }
                    } else {
                        accountRenameHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.ACCOUNT_RENAME_FAILED, context.getString(R.string.dialog_prompt_account_info_error)));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    accountRenameHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.ACCOUNT_RENAME_FAILED, e, context.getString(R.string.dialog_prompt_unknow_error)));
                }

            }
        });
    }

}
