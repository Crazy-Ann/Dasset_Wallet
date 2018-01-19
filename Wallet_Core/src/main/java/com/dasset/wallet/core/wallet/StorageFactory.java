package com.dasset.wallet.core.wallet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.dasset.wallet.components.BuildConfig;
import com.dasset.wallet.components.utils.FileProviderUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.SecurityUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

public final class StorageFactory {

    private static StorageFactory storageFactory;
    private File keystoreDirectory;
    private Storage storage;

    private StorageFactory() {
        // cannot be instantiated
    }

    public static synchronized StorageFactory getInstance() {
        if (storageFactory == null) {
            storageFactory = new StorageFactory();
        }
        return storageFactory;
    }

    public static void releaseInstance() {
        if (storageFactory != null) {
            storageFactory = null;
        }
    }

    public synchronized void create(Context context, File keystoreDirectory, File dataBaseDirectory, String dataBaseName) throws StorageException {
        LogUtil.getInstance().print(String.format("Trying to generate OB in %s, the OB name is %s.", dataBaseDirectory, dataBaseName));
        this.keystoreDirectory = keystoreDirectory;
        BoxStore boxStore = MyObjectBox.builder().androidContext(context).baseDirectory(dataBaseDirectory).name(dataBaseName).build();
        storage = new Storage(boxStore);
        if (BuildConfig.DEBUG) {
            new AndroidObjectBrowser(boxStore).start(context);
        }
    }

    public synchronized Account insert(String deviceId, String timestamp1, String cipher, String accountName, String privateKey, String password, String timestamp2, boolean isEncrypt) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, StorageException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
        if (storage != null) {
            return storage.insert(deviceId, timestamp1, cipher, accountName, privateKey, SecurityUtil.getInstance().encryptMD5With16Bit(password), timestamp2, isEncrypt);
        }
        return null;
    }

    public synchronized void delete(String address, String password, boolean isEncrypt) throws NoSuchPaddingException, UnsupportedEncodingException, StorageException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException {
        if (storage != null) {
            storage.delete(address, SecurityUtil.getInstance().encryptMD5With16Bit(password), isEncrypt);
        }
    }

    public synchronized Account update(String address, String accountName, String password, boolean isEncrypt) throws NoSuchPaddingException, UnsupportedEncodingException, StorageException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException {
        if (storage != null) {
            return storage.update(address, accountName, SecurityUtil.getInstance().encryptMD5With16Bit(password), isEncrypt);
        }
        return null;
    }

//    public synchronized List<Account> query() {
//        if (storage != null) {
//            for (Account account : storage.query()) {
//
//            }
//        }
//    }

    public synchronized void importKeyStore(File file, String password, boolean isEncrypt) throws BadPaddingException, NoSuchAlgorithmException, StorageException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, IOException {
        if (storage != null) {
            storage.importKeyStore(file, SecurityUtil.getInstance().encryptMD5With16Bit(password), isEncrypt);
        }
    }

    public synchronized Uri exportKeyStore(Context context, Intent intent, String address, String password, boolean isEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, StorageException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, IOException {
        if (storage != null) {
            return FileProviderUtil.getInstance().generateUri(context, intent, storage.exportKeyStore(keystoreDirectory, address, SecurityUtil.getInstance().encryptMD5With16Bit(password), isEncrypt));
        }
        return null;
    }
}
