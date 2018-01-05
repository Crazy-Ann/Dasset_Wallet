package com.dasset.wallet.ecc;

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
    private File keystoreDirectory;
    private File backupFileDirectory;
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

    public File getBackupFileDirectory() {
        return backupFileDirectory;
    }

    public void initialize() throws Exception {
        keystoreDirectory = IOUtil.getInstance().forceMkdir(BaseApplication.getInstance().getFilesDir() + Regex.LEFT_SLASH.getRegext() + Constant.Configuration.KEYSTORE);
        backupFileDirectory = IOUtil.getInstance().getExternalStorageDirectory(Constant.Configuration.KEYSTORE);
        keyStore = new KeyStore(keystoreDirectory);
        LogUtil.getInstance().print(String.format("There have %s accounts with KeyStore", Long.toString(getAccountInfos().size())));
    }

    public void deleteAccount(String address, String password) throws Exception {
        if (keyStore != null) {
            keyStore.deleteAccount(address, password);
        } else {
            LogUtil.getInstance().print(String.format("Is keyStore forget initialized?"));
        }
    }

    public Account createAccount(String serialNumber, String accountName, String privateKey, String publicKey, String address, String password) throws Exception {
        if (keyStore != null) {
            LogUtil.getInstance().print(String.format("Trying to generate wallet in %s", keyStore.getDirectory()));
            Account account = keyStore.createAccount(serialNumber, accountName, privateKey, publicKey, address, password);
            PasswordManagerFactory.put(BaseApplication.getInstance(), account.getAddress(), account.getPassword());
            return account;
        } else {
            LogUtil.getInstance().print(String.format("Is keyStore forget initialized?"));
            return null;
        }
    }

    public void exportAccount(String address, String password) throws PasswordException, IOException {
        if (keyStore != null && backupFileDirectory != null && backupFileDirectory.isDirectory() && backupFileDirectory.exists()) {
            keyStore.exportAccount(backupFileDirectory, address, password);
        } else {
            throw new NullPointerException("Please check whether the keyStore is initialized!");
        }
    }

    public void importAccount(String address, String password) throws PasswordException, IOException {
        if (keyStore != null) {
            keyStore.importAccount(address, password);
        } else {
            throw new NullPointerException("Please check whether the keyStore is initialized!");
        }
    }

    public Account getAccount(String address) throws Exception {
        if (keyStore != null) {
            Accounts accounts = keyStore.getAccounts();
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.get(i).getAddress().equals(address)) {
                    return accounts.get(i);
                }
            }
        } else {
            LogUtil.getInstance().print(String.format("Is keyStore forget initialized?"));
        }
        return null;
    }

    public List<AccountInfo> getAccountInfos() {
        if (keyStore != null) {
            List<AccountInfo> accountInfos = Lists.newArrayList();
            for (File file : keyStore.directoryTraversal(AccountStorageFactory.getInstance().getKeystoreDirectory())) {
                Account account = keyStore.getAccount(file);
                if (account != null) {
                    AccountInfo accountInfo = new AccountInfo();
                    accountInfo.setSerialNumber(account.getSerialNumber());
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
        } else {
            LogUtil.getInstance().print(String.format("Is keyStore forget initialized?"));
            return null;
        }
    }
}
