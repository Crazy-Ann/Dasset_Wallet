package com.dasset.wallet.components.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class KeyStoreUtil {

    private static KeyStoreUtil keyStoreUtil;

    private KeyStoreUtil() {
        // cannot be instantiated
    }

    public static synchronized KeyStoreUtil getInstance() {
        if (keyStoreUtil == null) {
            keyStoreUtil = new KeyStoreUtil();
        }
        return keyStoreUtil;
    }

    public static void releaseInstance() {
        if (keyStoreUtil != null) {
            keyStoreUtil = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private synchronized boolean setData(Context context, byte[] data, String alias, String aliasFilePath, String aliasIVFilePath) {
        if (data != null) {
            try {
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);
                // Create the keys if necessary
                if (!keyStore.containsAlias(alias)) {
                    KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                    // Set the alias of the entry in Android KeyStore where the key will appear
                    // and the constrains (purposes) in the constructor of the Builder
                    keyGenerator.init(new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_CBC).setKeySize(256).setUserAuthenticationRequired(false).setRandomizedEncryptionRequired(true).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build());
                    keyGenerator.generateKey();
                }
                SecretKey secretKey = (SecretKey) keyStore.getKey(alias, null);
                if (secretKey != null) {
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                    if (!IOUtil.getInstance().writeBytes(new File(context.getFilesDir(), aliasIVFilePath).getAbsolutePath(), cipher.getIV())) {
                        keyStore.deleteEntry(alias);
                        return false;
                    }
                    CipherOutputStream cipherOutputStream = null;
                    try {
                        cipherOutputStream = new CipherOutputStream(new FileOutputStream(new File(context.getFilesDir(), aliasFilePath).getAbsolutePath()), cipher);
                        cipherOutputStream.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    } finally {
                        if (cipherOutputStream != null) {
                            cipherOutputStream.close();
                        }
                    }
                    return true;
                } else {
                    LogUtil.getInstance().print("secret is null on setData: " + alias);
                }
            } catch (NoSuchProviderException | IOException | UnrecoverableKeyException | InvalidAlgorithmParameterException | InvalidKeyException | KeyStoreException | CertificateException | NoSuchAlgorithmException | NoSuchPaddingException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private synchronized byte[] getData(Context context, String alias, String aliasFilePath, String aliasIVFilePath) {
        try {
            File aliasIVFile = new File(context.getFilesDir(), aliasIVFilePath);
            File aliasFile = new File(context.getFilesDir(), aliasFilePath);
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey(alias, null);
            if (secretKey != null) {
                if (!aliasIVFile.exists() || !aliasFile.exists()) {
                    try {
                        keyStore.deleteEntry(alias);
                        aliasIVFile.delete();
                        aliasFile.delete();
                        if (aliasIVFile.exists() != aliasFile.exists()) {
                            return null;
                        }
                    } catch (KeyStoreException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                byte[] iv = IOUtil.getInstance().readBytes(aliasIVFile.getAbsolutePath());
                if (iv != null || iv.length != 0) {
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
                    CipherInputStream cipherInputStream = new CipherInputStream(new FileInputStream(aliasFile), cipher);
                    return IOUtil.getInstance().readBytes(cipherInputStream);
                } else {
                    return null;
                }
            } else {
                if (!new File(context.getFilesDir(), aliasFilePath).exists()) {
                    return null;/* file also not there, fine then */
                }
            }
        } catch (IOException | UnrecoverableKeyException | InvalidAlgorithmParameterException | InvalidKeyException | KeyStoreException | CertificateException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void put(Context context, String address, String password) {
        setData(context, password.getBytes(), address, address, address + "iv");
    }

    public byte[] get(Context context, String address) {
        return getData(context, address, address, address + "iv");
    }
}
