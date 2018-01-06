package com.dasset.wallet.core.ecc;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dasset.wallet.components.BuildConfig;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.IOUtil;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.core.exception.PasswordException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class KeyStore {

    private File keystoreDirectory;
    //    private File     backupsDirectory;

    public File getKeystoreDirectory() {
        return keystoreDirectory;
    }

//    public File getBackupsDirectory() {
//        return backupsDirectory;
//    }

    public KeyStore(File keystoreDirectory) {
        this.keystoreDirectory = keystoreDirectory;
    }

//    public KeyStore(File keystoreDirectory, File backupsDirectory) {
//        this.accounts = new Accounts();
//        this.keystoreDirectory = keystoreDirectory;
//        this.backupsDirectory = backupsDirectory;
//    }

    private synchronized void persistence(File file, Account account) throws IOException {
        String keyStoreData = generatorKeyStoreData(account);
        if (!TextUtils.isEmpty(keyStoreData)) {
            BufferedWriter bufferedWriter = null;
            try {
                bufferedWriter = new BufferedWriter(new FileWriter(file));
                bufferedWriter.write(keyStoreData);
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
                deleteAccount(account.getAddress());
            } finally {
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            LogUtil.getInstance().print("The keystore data is empty!");
        }
    }

    private synchronized String generatorKeyStoreData(Account account) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account_name", account.getAccountName());
        jsonObject.put("address", account.getAddress());
        jsonObject.put("private_key", account.getPrivateKey());
        jsonObject.put("public_key", account.getPublicKey());
        jsonObject.put("password", account.getPassword());
        jsonObject.put("time_stamp", account.getTimestamp());
        return jsonObject.toString();
    }

    private synchronized File generatorKeyStoreFile(String directoryPath, Account account) throws IOException {
        if (!TextUtils.isEmpty(directoryPath) && account != null) {
            DateFormat dateFormat = new SimpleDateFormat(Regex.UTC_DATE_FORMAT_ALL.getRegext());
            dateFormat.setTimeZone(TimeZone.getTimeZone(Regex.UTC.getRegext()));
            String fileName = Regex.UTC.getRegext() + Regex.DOUBLE_MINUS.getRegext() + dateFormat.format(new Date()) + Regex.DOUBLE_MINUS.getRegext() + account.getAddress();
            File   file     = new File(directoryPath + Regex.LEFT_SLASH.getRegext() + fileName);
            if (file.createNewFile()) {
                if (BuildConfig.DEBUG) {
                    directoryTraversal(file.getParentFile());
                }
                return file;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public Account createAccount(String accountName, String privateKey, String publicKey, String address, String password) throws IOException {
        if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
            Account account = new Account(accountName, privateKey, publicKey, address, password);
            File    file    = generatorKeyStoreFile(keystoreDirectory.getAbsolutePath(), account);
            if (file != null && file.exists()) {
                persistence(file, account);
            } else {
                throw new IOException("Failure of account create, there is a error in the directory!");
            }
            return account;
        } else {
            throw new IOException("Failure of account create, there is a error in the keystore directory!");
        }
    }

    public Account createAccount(File keystoreDirectoryFile, String accountName, String privateKey, String publicKey, String address, String password) throws IOException, PasswordException {
        Account account = new Account(accountName, privateKey, publicKey, address, password);
        if (keystoreDirectoryFile != null && keystoreDirectoryFile.exists()) {
            persistence(keystoreDirectoryFile, account);
        } else {
            throw new IOException("Failure of account create, there is a error in the keystore directory!");
        }
        return account;
    }

//    public Account createBackupsAccount(File backupsDirectoryFile, String accountName, String privateKey, String publicKey, String address, String password) throws IOException, PasswordException {
//        Account account = new Account(accountName, privateKey, publicKey, address, password);
//        if (backupsDirectoryFile != null && backupsDirectoryFile.exists()) {
//            persistence(backupsDirectoryFile, account);
//        } else {
//            throw new IOException("Failure of account create, there is a error in the directory!");
//        }
//        return account;
//    }

//    public void deleteAccount(File keystoreDirectoryFile) throws IOException, PasswordException {
//        Account keystoreDirectoryAccount = getAccount(keystoreDirectoryFile);
//        if (keystoreDirectoryAccount != null) {
//            if (keystoreDirectoryFile.delete()) {
//                LogUtil.getInstance().print(String.format("Keystore directory account %s has been deleted!", keystoreDirectoryAccount.getAddress()));
//            } else {
//                throw new IOException("Failure of keystore directory account delete!");
//            }
//        } else {
//            throw new IOException("Failure of account delete, the keystore directory account is null!");
//        }
//    }

    public void deleteAccount(String address, String password) throws IOException, PasswordException {
        if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
            File[] keystoreDirectoryFiles = keystoreDirectory.listFiles();
            LogUtil.getInstance().print(String.format("keystore directory file number is %s", keystoreDirectoryFiles.length));
            if (keystoreDirectoryFiles.length != 0) {
                for (File keystoreDirectoryFile : keystoreDirectoryFiles) {
                    if (keystoreDirectoryFile.getName().contains(address)) {
                        if (keystoreDirectoryFile.getName().contains(address)) {
                            Account keystoreDirectoryAccount = getAccount(keystoreDirectoryFile);
                            if (keystoreDirectoryAccount != null) {
                                if (TextUtils.equals(password, keystoreDirectoryAccount.getPassword())) {
                                    if (keystoreDirectoryFile.delete()) {
                                        LogUtil.getInstance().print(String.format("Keystore directory account %s has been deleted!", keystoreDirectoryAccount.getAddress()));
                                    } else {
                                        throw new IOException("Failure of keystore directory account delete!");
                                    }
                                } else {
                                    throw new PasswordException("Please enter the correct password of keystore directory account!");
                                }
                            } else {
                                throw new IOException("Failure of account delete, the keystore directory account is null!");
                            }
                        }
                    }
                }
            } else {
                throw new IOException("Failure of account delete, the keystore directory is empty!");
            }
        } else {
            throw new IOException("Failure of account delete, there is a error in the keystore directory file!");
        }
    }

//    public void deleteAccount(String address, String password) throws IOException, PasswordException {
//        if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
//            File[] keystoreDirectoryFiles = keystoreDirectory.listFiles();
//            LogUtil.getInstance().print(String.format("keystore directory file number is %s", keystoreDirectoryFiles.length));
//            if (keystoreDirectoryFiles.length != 0) {
//                for (File keystoreDirectoryFile : keystoreDirectoryFiles) {
//                    if (keystoreDirectoryFile.getName().contains(address)) {
//                        if (keystoreDirectoryFile.getName().contains(address)) {
//                            Account keystoreDirectoryAccount = getAccount(keystoreDirectoryFile);
//                            if (keystoreDirectoryAccount != null) {
//                                if (TextUtils.equals(password, keystoreDirectoryAccount.getPassword())) {
//                                    if (keystoreDirectoryFile.delete()) {
//                                        LogUtil.getInstance().print(String.format("Keystore directory account %s has been deleted!", keystoreDirectoryAccount.getAddress()));
//                                        deleteBackupsAccount(address, password);
//                                    } else {
//                                        throw new IOException("Failure of keystore directory account delete!");
//                                    }
//                                } else {
//                                    throw new PasswordException("Please enter the correct password of keystore directory account!");
//                                }
//                            } else {
//                                throw new IOException("Failure of account delete, the keystore directory account is null!");
//                            }
//                        }
//                    }
//                }
//            } else {
//                throw new IOException("Failure of account delete, the keystore directory is empty!");
//            }
//        } else {
//            throw new IOException("Failure of account delete, there is a error in the keystore directory file!");
//        }
//    }

    public void deleteAccount(String address) throws IOException {
        if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
            File[] keystoreDirectoryFiles = keystoreDirectory.listFiles();
            LogUtil.getInstance().print(String.format("keystore directory file number is %s", keystoreDirectoryFiles.length));
            if (keystoreDirectoryFiles.length != 0) {
                for (File keystoreDirectoryFile : keystoreDirectoryFiles) {
                    if (keystoreDirectoryFile.getName().contains(address)) {
                        if (keystoreDirectoryFile.getName().contains(address)) {
                            Account keystoreDirectoryAccount = getAccount(keystoreDirectoryFile);
                            if (keystoreDirectoryAccount != null) {
                                if (keystoreDirectoryFile.delete()) {
                                    LogUtil.getInstance().print(String.format("Keystore directory account %s has been deleted!", keystoreDirectoryAccount.getAddress()));
                                } else {
                                    throw new IOException("Failure of keystore directory account delete!");
                                }
                            } else {
                                throw new IOException("Failure of account delete, the keystore directory account is null!");
                            }
                        }
                    }
                }
            } else {
                throw new IOException("Failure of account delete, the keystore directory is empty!");
            }
        } else {
            throw new IOException("Failure of account delete, there is a error in the keystore directory file!");
        }
    }

//    private void deleteBackupsAccount(File backupsDirectoryFile) throws IOException, PasswordException {
//        Account backupsDirectoryAccount = getAccount(backupsDirectoryFile);
//        if (backupsDirectoryAccount != null) {
//            if (backupsDirectoryFile.delete()) {
//                LogUtil.getInstance().print(String.format("Backups directory account %s has been deleted!", backupsDirectoryAccount.getAddress()));
//            } else {
//                throw new IOException("Failure of backups directory account delete!");
//            }
//        } else {
//            throw new IOException("Failure of account delete, the backups directory account is null!");
//        }
//    }

//    private void deleteBackupsAccount(String address, String password) throws IOException, PasswordException {
//        if (backupsDirectory != null && backupsDirectory.exists() && backupsDirectory.isDirectory()) {
//            File[] backupsDirectoryFiles = backupsDirectory.listFiles();
//            LogUtil.getInstance().print(String.format("Backups directory file number is %s", backupsDirectoryFiles.length));
//            if (backupsDirectoryFiles.length != 0) {
//                for (File backupsDirectoryFile : backupsDirectoryFiles) {
//                    if (backupsDirectoryFile.getName().contains(address)) {
//                        if (backupsDirectoryFile.getName().contains(address)) {
//                            Account backupsDirectoryAccount = getAccount(backupsDirectoryFile);
//                            if (backupsDirectoryAccount != null) {
//                                if (TextUtils.equals(password, backupsDirectoryAccount.getPassword())) {
//                                    if (backupsDirectoryFile.delete()) {
//                                        LogUtil.getInstance().print(String.format("Backups directory account %s has been deleted!", backupsDirectoryAccount.getAddress()));
//                                    } else {
//                                        throw new IOException("Failure of backups directory account delete!");
//                                    }
//                                } else {
//                                    throw new PasswordException("Please enter the correct password of backups directory account!");
//                                }
//                            } else {
//                                throw new IOException("Failure of account delete, the backups directory account is null!");
//                            }
//                        }
//                    }
//                }
//            } else {
//                throw new IOException("Failure of account delete, the backups directory is empty!");
//            }
//        } else {
//            throw new IOException("Failure of account delete, there is a error in the backups directory file!");
//        }
//    }

    public void exportAccount(File directoryPath, String address, String password) throws IOException, PasswordException {
        if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
            File[] files = keystoreDirectory.listFiles();
            LogUtil.getInstance().print(String.format("directory file number is %s", files.length));
            if (files.length != 0) {
                for (File file : files) {
                    if (file.getName().contains(address)) {
                        Account account = getAccount(file);
                        if (TextUtils.equals(password, account.getPassword())) {
                            IOUtil.getInstance().copyFile(file.getAbsolutePath(), new File(directoryPath, file.getName()).getAbsolutePath());
                        } else {
                            throw new PasswordException("Please enter the correct password!");
                        }
                    }
                }
            } else {
                throw new IOException("Failure of account export, the directory is empty!");
            }
        } else {
            throw new IOException("Failure of account export, there is a error in the directory file!");
        }
    }

    public File exportAccount(String address, String password) throws IOException, PasswordException {
        if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
            File[] files = keystoreDirectory.listFiles();
            LogUtil.getInstance().print(String.format("directory file number is %s", files.length));
            if (files.length != 0) {
                for (File file : files) {
                    if (file.getName().contains(address)) {
                        Account account = getAccount(file);
                        if (TextUtils.equals(password, account.getPassword())) {
                            return file;
                        } else {
                            throw new PasswordException("Please enter the correct password!");
                        }
                    }
                }
            } else {
                throw new IOException("Failure of account export, the directory is empty!");
            }
        } else {
            throw new IOException("Failure of account export, there is a error in the directory file!");
        }
        return null;
    }

    public void importAccount(File file) throws IOException, PasswordException {
        if (file != null && file.exists()) {
            if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
                File[] keystoreFiles = keystoreDirectory.listFiles();
                LogUtil.getInstance().print(String.format("Keystore directory file number is %s", keystoreFiles.length));
                if (keystoreFiles.length != 0) {
                    for (File keystoreFile : keystoreFiles) {
                        Account account = getAccount(keystoreFile);
                        if (!file.getName().contains(account.getAddress())) {
                            IOUtil.getInstance().copyFile(file.getAbsolutePath(), new File(keystoreDirectory.getAbsoluteFile(), file.getName()).getAbsolutePath());
                        } else {
                            throw new IOException("Failure of account import, there is a same address account in the keystore directory file!");
                        }
                    }
                } else {
                    Account account = getAccount(file);
                    createAccount(account.getAccountName(), account.getPrivateKey(), account.getPublicKey(), account.getAddress(), account.getPassword());
                }
            } else {
                throw new IOException("Failure of account import, there is a error in the keystore directory file!");
            }
        } else {
            throw new IOException("Failure of account import, there is a error in the keystore directory file!");
        }
    }

    public void renameAccount(String address, String accountName) throws IOException, PasswordException {
        if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
            File[] keystoreDirectoryFiles = keystoreDirectory.listFiles();
            LogUtil.getInstance().print(String.format("Beystore directory file number is %s", keystoreDirectoryFiles.length));
            if (keystoreDirectoryFiles.length != 0) {
                for (File keystoreDirectoryFile : keystoreDirectoryFiles) {
                    if (keystoreDirectoryFile.getName().contains(address)) {
                        if (keystoreDirectoryFile.getName().contains(address)) {
                            Account keystoreDirectoryAccount = getAccount(keystoreDirectoryFile);
                            if (keystoreDirectoryAccount != null) {
                                createAccount(keystoreDirectoryFile, accountName, keystoreDirectoryAccount.getPrivateKey(), keystoreDirectoryAccount.getPublicKey(), keystoreDirectoryAccount.getAddress(), keystoreDirectoryAccount.getPassword());
                            } else {
                                throw new IOException("Failure of account delete, the keystore directory account is null!");
                            }
                        }
                    }
                }
            } else {
                throw new IOException("Failure of account delete, the keystore directory is empty!");
            }
        } else {
            throw new IOException("Failure of account delete, there is a error in the keystore directory file!");
        }
    }

//    public void renameAccount(String address, String accountName) throws IOException, PasswordException {
//        if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
//            File[] keystoreDirectoryFiles = keystoreDirectory.listFiles();
//            LogUtil.getInstance().print(String.format("Beystore directory file number is %s", keystoreDirectoryFiles.length));
//            if (keystoreDirectoryFiles.length != 0) {
//                for (File keystoreDirectoryFile : keystoreDirectoryFiles) {
//                    if (keystoreDirectoryFile.getName().contains(address)) {
//                        if (keystoreDirectoryFile.getName().contains(address)) {
//                            Account keystoreDirectoryAccount = getAccount(keystoreDirectoryFile);
//                            if (keystoreDirectoryAccount != null) {
//                                createAccount(keystoreDirectoryFile, accountName, keystoreDirectoryAccount.getPrivateKey(), keystoreDirectoryAccount.getPublicKey(), keystoreDirectoryAccount.getAddress(), keystoreDirectoryAccount.getPassword());
//                                renameBackupsAccount(address, accountName);
//                            } else {
//                                throw new IOException("Failure of account delete, the keystore directory account is null!");
//                            }
//                        }
//                    }
//                }
//            } else {
//                throw new IOException("Failure of account delete, the keystore directory is empty!");
//            }
//        } else {
//            throw new IOException("Failure of account delete, there is a error in the keystore directory file!");
//        }
//    }

//    public void renameBackupsAccount(String address, String accountName) throws IOException, PasswordException {
//        if (backupsDirectory != null && backupsDirectory.exists() && backupsDirectory.isDirectory()) {
//            File[] backupsDirectoryFiles = backupsDirectory.listFiles();
//            LogUtil.getInstance().print(String.format("Backups directory file number is %s", backupsDirectoryFiles.length));
//            if (backupsDirectoryFiles.length != 0) {
//                for (File backupsDirectoryFile : backupsDirectoryFiles) {
//                    if (backupsDirectoryFile.getName().contains(address)) {
//                        if (backupsDirectoryFile.getName().contains(address)) {
//                            Account keystoreDirectoryAccount = getAccount(backupsDirectoryFile);
//                            if (keystoreDirectoryAccount != null) {
//                                createAccount(backupsDirectoryFile, accountName, keystoreDirectoryAccount.getPrivateKey(), keystoreDirectoryAccount.getPublicKey(), keystoreDirectoryAccount.getAddress(), keystoreDirectoryAccount.getPassword());
//                            } else {
//                                throw new IOException("Failure of account delete, the backups directory account is null!");
//                            }
//                        }
//                    }
//                }
//            } else {
//                throw new IOException("Failure of account delete, the backups directory is empty!");
//            }
//        } else {
//            throw new IOException("Failure of account delete, there is a error in the backups directory file!");
//        }
//    }

    public synchronized File[] directoryTraversal(File directory) {
        LogUtil.getInstance().print(String.format("The directory is %s", directory.getAbsolutePath()));
        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles();
            LogUtil.getInstance().print(String.format("The files numbers of %s is %s", directory.getAbsolutePath(), files.length));
            if (files.length == 0) {
                return new File[]{};
            } else {
                for (File file : files) {
                    if (file.isDirectory()) {
                        directoryTraversal(file);
                    } else {
                        LogUtil.getInstance().print(String.format("The file name of %s is %s", directory.getAbsolutePath(), file.getName()));
                        return files;
                    }
                }
            }
        } else {
            LogUtil.getInstance().print(String.format("The %s is not exsist", directory.getAbsolutePath()));
        }
        return null;
    }

    public synchronized Account getAccount(File file) throws IOException {
        if (file != null && file.exists()) {
            Account        account        = new Account();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String         data;
            while ((data = bufferedReader.readLine()) != null) {
                JSONObject jsonObject = JSON.parseObject(data);
                account.setAccountName(jsonObject.getString("account_name"));
                account.setAddress(jsonObject.getString("address"));
                account.setPrivateKey(jsonObject.getString("private_key"));
                account.setPublicKey(jsonObject.getString("public_key"));
                account.setPassword(jsonObject.getString("password"));
                account.setTimestamp(jsonObject.getString("time_stamp"));
                LogUtil.getInstance().print(account.toString());
            }
            return account;
        } else {
            return null;
        }
    }

}
