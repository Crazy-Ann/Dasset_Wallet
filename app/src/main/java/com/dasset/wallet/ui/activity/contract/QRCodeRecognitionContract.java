package com.dasset.wallet.ui.activity.contract;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.dasset.wallet.base.presenter.BasePresenter;
import com.dasset.wallet.base.view.BaseView;
import com.dasset.wallet.components.zxing.listener.OnDecodeHandlerListener;

public interface QRCodeRecognitionContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

    }

    interface Presenter extends BasePresenter {

        void initializeCamera(OnDecodeHandlerListener listener, SurfaceHolder holder);

        void startScan(OnDecodeHandlerListener listener, SurfaceHolder.Callback callback, SurfaceView surfaceView);

        void stopScan();

        void initializeBeepSound();

        void playBeepSoundAndVibrate();

        String generatorPathFromUri(Uri uri);

        String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs);

        void getQRCodeBitmap(Intent data);

    }
}
