package com.dasset.wallet.core.qrcode;

import com.dasset.wallet.core.contant.AbstractApp;
import com.dasset.wallet.core.contant.Constant;
import com.dasset.wallet.core.utils.Base58;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QRCodeUtil {

    public enum QRQuality {
        Normal(328), LOW(216);
        private int quality;

        private QRQuality(int quality) {
            this.quality = quality;
        }

        public int getQuality() {
            return this.quality;
        }

    }


    public static String[] split(String data) {
        if (data.contains(Constant.OLD_QR_CODE_SPLIT)) {
            return data.split(Constant.OLD_QR_CODE_SPLIT);
        } else {
            return data.split(Constant.QR_CODE_SPLIT);
        }
    }

    public static int indexOfPasswordSeed(String data) {
        int index;
        if (data.contains(Constant.OLD_QR_CODE_SPLIT)) {
            index = data.indexOf(Constant.OLD_QR_CODE_SPLIT);
        } else {
            index = data.indexOf(Constant.QR_CODE_SPLIT);
        }
        return index;
    }

    public static String getAddressFromPasswordSeed(String data) {
        if (data.contains(Constant.OLD_QR_CODE_SPLIT)) {
            return data.substring(0, data.indexOf(Constant.OLD_QR_CODE_SPLIT));
        } else {
            return Base58.hexToBase58WithAddress(data.substring(0, data.indexOf(Constant.QR_CODE_SPLIT)));
        }

    }

    public static String[] splitOfPasswordSeed(String data) {
        if (data.contains(Constant.OLD_QR_CODE_SPLIT)) {
            return data.split(Constant.OLD_QR_CODE_SPLIT);
        } else {
            return data.split(Constant.QR_CODE_SPLIT);
        }

    }

    public static String getNewVersionEncryptPrivKey(String encryptPrivKey) {
        if (encryptPrivKey.contains(Constant.OLD_QR_CODE_SPLIT)) {
            return encryptPrivKey.replace(Constant.OLD_QR_CODE_SPLIT, Constant.QR_CODE_SPLIT);
        } else {
            return encryptPrivKey;
        }
    }

    public static String encodeQrCodeString(String text) {
        return text.toUpperCase(Locale.US);
    }

    public static String decodeQrCodeString(String formatString) {
        if (oldVerifyQrcodeTransport(formatString)) {
            return oldDecodeQrCodeString(formatString);
        }
        return formatString;

    }

    public static boolean verifyBitherQRCode(String text) {
        boolean verifyNewVersion = true;
        boolean verifyOldVersion = true;
        if (Pattern.compile("[^0-9a-zA-Z/\\+\\$%-]").matcher(text).find()) {
            verifyNewVersion = false;
        }
        if (!oldVerifyQrcodeTransport(text)) {
            verifyOldVersion = false;
        }
        return verifyNewVersion || verifyOldVersion;
    }

    public static List<String> getQrCodeStringList(String data) {
        List<String> stringList = Lists.newArrayList();
        int          length     = data.length();
        int          number     = getNumOfQrCodeString(length);
        int          size       = (length + (number - 1)) / number;
        for (int i = 0; i < number; i++) {
            int start = i * size;
            int end   = (i + 1) * size;
            if (start > length - 1) {
                continue;
            }
            if (end > length) {
                end = length;
            }
            stringList.add(Integer.toString(number - 1) + Constant.QR_CODE_SPLIT + Integer.toString(i) + Constant.QR_CODE_SPLIT + data.substring(start, end));
        }
        return stringList;
    }

    public static int getNumOfQrCodeString(int length) {
        int quality = AbstractApp.bitherjSetting.getQRQuality().getQuality();
        if (length < quality) {
            return 1;
        } else if (length <= (quality - 4) * 10) {
            return length / (quality - 4) + 1;
        } else if (length <= (quality - 5) * 100) {
            return length / (quality - 5) + 1;
        } else if (length <= (quality - 6) * 1000) {
            return length / (quality - 6) + 1;
        } else {
            return 1000;
        }

    }

    private static String oldDecodeQrCodeString(String data) {
        Pattern      pattern      = Pattern.compile("\\*([a-z])");
        Matcher      matcher      = pattern.matcher(data.toLowerCase(Locale.US));
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            String letter = matcher.group(1);
            matcher.appendReplacement(stringBuffer, letter.toUpperCase(Locale.US));
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();

    }

    private static boolean oldVerifyQrcodeTransport(String data) {
        if (Pattern.compile("[^0-9A-Z\\*:]").matcher(data).find()) {
            return false;
        }
        return true;
    }
}
