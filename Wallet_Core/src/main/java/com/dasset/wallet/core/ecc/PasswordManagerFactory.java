package com.dasset.wallet.core.ecc;

import android.content.Context;
import android.os.Build;

import com.dasset.wallet.components.utils.KeyStoreUtil;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public final class PasswordManagerFactory {

    private PasswordManagerFactory() {
    }

    public static void put(Context context, String address, String password) throws NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyStoreUtil.getInstance().put(context, address, password);
        } else {
            PasswordManager.getInstance().setPassword(context, address, password);
        }
    }

    public static byte[] get(Context context, String address) throws NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return KeyStoreUtil.getInstance().get(context, address);
        } else {
            return PasswordManager.getInstance().getPassword(context, address).getBytes();
        }
    }
}
