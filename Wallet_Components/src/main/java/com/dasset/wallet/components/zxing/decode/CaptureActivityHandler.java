/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dasset.wallet.components.zxing.decode;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.dasset.wallet.components.R;
import com.dasset.wallet.components.constant.State;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.zxing.camera.CameraManager;
import com.dasset.wallet.components.zxing.listener.OnDecodeHandlerListener;
import com.dasset.wallet.components.zxing.view.ViewfinderResultPointCallback;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Vector;

public final class CaptureActivityHandler extends Handler {

    private final OnDecodeHandlerListener onDecodeHandlerListener;
    private final DecodeThread decodeThread;
    private State state;

    public CaptureActivityHandler(OnDecodeHandlerListener listener, Vector<BarcodeFormat> decodeFormats, String characterSet) {
        this.onDecodeHandlerListener = listener;
        decodeThread = new DecodeThread(onDecodeHandlerListener, decodeFormats, characterSet, new ViewfinderResultPointCallback(onDecodeHandlerListener.getViewfinderView()));
        decodeThread.start();
        state = State.SUCCESS;
        CameraManager.get().startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.auto_focus) {
            if (state == State.PREVIEW) {
                CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
            }
        } else if (message.what == R.id.restart_preview) {
            LogUtil.getInstance().print("Got restart preview message");
            restartPreviewAndDecode();
        } else if (message.what == R.id.decode_succeeded) {
            LogUtil.getInstance().print("Got decode succeeded message");
            state = State.SUCCESS;
//                Bundle bundle = message.getData();
//                Bitmap barcode = bundle == null ? null : (Bitmap) bundle
//                        .getParcelable(DecodeThread.BARCODE_BITMAP);
            onDecodeHandlerListener.handleDecode((Result) message.obj/*, barcode*/);
        } else if (message.what == R.id.decode_failed) {
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
        } else if (message.what == R.id.return_scan_result) {
            LogUtil.getInstance().print("Got return scan result message");
            onDecodeHandlerListener.returnScanResult(Activity.RESULT_OK, (Intent) message.obj);
        } else if (message.what == R.id.launch_product_query) {
            LogUtil.getInstance().print("Got product query message");
            onDecodeHandlerListener.launchProductQuary((String) message.obj);
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        CameraManager.get().stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            decodeThread.join();
        } catch (InterruptedException e) {
            // continue
        }
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
            CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
            onDecodeHandlerListener.drawViewfinder();
        }
    }
}
