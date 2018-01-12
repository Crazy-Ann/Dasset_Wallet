package com.dasset.wallet.components.utils;

import android.text.TextUtils;

import com.dasset.wallet.components.constant.Constant;
import com.dasset.wallet.components.constant.Regex;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class SecurityUtil {

//    static {
//        try {
//            Class<?> clazz = Class.forName("javax.crypto.JceSecurity");
//            Field nameField = clazz.getDeclaredField("isRestricted");
//            Field modifiersField = Field.class.getDeclaredField("modifiers");
//            modifiersField.setAccessible(true);
//            modifiersField.setInt(nameField, nameField.getModifiers() & ~Modifier.FINAL);
//            nameField.setAccessible(true);
//            nameField.set(null, java.lang.Boolean.FALSE);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    private static SecurityUtil securityUtil;

    private SecurityUtil() {
        // cannot be instantiated
    }

    public static synchronized SecurityUtil getInstance() {
        if (securityUtil == null) {
            securityUtil = new SecurityUtil();
        }
        return securityUtil;
    }

    public static void releaseInstance() {
        if (securityUtil != null) {
            securityUtil = null;
        }
    }

    public char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    public char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? Constant.Data.DIGITS_LOWER : Constant.Data.DIGITS_UPPER);
    }

    public String encodeHexStr(byte[] data) {
        return encodeHexString(data, true);
    }

    public String encodeHexString(byte[] data, boolean toLowerCase) {
        return encodeHexString(data, toLowerCase ? Constant.Data.DIGITS_LOWER : Constant.Data.DIGITS_UPPER);
    }

    private String encodeHexString(byte[] data, char[] toDigits) {
        return String.valueOf(encodeHex(data, toDigits));
    }

    private char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    public byte[] decodeHex(char[] data) {
        int len = data.length;
        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }

        byte[] out = new byte[len >> 1];
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }
        return out;
    }

    private int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

    public String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            byte high = (byte) ((bytes[i] & 0xf0) >> 4);
            byte low = (byte) (bytes[i] & 0x0f);
            builder.append(nibble2char(high));
            builder.append(nibble2char(low));
        }
        return builder.toString();
    }

    public byte[] hexStringToByte(String data) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        int len = data.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; ++i) {
            char ch1 = data.charAt(i * 2);
            char ch2 = data.charAt(i * 2 + 1);
            result[i] = (byte) ((charToHex(ch1) << 4) + charToHex(ch2));
        }
        return result;
    }

    public byte charToHex(char data) {
        if (data >= '0' && data <= '9')
            return (byte) (data - '0');
        if (data >= 'a' && data <= 'f')
            return (byte) (10 + (data - 'a'));
        if (data >= 'A' && data <= 'F')
            return (byte) (10 + (data - 'A'));
        return 0;
    }

    private int byteToInt(byte b, byte c) {
        short s0 = (short) (c & 0xff);
        short s1 = (short) (b & 0xff);
        s1 <<= 8;
        return (short) (s0 | s1);
    }

    private byte[] intToByte(int res) {
        byte[] targets = new byte[2];
        targets[1] = (byte) (res & 0xff);
        targets[0] = (byte) ((res >> 8) & 0xff);
        return targets;
    }

    private byte toByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private static char nibble2char(byte data) {
        byte nibble = (byte) (data & 0x0f);
        if (nibble < 10) {
            return (char) ('0' + nibble);
        }
        return (char) ('A' + nibble - 10);
    }

    /************************************************/

    public byte[] encryptDes(byte[] data, String key, String alg, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(alg);
        if (alg.contains("/CBC/") && iv != null) {
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeyFactory.getInstance(Constant.Data.ALGORITHM0).generateSecret(new DESKeySpec(key.getBytes(Regex.UTF_8.getRegext()))), new IvParameterSpec(iv));
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeyFactory.getInstance(Constant.Data.ALGORITHM0).generateSecret(new DESKeySpec(key.getBytes(Regex.UTF_8.getRegext()))));
        }
        return cipher.doFinal(data);
    }

    public byte[] decryptDes(byte[] data, String key, String alg, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(alg);
        if (alg.contains("/CBC/") && iv != null) {
            cipher.init(Cipher.DECRYPT_MODE, SecretKeyFactory.getInstance(Constant.Data.ALGORITHM0).generateSecret(new DESKeySpec(key.getBytes(Regex.UTF_8.getRegext()))), new IvParameterSpec(iv));
        } else {
            cipher.init(Cipher.DECRYPT_MODE, SecretKeyFactory.getInstance(Constant.Data.ALGORITHM0).generateSecret(new DESKeySpec(key.getBytes(Regex.UTF_8.getRegext()))));
        }
        return cipher.doFinal(data);
    }

    public String encrypt3Des(String message, String key, String format) throws UnsupportedEncodingException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        String k1 = key.substring(0, 8);
        String k2 = key.substring(8, 16);
        String k3 = key.substring(16, 24);
        boolean bHex16 = "ToHex16".equalsIgnoreCase(format);
        byte[] msg = message.getBytes(Regex.UTF_8.getRegext());
        int newLen = (msg.length + 8) & (~0x07);
        if (newLen != msg.length) {
            int k;
            byte[] msg2 = new byte[newLen];
            for (k = 0; k < msg.length; ++k)
                msg2[k] = msg[k];
            for (k = msg.length; k < msg2.length; ++k)
                msg2[k] = (byte) (newLen - msg.length);
            msg = msg2;
        }

        byte[] data = new byte[msg.length];
        byte[] iv = new byte[8];
        int i;

        for (i = 0; i + 8 <= msg.length; i += 8) {
            byte[] data1 = new byte[8];
            int j;
            for (j = 0; j < 8; ++j)
                data1[j] = msg[i + j];

            if (i == 0)
                iv = k1.getBytes(Regex.UTF_8.getRegext());
            else
                for (j = 0; j < 8; ++j)
                    iv[j] = data[i + j - 8];

            data1 = encryptDes(data1, k1, Constant.Data.ALGORITHM_CBC, iv);
            data1 = decryptDes(data1, k2, Constant.Data.ALGORITHM_ECB, null);
            data1 = encryptDes(data1, k3, Constant.Data.ALGORITHM_ECB, null);
            for (j = 0; j < 8; ++j)
                data[i + j] = data1[j];
        }

        String result;
        if (bHex16)
            result = bytesToHexString(data);
        else
            result = Base64Util.encode(data);
        return result;
    }

    public String decrypt3Des(String message, String key, String format) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, InvalidKeySpecException {
        String k1 = key.substring(0, 8);
        String k2 = key.substring(8, 16);
        String k3 = key.substring(16, 24);
        byte[] iv = new byte[8];
        int i;
        boolean bHex16 = "ToHex16".equalsIgnoreCase(format);
        byte[] msg;
        if (bHex16)
            msg = hexStringToByte(message);
        else
            msg = Base64Util.decode(message);
        byte[] data = new byte[msg.length];

        for (i = 0; i + 8 <= msg.length; i += 8) {
            byte[] data1 = new byte[8];
            int j;
            for (j = 0; j < 8; ++j)
                data1[j] = msg[i + j];

            data1 = decryptDes(data1, k3, Constant.Data.ALGORITHM_ECB, null);
            data1 = encryptDes(data1, k2, Constant.Data.ALGORITHM_ECB, null);

            if (i == 0)
                iv = k1.getBytes(Regex.UTF_8.getRegext());
            else
                for (j = 0; j < 8; ++j)
                    iv[j] = msg[i + j - 8];

            data1 = decryptDes(data1, k1, Constant.Data.ALGORITHM_CBC, iv);
            for (j = 0; j < 8; ++j)
                data[i + j] = data1[j];
        }

        // 去掉尾部的padding
        byte val = data[data.length - 1];
        if (val > 0 && val <= data.length) {
            if (val == data.length)
                return "";

            byte[] data2 = new byte[data.length - val];
            // Arrays.copyOf( data, data.length-val);
            for (i = 0; i < data2.length; ++i) {
                data2[i] = data[i];
            }
            return new String(data2, Regex.UTF_8.getRegext());
        }
        return new String(data, Regex.UTF_8.getRegext());
    }

    public byte[] des3EncodeECB(byte[] key, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(Regex.DESEDE_EBC_PKCS5PADDING.getRegext());
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeyFactory.getInstance(Regex.DESEDE.getRegext()).generateSecret(new DESedeKeySpec(key)));
        return cipher.doFinal(data);
    }

    public byte[] des3DecodeECB(byte[] key, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(Regex.DESEDE_EBC_PKCS5PADDING.getRegext());
        cipher.init(Cipher.DECRYPT_MODE, SecretKeyFactory.getInstance(Regex.DESEDE.getRegext()).generateSecret(new DESedeKeySpec(key)));
        return cipher.doFinal(data);
    }

    public byte[] des3EncodeCBC(byte[] key, byte[] keyiv, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(Regex.DESEDE_CBC_PKCS5PADDING.getRegext());
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeyFactory.getInstance(Regex.DESEDE.getRegext()).generateSecret(new DESedeKeySpec(key)), new IvParameterSpec(keyiv));
        return cipher.doFinal(data);
    }

    public byte[] des3DecodeCBC(byte[] key, byte[] keyiv, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(Regex.DESEDE_CBC_PKCS5PADDING.getRegext());
        cipher.init(Cipher.DECRYPT_MODE, SecretKeyFactory.getInstance(Regex.DESEDE.getRegext()).generateSecret(new DESedeKeySpec(key)), new IvParameterSpec(keyiv));
        return cipher.doFinal(data);
    }

    public PublicKey getRsaPublicKey(String key) throws CertificateException {
        return CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(hexStringToByte(key))).getPublicKey();
    }

//    public byte[] encryptAESEBC(String data, String key) throws NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
//            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(Regex.UTF_8.getRegext()), Regex.AES.getRegext());
//            Cipher cipher = Cipher.getInstance(Regex.AES_ECB_PKCS5PADDING.getRegext());
//            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
//            return cipher.doFinal(data.getBytes(Regex.UTF_8.getRegext()));
//    }

    public byte[] encryptAESEBC(String data, String key) throws NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        return encryptAESEBC(data, key, 128);
    }

    public byte[] encryptAESEBC(String data, String key, int keySize) throws NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        LogUtil.getInstance().print(String.format("aes encrypt key is:%s", key));
        KeyGenerator keyGenerator = KeyGenerator.getInstance(Regex.AES.getRegext());
        keyGenerator.init(keySize, new SecureRandom(key.getBytes()));
//        SecretKey secretKey = keyGenerator.generateKey();
//        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), Regex.AES.getRegext());
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(Regex.UTF_8.getRegext()), Regex.AES.getRegext());
        Cipher cipher = Cipher.getInstance(Regex.AES_ECB_PKCS5PADDING.getRegext());
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher.doFinal(data.getBytes(Regex.UTF_8.getRegext()));
    }

//    public byte[] decryptAESEBC(byte[] data, String key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
//            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(Regex.UTF_8.getRegext()), Regex.AES.getRegext());
//            Cipher cipher = Cipher.getInstance(Regex.AES_ECB_PKCS5PADDING.getRegext());
//            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
//            return cipher.doFinal(data);
//    }

    public byte[] decryptAESEBC(byte[] data, String key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        return decryptAESEBC(data, key, 128);
    }

    public byte[] decryptAESEBC(byte[] data, String key, int keySize) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        LogUtil.getInstance().print(String.format("aes decrypt key is:%s", key));
        KeyGenerator keyGenerator = KeyGenerator.getInstance(Regex.AES.getRegext());
        keyGenerator.init(keySize, new SecureRandom(key.getBytes()));
//        SecretKey secretKey = keyGenerator.generateKey();
//        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), Regex.AES.getRegext());
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(Regex.UTF_8.getRegext()), Regex.AES.getRegext());
        Cipher cipher = Cipher.getInstance(Regex.AES_ECB_PKCS5PADDING.getRegext());
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return cipher.doFinal(data);
    }

    public byte[] encryptAESCBC(String data, String key) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(Regex.AES.getRegext());
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(), Regex.AES.getRegext()));
        return cipher.doFinal(data.getBytes(Regex.UTF_8.getRegext()));
    }

    public byte[] decryptAESCBC(byte[] data, String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(Regex.AES.getRegext());
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), Regex.AES.getRegext()));
        return cipher.doFinal(data);
    }

    public byte[] encryptAESCBC(String data, SecretKey secretKey, IvParameterSpec ivParameterSpec) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        if (!TextUtils.isEmpty(data) && secretKey != null && ivParameterSpec != null) {
            Cipher cipher = Cipher.getInstance(Regex.AES_CBC_PKCS5PADDING.getRegext());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            return cipher.doFinal(data.getBytes(Regex.UTF_8.getRegext()));
        } else {
            return null;
        }
    }

    public String decryptAESCBC(byte[] data, SecretKey secretKey, IvParameterSpec ivParameterSpec) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(Regex.AES_CBC_PKCS5PADDING.getRegext());
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        return new String(cipher.doFinal(data), Regex.UTF_8.getRegext());
    }

    public String encryptMD5With16Bit(String data) {
        if (!TextUtils.isEmpty(data)) {
            return encryptMD5With16Bit(data.getBytes());
        } else {
            return null;
        }
    }

    public String encryptMD5With16Bit(byte[] buffer) {
        try {
            return bytesToHexString(MessageDigest.getInstance(Regex.MD5.getRegext()).digest(buffer)).substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String encryptMD5With32Bit(String data) {
        if (!TextUtils.isEmpty(data)) {
            return encryptMD5With32Bit(data.getBytes());
        } else {
            return null;
        }
    }

    public String encryptMD5With32Bit(byte[] buffer) {
        try {
            return bytesToHexString(MessageDigest.getInstance(Regex.MD5.getRegext()).digest(buffer));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String encryptSHA1(byte[] buffer) {
        try {
            return bytesToHexString(MessageDigest.getInstance(Regex.SHA_1.getRegext()).digest(buffer));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String encryptSHA1(String s) {
        return encryptSHA1(s.getBytes());
    }

    static {
        System.loadLibrary("encrypt");
    }

    public static native String encryptAES(String json, String apk, boolean isEncrypt, int type);
}
