package com.dasset.wallet.ui.activity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import com.dasset.wallet.model.WalletInfo;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.CreateWalletResultActivity;
import com.dasset.wallet.ui.activity.contract.CreateWalletResultContract;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;

public class CreateWalletResultPresenter extends BasePresenterImplement implements CreateWalletResultContract.Presenter {

    private CreateWalletResultContract.View view;
    private CreateAccountResultHandler createAccountResultHandler;
    private WalletInfo walletInfo;

    public WalletInfo getWalletInfo() {
        return walletInfo;
    }

    private class CreateAccountResultHandler extends ActivityHandler<CreateWalletResultActivity> {

        public CreateAccountResultHandler(CreateWalletResultActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(CreateWalletResultActivity activity, Message message) {
            if (activity != null) {
                switch (message.what) {
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

    public CreateWalletResultPresenter(Context context, CreateWalletResultContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        createAccountResultHandler = new CreateAccountResultHandler((CreateWalletResultActivity) view);
        walletInfo = BundleUtil.getInstance().getParcelableData((Activity) context, Constant.BundleKey.WALLET_INFO);
    }

    @Override
    public void generateAddresQRCode() {
        if (walletInfo != null) {
            view.showAddressQRCodePromptDialog(QRCodeEncode.createQRCode(walletInfo.getHdAccount().getFirstAddressFromDb(), ViewUtil.getInstance().dp2px(context, 160)), walletInfo.getHdAccount().getFirstAddressFromDb());
        } else {
            view.showPromptDialog(context.getString(R.string.dialog_prompt_get_qrcode_bitmap_error), false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_BITMAP_GET_ERROR);
        }
    }

    @Override
    public void save() {
        if (walletInfo != null) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    if (!TextUtils.isEmpty(walletInfo.getHdAccount().getFirstAddressFromDb())) {
                        OutputStream outputStream = null;
                        InputStream inputStream = null;
                        try {
                            inputStream = new ByteArrayInputStream(QRCodeEncode.createQRCode(walletInfo.getHdAccount().getFirstAddressFromDb(), ViewUtil.getInstance().dp2px(context, 160)));
                            SoftReference softReference = new SoftReference(BitmapFactory.decodeStream(inputStream));
                            Bitmap bitmap = (Bitmap) softReference.get();
                            if (bitmap != null) {
                                String fileName = walletInfo.getHdAccount().getFirstAddressFromDb() + Regex.IMAGE_JPG.getRegext();
                                File file = new File(IOUtil.getInstance().getExternalStorageDirectory(Constant.FilePath.IMAGE_CACHE), fileName);
                                outputStream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                if (!TextUtils.isEmpty(MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null))) {
                                    createAccountResultHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_SAVE_SUCCESS, context.getString(R.string.dialog_prompt_qrcode_save_success)));
                                } else {
                                    createAccountResultHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_SAVE_FAILED, context.getString(R.string.dialog_prompt_get_qrcode_bitmap_error)));
                                }
                            } else {
                                createAccountResultHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_SAVE_FAILED, context.getString(R.string.dialog_prompt_get_qrcode_bitmap_error)));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            createAccountResultHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.QRCODE_SAVE_FAILED, e, context.getString(R.string.dialog_prompt_qrcode_save_error)));
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
                                createAccountResultHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.QRCODE_SAVE_FAILED, e, context.getString(R.string.dialog_prompt_qrcode_save_error)));
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
        if (walletInfo != null) {
            ThreadPoolUtil.execute(new Runnable() {

                @Override
                public void run() {
                    OutputStream outputStream = null;
                    InputStream inputStream = null;
                    try {
                        if (!TextUtils.isEmpty(walletInfo.getHdAccount().getFirstAddressFromDb())) {
                            inputStream = new ByteArrayInputStream(QRCodeEncode.createQRCode(walletInfo.getHdAccount().getFirstAddressFromDb(), ViewUtil.getInstance().dp2px(context, 160)));
                            SoftReference softReference = new SoftReference(BitmapFactory.decodeStream(inputStream));
                            Bitmap bitmap = (Bitmap) softReference.get();
                            if (bitmap != null) {
                                String fileName = walletInfo.getHdAccount().getFirstAddressFromDb() + Regex.IMAGE_JPG.getRegext();
                                File file = new File(IOUtil.getInstance().getExternalStorageDirectory(Constant.FilePath.IMAGE_CACHE), fileName);
                                outputStream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                outputStream.flush();
                                MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
                                createAccountResultHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_SHARE_SUCCESS, file));
                            } else {
                                createAccountResultHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_SHARE_FAILED, context.getString(R.string.dialog_prompt_qrcode_share_error)));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        createAccountResultHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.QRCODE_SHARE_FAILED, e, context.getString(R.string.dialog_prompt_qrcode_share_error)));
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
                            createAccountResultHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.QRCODE_SHARE_FAILED, e, context.getString(R.string.dialog_prompt_qrcode_share_error)));
                        }
                    }
                }
            });
        } else {
            view.showPromptDialog(context.getString(R.string.dialog_prompt_qrcode_share_error), false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_SHARE_ERROR);
        }
    }

    @Override
    public void backupsWallet() {

    }
}
