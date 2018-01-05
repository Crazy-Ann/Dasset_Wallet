package com.dasset.wallet.components.zxing.listener;

import android.content.Intent;
import android.os.Handler;

import com.dasset.wallet.components.zxing.view.ViewfinderView;
import com.google.zxing.Result;

public interface OnDecodeHandlerListener {

    void drawViewfinder();

    ViewfinderView getViewfinderView();

    Handler getHandler();

    void handleDecode(Result result);

    void returnScanResult(int resultCode, Intent data);

    void launchProductQuary(String url);
}
