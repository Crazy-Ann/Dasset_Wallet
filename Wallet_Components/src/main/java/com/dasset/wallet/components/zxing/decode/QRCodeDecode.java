package com.dasset.wallet.components.zxing.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.dasset.wallet.components.utils.LogUtil;
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

    public static final int                         MAX_FRAME_WIDTH  = 1200; // = 5/8 * 1920
    public static final int                         MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080
    public static final Map<DecodeHintType, Object> HINTS            = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);

    static {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.QR_CODE);
        HINTS.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        HINTS.put(DecodeHintType.CHARACTER_SET, "utf-8");
    }

    private QRCodeDecode() {
    }

    public static void decodeQR(String path, OnScannerCompletionListener onScannerCompletionListener) throws FileNotFoundException, FormatException, ChecksumException, NotFoundException {
        decodeQR(loadBitmap(path), onScannerCompletionListener);
    }

    public static void decodeQR(Bitmap bitmap, final OnScannerCompletionListener onScannerCompletionListener) throws FormatException, ChecksumException, NotFoundException {
        if (bitmap != null) {
            int   width  = bitmap.getWidth();
            int   height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap       binaryBitmap       = new BinaryBitmap(new GlobalHistogramBinarizer(rgbLuminanceSource));
            QRCodeReader       qrCodeReader       = new QRCodeReader();
            if (onScannerCompletionListener != null) {
                onScannerCompletionListener.OnScannerCompletion(qrCodeReader.decode(binaryBitmap, HINTS), bitmap);
            }
        }
    }

    private static Bitmap loadBitmap(String path) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        int width        = options.outWidth;
        int height       = options.outHeight;
        int screenWidth  = MAX_FRAME_WIDTH;
        int screenHeight = MAX_FRAME_HEIGHT;
        options.inSampleSize = 1;
        if (width > height) {
            if (width > screenWidth) {
                options.inSampleSize = width / screenWidth;
            }
        } else {
            if (height > screenHeight) {
                options.inSampleSize = height / screenHeight;
            }
        }
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (bitmap == null) {
            throw new FileNotFoundException("Couldn't open " + path);
        }
        return bitmap;
    }
}
