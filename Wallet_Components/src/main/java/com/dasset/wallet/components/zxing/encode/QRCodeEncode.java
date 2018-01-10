package com.dasset.wallet.components.zxing.encode;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;

public class QRCodeEncode {

    private static int IMAGE_HALFWIDTH = 50;

    public static byte[] createQRCode(String data, int size) {
        if (!TextUtils.isEmpty(data)) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap bitmap = createQRCode(data, size, null, -1);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } else {
            return null;
        }
    }

    public static Bitmap createQRCode(String data, int size, Bitmap bitmap, int type) {
        try {
            IMAGE_HALFWIDTH = size / 10;
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            BitMatrix bitMatrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints);

            int[] pixels = new int[size * size];
            if (bitmap != null) {
                bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);

                boolean flag = true;
                int width = bitMatrix.getWidth();//矩阵高度
                int height = bitMatrix.getHeight();//矩阵宽度
                int halfW = width / 2;
                int halfH = height / 2;
                Matrix matrix = new Matrix();
                float sx = (float) 2 * IMAGE_HALFWIDTH / bitmap.getWidth();
                float sy = (float) 2 * IMAGE_HALFWIDTH / bitmap.getHeight();
                switch (type) {
                    case 0:
                        for (int y = 0; y < size; y++) {
                            for (int x = 0; x < size; x++) {
                                if (bitMatrix.get(x, y)) {
                                    pixels[y * size + x] = bitmap.getPixel(x, y);
                                } else {
                                    pixels[y * size + x] = 0xffffffff;
                                }

                            }
                        }
                        break;
                    case 1:
                        for (int y = 0; y < size; y++) {
                            for (int x = 0; x < size; x++) {
                                if (bitMatrix.get(x, y)) {
                                    pixels[y * size + x] = 0xfff92736;
                                } else {
                                    pixels[y * size + x] = bitmap.getPixel(x, y) & 0x66ffffff;
                                }
                            }
                        }
                        break;
                    case 2:
                        for (int y = 0; y < size; y++) {
                            for (int x = 0; x < size; x++) {
                                if (bitMatrix.get(x, y)) {
                                    if (flag) {
                                        flag = false;
                                        pixels[y * size + x] = 0xff000000;
                                    } else {
                                        pixels[y * size + x] = bitmap.getPixel(x, y);
                                        flag = true;
                                    }
                                } else {
                                    pixels[y * size + x] = 0xffffffff;
                                }
                            }
                        }
                        break;
                    case 3:
                        matrix.setScale(sx, sy);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                        for (int y = 0; y < size; y++) {
                            for (int x = 0; x < size; x++) {
                                if (x > halfW - IMAGE_HALFWIDTH && x < halfW + IMAGE_HALFWIDTH && y > halfH - IMAGE_HALFWIDTH && y < halfH + IMAGE_HALFWIDTH) {
                                    pixels[y * width + x] = bitmap.getPixel(x - halfW + IMAGE_HALFWIDTH, y - halfH + IMAGE_HALFWIDTH);
                                } else {
                                    if (bitMatrix.get(x, y)) {
                                        pixels[y * size + x] = 0xff37b19e;
                                    } else {
                                        pixels[y * size + x] = 0xffffffff;
                                    }
                                }
                            }
                        }
                        break;
                    case 4:
                        matrix.setScale(sx, sy);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                        for (int y = 0; y < size; y++) {
                            for (int x = 0; x < size; x++) {
                                if (x > halfW - IMAGE_HALFWIDTH && x < halfW + IMAGE_HALFWIDTH && y > halfH - IMAGE_HALFWIDTH && y < halfH + IMAGE_HALFWIDTH) {
                                    pixels[y * width + x] = bitmap.getPixel(x - halfW + IMAGE_HALFWIDTH, y - halfH + IMAGE_HALFWIDTH);
                                } else {
                                    if (bitMatrix.get(x, y)) {
                                        pixels[y * size + x] = 0xff111111;
                                        if (x < 115 && (y < 115 || y >= size - 115) || (y < 115 && x >= size - 115)) {
                                            pixels[y * size + x] = 0xfff92736;
                                        }
                                    } else {
                                        pixels[y * size + x] = 0xffffffff;
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        for (int y = 0; y < size; y++) {
                            for (int x = 0; x < size; x++) {
                                if (bitMatrix.get(x, y)) {
                                    pixels[y * size + x] = bitmap.getPixel(x, y);
                                } else {
                                    pixels[y * size + x] = 0xffffffff;
                                }

                            }
                        }
                        break;
                }
            } else {
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        if (bitMatrix.get(x, y)) {
                            pixels[y * size + x] = 0xff000000;
                        } else {
                            pixels[y * size + x] = 0xffffffff;
                        }
                    }
                }
            }

            Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            result.setPixels(pixels, 0, size, 0, 0, size, size);
            return result;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
