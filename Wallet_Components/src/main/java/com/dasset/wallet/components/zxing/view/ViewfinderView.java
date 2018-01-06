package com.dasset.wallet.components.zxing.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.dasset.wallet.components.R;
import com.dasset.wallet.components.utils.DensityUtil;
import com.dasset.wallet.components.zxing.camera.CameraManager;
import com.google.zxing.ResultPoint;

import java.util.Collection;
import java.util.HashSet;

public final class ViewfinderView extends View {

    //    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final int[] SCANNER_ALPHA = {255, 255, 255, 255, 255, 255, 255, 255};
    private static final int   OPAQUE        = 0xFF;

    private final Paint                   paint;
    private       Bitmap                  resultBitmap;
    private final int                     maskColor;
    private final int                     resultColor;
    private final int                     frameColor;
    private final int                     laserColor;
    private final int                     borderColor;
    private       int                     scannerAlpha;
    private       Collection<ResultPoint> possibleResultPoints;
    private boolean laserLinePortrait = true;
    int i = 0;
    private int laserLineTop;// 扫描线最顶端位置
    private int animationDelay = 0;
    private int laserMoveSpeed = 3;// 扫描线默认移动距离px

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        maskColor = getResources().getColor(android.R.color.transparent);
        resultColor = getResources().getColor(R.color.color_b0000000);
        frameColor = getResources().getColor(android.R.color.white);
        laserColor = getResources().getColor(R.color.color_129fdd);
        borderColor = getResources().getColor(R.color.color_ff5c5c5c);
        scannerAlpha = 0;
        possibleResultPoints = new HashSet<>(5);
        laserMoveSpeed = DensityUtil.getInstance(context).dp2px(3f);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }

        int width  = canvas.getWidth();
        int height = canvas.getHeight();

        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultBitmap != null) {
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {
            paint.setColor(borderColor);
            paint.setColor(frameColor);
            canvas.drawRect(frame.left, frame.top, frame.left + 50, frame.top + 10, paint);
            canvas.drawRect(frame.left, frame.top, frame.left + 10, frame.top + 50, paint);

            canvas.drawRect(frame.right - 50, frame.top, frame.right, frame.top + 10, paint);
            canvas.drawRect(frame.right - 10, frame.top, frame.right, frame.top + 50, paint);

            canvas.drawRect(frame.left, frame.bottom - 50, frame.left + 10, frame.bottom, paint);
            canvas.drawRect(frame.left, frame.bottom - 10, frame.left + 50, frame.bottom, paint);

            canvas.drawRect(frame.right - 10, frame.bottom - 50, frame.right, frame.bottom, paint);
            canvas.drawRect(frame.right - 50, frame.bottom - 10, frame.right, frame.bottom, paint);
//            paint.setColor(laserColor);
//            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
//            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            if (laserLinePortrait) {
//                if ((i += 5) < frame.bottom - frame.top) {
//                    canvas.drawRect(frame.left + 2, frame.top - 2 + i, frame.right - 1, frame.top + 2 + i, paint);
//                    invalidate();
//                } else {
//                    i = 0;
//                }
                //图片资源文件转为 Bitmap
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_scan_net);

                @SuppressLint("DrawAllocation") RectF dstRectF = new RectF(frame.left, frame.top, frame.right, laserLineTop);
                @SuppressLint("DrawAllocation") Rect srcRect = new Rect(0, (int) (bitmap.getHeight() - dstRectF.height())
                        , bitmap.getWidth(), height);
                canvas.drawBitmap(bitmap, srcRect, dstRectF, paint);
                moveLaserSpeed(frame);

            } else {
                float left = frame.left + (frame.right - frame.left) / 2 - 2;
                canvas.drawRect(left, frame.top, left + 2, frame.bottom - 2, paint);
            }
//            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
        }
    }

    private void moveLaserSpeed(Rect frame) {
        //初始化扫描线最顶端位置
        if (laserLineTop == 0) {
            laserLineTop = frame.top;
        }
        // 每次刷新界面，扫描线往下移动 LASER_VELOCITY
        laserLineTop += laserMoveSpeed;
        if (laserLineTop >= frame.bottom) {
            laserLineTop = frame.top;
        }
        if (animationDelay == 0) {
            animationDelay = (int) ((1.0f * 1000 * laserMoveSpeed) / (frame.bottom - frame.top));
        }
        // 只刷新扫描框的内容，其他地方不刷新
        postInvalidateDelayed(animationDelay, frame.left, frame.top, frame.right, frame.bottom);
    }

    public void drawViewfinder() {
        resultBitmap = null;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }
}
