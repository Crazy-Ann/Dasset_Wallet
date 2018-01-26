package com.dasset.wallet.core.crypto;


import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.core.contant.Constant;
import com.dasset.wallet.core.db.facade.BaseProvider;
import com.dasset.wallet.core.exception.AddressFormatException;
import com.dasset.wallet.core.qrcode.QRCodeUtil;
import com.dasset.wallet.core.utils.Base58;
import com.dasset.wallet.core.utils.PrivateKeyUtil;
import com.dasset.wallet.core.utils.Utils;

public class PasswordSeed {
    
    private String address;
    private String keyStr;

    public PasswordSeed(String str) {
        LogUtil.getInstance().i("------------------------------------", "PasswordSeed start");
        LogUtil.getInstance().i("------>str:", str);
        int indexOfSplit = QRCodeUtil.indexOfPasswordSeed(str);
        LogUtil.getInstance().i("------>indexOfSplit:", String.valueOf(indexOfSplit));
        this.address = QRCodeUtil.getAddressFromPasswordSeed(str);
        LogUtil.getInstance().i("------>address:", address);
        this.keyStr = str.substring(indexOfSplit + 1);
        LogUtil.getInstance().i("------>keyStr:", keyStr);
        LogUtil.getInstance().i("------------------------------------", "PasswordSeed end");
    }


    public PasswordSeed(String address, String encryptedKey) {
        this.address = address;
        this.keyStr = encryptedKey;
        LogUtil.getInstance().i("------------------------------------", "PasswordSeed start");
        LogUtil.getInstance().i("------>address:", address);
        LogUtil.getInstance().i("------>encryptedKey:", encryptedKey);
        LogUtil.getInstance().i("------------------------------------", "PasswordSeed end");
    }

    public boolean checkPassword(CharSequence password) {
        ECKey  ecKey = PrivateKeyUtil.getECKeyFromSingleString(keyStr, password);
        String ecKeyAddress;
        if (ecKey == null) {
            return false;
        } else {
            ecKeyAddress = ecKey.toAddress();
            ecKey.clearPrivateKey();
        }
        return Utils.compareString(this.address,
                                   ecKeyAddress);

    }

    public boolean changePassword(CharSequence oldPassword, CharSequence newPassword) {
        keyStr = PrivateKeyUtil.changePassword(keyStr, oldPassword, newPassword);
        return !Utils.isEmpty(keyStr);

    }

    public ECKey getECKey(CharSequence password) {
        return PrivateKeyUtil.getECKeyFromSingleString(keyStr, password);
    }

    public String getAddress() {
        return this.address;
    }

    public String getKeyStr() {
        return this.keyStr;
    }


    public String toPasswordSeedString() {
        try {
            String passwordSeedString = Base58.bas58ToHexWithAddress(this.address) + Constant.QR_CODE_SPLIT + QRCodeUtil.getNewVersionEncryptPrivKey(this.keyStr);
            LogUtil.getInstance().i("------------------------------------", "toPasswordSeedString start");
            LogUtil.getInstance().i("------>passwordSeedString:", passwordSeedString);
            LogUtil.getInstance().i("------------------------------------", "toPasswordSeedString end");
            return passwordSeedString;
        } catch (AddressFormatException e) {
            throw new RuntimeException("passwordSeed  address is format error ," + this.address);

        }

    }

    public static boolean hasPasswordSeed() {
        return BaseProvider.iAddressProvider.hasPasswordSeed();
    }

    public static PasswordSeed getPasswordSeed() {
        return BaseProvider.iAddressProvider.getPasswordSeed();
    }

}
