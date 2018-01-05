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

    private File directory;
    private Accounts accounts;

    public String getDirectory() {
        return directory.getAbsolutePath();
    }

    public Accounts getAccounts() {
        return accounts;
    }

    public KeyStore(File directory) {
        this.accounts = new Accounts();
        this.directory = directory;
    }

    private synchronized void persistence(File file, Account account) throws IOException, PasswordException {
        String keyStoreData = generatorKeyStoreData(account);
        if (!TextUtils.isEmpty(keyStoreData)) {
            BufferedWriter bufferedWriter = null;
            try {
                bufferedWriter = new BufferedWriter(new FileWriter(file));
                bufferedWriter.write(keyStoreData);
                bufferedWriter.flush();
                accounts.add(account);
            } catch (IOException e) {
                e.printStackTrace();
                deleteAccount(account.getAddress(), account.getPassword());
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
        jsonObject.put("serial_number", account.getSerialNumber());
        jsonObject.put("account_name", account.getAccountName());
        jsonObject.put("address", account.getAddress());
        jsonObject.put("private_key", account.getPrivateKey());
        jsonObject.put("public_key", account.getPublicKey());
        jsonObject.put("password", account.getPassword());
        jsonObject.put("time", account.getTimestamp());
        return jsonObject.toString();
    }

    private synchronized File generatorKeyStoreFile(String directoryPath, Account account) throws IOException {
        if (!TextUtils.isEmpty(directoryPath) && account != null) {
            DateFormat dateFormat = new SimpleDateFormat(Regex.UTC_DATE_FORMAT_ALL.getRegext());
            dateFormat.setTimeZone(TimeZone.getTimeZone(Regex.UTC.getRegext()));
            String fileName = Regex.UTC.getRegext() + Regex.DOUBLE_MINUS.getRegext() + dateFormat.format(new Date()) + Regex.DOUBLE_MINUS.getRegext() + account.getAddress();
            File file = new File(directoryPath + Regex.LEFT_SLASH.getRegext() + fileName);
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

    public Account createAccount(String serialNumber, String accountName, String privateKey, String publicKey, String address, String password) throws IOException, PasswordException {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            Account account = new Account(serialNumber, accountName, privateKey, publicKey, address, password);
            File file = generatorKeyStoreFile(directory.getAbsolutePath(), account);
            if (file != null && file.exists()) {
                persistence(file, account);
            } else {
                throw new PasswordException("Please enter the correct password!");
            }
            return account;
        } else {
            throw new IOException("Failure of account delete, there is a error in the directory file!");
        }
    }

    public void deleteAccount(String address, String password) throws IOException, PasswordException {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            LogUtil.getInstance().print(String.format("directory file number is %s", files.length));
            if (files.length != 0) {
                for (File file : files) {
                    if (file.getName().contains(address)) {
                        if (file.getName().contains(address)) {
                            Account account = getAccount(file);
                            if (TextUtils.equals(password, account.getPassword())) {
                                file.delete();
                            } else {
                                throw new PasswordException("Please enter the correct password!");
                            }
                        }
                    }
                }
            } else {
                throw new IOException("Failure of account delete, the directory is empty!");
            }
        } else {
            throw new IOException("Failure of account delete, there is a error in the directory file!");
        }
    }

    public void exportAccount(File directoryPath, String address, String password) throws IOException, PasswordException {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
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

    public void importAccount(String address, String password) throws IOException {

    }

    public synchronized File[] directoryTraversal(File directory) {
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

    public synchronized Account getAccount(File file) {
        if (file != null && file.exists()) {
            try {
                Account account = new Account();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String data;
                while ((data = bufferedReader.readLine()) != null) {
                    JSONObject jsonObject = JSON.parseObject(data);
                    account.setSerialNumber(jsonObject.getString("serial_number"));
                    account.setAccountName(jsonObject.getString("account_name"));
                    account.setAddress(jsonObject.getString("address"));
                    account.setPrivateKey(jsonObject.getString("private_key"));
                    account.setPublicKey(jsonObject.getString("public_key"));
                    account.setPassword(jsonObject.getString("password"));
                    account.setTimestamp(jsonObject.getString("time"));
                    LogUtil.getInstance().print(account.toString());
                }
                return account;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
}
