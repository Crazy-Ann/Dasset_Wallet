package com.dasset.wallet.core.random;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;

import com.dasset.wallet.components.utils.HandlerUtil;
import com.dasset.wallet.core.random.view.AudioVisualizerView;

import java.util.Arrays;

public class UEntropyMic implements IUEntropySource, Thread.UncaughtExceptionHandler {

    private static final int CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int SAMPLE_PERCENT_SECOND = 8000;
    private static final int MAX_BUFFER_SIZE = 6400;

    private int bufferSizeBytes;
    private HandlerThread handlerThread;
    private Handler handler;
    private AudioRecord audioRecord;
    private UEntropyCollector uEntropyCollector;
    private AudioVisualizerView audioVisualizerView;

    public UEntropyMic(UEntropyCollector uEntropyCollector, AudioVisualizerView audioVisualizerView) {
        this.uEntropyCollector = uEntropyCollector;
        this.audioVisualizerView = audioVisualizerView;
    }

    private final Runnable openRunnable = new Runnable() {

        @Override
        public void run() {
            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_PERCENT_SECOND, CHANNEL_CONFIGURATION, AUDIO_ENCODING);
            if (minBufferSize > MAX_BUFFER_SIZE) {
                bufferSizeBytes = minBufferSize;
            } else {
                bufferSizeBytes = (MAX_BUFFER_SIZE / minBufferSize) * minBufferSize;
            }
            audioRecord = new AudioRecord(android.media.MediaRecorder.AudioSource.MIC, SAMPLE_PERCENT_SECOND, CHANNEL_CONFIGURATION, AUDIO_ENCODING, bufferSizeBytes);
            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                audioRecord.startRecording();
                handler.post(readRunnable);
                audioVisualizerView.setVisibility(View.VISIBLE);
            } else {
                uncaughtException(Thread.currentThread(), new IllegalStateException("startRecording() called on an " + "uninitialized AudioRecord."));
            }
        }
    };

    private final Runnable readRunnable = new Runnable() {

        @Override
        public void run() {
            if (audioRecord != null && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                byte[] data = new byte[bufferSizeBytes];
                data = Arrays.copyOf(data, audioRecord.read(data, 0, bufferSizeBytes));
                uEntropyCollector.onNewData(data, UEntropyCollector.UEntropySource.Mic);
                audioVisualizerView.onNewData(data);
            }
            handler.post(readRunnable);
        }
    };

    private final Runnable closeRunnable = new Runnable() {
        @Override
        public void run() {
            if (audioRecord != null && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
            handler.removeCallbacksAndMessages(null);
            handlerThread.quit();
        }
    };

    @Override
    public void onResume() {
        audioVisualizerView.onResume();
        if (handlerThread != null && handlerThread.isAlive()) {
            return;
        }
        handlerThread = new HandlerThread(getClass().getSimpleName(), android.os.Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.setUncaughtExceptionHandler(this);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler.post(openRunnable);
    }

    @Override
    public void onPause() {
        audioVisualizerView.onPause();
        if (handlerThread != null && handlerThread.isAlive()) {
            handler.removeCallbacksAndMessages(null);
            handler.post(closeRunnable);
        }
    }

    @Override
    public UEntropyCollector.UEntropySource type() {
        return UEntropyCollector.UEntropySource.Mic;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        onPause();
        HandlerUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                audioVisualizerView.setVisibility(View.GONE);
            }
        });
        uEntropyCollector.onError(new Exception(throwable), UEntropyMic.this);
    }
}
