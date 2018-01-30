package com.dasset.wallet.ui.activity.presenter;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.dasset.wallet.R;
import com.dasset.wallet.base.handler.ActivityHandler;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.FileProviderUtil;
import com.dasset.wallet.components.utils.IOUtil;
import com.dasset.wallet.components.utils.MessageUtil;
import com.dasset.wallet.components.utils.ThreadPoolUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.components.zxing.encode.QRCodeEncode;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.ecc.AccountStorageFactory;
import com.dasset.wallet.model.Menus;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.MainActivity;
import com.dasset.wallet.ui.activity.contract.MainContract;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainPresenter extends BasePresenterImplement implements MainContract.Presenter {

    private MainContract.View view;
    private MainHandler       mainHandler;

    private Menus  menus;
    private String address;

    private class MainHandler extends ActivityHandler<MainActivity> {

        public MainHandler(MainActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(MainActivity activity, final Message message) {
            if (activity != null) {
                switch (message.what) {
                    case Constant.StateCode.ACCOUNT_IMPORT_SUCCESS:
                        activity.hideLoadingPromptDialog();
                        activity.loadAccountData();
                        break;
                    case Constant.StateCode.ACCOUNT_IMPORT_FAILED:
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_IMPORT_ACCOUNT_ERROR);
                        break;
                    case Constant.StateCode.QRCODE_SAVE_SUCCESS:
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_SAVE_QRCODE_SUCCESS);
                        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(Regex.FILE_URI.getRegext() + Environment.getExternalStorageDirectory())));
                        break;
                    case Constant.StateCode.QRCODE_SAVE_FAILED:
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_SAVE_QRCODE_ERROR);
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
                                activity.showPromptDialog(R.string.dialog_prompt_qrcode_share_error, false, false, Constant.RequestCode.DIALOG_PROMPT_SHARE_QRCODE_ERROR);
                            }
                        } else {
                            activity.showPromptDialog(R.string.dialog_prompt_qrcode_share_error, false, false, Constant.RequestCode.DIALOG_PROMPT_SHARE_QRCODE_ERROR);
                        }
                        break;
                    case Constant.StateCode.QRCODE_SHARE_FAILED:
                        activity.showPromptDialog(message.obj.toString(), false, false, Constant.RequestCode.DIALOG_PROMPT_SHARE_QRCODE_ERROR);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public Menus getMenus() {
        return menus;
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
            menus = new Menus().parse(JSONObject.parseObject(IOUtil.getInstance().readAsset(context, "menu.json")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void importAccount(final String path, final String password) {
        view.showLoadingPromptDialog(R.string.dialog_prompt_import_account2, Constant.RequestCode.DIALOG_PROMPT_IMPORT_ACCOUNT2);
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!TextUtils.isEmpty(path)) {
                        AccountStorageFactory.getInstance().importAccount(new File(path), password);
                        mainHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.ACCOUNT_IMPORT_SUCCESS));
                    } else {
                        mainHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.ACCOUNT_IMPORT_FAILED, context.getString(R.string.dialog_prompt_import_account_error)));
                    }
                } catch (IOException | JSONException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    mainHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.ACCOUNT_IMPORT_FAILED, e, context.getString(R.string.dialog_prompt_unknow_error)));
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

    @Override
    public void generateAddresQRCode(int position) {
        try {
            address = AccountStorageFactory.getInstance().getAccountInfos(AccountStorageFactory.getInstance().getKeystoreDirectory()).get(position).getAddress2();
            view.showAddressQRCodePromptDialog(QRCodeEncode.createQRCode(address, ViewUtil.getInstance().dp2px(context, 160)), address);
        } catch (IOException e) {
            e.printStackTrace();
            view.showPromptDialog(context.getString(R.string.dialog_prompt_get_qrcode_bitmap_error), false, false, Constant.RequestCode.DIALOG_PROMPT_QRCODE_BITMAP_GET_ERROR);
        }
    }

    @Override
    public void save() {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(address)) {
                    OutputStream outputStream = null;
                    InputStream  inputStream  = null;
                    try {
                        inputStream = new ByteArrayInputStream(QRCodeEncode.createQRCode(address, ViewUtil.getInstance().dp2px(context, 160)));
                        SoftReference softReference = new SoftReference(BitmapFactory.decodeStream(inputStream));
                        Bitmap        bitmap        = (Bitmap) softReference.get();
                        if (bitmap != null) {
                            String fileName = address + Regex.IMAGE_JPG.getRegext();
                            File   file     = new File(IOUtil.getInstance().getExternalStorageDirectory(Constant.FilePath.IMAGE_CACHE), fileName);
                            outputStream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            if (!TextUtils.isEmpty(MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null))) {
                                mainHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_SAVE_SUCCESS, context.getString(R.string.dialog_prompt_qrcode_save_success)));
                            } else {
                                mainHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_SAVE_FAILED, context.getString(R.string.dialog_prompt_get_qrcode_bitmap_error)));
                            }
                        } else {
                            mainHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_SAVE_FAILED, context.getString(R.string.dialog_prompt_get_qrcode_bitmap_error)));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        mainHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.QRCODE_SAVE_FAILED, e, context.getString(R.string.dialog_prompt_qrcode_save_error)));
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
                            mainHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.QRCODE_SAVE_FAILED, e, context.getString(R.string.dialog_prompt_qrcode_save_error)));
                        }
                    }
                }
            }
        });
    }

    @Override
    public void share() {
        ThreadPoolUtil.execute(new Runnable() {

            @Override
            public void run() {
                OutputStream outputStream = null;
                InputStream  inputStream  = null;
                try {
                    if (!TextUtils.isEmpty(address)) {
                        inputStream = new ByteArrayInputStream(QRCodeEncode.createQRCode(address, ViewUtil.getInstance().dp2px(context, 160)));
                        SoftReference softReference = new SoftReference(BitmapFactory.decodeStream(inputStream));
                        Bitmap        bitmap        = (Bitmap) softReference.get();
                        if (bitmap != null) {
                            String fileName = address + Regex.IMAGE_JPG.getRegext();
                            File   file     = new File(IOUtil.getInstance().getExternalStorageDirectory(Constant.FilePath.IMAGE_CACHE), fileName);
                            outputStream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            outputStream.flush();
                            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
                            mainHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_SHARE_SUCCESS, file));
                        } else {
                            mainHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_SHARE_FAILED, context.getString(R.string.dialog_prompt_qrcode_share_error)));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    mainHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.QRCODE_SHARE_FAILED, e, context.getString(R.string.dialog_prompt_qrcode_share_error)));
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
                        mainHandler.sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.QRCODE_SHARE_FAILED, e, context.getString(R.string.dialog_prompt_qrcode_share_error)));
                    }
                }
            }
        });
    }
}
