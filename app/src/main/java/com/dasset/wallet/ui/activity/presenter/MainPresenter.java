package com.dasset.wallet.ui.activity.presenter;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.dasset.wallet.R;
import com.dasset.wallet.base.handler.ActivityHandler;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.MessageUtil;
import com.dasset.wallet.components.utils.ThreadPoolUtil;
import com.dasset.wallet.components.zxing.decode.QRCodeDecode;
import com.dasset.wallet.components.zxing.listener.OnScannerCompletionListener;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.core.exception.PasswordException;
import com.dasset.wallet.ecc.AccountStorageFactory;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.MainActivity;
import com.dasset.wallet.ui.activity.contract.MainContract;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainPresenter extends BasePresenterImplement implements MainContract.Presenter {

    private MainContract.View view;
    private MainHandler       mainHandler;

    private class MainHandler extends ActivityHandler<MainActivity> {

        public MainHandler(MainActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(MainActivity activity, final Message message) {
            if (activity != null) {
                switch (message.what) {
                    case Constant.StateCode.IMPORT_ACCOUNT_SUCCESS:
                        activity.hideLoadingPromptDialog();
                        LogUtil.getInstance().print("-------->:::::::" + AccountStorageFactory.getInstance().getKeystoreDirectory().listFiles().length);
                        view.loadAccountData();
                        break;
                    case Constant.StateCode.IMPORT_ACCOUNT_FAILED:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(R.string.dialog_prompt_import_account_error, false, false, Constant.RequestCode.DIALOG_PROMPT_IMPORT_ACCOUNT_ERROR);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public MainPresenter(Context context, MainContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            AccountStorageFactory.getInstance().initialize();
            mainHandler = new MainHandler((MainActivity) view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void importAccount(final Intent data) {
        view.showLoadingPromptDialog(R.string.dialog_prompt_import_account2, Constant.RequestCode.DIALOG_PROMPT_IMPORT_ACCOUNT2);
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        try {
                            String path = generatorPathFromUri(uri);
                            if (!TextUtils.isEmpty(path)) {
                                AccountStorageFactory.getInstance().importAccount(new File(uri.getPath()));
                                mainHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.IMPORT_ACCOUNT_SUCCESS));
                            } else {
                                mainHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.IMPORT_ACCOUNT_FAILED));
                            }
                        } catch (PasswordException | IOException e) {
                            e.printStackTrace();
                            mainHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.IMPORT_ACCOUNT_FAILED, e, context.getString(R.string.dialog_prompt_unknow_error)));
                        }
                    } else {
                        mainHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.IMPORT_ACCOUNT_FAILED));
                    }
                } else {
                    mainHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.IMPORT_ACCOUNT_FAILED));
                }
            }
        });
    }

    @Override
    public String generatorPathFromUri(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String filePath = null;
            if (DocumentsContract.isDocumentUri(context, uri)) {
                String documentId = DocumentsContract.getDocumentId(uri);
                if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    String   selection     = MediaStore.Images.Media._ID + "=?";
                    String[] selectionArgs = {documentId.split(Regex.COLON.getRegext())[1]};
                    filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
                } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                    filePath = getDataColumn(context, contentUri, null, null);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                filePath = getDataColumn(context, uri, null, null);
            } else if ("file".equals(uri.getScheme())) {
                filePath = uri.getPath();
            }
            return filePath;
        } else {
            return getDataColumn(context, uri, null, null);
        }
    }

    @Override
    public String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String   path       = null;
        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor   cursor     = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndexOrThrow(projection[0]));
            cursor.close();
        }
        return path;
    }
}
