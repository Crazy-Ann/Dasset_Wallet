package com.dasset.wallet.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.dasset.wallet.R;
import com.dasset.wallet.base.toolbar.listener.OnLeftIconEventListener;
import com.dasset.wallet.base.toolbar.listener.OnRightTextEventListener;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.permission.listener.PermissionCallback;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.components.zxing.listener.OnDecodeHandlerListener;
import com.dasset.wallet.components.zxing.view.ViewfinderView;
import com.dasset.wallet.constant.Constant;
import com.dasset.wallet.ui.ActivityViewImplement;
import com.dasset.wallet.ui.activity.contract.QRCodeRecognitionContract;
import com.dasset.wallet.ui.activity.presenter.QRCodeRecognitionPresenter;
import com.google.zxing.Result;

import java.util.List;

public class QRCodeRecognitionActivity extends ActivityViewImplement<QRCodeRecognitionContract.Presenter> implements QRCodeRecognitionContract.View, OnLeftIconEventListener, OnRightTextEventListener, SurfaceHolder.Callback, OnDecodeHandlerListener {

    private QRCodeRecognitionPresenter qrCodeRecognitionPresenter;
    private ViewfinderView vfvFinder;
    private SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_recognition);
        getSavedInstanceState(savedInstanceState);
        findViewById();
        initialize(savedInstanceState);
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            qrCodeRecognitionPresenter.checkPermission(new PermissionCallback() {

                @Override
                public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                    qrCodeRecognitionPresenter.startScan(QRCodeRecognitionActivity.this, QRCodeRecognitionActivity.this, surfaceView);
                }

                @Override
                public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                    showPermissionPromptDialog();
                }
            });
        } else {
            qrCodeRecognitionPresenter.startScan(this, this, surfaceView);
        }
    }

    @Override
    protected void findViewById() {
        inToolbar = ViewUtil.getInstance().findView(this, R.id.inToolbar);
        vfvFinder = ViewUtil.getInstance().findView(this, R.id.vfvFinder);
        surfaceView = ViewUtil.getInstance().findView(this, R.id.svPreview);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        initializeToolbar(R.color.color_383856, true, R.mipmap.icon_back_white, this, android.R.color.white, getString(R.string.qrcode_recognition_scan), android.R.color.white, getString(R.string.qrcode_recognition_album), this);
        qrCodeRecognitionPresenter = new QRCodeRecognitionPresenter(this, this);
        qrCodeRecognitionPresenter.initialize();
        setBasePresenterImplement(qrCodeRecognitionPresenter);
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.RequestCode.NET_WORK_SETTING:
            case Constant.RequestCode.PREMISSION_SETTING:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    qrCodeRecognitionPresenter.checkPermission(new PermissionCallback() {

                        @Override
                        public void onSuccess(int requestCode, @NonNull List<String> grantPermissions) {
                            //TODO
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                            showPermissionPromptDialog();
                        }
                    });
                } else {
                    //TODO
                }
                break;
            case Constant.RequestCode.QRCODE_RECOGNITION_ALBUM:
                hideLoadingPromptDialog();
                if (data != null) {
                    qrCodeRecognitionPresenter.getQRCodeBitmap(data);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        qrCodeRecognitionPresenter.stopScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        qrCodeRecognitionPresenter.getInactivityTimer().shutdown();
    }

    @Override
    public void onPositiveButtonClicked(int requestCode) {
        switch (requestCode) {
            case Constant.RequestCode.DIALOG_PROMPT_SET_NET_WORK:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_NET_WORK_ERROR");
                startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), Constant.RequestCode.NET_WORK_SETTING);
                break;
            case Constant.RequestCode.DIALOG_PROMPT_SET_PERMISSION:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_SET_PERMISSION");
                startPermissionSettingActivity();
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QRCODE_RECOGNITION_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DDIALOG_PROMPT_QRCODE_RECOGNITION_ERROR");
                qrCodeRecognitionPresenter.startScan(this, this, surfaceView);
                break;
            case Constant.RequestCode.DIALOG_PROMPT_QRCODE_BITMAP_GET_ERROR:
                LogUtil.getInstance().print("onPositiveButtonClicked_DIALOG_PROMPT_GET_QRCODE_BITMAP_ERROR");
                qrCodeRecognitionPresenter.startScan(this, this, surfaceView);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNegativeButtonClicked(int requestCode) {
        switch (requestCode) {
            case Constant.RequestCode.DIALOG_PROMPT_SET_NET_WORK:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_SET_NET_WORK");
                break;
            case Constant.RequestCode.DIALOG_PROMPT_SET_PERMISSION:
                LogUtil.getInstance().print("onNegativeButtonClicked_DIALOG_PROMPT_SET_PERMISSION");
                refusePermissionSetting();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void onLeftIconEvent() {
        onFinish("onLeftIconEvent");
    }

    @Override
    public void onRightTextEvent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(Regex.IMAGE_DIRECTORY_TYPE.getRegext());
        startActivityForResult(intent, Constant.RequestCode.QRCODE_RECOGNITION_ALBUM);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (!qrCodeRecognitionPresenter.isHasSurface()) {
            qrCodeRecognitionPresenter.setHasSurface(true);
            qrCodeRecognitionPresenter.initializeCamera(this, surfaceHolder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        qrCodeRecognitionPresenter.setHasSurface(false);
    }

    @Override
    public void drawViewfinder() {
        vfvFinder.drawViewfinder();
    }

    @Override
    public ViewfinderView getViewfinderView() {
        return vfvFinder;
    }

    @Override
    public Handler getHandler() {
        return qrCodeRecognitionPresenter.getCaptureActivityHandler();
    }

    @Override
    public void handleDecode(Result result) {
        qrCodeRecognitionPresenter.getInactivityTimer().onActivity();
        qrCodeRecognitionPresenter.playBeepSoundAndVibrate();
        if (result != null && (!TextUtils.isEmpty(result.getText()))) {
            qrCodeRecognitionPresenter.stopScan();
            Intent intent = new Intent();
            intent.putExtra(Constant.BundleKey.QRCODE_RESULT, result.getText());
            setResult(Constant.ResultCode.QRCODE_RECOGNITION, intent);
            onFinish("QRCODE_RECOGNITION");
        } else {
            showPromptDialog(R.string.dialog_prompt_qrcode_recognition_error, true, true, Constant.RequestCode.DIALOG_PROMPT_QRCODE_RECOGNITION_ERROR);
        }
    }

    @Override
    public void returnScanResult(int resultCode, Intent data) {

    }

    @Override
    public void launchProductQuary(String url) {

    }
}
