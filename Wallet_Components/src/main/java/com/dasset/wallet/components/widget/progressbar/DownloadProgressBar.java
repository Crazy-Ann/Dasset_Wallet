package com.dasset.wallet.components.widget.progressbar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.dasset.wallet.components.R;
import com.dasset.wallet.components.constant.Constant;
import com.dasset.wallet.components.constant.State;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.widget.progressbar.listener.OnProgressUpdateListener;

public class DownloadProgressBar extends View {

    private Paint circlePaint;
    private Paint drawingPaint;
    private Paint progressPaint;
    private Paint progressBackgroundPaint;

    private float radius;
    private float strokeWidth;
    private float lineWidth;
    private float lengthFix;
    private float arrowLineToDotAnimatedValue;
    private float arrowLineToHorizontalLineAnimatedValue;
    private float dotToProgressAnimatedValue;
    private float currentGlobalProgressValue;
    private float successValue;
    private float expandCollapseValue;
    private float errorValue;
    private float overshootValue;

    private float centerX;
    private float centerY;
    private float paddingX;
    private float paddingY;

    private int circleBackgroundColor;
    private int drawingColor;
    private int progressBackgroundColor;
    private int progressColor;
    private int progressDuration;
    private int resultDuration;

    private AnimatorSet arrowToLineAnimatorSet;
    private AnimatorSet progressAnimationSet;

    private OvershootInterpolator overshootInterpolator;

    private ValueAnimator dotToProgressAnimation;
    private ValueAnimator progressAnimation;
    private ValueAnimator successAnimation;
    private ValueAnimator expandAnimation;
    private ValueAnimator collapseAnimation;
    private ValueAnimator errorAnimation;
    private ValueAnimator arrowLineToDot;
    private ValueAnimator arrowLineToHorizontalLine;
    private ValueAnimator manualProgressAnimation;

    private RectF circleBounds;
    private RectF progressBackgroundBounds = new RectF();
    private RectF progressBounds = new RectF();

    private OnProgressUpdateListener onProgressUpdateListener;
    private AnimatorSet manualProgressAnimationSet;
    private float fromArc = 0;
    private float toArc = 0;
    private float currentGlobalManualProgressValue;

    private State state;
    private State resultState;
    private State whichProgress;

    public DownloadProgressBar(Context context) {
        super(context);
    }

    public DownloadProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DownloadProgressBar, 0, 0);
        try {
            radius = array.getDimension(R.styleable.DownloadProgressBar_circleRadius, 0);
            strokeWidth = array.getDimension(R.styleable.DownloadProgressBar_strokeWidth, 0);
            lineWidth = array.getDimension(R.styleable.DownloadProgressBar_lineWidth, 0);
            lengthFix = (float) (lineWidth / (2 * Math.sqrt(2)));
            progressDuration = array.getInteger(R.styleable.DownloadProgressBar_progressDuration, Constant.DownloadProgressBar.DEFAULT_PROGRESS_DURATION);
            resultDuration = array.getInteger(R.styleable.DownloadProgressBar_resultDuration, Constant.DownloadProgressBar.DEFAULT_RESULT_DURATION);
            progressBackgroundColor = array.getColor(R.styleable.DownloadProgressBar_progressBackgroundColor, 0);
            drawingColor = array.getColor(R.styleable.DownloadProgressBar_drawingColor, 0);
            progressColor = array.getColor(R.styleable.DownloadProgressBar_progressColor, 0);
            circleBackgroundColor = array.getColor(R.styleable.DownloadProgressBar_circleBackgroundColor, 0);
            overshootValue = array.getFloat(R.styleable.DownloadProgressBar_overshootValue, Constant.DownloadProgressBar.DEFAULT_OVERSHOOT_VALUE);
        } finally {
            array.recycle();
        }

        circlePaint = new Paint();
        circlePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(circleBackgroundColor);
        circlePaint.setStrokeWidth(strokeWidth);

        drawingPaint = new Paint();
        drawingPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        drawingPaint.setStyle(Paint.Style.STROKE);
        drawingPaint.setColor(drawingColor);
        drawingPaint.setStrokeWidth(lineWidth);

        progressPaint = new Paint();
        progressPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.FILL);

        progressBackgroundPaint = new Paint();
        progressBackgroundPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        progressBackgroundPaint.setColor(progressBackgroundColor);
        progressBackgroundPaint.setStyle(Paint.Style.FILL);

        state = State.IDLE;
        setupAnimations();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        paddingX = w / 2f - radius;
        paddingY = h / 2f - radius;

        circleBounds = new RectF();
        circleBounds.top = paddingY;
        circleBounds.left = paddingX;
        circleBounds.bottom = h / 2f + radius;
        circleBounds.right = w / 2f + radius;
    }

    private void setupAnimations() {
        overshootInterpolator = new OvershootInterpolator(overshootValue);
        arrowLineToDot = ValueAnimator.ofFloat(0, radius / 4);
        arrowLineToDot.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                arrowLineToDotAnimatedValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        arrowLineToDot.setDuration(200);
        arrowLineToDot.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                state = State.ANIMATING_LINE_TO_DOT;
                if (onProgressUpdateListener != null) {
                    onProgressUpdateListener.onAnimationStarted();
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        arrowLineToDot.setInterpolator(new AccelerateInterpolator());

        arrowLineToHorizontalLine = ValueAnimator.ofFloat(0, radius / 2);
        arrowLineToHorizontalLine.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                arrowLineToHorizontalLineAnimatedValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        arrowLineToHorizontalLine.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        arrowLineToHorizontalLine.setDuration(600);
        arrowLineToHorizontalLine.setStartDelay(400);
        arrowLineToHorizontalLine.setInterpolator(overshootInterpolator);

        dotToProgressAnimation = ValueAnimator.ofFloat(0, radius);
        dotToProgressAnimation.setDuration(600);
        dotToProgressAnimation.setStartDelay(600);
        dotToProgressAnimation.setInterpolator(overshootInterpolator);
        dotToProgressAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                dotToProgressAnimatedValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        dotToProgressAnimation.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (whichProgress == State.ANIMATING_PROGRESS)
                    progressAnimationSet.start();
                else if (whichProgress == State.ANIMATING_MANUAL_PROGRESS)
                    manualProgressAnimationSet.start();

                state = whichProgress;

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        arrowToLineAnimatorSet = new AnimatorSet();
        arrowToLineAnimatorSet.playTogether(arrowLineToDot, arrowLineToHorizontalLine, dotToProgressAnimation);

        progressAnimation = ValueAnimator.ofFloat(0, 360f);
        progressAnimation.setStartDelay(500);
        progressAnimation.setInterpolator(new LinearInterpolator());
        progressAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                currentGlobalProgressValue = (float) valueAnimator.getAnimatedValue();
                if (onProgressUpdateListener != null) {
                    onProgressUpdateListener.onProgressUpdate(currentGlobalProgressValue);
                }
                invalidate();
            }
        });
        progressAnimation.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                dotToProgressAnimatedValue = 0;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        progressAnimation.setDuration(progressDuration);

        manualProgressAnimation = ValueAnimator.ofFloat(fromArc, toArc);
        manualProgressAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                currentGlobalManualProgressValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        manualProgressAnimation.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                if (onProgressUpdateListener != null) {
                    onProgressUpdateListener.onManualProgressStarted();
                }
                dotToProgressAnimatedValue = 0;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (onProgressUpdateListener != null) {
                    onProgressUpdateListener.onManualProgressEnded();
                }
                if (toArc > 359) {
                    collapseAnimation.start();
                }

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });


        expandAnimation = ValueAnimator.ofFloat(0, radius / 6);
        expandAnimation.setDuration(300);
        expandAnimation.setInterpolator(new DecelerateInterpolator());
        expandAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                expandCollapseValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        collapseAnimation = ValueAnimator.ofFloat(radius / 6, strokeWidth / 2);
        collapseAnimation.setDuration(300);
        collapseAnimation.setStartDelay(300);
        collapseAnimation.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (state == State.ANIMATING_MANUAL_PROGRESS) {
                    if (resultState == State.ANIMATING_FAILED) {
                        errorAnimation.start();
                    } else if (resultState == State.ANIMATING_SUCCESS) {
                        successAnimation.start();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        collapseAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        collapseAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                expandCollapseValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        manualProgressAnimationSet = new AnimatorSet();
        manualProgressAnimationSet.playSequentially(expandAnimation, manualProgressAnimation);

        progressAnimationSet = new AnimatorSet();
        progressAnimationSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (resultState == State.ANIMATING_FAILED) {
                    errorAnimation.start();
                } else if (resultState == State.ANIMATING_SUCCESS) {
                    successAnimation.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        progressAnimationSet.playSequentially(expandAnimation, progressAnimation, collapseAnimation);

        errorAnimation = ValueAnimator.ofFloat(0, radius / 4);
        errorAnimation.setDuration(600);
        errorAnimation.setStartDelay(500);
        errorAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        errorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                errorValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        errorAnimation.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                state = State.ANIMATING_FAILED;
                if (onProgressUpdateListener != null) {
                    onProgressUpdateListener.onAnimationFailed();
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {

                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (onProgressUpdateListener != null) {
                            onProgressUpdateListener.onAnimationEnded();
                        }
                        state = State.IDLE;
                        resetValues();
                        invalidate();
                    }
                }, resultDuration);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        successAnimation = ValueAnimator.ofFloat(0, radius / 4);
        successAnimation.setDuration(600);
        successAnimation.setStartDelay(500);
        successAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        successAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                successValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        successAnimation.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                state = State.ANIMATING_SUCCESS;
                if (onProgressUpdateListener != null) {
                    onProgressUpdateListener.onAnimationSuccess();
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (onProgressUpdateListener != null) {
                            onProgressUpdateListener.onAnimationEnded();
                        }
                        state = State.IDLE;
                        resetValues();
                        invalidate();
                    }
                }, resultDuration);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void resetValues() {
        arrowLineToDotAnimatedValue = 0;
        arrowLineToHorizontalLineAnimatedValue = 0;
        currentGlobalProgressValue = 0;
        currentGlobalManualProgressValue = 0;
        manualProgressAnimation.setFloatValues(0, 0);
        toArc = 0;
        fromArc = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(centerX, centerY, radius, circlePaint);
        switch (state) {
            case IDLE:
                canvas.drawLine(centerX, centerY - radius / 2, centerX, centerY + radius / 2, drawingPaint);
                canvas.drawLine(centerX - radius / 2, centerY, centerX + lengthFix, centerY + radius / 2 + lengthFix, drawingPaint);
                canvas.drawLine(centerX - lengthFix, centerY + radius / 2 + lengthFix, centerX + radius / 2, centerY, drawingPaint);
                break;
            case ANIMATING_LINE_TO_DOT:
                if (!dotToProgressAnimation.isRunning()) {
                    canvas.drawLine(
                            centerX,
                            centerY - radius / 2 + arrowLineToDotAnimatedValue * 2 - strokeWidth / 2,
                            centerX,
                            centerY + radius / 2 - arrowLineToDotAnimatedValue * 2 + strokeWidth / 2,
                            drawingPaint
                    );
                }
                canvas.drawLine(
                        centerX - radius / 2 - arrowLineToHorizontalLineAnimatedValue / 2,
                        centerY,
                        centerX + lengthFix,
                        centerY + radius / 2 - arrowLineToHorizontalLineAnimatedValue + lengthFix,
                        drawingPaint
                );
                canvas.drawLine(
                        centerX - lengthFix,
                        centerY + radius / 2 - arrowLineToHorizontalLineAnimatedValue + lengthFix,
                        centerX + radius / 2 + arrowLineToHorizontalLineAnimatedValue / 2,
                        centerY,
                        drawingPaint
                );
                break;
            case ANIMATING_PROGRESS:
                float progress = ((centerX + radius / 2 + arrowLineToHorizontalLineAnimatedValue / 2) - (centerX - radius / 2 - arrowLineToHorizontalLineAnimatedValue / 2)) / 360f;

                drawingPaint.setStrokeWidth(strokeWidth);
                canvas.drawArc(circleBounds, -90, currentGlobalProgressValue, false, drawingPaint);

                progressBackgroundBounds.left = centerX - radius / 2 - arrowLineToHorizontalLineAnimatedValue / 2;
                progressBackgroundBounds.top = centerY - expandCollapseValue;
                progressBackgroundBounds.right = centerX + radius / 2 + arrowLineToHorizontalLineAnimatedValue / 2;
                progressBackgroundBounds.bottom = centerY + expandCollapseValue;
                canvas.drawRoundRect(progressBackgroundBounds, 45, 45, progressBackgroundPaint);

                progressBounds.left = centerX - radius / 2 - arrowLineToHorizontalLineAnimatedValue / 2;
                progressBounds.top = centerY - expandCollapseValue;
                progressBounds.right = centerX - radius / 2 - arrowLineToHorizontalLineAnimatedValue / 2 + progress * currentGlobalProgressValue;
                progressBounds.bottom = centerY + expandCollapseValue;
                canvas.drawRoundRect(progressBounds, 45, 45, progressPaint);
                break;
            case ANIMATING_MANUAL_PROGRESS:
                float manualProgress = ((centerX + radius / 2 + arrowLineToHorizontalLineAnimatedValue / 2) - (centerX - radius / 2 - arrowLineToHorizontalLineAnimatedValue / 2)) / 360f;

                drawingPaint.setStrokeWidth(strokeWidth);
                canvas.drawArc(circleBounds, -90, currentGlobalManualProgressValue, false, drawingPaint);

                progressBackgroundBounds.left = centerX - radius / 2 - arrowLineToHorizontalLineAnimatedValue / 2;
                progressBackgroundBounds.top = centerY - expandCollapseValue;
                progressBackgroundBounds.right = centerX + radius / 2 + arrowLineToHorizontalLineAnimatedValue / 2;
                progressBackgroundBounds.bottom = centerY + expandCollapseValue;
                canvas.drawRoundRect(progressBackgroundBounds, 45, 45, progressBackgroundPaint);

                progressBounds.left = centerX - radius / 2 - arrowLineToHorizontalLineAnimatedValue / 2;
                progressBounds.top = centerY - expandCollapseValue;
                progressBounds.right = centerX - radius / 2 - arrowLineToHorizontalLineAnimatedValue / 2 + manualProgress * currentGlobalManualProgressValue;
                progressBounds.bottom = centerY + expandCollapseValue;
                canvas.drawRoundRect(progressBounds, 45, 45, progressPaint);
                break;
            case ANIMATING_SUCCESS:
                drawingPaint.setStrokeWidth(lineWidth);
                canvas.drawArc(circleBounds, 0, 360, false, drawingPaint);
                canvas.drawLine(
                        centerX - radius / 2 + successValue * 2 - successValue / (float) Math.sqrt(2f) / 2,
                        centerY + successValue,
                        centerX + successValue * 2 - successValue / (float) Math.sqrt(2f) / 2,
                        centerY - successValue,
                        drawingPaint
                );
                canvas.drawLine(
                        centerX - successValue - 2 * successValue / (float) Math.sqrt(2f) / 2,
                        centerY,
                        centerX + radius / 2 - successValue * 2 - successValue / (float) Math.sqrt(2f) / 2,
                        centerY + successValue,
                        drawingPaint
                );
                break;
            case ANIMATING_FAILED:
                drawingPaint.setStrokeWidth(lineWidth);
                canvas.drawArc(circleBounds, 0, 360, false, drawingPaint);

                canvas.drawLine(
                        centerX - radius / 2 - radius / 4 + errorValue * 2,
                        centerY + errorValue,
                        centerX + errorValue,
                        centerY - errorValue,
                        drawingPaint
                );
                canvas.drawLine(
                        centerX - errorValue,
                        centerY - errorValue,
                        centerX + radius / 2 + radius / 4 - errorValue * 2,
                        centerY + errorValue,
                        drawingPaint
                );
                break;
        }
        if (dotToProgressAnimatedValue > 0) {
            canvas.drawCircle(
                    centerX,
                    centerY - dotToProgressAnimatedValue,
                    strokeWidth / 2,
                    drawingPaint
            );
        }

        if (dotToProgressAnimation.isRunning() && !arrowLineToHorizontalLine.isRunning()) {
            canvas.drawLine(
                    centerX - radius / 2 - arrowLineToHorizontalLineAnimatedValue / 2,
                    centerY,
                    centerX + radius / 2 + arrowLineToHorizontalLineAnimatedValue / 2,
                    centerY,
                    drawingPaint
            );
        }
    }

    public void onSuccess() {
        resultState = State.ANIMATING_SUCCESS;
        whichProgress = State.ANIMATING_PROGRESS;
        arrowToLineAnimatorSet.start();
        invalidate();
    }

    public void onFailed() {
        whichProgress = State.ANIMATING_PROGRESS;
        resultState = State.ANIMATING_FAILED;
        arrowToLineAnimatorSet.start();
        invalidate();
    }

    public void onManualProgressAnimation() {
        whichProgress = State.ANIMATING_MANUAL_PROGRESS;
        resultState = State.ANIMATING_SUCCESS;
        arrowToLineAnimatorSet.start();
        invalidate();
    }

    public void abortDownload() {
        if (expandAnimation.isRunning() || progressAnimation.isRunning()) {
            progressAnimationSet.cancel();
            collapseAnimation.start();
            invalidate();
        }
    }

    public void setErrorResultState() {
        if (successAnimation.isRunning() || errorAnimation.isRunning())
            return;
        resultState = State.ANIMATING_FAILED;
    }

    public void setSuccessResultState() {
        if (successAnimation.isRunning() || errorAnimation.isRunning())
            return;
        resultState = State.ANIMATING_SUCCESS;
    }

    public void setProgress(float value) {
        LogUtil.getInstance().print(value);
        if (value < 1 || value > 100) {
            return;
        }
        toArc = value * 3.6f;
        manualProgressAnimation.setFloatValues(fromArc, toArc);
        manualProgressAnimation.start();
        fromArc = toArc;
        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mState = state;
        savedState.mmCurrentPlayTime = getCurrentPlayTimeByState(state);
        return savedState;
    }

    private void setCurrentPlayTimeByStateAndPlay(long[] tab, State mState) {
        switch (mState) {
            case ANIMATING_LINE_TO_DOT:
                arrowToLineAnimatorSet.start();
                for (int i = 0; i < arrowToLineAnimatorSet.getChildAnimations().size(); i++) {
                    ((ValueAnimator) arrowToLineAnimatorSet.getChildAnimations().get(i)).setCurrentPlayTime(tab[i]);
                }
                break;
            case ANIMATING_PROGRESS:
                progressAnimationSet.start();
                for (int i = 0; i < progressAnimationSet.getChildAnimations().size(); i++) {
                    ((ValueAnimator) progressAnimationSet.getChildAnimations().get(i)).setCurrentPlayTime(tab[i]);
                }
                break;
            case ANIMATING_FAILED:
                errorAnimation.start();
                errorAnimation.setCurrentPlayTime(tab[0]);
                break;
            case ANIMATING_SUCCESS:
                successAnimation.start();
                successAnimation.setCurrentPlayTime(tab[0]);
                break;
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            this.state = savedState.mState;
            super.onRestoreInstanceState(savedState.getSuperState());
            if (this.state != State.IDLE) {
                continueAnimation(this.state, savedState.mmCurrentPlayTime);
            }
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private void continueAnimation(State mState, long[] mmCurrentPlayTime) {
        setCurrentPlayTimeByStateAndPlay(mmCurrentPlayTime, mState);
    }

    private long[] getCurrentPlayTimeByState(State mState) {
        long[] tab = new long[3];
        switch (mState) {
            case ANIMATING_LINE_TO_DOT:
                for (int i = 0; i < arrowToLineAnimatorSet.getChildAnimations().size(); i++) {
                    tab[i] = ((ValueAnimator) arrowToLineAnimatorSet.getChildAnimations().get(i)).getCurrentPlayTime();
                }
                arrowToLineAnimatorSet.cancel();
                break;
            case ANIMATING_PROGRESS:
                for (int i = 0; i < progressAnimationSet.getChildAnimations().size(); i++) {
                    tab[i] = ((ValueAnimator) progressAnimationSet.getChildAnimations().get(i)).getCurrentPlayTime();
                }
                progressAnimationSet.cancel();
                break;
            case ANIMATING_FAILED:
                tab[0] = errorAnimation.getCurrentPlayTime();
                errorAnimation.cancel();
                break;
            case ANIMATING_SUCCESS:
                tab[0] = successAnimation.getCurrentPlayTime();
                successAnimation.cancel();
                break;
        }
        return tab;
    }

    static class SavedState extends BaseSavedState {

        private boolean isFlashing;
        private boolean isConfigurationChanged;
        private long[] mmCurrentPlayTime;
        private State mState;

        public SavedState(Parcel source) {
            super(source);
            isFlashing = source.readInt() == 1;
            isConfigurationChanged = source.readInt() == 1;
            mmCurrentPlayTime = source.createLongArray();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(isFlashing ? 1 : 0);
            dest.writeInt(isConfigurationChanged ? 1 : 0);
            dest.writeLongArray(mmCurrentPlayTime);

        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public void setOnProgressUpdateListener(OnProgressUpdateListener onProgressUpdateListener) {
        this.onProgressUpdateListener = onProgressUpdateListener;
    }
}
