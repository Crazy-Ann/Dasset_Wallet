package com.dasset.wallet.components.zxing.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.dasset.wallet.components.zxing.listener.OnScannerCompletionListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class QRCodeDecode {

    public static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
    public static final int MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080
    public static final Map<DecodeHintType, Object> HINTS = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);

    static {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.QR_CODE);
        HINTS.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        HINTS.put(DecodeHintType.CHARACTER_SET, "utf-8");
    }

    private QRCodeDecode() {
    }

    public static void decodeQR(String picturePath, OnScannerCompletionListener listener) {
        try {
            decodeQR(loadBitmap(picturePath), listener);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析二维码图片
     *
     * @param srcBitmap
     * @param listener
     *
     * @return
     */
    public static void decodeQR(Bitmap srcBitmap, final OnScannerCompletionListener listener) {
        Result result = null;
        try {
            if (srcBitmap != null) {
                int width = srcBitmap.getWidth();
                int height = srcBitmap.getHeight();
                int[] pixels = new int[width * height];
                srcBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                //新建一个RGBLuminanceSource对象
                RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                //将图片转换成二进制图片
                BinaryBitmap binaryBitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
                QRCodeReader qrCodeReader = new QRCodeReader();
                result = qrCodeReader.decode(binaryBitmap, HINTS);
            }
            if (listener != null) {
                listener.OnScannerCompletion(result, srcBitmap);
            }
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
        }
    }

    private static Bitmap loadBitmap(String picturePath) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取到这个图片的原始宽度和高度
        int width = options.outWidth;
        int height = options.outHeight;
        // 获取画布中间方框的宽度和高度
        int screenWidth = MAX_FRAME_WIDTH;
        int screenHeight = MAX_FRAME_HEIGHT;
        // isSampleSize是表示对图片的缩放程度，比如值为2图片的宽度和高度都变为以前的1/2
        options.inSampleSize = 1;
        // 根据屏的大小和图片大小计算出缩放比例
        if (width > height) {
            if (width > screenWidth)
                options.inSampleSize = width / screenWidth;
        } else {
            if (height > screenHeight)
                options.inSampleSize = height / screenHeight;
        }
        // 生成有像素经过缩放了的bitmap
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath, options);
        if (bitmap == null) {
            throw new FileNotFoundException("Couldn't open " + picturePath);
        }
        return bitmap;
    }
}
