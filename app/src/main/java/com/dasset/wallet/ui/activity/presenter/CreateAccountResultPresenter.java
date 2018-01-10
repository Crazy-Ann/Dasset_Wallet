package com.dasset.wallet.ui.activity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.dasset.wallet.R;
import com.dasset.wallet.base.handler.ActivityHandler;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.BundleUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.MessageUtil;
import com.dasset.wallet.components.utils.ThreadPoolUtil;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.core.ecc.Account;
import com.dasset.wallet.ecc.AccountStorageFactory;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.CreateAccountResultActivity;
import com.dasset.wallet.ui.activity.contract.CreateAccountResultContract;

import java.io.IOException;

public class CreateAccountResultPresenter extends BasePresenterImplement implements CreateAccountResultContract.Presenter {

    private CreateAccountResultContract.View view;
    private CreateAccountResultHandler createAccountResultHandler;
    private Account account;

    private class CreateAccountResultHandler extends ActivityHandler<CreateAccountResultActivity> {

        public CreateAccountResultHandler(CreateAccountResultActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(CreateAccountResultActivity activity, Message message) {
            if (activity != null) {
                switch (message.what) {
                    case Constant.StateCode.ACCOUNT_EXPORT_SUCCESS:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT_SUCCESS);
                        break;
                    case Constant.StateCode.ACCOUNT_EXPORT_FAILED:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_EXPORT_ACCOUNT_FAILED);
                        break;
                    case Constant.StateCode.BACKUPS_ACCOUNT_DELETE_SUCCESS:
                        activity.onFinish("BACKUPS_ACCOUNT_DELETE_SUCCESS");
                        break;
                    case Constant.StateCode.BACKUPS_ACCOUNT_DELETE_FAILED:
                        LogUtil.getInstance().print(message.obj.toString());
                        activity.onFinish("BACKUPS_ACCOUNT_DELETE_FAILED");
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public Account getAccount() {
        return account;
    }

    public CreateAccountResultPresenter(Context context, CreateAccountResultContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            AccountStorageFactory.getInstance().initialize();
            createAccountResultHandler = new CreateAccountResultHandler((CreateAccountResultActivity) view);
            account = BundleUtil.getInstance().getParcelableData((Activity) context, Constant.BundleKey.WALLET_ACCOUNT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exportAccount(final String password) {
//        ThreadPoolUtil.execute(new Runnable() {
//            @Override
//            public void run() {
//                if (account != null) {
//                    try {
//                        AccountStorageFactory.getInstance().exportAccountToExternalStorageDirectory(account.getAddress2(), account.getPassword());
//                        createAccountResultHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.ACCOUNT_EXPORT_SUCCESS, String.format("备份文件已存储在：%s", AccountStorageFactory.getInstance().getBackupsDirectory().getAbsolutePath())));
//                    } catch (PasswordException | IOException e) {
//                        createAccountResultHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.ACCOUNT_EXPORT_FAILED, e, context.getString(R.string.dialog_prompt_unknow_error)));
//                    }
//                } else {
//                    createAccountResultHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.ACCOUNT_EXPORT_FAILED, context.getString(R.string.dialog_prompt_account_info_error)));
//                }
//            }
//        });
        ThreadPoolUtil.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    if (account != null) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType(Regex.UNLIMITED_DIRECTORY_TYPE.getRegext());
                        intent.putExtra(Intent.EXTRA_STREAM, AccountStorageFactory.getInstance().exportAccountToThird(intent, account.getAddress2(), password));
                        if (intent.resolveActivity(context.getPackageManager()) != null) {
                            ((CreateAccountResultActivity) view).startActivityForResult(Intent.createChooser(intent, context.getString(R.string.dialog_prompt_import_account_to)), Constant.RequestCode.EXPORT_ACCOUNT);
                        } else {
                            createAccountResultHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.ACCOUNT_EXPORT_FAILED, context.getString(R.string.dialog_prompt_account_info_error)));
                        }
                    }
                } catch (IOException | IllegalArgumentException e) {
                    e.printStackTrace();
                    createAccountResultHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.ACCOUNT_EXPORT_FAILED, e, context.getString(R.string.dialog_prompt_unknow_error)));
                }
            }
        });
    }

    @Override
    public void deleteBackupsAccount() {
        if (account != null) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        AccountStorageFactory.getInstance().deleteBackupsAccount(account.getAddress2());
                        createAccountResultHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.BACKUPS_ACCOUNT_DELETE_SUCCESS));
                    } catch (IOException e) {
                        e.printStackTrace();
                        createAccountResultHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.BACKUPS_ACCOUNT_DELETE_FAILED, e, context.getString(R.string.dialog_prompt_unknow_error)));
                    }
                }
            });
        }
    }
}
