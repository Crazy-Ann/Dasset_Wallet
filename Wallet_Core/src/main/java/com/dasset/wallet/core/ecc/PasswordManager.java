package com.dasset.wallet.core.ecc;


import android.content.Context;

import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.SecurityUtil;
import com.dasset.wallet.components.utils.SharedPreferenceUtil;

import org.spongycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PasswordManager {

    private final String key = "35TheTru5tWa11ets3cr3tK3y377123!";
    private final String iv = "8201va0184a0md8i";

    private static PasswordManager passwordManager;

    private PasswordManager() {
        // cannot be instantiated
    }

    public static synchronized PasswordManager getInstance() {
        if (passwordManager == null) {
            passwordManager = new PasswordManager();
        }
        return passwordManager;
    }

    public static void releaseInstance() {
        if (passwordManager != null) {
            passwordManager = null;
        }
    }

    public void setPassword(Context context, String address, String password) throws NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        SecretKey secretKey = new SecretKeySpec(key.getBytes(Regex.UTF_8.getRegext()), Regex.AES.getRegext());
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(Regex.UTF_8.getRegext()));
        byte[] encryptedPassword = SecurityUtil.getInstance().encryptAESCBC(password, secretKey, ivParameterSpec);
        SharedPreferenceUtil.getInstance().putString(context, Constant.FilePath.PASSWORD, Context.MODE_PRIVATE, address, Hex.toHexString(encryptedPassword));
    }

    public String getPassword(Context context, String address) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        byte[] encryptedPassword = Hex.decode(SharedPreferenceUtil.getInstance().getString(context, Constant.FilePath.PASSWORD, Context.MODE_PRIVATE, address, null));
        SecretKey secretKey = new SecretKeySpec(key.getBytes(Regex.UTF_8.getRegext()), Regex.AES.getRegext());
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(Regex.UTF_8.getRegext()));
        return SecurityUtil.getInstance().decryptAESCBC(encryptedPassword, secretKey, ivParameterSpec);
    }
}
