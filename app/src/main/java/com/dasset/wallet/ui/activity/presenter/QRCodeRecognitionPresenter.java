package com.dasset.wallet.ui.activity.presenter;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.dasset.wallet.R;
import com.dasset.wallet.base.handler.ActivityHandler;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.BitmapUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.MessageUtil;
import com.dasset.wallet.components.utils.ThreadPoolUtil;
import com.dasset.wallet.components.zxing.camera.CameraManager;
import com.dasset.wallet.components.zxing.decode.CaptureActivityHandler;
import com.dasset.wallet.components.zxing.decode.InactivityTimer;
import com.dasset.wallet.components.zxing.decode.QRCodeDecode;
import com.dasset.wallet.components.zxing.listener.OnDecodeHandlerListener;
import com.dasset.wallet.components.zxing.listener.OnScannerCompletionListener;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.ui.BasePresenterImplement;
import com.dasset.wallet.ui.activity.QRCodeRecognitionActivity;
import com.dasset.wallet.ui.activity.contract.QRCodeRecognitionContract;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

public class QRCodeRecognitionPresenter extends BasePresenterImplement implements QRCodeRecognitionContract.Presenter, MediaPlayer.OnCompletionListener {

    private QRCodeRecognitionContract.View view;

    private boolean                  hasSurface;
    private Vector<BarcodeFormat>    decodeFormats;
    private String                   characterSet;
    private InactivityTimer          inactivityTimer;
    private MediaPlayer              mediaPlayer;
    private boolean                  playBeep;
    private boolean                  vibrate;
    private CaptureActivityHandler   captureActivityHandler;
    private QRCodeRecognitionHandler qrCodeRecognitionHandler;

    private class QRCodeRecognitionHandler extends ActivityHandler<QRCodeRecognitionActivity> {

        public QRCodeRecognitionHandler(QRCodeRecognitionActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(QRCodeRecognitionActivity activity, final Message message) {
            if (activity != null) {
                switch (message.what) {
                    case Constant.StateCode.GET_QRCODE_BITMAP_SUCCESS:
                        final String path = message.obj.toString();
                        view.showLoadingPromptDialog(R.string.prompt_qrcode_recognition, Constant.RequestCode.DIALOG_PROGRESS_QRCODE_RECOGNITION);
                        ThreadPoolUtil.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    QRCodeDecode.decodeQR(path, new OnScannerCompletionListener() {

                                        @Override
                                        public void OnScannerCompletion(Result result, Bitmap barcode) {
                                            if (!TextUtils.isEmpty(result.getText())) {
                                                sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_RECOGNITION_SUCCESS, result));
                                            } else {
                                                sendMessage(MessageUtil.getMessage(Constant.StateCode.QRCODE_RECOGNITION_FAILED));
                                            }
                                        }
                                    });
                                } catch (NotFoundException | ChecksumException | FileNotFoundException | FormatException e) {
                                    e.printStackTrace();
                                    sendMessage(MessageUtil.getErrorMessage(Constant.StateCode.QRCODE_RECOGNITION_FAILED, e, context.getString(R.string.dialog_prompt_unknow_error)));
                                }
                            }
                        });
                        break;
                    case Constant.StateCode.GET_QRCODE_BITMAP_FAILED:
                        stopScan();
                        activity.showPromptDialog(context.getString(R.string.dialog_prompt_get_qrcode_bitmap_error), false, false, Constant.RequestCode.DIALOG_PROMPT_GET_QRCODE_BITMAP_ERROR);
                        break;
                    case Constant.StateCode.QRCODE_RECOGNITION_SUCCESS:
                        activity.hideLoadingPromptDialog();
                        Intent intent = new Intent();
                        intent.putExtra(Constant.BundleKey.QRCODE_RESULT, ((Result) message.obj).getText());
                        ((Activity) view).setResult(Constant.ResultCode.QRCODE_RECOGNITION, intent);
                        ((Activity) view).finish();
                        break;
                    case Constant.StateCode.QRCODE_RECOGNITION_FAILED:
                        stopScan();
                        activity.hideLoadingPromptDialog();
                        activity.showPromptDialog(context.getString(R.string.dialog_prompt_qrcode_recognition_error), false, false, Constant.RequestCode.DIALOG_PROMPT_GET_QRCODE_BITMAP_ERROR);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public boolean isHasSurface() {
        return hasSurface;
    }

    public void setHasSurface(boolean hasSurface) {
        this.hasSurface = hasSurface;
    }

    public Vector<BarcodeFormat> getDecodeFormats() {
        return decodeFormats;
    }

    public String getCharacterSet() {
        return characterSet;
    }

    public InactivityTimer getInactivityTimer() {
        return inactivityTimer;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public boolean isPlayBeep() {
        return playBeep;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public CaptureActivityHandler getCaptureActivityHandler() {
        return captureActivityHandler;
    }

    public QRCodeRecognitionPresenter(Context context, QRCodeRecognitionContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void initialize() {
        super.initialize();
        qrCodeRecognitionHandler = new QRCodeRecognitionHandler((QRCodeRecognitionActivity) view);
        hasSurface = false;
        inactivityTimer = new InactivityTimer((Activity) context);
        decodeFormats = null;
        characterSet = null;
        playBeep = true;
        AudioManager audioService = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initializeBeepSound();
        vibrate = true;
    }

    @Override
    public void initializeCamera(OnDecodeHandlerListener listener, SurfaceHolder holder) {
        try {
            CameraManager.get().openDriver(holder);
        } catch (IOException | RuntimeException ioe) {
            return;
        }
        if (captureActivityHandler == null) {
            captureActivityHandler = new CaptureActivityHandler(listener, decodeFormats, characterSet);
        }
    }

    @Override
    public void startScan(OnDecodeHandlerListener listener, SurfaceHolder.Callback callback, SurfaceView surfaceView) {
        SurfaceHolder surfaceHolder = (surfaceView).getHolder();
        if (hasSurface) {
            initializeCamera(listener, surfaceHolder);
        } else {
            surfaceHolder.addCallback(callback);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    @Override
    public void stopScan() {
        if (captureActivityHandler != null) {
            captureActivityHandler.quitSynchronously();
            captureActivityHandler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    public void initializeBeepSound() {
        if (playBeep && mediaPlayer == null) {
            ((Activity) context).setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(this);
            AssetFileDescriptor file = context.getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(com.dasset.wallet.components.constant.Constant.Scanner.BEEP_VOLUME, com.dasset.wallet.components.constant.Constant.Scanner.BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
                e.printStackTrace();
            }
        }
    }

    @Override
    public void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(com.dasset.wallet.components.constant.Constant.Scanner.VIBRATE_DURATION, com.dasset.wallet.components.constant.Constant.Scanner.VIBRATE_AMPLITUDE));
            } else {
                vibrator.vibrate(com.dasset.wallet.components.constant.Constant.Scanner.VIBRATE_DURATION);
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.seekTo(0);
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
    public void getQRCodeBitmap(final Intent data) {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        String path = generatorPathFromUri(uri);
                        if (!TextUtils.isEmpty(path)) {
                            qrCodeRecognitionHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.GET_QRCODE_BITMAP_SUCCESS, path));
                        } else {
                            qrCodeRecognitionHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.GET_QRCODE_BITMAP_FAILED));
                        }
                    }
                } else {
                    qrCodeRecognitionHandler.sendMessage(MessageUtil.getMessage(Constant.StateCode.GET_QRCODE_BITMAP_FAILED));
                }
            }
        });
    }
}
