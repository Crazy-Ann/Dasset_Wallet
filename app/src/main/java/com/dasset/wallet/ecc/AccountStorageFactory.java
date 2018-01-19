package com.dasset.wallet.ecc;

import android.content.Intent;
import android.net.Uri;

import com.alibaba.fastjson.JSONException;
import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.FileProviderUtil;
import com.dasset.wallet.components.utils.IOUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.SecurityUtil;
import com.dasset.wallet.core.wallet.Account;
import com.dasset.wallet.core.wallet.Constant;
import com.dasset.wallet.core.wallet.KeyStore;
import com.dasset.wallet.core.wallet.PasswordManagerFactory;
import com.dasset.wallet.model.AccountInfo;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public final class AccountStorageFactory {

    private static AccountStorageFactory accountStorageFactory;
    private File keystoreDirectory;
    private File backupsDirectory;
    private KeyStore keyStore;

    private AccountStorageFactory() {
        // cannot be instantiated
    }

    public static synchronized AccountStorageFactory getInstance() {
        if (accountStorageFactory == null) {
            accountStorageFactory = new AccountStorageFactory();
        }
        return accountStorageFactory;
    }

    public static void releaseInstance() {
        if (accountStorageFactory != null) {
            accountStorageFactory = null;
        }
    }

    public File getKeystoreDirectory() {
        return keystoreDirectory;
    }

    public File getBackupsDirectory() {
        return backupsDirectory;
    }

    public void initialize() throws Exception {
        keystoreDirectory = IOUtil.getInstance().forceMkdir(BaseApplication.getInstance().getFilesDir() + Regex.LEFT_SLASH.getRegext() + Constant.FilePath.KEYSTORE);
        backupsDirectory = IOUtil.getInstance().getExternalStorageDirectory(Constant.FilePath.KEYSTORE_CACHE);
//        keyStore = new KeyStore(keystoreDirectory);
        keyStore = new KeyStore(keystoreDirectory, backupsDirectory);
        LogUtil.getInstance().print(String.format("There have %s accounts with KeyStore", Long.toString(getAccountInfos(AccountStorageFactory.getInstance().getKeystoreDirectory()).size())));
    }

    public void deleteAccount(String address) throws IOException {
        if (keyStore != null) {
            keyStore.deleteAccount(address);
        } else {
            LogUtil.getInstance().print(String.format("Is keyStore forget initialized?"));
        }
    }

    public void deleteBackupsAccount(String address) throws IOException {
        if (keyStore != null) {
            keyStore.deleteBackupsAccount(address);
        } else {
            LogUtil.getInstance().print(String.format("Is keyStore forget initialized?"));
        }
    }

    public Account createAccount(String deviceId, String timestamp1, String cipher, String accountName, String privateKey, String password, String timestamp2, boolean isEncrypt) throws Exception {
        if (keyStore != null) {
            LogUtil.getInstance().print(String.format("Trying to generate wallet in %s", keyStore.getKeystoreDirectory()));
            Account account = keyStore.createAccount(deviceId, timestamp1, cipher, accountName, privateKey, SecurityUtil.getInstance().encryptMD5With16Bit(password), timestamp2, isEncrypt);
            PasswordManagerFactory.put(BaseApplication.getInstance(), account.getAddress2(), account.getPassword());
            return account;
        } else {
            LogUtil.getInstance().print(String.format("Is keyStore forget initialized?"));
            return null;
        }
    }

//    public void exportAccountToExternalStorageDirectory(String address, String password) throws PasswordException, IOException {
//        if (keyStore != null && backupsDirectory != null && backupsDirectory.isDirectory() && backupsDirectory.exists()) {
//            keyStore.exportAccount(backupsDirectory, address, password);
//        } else {
//            throw new NullPointerException("Please check whether the keyStore is initialized!");
//        }
//    }

    public Uri exportAccountToThird(Intent intent, String address, String password) throws IOException, IllegalArgumentException {
        return FileProviderUtil.getInstance().generateUri(BaseApplication.getInstance(), intent, keyStore.exportAccount(address, password));
//        return FileProvider.getUriForFile(BaseApplication.getInstance(), com.dasset.wallet.components.constant.Constant.FILE_PROVIDER_AUTHORITY, keyStore.exportAccount(address, password));
    }

    public void importAccount(File file, String password) throws JSONException, IOException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException {
        if (keyStore != null) {
            keyStore.importAccount(file, SecurityUtil.getInstance().encryptMD5With16Bit(password));
//            keyStore.importAccount(file, password);
        } else {
            throw new NullPointerException("Please check whether the keyStore is initialized!");
        }
    }

    public Account renameAccount(String address, String accountName) throws IOException {
        if (keyStore != null) {
            return keyStore.renameAccount(address, accountName);
        } else {
            throw new NullPointerException("Please check whether the keyStore is initialized!");
        }
    }

//    public List<AccountInfo> getAccountInfos(FilePath keystoreDirectory, FilePath backupsDirectory) {
//        if (keyStore != null) {
//            try {
//                List<AccountInfo> accountInfos = Lists.newArrayList();
//                FilePath              directory;
//                if (keystoreDirectory.listFiles().length > backupsDirectory.listFiles().length) {
//                    directory = keystoreDirectory;
//                } else {
//                    directory = backupsDirectory;
//                }
//                for (FilePath file : keyStore.directoryTraversal(directory)) {
//                    Account account = keyStore.generatorAccountFile(file);
//                    if (account != null) {
//                        AccountInfo accountInfo = new AccountInfo();
//                        accountInfo.setAccountName(account.getAccountName());
//                        accountInfo.setAddress2(account.getAddress2());
//                        accountInfo.setPrivateKey(account.getPrivateKey());
//                        accountInfo.setPublicKey(account.getPublicKey());
//                        accountInfo.setPassword(account.getPassword());
//                        accountInfo.setTime2(account.getTime2());
//                        LogUtil.getInstance().print(accountInfo.toString());
//                        accountInfos.add(accountInfo);
//                    }
//                }
//                return accountInfos;
//            } catch (IOException e) {
//                e.printStackTrace();
//                return null;
//            }
//        } else {
//            LogUtil.getInstance().print(String.format("Is keyStore forget initialized?"));
//            return null;
//        }
//    }

    public List<AccountInfo> getAccountInfos(File keystoreDirectory) throws IOException {
        if (keyStore != null) {
            List<AccountInfo> accountInfos = Lists.newArrayList();
            for (File file : keyStore.directoryTraversal(keystoreDirectory)) {
                try {
                    Account account = keyStore.generateAccount(file, Cipher.ENCRYPT_MODE, false, null);
                    if (account != null) {
                        AccountInfo accountInfo = new AccountInfo();
                        accountInfo.setAddress1(account.getAddress1());
                        accountInfo.setDevice(account.getDevice());
                        accountInfo.setTimes1(account.getTime1());
                        accountInfo.setCipher(account.getCipher());
                        accountInfo.setAccountName(account.getAccountName());
                        accountInfo.setPrivateKey(account.getPrivateKey());
                        accountInfo.setAddress2(account.getAddress2());
                        accountInfo.setPassword(account.getPassword());
                        accountInfo.setTimes2(account.getTime2());
                        LogUtil.getInstance().print(accountInfo.toString());
                        accountInfos.add(accountInfo);
                    }
                } catch (IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                    e.printStackTrace();
                    throw new IOException("Failed of get account infos!");
                }
            }
            return accountInfos;
        } else {
            LogUtil.getInstance().print(String.format("Is keyStore forget initialized?"));
            return null;
        }
    }
}
