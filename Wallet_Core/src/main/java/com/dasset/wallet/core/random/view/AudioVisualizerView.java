package com.dasset.wallet.core.random.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.view.View;

import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.core.random.audio.AmplitudeData;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class AudioVisualizerView extends View {

    private static float LINE_WIDTH;
    private static final int LINE_COLOR = Color.WHITE;
    private static final int SUBLINE_COLOR = Color.argb(100, 255, 255, 255);
    private static final int SUBLINE_WIDTH = 1;
    private static final int SUBLINE_COUNT = 5;
    private static int HORIZONTAL_STRAIGHT_LINE_LENGTH;
    private static final float MIN_AMPLITUDE_RATE = 0.1f;
    private static final int WAVE_COUNT = 1;
    private static final long WAVE_DURATION = 500;
    private static final int MIN_AMPLITUDE = 15000;
    private static final int MAX_AMPLITUDE = 26000;

    private Paint paint;
    private Paint subLinePaint;
    private HandlerThread handlerThread;
    private Handler handler;
    private byte[] rawData;
    private AmplitudeData amplitudeData;
    private YCalculator yCalculator;
    private PathDrawer pathDrawer;
    private boolean shouldDraw = true;

    public AudioVisualizerView(Context context) {
        super(context);
        LINE_WIDTH = ViewUtil.getInstance().dp2px(context, 1);
        HORIZONTAL_STRAIGHT_LINE_LENGTH = ViewUtil.getInstance().dp2px(context, 20);
    }

    public AudioVisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LINE_WIDTH = ViewUtil.getInstance().dp2px(context, 1);
        HORIZONTAL_STRAIGHT_LINE_LENGTH = ViewUtil.getInstance().dp2px(context, 20);
    }

    public AudioVisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LINE_WIDTH = ViewUtil.getInstance().dp2px(context, 1);
        HORIZONTAL_STRAIGHT_LINE_LENGTH = ViewUtil.getInstance().dp2px(context, 20);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (!isVisible()) {
            return;
        }
        if (shouldDraw) {
            if (yCalculator == null) {
                yCalculator = new YCalculator();
            } else {
                yCalculator.reset();
            }
            pathDrawer.draw();
            canvas.drawPath(pathDrawer.getPath(), paint);
            for (Path path : pathDrawer.getPaths()) {
                canvas.drawPath(path, subLinePaint);
            }
            invalidate();
        }
    }


    public void onPause() {
        shouldDraw = false;
        postInvalidate();
    }

    public void onResume() {
        shouldDraw = true;
    }

    private class PathDrawer {
        private int subLineCount;
        private Path path;
        private ArrayList<Path> paths;

        public PathDrawer() {
            this(0);
        }

        public PathDrawer(int subLineCount) {
            this.subLineCount = subLineCount;
        }

        public void draw() {
            this.path = new Path();
            this.paths = Lists.newArrayListWithCapacity(subLineCount);
            yCalculator.amplitudeRate = 1;
            straightLineBefore(path);
            for (int i = 0; i < subLineCount; i++) {
                Path path = new Path();
                paths.add(path);
                yCalculator.amplitudeRate = rate(i);
                straightLineBefore(path);
            }
            for (int x = HORIZONTAL_STRAIGHT_LINE_LENGTH * 4; x < yCalculator.width - HORIZONTAL_STRAIGHT_LINE_LENGTH * 4; x++) {
                yCalculator.amplitudeRate = 1;
                path.lineTo(x, yCalculator.y(x));
                for (int i = 0; i < subLineCount; i++) {
                    yCalculator.amplitudeRate = rate(i);
                    paths.get(i).lineTo(x, yCalculator.y(x));
                }
            }
            yCalculator.amplitudeRate = 1;
            straightLineEnd(path);
            for (int i = 0; i < subLineCount; i++) {
                yCalculator.amplitudeRate = rate(i);
                straightLineEnd(paths.get(i));
            }
        }

        public Path getPath() {
            return path;
        }

        public List<Path> getPaths() {
            return paths;
        }

        private float rate(int index) {
            return (float) (index + 1) / (float) (subLineCount + 1);
        }

        private void straightLineBefore(Path path) {
            path.moveTo(0, yCalculator.height / 2);
            float controlY = yCalculator.y(HORIZONTAL_STRAIGHT_LINE_LENGTH * 3);
            path.quadTo(HORIZONTAL_STRAIGHT_LINE_LENGTH, yCalculator.height / 2, HORIZONTAL_STRAIGHT_LINE_LENGTH * 2, (yCalculator.height / 2 + controlY) / 2);
            path.quadTo(HORIZONTAL_STRAIGHT_LINE_LENGTH * 3, controlY, HORIZONTAL_STRAIGHT_LINE_LENGTH * 4, yCalculator.y(HORIZONTAL_STRAIGHT_LINE_LENGTH * 4));
        }

        private void straightLineEnd(Path path) {
            float controlY = yCalculator.y(yCalculator.width - HORIZONTAL_STRAIGHT_LINE_LENGTH * 3);
            path.quadTo(yCalculator.width - HORIZONTAL_STRAIGHT_LINE_LENGTH * 3, controlY, yCalculator.width - HORIZONTAL_STRAIGHT_LINE_LENGTH * 2, (yCalculator.height / 2 + controlY) / 2);
            path.quadTo(yCalculator.width - HORIZONTAL_STRAIGHT_LINE_LENGTH, yCalculator.height / 2, yCalculator.width, yCalculator.height / 2);
        }
    }

    private class YCalculator {

        private long animBeginTime;
        private long currentTime;
        public int width;
        public int height;
        private int amplitude;
        private float xOffset;
        private float amplitudeRate;

        public YCalculator() {
            reset();
        }

        public void reset() {
            this.currentTime = System.currentTimeMillis();
            if (animBeginTime <= 0 || currentTime - animBeginTime > WAVE_DURATION) {
                animBeginTime = currentTime;
            }
            width = getWidth();
            height = getHeight();
            if (amplitudeData != null) {
                amplitude = height * (amplitudeData.getAmplitude() - MIN_AMPLITUDE) / (MAX_AMPLITUDE - MIN_AMPLITUDE);
                amplitude = Math.max(Math.min(height - (int) (2 * LINE_WIDTH), amplitude), (int) (height * MIN_AMPLITUDE_RATE));
            } else {
                amplitude = (int) (height * MIN_AMPLITUDE_RATE);
            }
            xOffset = (float) (width - HORIZONTAL_STRAIGHT_LINE_LENGTH * 2) / (float) WAVE_COUNT / (float) WAVE_DURATION * (float) (currentTime - animBeginTime);
            amplitudeRate = 1;
        }

        public float y(float x) {
            return (float) (amplitude * amplitudeRate / 2.0f * Math.sin(2 * Math.PI * ((x - xOffset) / (width - HORIZONTAL_STRAIGHT_LINE_LENGTH * 2)) * WAVE_COUNT) + height / 2);
        }
    }

    public void onNewData(byte[] data) {
        rawData = data;
        if (handler == null) {
            return;
        }
        handler.removeCallbacks(analyzeData);
        handler.post(analyzeData);
    }

    private Runnable analyzeData = new Runnable() {
        @Override
        public void run() {
            if (rawData != null && rawData.length > 0 && isVisible()) {
                amplitudeData = new AmplitudeData(rawData);
                rawData = null;
                postInvalidate();
            }
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (handlerThread != null && handlerThread.isAlive()) {
            handlerThread.quit();
        }
        handlerThread = new HandlerThread(AudioVisualizerView.class.getClass().getSimpleName(), android.os.Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        paint = new Paint();
        paint.setColor(LINE_COLOR);
        paint.setStrokeWidth(LINE_WIDTH);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        subLinePaint = new Paint(paint);
        subLinePaint.setStrokeWidth(SUBLINE_WIDTH);
        subLinePaint.setColor(SUBLINE_COLOR);

        pathDrawer = new PathDrawer(SUBLINE_COUNT);
    }

    @Override
    protected void onDetachedFromWindow() {
        handler.removeCallbacksAndMessages(null);
        handlerThread.quit();
        handler = null;
        handlerThread = null;
        super.onDetachedFromWindow();
    }

    private boolean isVisible() {
        return getGlobalVisibleRect(new Rect());
    }
}
