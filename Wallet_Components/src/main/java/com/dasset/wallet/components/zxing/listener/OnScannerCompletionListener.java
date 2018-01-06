package com.dasset.wallet.components.zxing.listener;

import android.graphics.Bitmap;

import com.google.zxing.Result;


public interface OnScannerCompletionListener {
    
    void OnScannerCompletion(Result result, Bitmap barcode);
}
