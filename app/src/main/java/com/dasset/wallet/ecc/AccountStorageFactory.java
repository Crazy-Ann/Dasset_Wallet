package com.dasset.wallet.ecc;

import android.net.Uri;
import android.support.v4.content.FileProvider;

import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.IOUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.core.ecc.Account;
import com.dasset.wallet.core.ecc.Accounts;
import com.dasset.wallet.core.ecc.Constant;
import com.dasset.wallet.core.ecc.KeyStore;
import com.dasset.wallet.core.ecc.PasswordManagerFactory;
import com.dasset.wallet.core.exception.PasswordException;
import com.dasset.wallet.model.AccountInfo;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class AccountStorageFactory {

    private static AccountStorageFactory accountStorageFactory;
    private        File                  keystoreDirectory;
    //    private        File                  backupsDirectory;
    private        KeyStore              keyStore;

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

//    public File getBackupsDirectory() {
//        return backupsDirectory;
//    }

    public void initialize() throws Exception {
        keystoreDirectory = IOUtil.getInstance().forceMkdir(BaseApplication.getInstance().getFilesDir() + Regex.LEFT_SLASH.getRegext() + Constant.Configuration.KEYSTORE);
//        backupsDirectory = IOUtil.getInstance().getExternalStorageDirectory(Constant.Configuration.KEYSTORE);
        keyStore = new KeyStore(keystoreDirectory);
//        keyStore = new KeyStore(keystoreDirectory, backupsDirectory);
        LogUtil.getInstance().print(String.format("There have %s accounts with KeyStore", Long.toString(getAccountInfos(AccountStorageFactory.getInstance().getKeystoreDirectory()).size())));
    }

    public void deleteAccount(String address, String password) throws Exception {
        if (keyStore != null) {
            keyStore.deleteAccount(address, password);
        } else {
            LogUtil.getInstance().print(String.format("Is keyStore forget initialized?"));
        }
    }

    public Account createAccount(String accountName, String privateKey, String publicKey, String address, String password) throws Exception {
        if (keyStore != null) {
            LogUtil.getInstance().print(String.format("Trying to generate wallet in %s", keyStore.getKeystoreDirectory()));
            Account account = keyStore.createAccount(accountName, privateKey, publicKey, address, password);
            PasswordManagerFactory.put(BaseApplication.getInstance(), account.getAddress(), account.getPassword());
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

    public Uri exportAccountToThird(String address, String password) throws PasswordException, IOException {
        return FileProvider.getUriForFile(BaseApplication.getInstance(), com.dasset.wallet.constant.Constant.FILE_PROVIDER_AUTHORITY, keyStore.exportAccount(address, password));
    }

    public void importAccount(File file) throws PasswordException, IOException {
        if (keyStore != null) {
            keyStore.importAccount(file);
        } else {
            throw new NullPointerException("Please check whether the keyStore is initialized!");
        }
    }

    public void renameAccount(String address, String accountName) throws PasswordException, IOException {
        if (keyStore != null) {
            keyStore.renameAccount(address, accountName);
        } else {
            throw new NullPointerException("Please check whether the keyStore is initialized!");
        }
    }

//    public List<AccountInfo> getAccountInfos(File keystoreDirectory, File backupsDirectory) {
//        if (keyStore != null) {
//            try {
//                List<AccountInfo> accountInfos = Lists.newArrayList();
//                File              directory;
//                if (keystoreDirectory.listFiles().length > backupsDirectory.listFiles().length) {
//                    directory = keystoreDirectory;
//                } else {
//                    directory = backupsDirectory;
//                }
//                for (File file : keyStore.directoryTraversal(directory)) {
//                    Account account = keyStore.getAccount(file);
//                    if (account != null) {
//                        AccountInfo accountInfo = new AccountInfo();
//                        accountInfo.setAccountName(account.getAccountName());
//                        accountInfo.setAddress(account.getAddress());
//                        accountInfo.setPrivateKey(account.getPrivateKey());
//                        accountInfo.setPublicKey(account.getPublicKey());
//                        accountInfo.setPassword(account.getPassword());
//                        accountInfo.setTimestamp(account.getTimestamp());
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

    public List<AccountInfo> getAccountInfos(File keystoreDirectory) {
        if (keyStore != null) {
            try {
                List<AccountInfo> accountInfos = Lists.newArrayList();
                for (File file : keyStore.directoryTraversal(keystoreDirectory)) {
                    Account account = keyStore.getAccount(file);
                    if (account != null) {
                        AccountInfo accountInfo = new AccountInfo();
                        accountInfo.setAccountName(account.getAccountName());
                        accountInfo.setAddress(account.getAddress());
                        accountInfo.setPrivateKey(account.getPrivateKey());
                        accountInfo.setPublicKey(account.getPublicKey());
                        accountInfo.setPassword(account.getPassword());
                        accountInfo.setTimestamp(account.getTimestamp());
                        LogUtil.getInstance().print(accountInfo.toString());
                        accountInfos.add(accountInfo);
                    }
                }
                return accountInfos;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            LogUtil.getInstance().print(String.format("Is keyStore forget initialized?"));
            return null;
        }
    }
}
