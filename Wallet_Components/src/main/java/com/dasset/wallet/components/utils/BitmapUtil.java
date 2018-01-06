package com.dasset.wallet.components.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * 图片处理
 */
public final class BitmapUtil {

    private BitmapUtil() {
        // cannot be instantiated
    }

    private static BitmapUtil mBitmapUtil;


    public static synchronized BitmapUtil getInstance() {
        if (mBitmapUtil == null) {
            mBitmapUtil = new BitmapUtil();
        }
        return mBitmapUtil;
    }

    public static void releaseInstantce() {
        if (mBitmapUtil != null) {
            mBitmapUtil = null;
        }
    }

    public Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64Util.decode(base64Data);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public String bitmapToBase64(Bitmap bitmap, int quality) {
        if (bitmap != null) {
            ByteArrayOutputStream outputStream = null;
            try {
                outputStream = new ByteArrayOutputStream();
                if (quality >= 0) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                } else {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                }
                outputStream.flush();
                outputStream.close();
//            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
                return Base64Util.encode(outputStream.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bitmap.recycle();
            }
        } else {
            return null;
        }
    }

    public Bitmap compressBitmap(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int inSampleSize = 1;
        if (options.outWidth > width || options.outHeight > height) {
            while (((height / 2) / inSampleSize) >= height && ((width / 2) / inSampleSize) >= width) {
                inSampleSize *= 2;
            }
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 生成一个二维码图像
     *
     * @param url
     *         传入的字符串，通常是一个URL
     * @param qrWidth
     *         宽度（像素值px）
     * @param qrHeight
     *         高度（像素值px）
     *
     * @return
     */
    public Bitmap create2DCoderBitmap(String url, int qrWidth, int qrHeight) {
        try {
            // 判断URL合法性
            if (TextUtils.isEmpty(url)) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, qrWidth, qrHeight, hints);
            int[]     pixels    = new int[qrWidth * qrHeight];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < qrHeight; y++) {
                for (int x = 0; x < qrWidth; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * qrWidth + x] = 0xff000000;
                    } else {
                        pixels[y * qrWidth + x] = 0xffffffff;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(qrWidth, qrHeight,
                                                Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, qrWidth, 0, 0, qrWidth, qrHeight);
            // 显示到一个ImageView上面
            // sweepIV.setImageBitmap(bitmap);
            return bitmap;
        } catch (WriterException e) {
            LogUtil.getInstance().i("log", "生成二维码错误" + e.getMessage());
            return null;
        }
    }

    /**
     * 保存图片到指定文件夹
     *
     * @param bmp
     * @param fileName
     *
     * @return
     */
    public boolean saveBitmap(Bitmap bmp, String fileName) {
        if (bmp == null || fileName == null) {
            return false;
        }
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        Bitmap.CompressFormat format  = Bitmap.CompressFormat.JPEG;
        int                   quality = 100;
        OutputStream          stream  = null;
        try {
            stream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bmp.compress(format, quality, stream);
    }
}
