package com.dasset.wallet.ui.activity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.dasset.wallet.R;
import com.dasset.wallet.base.handler.ActivityHandler;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.BundleUtil;
import com.dasset.wallet.components.utils.FileProviderUtil;
import com.dasset.wallet.components.utils.IOUtil;
import com.dasset.wallet.components.utils.MessageUtil;
import com.dasset.wallet.components.utils.ThreadPoolUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.components.zxing.encode.QRCodeEncode;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.ecc.AccountStorageFactory;
import com.dasset.wallet.model.AccountInfo;
import com.dasset.wallet.model.TransactionRecords;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.AccountInfoActivity;
import com.dasset.wallet.ui.activity.AccountRenameActivity;
import com.dasset.wallet.ui.activity.contract.AccountInfoContract;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;

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
        protected void handleMessage(AccountInfoActivity activity, Message message) {
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
                    case Constant.StateCode.ACCOUNT_DELETE_SUCCESS:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_DELETE_ACCOUNT_SUCCESS);
                        break;
                    case Constant.StateCode.ACCOUNT_DELETE_FAILED:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_DELETE_ACCOUNT_FAILED);
                        break;
                    case Constant.StateCode.QRCODE_SAVE_SUCCESS:
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_SAVE_SUCCESS);
                        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(Regex.FILE_URI.getRegext() + Environment.getExternalStorageDirectory())));
                        break;
                    case Constant.StateCode.QRCODE_SAVE_FAILED:
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_SAVE_ERROR);
                        break;
                    case Constant.StateCode.QRCODE_SHARE_SUCCESS:
                        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(Regex.FILE_URI.getRegext() + Environment.getExternalStorageDirectory())));
                        File file = (File) message.obj;
                        if (file != null && file.exists()) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType(Regex.IMAGE_DIRECTORY_TYPE.getRegext());
                            intent.putExtra(Intent.EXTRA_STREAM, FileProviderUtil.getInstance().generateUri(activity, intent, file));
                            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                                activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.dialog_prompt_import_account_to)), Constant.RequestCode.EXPORT_QRCODE);
                            } else {
                                activity.showPromptDialog(R.string.dialog_prompt_qrcode_share_error, false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_SHARE_ERROR);
                            }
                        } else {
                            activity.showPromptDialog(R.string.dialog_prompt_qrcode_share_error, false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_SHARE_ERROR);
                        }
                        break;
                    case Constant.StateCode.QRCODE_SHARE_FAILED:
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_SHARE_ERROR);
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
    public TransactionRecords getTransactionRecords() {
        //todo
//        TransactionRecords transactionRecords = new TransactionRecords();
//        List<TransactionRecord> records = Lists.newArrayList();
//        for (int i = 0; i < 10; i++) {
//            TransactionRecord transactionRecord = new TransactionRecord();
//            transactionRecord.setAssetName("Intel" + i);
//            transactionRecord.setAssetAmount(String.valueOf(i * 20));
//            transactionRecord.setAssetType("btc");
//            transactionRecord.setTransactionDate(new SimpleDateFormat(Regex.DATE_FORMAT_ALL.getRegext(), Locale.getDefault()).format(new Date(System.currentTimeMillis())));
//            records.add(transactionRecord);
//        }
//        transactionRecords.setTransactionRecords(records);
//        return transactionRecords;
        return null;
    }

    @Override
    public void exportAccount(final String password) {
//        ThreadPoolUtil.execute(new Runnable() {
//            @Override
//            public void run() {
//                if (accountInfo != null) {
//                    try {
//                        AccountStorageFactory.getInstance().exportAccountToExternalStorageDirectory(accountInfo.getAddress2(), accountInfo.getPassword());
//                        accountInfoHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.ACCOUNT_EXPORT_SUCCESS, String.format("备份文件已存储在：%s", AccountStorageFactory.getInstance().getBackupsDirectory().getAbsolutePath())));
//                    } catch (PasswordException | IOException e) {
//                        accountInfoHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.ACCOUNT_EXPORT_FAILED, e, context.getString(R.string.dialog_prompt_unknow_error)));
//                    }
//                } else {
//                    accountInfoHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.ACCOUNT_EXPORT_FAILED, context.getString(R.string.dialog_prompt_account_info_error)));
//                }
//            }
//        });
        ThreadPoolUtil.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    if (accountInfo != null) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType(Regex.UNLIMITED_DIRECTORY_TYPE.getRegext());
                        intent.putExtra(Intent.EXTRA_STREAM, AccountStorageFactory.getInstance().exportAccountToThird(intent, accountInfo.getAddress2(), password));
                        if (intent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.dialog_prompt_import_account_to)));
                        } else {
                            accountInfoHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.ACCOUNT_EXPORT_FAILED, context.getString(R.string.dialog_prompt_account_info_error)));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    accountInfoHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.ACCOUNT_EXPORT_FAILED, e, context.getString(R.string.dialog_prompt_unknow_error)));
                }
            }
        });
    }

    @Override
    public void renameAccount() {
        if (accountInfo != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constant.BundleKey.WALLET_ACCOUNT, accountInfo);
            ((AccountInfoActivity) view).startActivityForResult(AccountRenameActivity.class, Constant.RequestCode.ACCOUNT_RENAME, bundle);
        }
    }

    @Override
    public void deleteAccount() {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                if (accountInfo != null) {
                    try {
                        AccountStorageFactory.getInstance().deleteAccount(accountInfo.getAddress2());
                        accountInfoHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.ACCOUNT_DELETE_SUCCESS, String.format("账户%s解除成功", accountInfo.getAddress2())));
                    } catch (Exception e) {
                        e.printStackTrace();
                        accountInfoHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.ACCOUNT_DELETE_FAILED, e, context.getString(R.string.dialog_prompt_unknow_error)));
                    }
                } else {
                    accountInfoHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.ACCOUNT_DELETE_FAILED, context.getString(R.string.dialog_prompt_account_info_error)));
                }
            }
        });
    }

    @Override
    public void generateAddresQRCode() {
        if (accountInfo != null) {
            view.showAddressQRCodePromptDialog(QRCodeEncode.createQRCode(accountInfo.getAddress2(), ViewUtil.getInstance().dp2px(context, 160)), accountInfo.getAddress2());
        } else {
            view.showPromptDialog(context.getString(R.string.dialog_prompt_get_qrcode_bitmap_error), false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_BITMAP_GET_ERROR);
        }
    }

    @Override
    public void save() {
        if (accountInfo != null) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    if (!TextUtils.isEmpty(accountInfo.getAddress2())) {
                        OutputStream outputStream = null;
                        InputStream inputStream = null;
                        try {
                            inputStream = new ByteArrayInputStream(QRCodeEncode.createQRCode(accountInfo.getAddress2(), ViewUtil.getInstance().dp2px(context, 160)));
                            SoftReference softReference = new SoftReference(BitmapFactory.decodeStream(inputStream));
                            Bitmap bitmap = (Bitmap) softReference.get();
                            if (bitmap != null) {
                                String fileName = accountInfo.getAddress2() + Regex.IMAGE_JPG.getRegext();
                                File file = new File(IOUtil.getInstance().getExternalStorageDirectory(Constant.FilePath.IMAGE_CACHE), fileName);
                                outputStream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                if (!TextUtils.isEmpty(MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null))) {
                                    accountInfoHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_SAVE_SUCCESS, context.getString(R.string.dialog_prompt_qrcode_save_success)));
                                } else {
                                    accountInfoHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_SAVE_FAILED, context.getString(R.string.dialog_prompt_get_qrcode_bitmap_error)));
                                }
                            } else {
                                accountInfoHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_SAVE_FAILED, context.getString(R.string.dialog_prompt_get_qrcode_bitmap_error)));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            accountInfoHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.QRCODE_SAVE_FAILED, e, context.getString(R.string.dialog_prompt_qrcode_save_error)));
                        } finally {
                            try {
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                                if (outputStream != null) {
                                    outputStream.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                accountInfoHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.QRCODE_SAVE_FAILED, e, context.getString(R.string.dialog_prompt_qrcode_save_error)));
                            }
                        }
                    }
                }
            });
        } else {
            view.showPromptDialog(context.getString(R.string.dialog_prompt_qrcode_save_error), false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_SAVE_ERROR);
        }
    }

    @Override
    public void share() {
        if (accountInfo != null) {
            ThreadPoolUtil.execute(new Runnable() {

                @Override
                public void run() {
                    OutputStream outputStream = null;
                    InputStream inputStream = null;
                    try {
                        if (!TextUtils.isEmpty(accountInfo.getAddress2())) {
                            inputStream = new ByteArrayInputStream(QRCodeEncode.createQRCode(accountInfo.getAddress2(), ViewUtil.getInstance().dp2px(context, 160)));
                            SoftReference softReference = new SoftReference(BitmapFactory.decodeStream(inputStream));
                            Bitmap bitmap = (Bitmap) softReference.get();
                            if (bitmap != null) {
                                String fileName = accountInfo.getAddress2() + Regex.IMAGE_JPG.getRegext();
                                File file = new File(IOUtil.getInstance().getExternalStorageDirectory(Constant.FilePath.IMAGE_CACHE), fileName);
                                outputStream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                outputStream.flush();
                                MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
                                accountInfoHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_SHARE_SUCCESS, file));
                            } else {
                                accountInfoHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_SHARE_FAILED, context.getString(R.string.dialog_prompt_qrcode_share_error)));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        accountInfoHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.QRCODE_SHARE_FAILED, e, context.getString(R.string.dialog_prompt_qrcode_share_error)));
                    } finally {
                        try {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            accountInfoHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.QRCODE_SHARE_FAILED, e, context.getString(R.string.dialog_prompt_qrcode_share_error)));
                        }
                    }
                }
            });
        } else {
            view.showPromptDialog(context.getString(R.string.dialog_prompt_qrcode_share_error), false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_SHARE_ERROR);
        }
    }
}
