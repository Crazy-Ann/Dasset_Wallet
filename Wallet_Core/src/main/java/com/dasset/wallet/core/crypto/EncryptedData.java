package com.dasset.wallet.core.crypto;

import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.core.contant.Constant;
import com.dasset.wallet.core.qrcode.QRCodeUtil;
import com.dasset.wallet.core.qrcode.SaltForQRCode;
import com.dasset.wallet.core.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptedData {

    private byte[]        encryptedData;
    private byte[]        initialisationVector;
    private SaltForQRCode saltForQRCode;


    public EncryptedData(String data) {
        String[] datas = QRCodeUtil.splitOfPasswordSeed(data);
        if (datas.length != 3) {
            LogUtil.getInstance().print("ncryptedData format error");
        }
        this.initialisationVector = Utils.hexStringToByteArray(datas[1]);
        this.encryptedData = Utils.hexStringToByteArray(datas[0]);
        this.saltForQRCode = new SaltForQRCode(Utils.hexStringToByteArray(datas[2]));
    }

    public EncryptedData(byte[] dataToEncrypt, CharSequence password) {
        this(dataToEncrypt, password, true, false);
    }

    public EncryptedData(byte[] dataToEncrypt, CharSequence password, boolean isFromXRandom) {
        this(dataToEncrypt, password, true, isFromXRandom);
    }

    public EncryptedData(byte[] dataToEncrypt, CharSequence password, boolean isCompress, boolean isFromXRandom) {
        KeyCrypterScrypt    keyCrypterScrypt    = new KeyCrypterScrypt();
        EncryptedPrivateKey encryptedPrivateKey = keyCrypterScrypt.encrypt(dataToEncrypt, keyCrypterScrypt.deriveKey(password));
        this.encryptedData = encryptedPrivateKey.getEncryptedBytes();
        this.initialisationVector = encryptedPrivateKey.getInitialisationVector();
        this.saltForQRCode = new SaltForQRCode(keyCrypterScrypt.getSalt(), isCompress, isFromXRandom);
    }

    public byte[] decrypt(CharSequence password) {
        KeyCrypterScrypt keyCrypterScrypt = new KeyCrypterScrypt(saltForQRCode.getSalt());
        return keyCrypterScrypt.decrypt(new EncryptedPrivateKey(initialisationVector, encryptedData), keyCrypterScrypt.deriveKey(password));
    }

    public String toEncryptedString() {
        return Utils.bytesToHexString(encryptedData).toUpperCase()
                + Constant.QR_CODE_SPLIT + Utils.bytesToHexString(initialisationVector).toUpperCase()
                + Constant.QR_CODE_SPLIT + Utils.bytesToHexString(saltForQRCode.getSalt()).toUpperCase();
    }

    public String toEncryptedStringForQRCode() {
        return Utils.bytesToHexString(encryptedData).toUpperCase()
                + Constant.QR_CODE_SPLIT + Utils.bytesToHexString(initialisationVector).toUpperCase()
                + Constant.QR_CODE_SPLIT + Utils.bytesToHexString(saltForQRCode.getQrCodeSalt()).toUpperCase();
    }

    public String toEncryptedStringForQRCode(boolean isCompress, boolean isFromXRandom) {
        SaltForQRCode newSaltForQRCode = new SaltForQRCode(saltForQRCode.getSalt(), isCompress, isFromXRandom);
        return Utils.bytesToHexString(encryptedData).toUpperCase()
                + Constant.QR_CODE_SPLIT + Utils.bytesToHexString(initialisationVector).toUpperCase()
                + Constant.QR_CODE_SPLIT + Utils.bytesToHexString(newSaltForQRCode.getQrCodeSalt()).toUpperCase();
    }

    public boolean isXRandom() {
        return saltForQRCode.isFromXRandom();
    }

    public boolean isCompressed() {
        return saltForQRCode.isCompressed();
    }

    public static String changePassword(String encryptStr, CharSequence oldPassword, CharSequence newPassword) {
        EncryptedData encryptedData = new EncryptedData(encryptStr);
        return new EncryptedData(encryptedData.decrypt(oldPassword), newPassword).toEncryptedString();
    }

    public static String changePasswordKeepFlag(String encryptStr, CharSequence oldPassword, CharSequence newPassword) {
        EncryptedData encryptedData = new EncryptedData(encryptStr);
        return new EncryptedData(encryptedData.decrypt(oldPassword), newPassword, encryptedData.isCompressed(), encryptedData.isXRandom()).toEncryptedString();
    }
}
