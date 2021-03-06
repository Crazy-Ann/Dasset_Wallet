package com.dasset.wallet.components.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public final class SharedPreferenceUtil {

    private static SharedPreferenceUtil sharedPreferenceUtil;
    private SharedPreferences preferences;

    private SharedPreferenceUtil() {
        // cannot be instantiated
    }

    public static synchronized SharedPreferenceUtil getInstance() {
        if (sharedPreferenceUtil == null) {
            sharedPreferenceUtil = new SharedPreferenceUtil();
        }
        return sharedPreferenceUtil;
    }

    public static void releaseInstance() {
        if (sharedPreferenceUtil != null) {
            sharedPreferenceUtil = null;
        }
    }

    public void putLong(Context ctx, String fileName, int mode, String key, long value) {
        if (preferences == null) {
            preferences = ctx.getSharedPreferences(fileName, mode);
        }
        preferences.edit().putLong(key, value).apply();
    }

    public void putInt(Context ctx, String fileName, int mode, String key, int value) {
        if (preferences == null) {
            preferences = ctx.getSharedPreferences(fileName, mode);
        }
        preferences.edit().putInt(key, value).apply();
    }

    public void putString(Context ctx, String fileName, int mode, String key, String value) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        if (preferences == null) {
            preferences = ctx.getSharedPreferences(fileName, mode);
        }
        preferences.edit().putString(key, value).apply();
    }

    public void putBoolean(Context ctx, String fileName, int mode, String key, boolean value) {
        if (preferences == null) {
            preferences = ctx.getSharedPreferences(fileName, mode);
        }
        preferences.edit().putBoolean(key, value).apply();
    }

    public void putObject(Context ctx, String fileName, int mode, String key, List<?> value) {
        try {
            if (preferences == null) {
                preferences = ctx.getSharedPreferences(fileName, mode);
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            new ObjectOutputStream(byteArrayOutputStream).writeObject(value);
            preferences.edit().putString(key, new String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT))).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getLong(Context ctx, String fileName, int mode, String key, long defValue) {
        if (preferences == null) {
            preferences = ctx.getSharedPreferences(fileName, mode);
        }
        return preferences.getLong(key, defValue);
    }

    public int getInt(Context ctx, String fileName, int mode, String key, int defValue) {
        if (preferences == null) {
            preferences = ctx.getSharedPreferences(fileName, mode);
        }
        return preferences.getInt(key, defValue);
    }

    public String getString(Context ctx, String fileName, int mode, String key, String defValue) {
        if (!TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(key)) {
            if (preferences == null) {
                preferences = ctx.getSharedPreferences(fileName, mode);
            }
            return preferences.getString(key, defValue);
        } else {
            return null;
        }
    }

    public boolean getBoolean(Context ctx, String fileName, int mode, String key, boolean defValue) {
        if (preferences == null) {
            preferences = ctx.getSharedPreferences(fileName, mode);
        }
        return preferences.getBoolean(key, defValue);
    }

    public List<Map<String, String>> getObject(Context ctx, String fileName, int mode, String key, String defValue) {
        return getObject(ctx, fileName, mode, key, defValue, false);
    }

    public List<Map<String, String>> getObject(Context ctx, String fileName, int mode, String key, String defValue, boolean isEncrypt) {
        try {
            if (!TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(key)) {
                if (preferences == null) {
                    preferences = ctx.getSharedPreferences(fileName, mode);
                }
                String value = preferences.getString(key, defValue);
                if (!TextUtils.isEmpty(value)) {
                    if (isEncrypt) {
                        key = SecurityUtil.getInstance().encryptMD5With32Bit(key);
                        return (List<Map<String, String>>) new ObjectInputStream(new ByteArrayInputStream(SecurityUtil.getInstance().decryptAESCBC(Base64.decode(value, Base64.DEFAULT), key))).readObject();
                    } else {
                        return (List<Map<String, String>>) new ObjectInputStream(new ByteArrayInputStream(Base64.decode(value, Base64.DEFAULT))).readObject();
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (IOException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | ClassNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void remove(Context ctx, String fileName, int mode, String key) {
        remove(ctx, fileName, mode, key, false);
    }

    public void remove(Context ctx, String fileName, int mode, String key, boolean isEncrypt) {
        if (preferences == null) {
            preferences = ctx.getSharedPreferences(fileName, mode);
        }
        if (isEncrypt) {
            key = SecurityUtil.getInstance().encryptMD5With32Bit(key);
        }
        preferences.edit().remove(key).apply();
    }

    public void clear(Context ctx, String fileName, int mode) {
        if (preferences == null) {
            preferences = ctx.getSharedPreferences(fileName, mode);
        }
        preferences.edit().clear().apply();
    }
}
