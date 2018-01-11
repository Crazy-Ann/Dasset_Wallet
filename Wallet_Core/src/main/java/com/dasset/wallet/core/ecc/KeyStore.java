package com.dasset.wallet.core.ecc;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.dasset.wallet.components.BuildConfig;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.SecurityUtil;

import org.spongycastle.util.encoders.Hex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public final class KeyStore {

    private File keystoreDirectory;
    private File backupsDirectory;

    public File getKeystoreDirectory() {
        return keystoreDirectory;
    }

//    public FilePath getBackupsDirectory() {
//        return backupsDirectory;
//    }

//    public KeyStore(FilePath keystoreDirectory) {
//        this.keystoreDirectory = keystoreDirectory;
//    }

    public KeyStore(File keystoreDirectory, File backupsDirectory) {
        this.keystoreDirectory = keystoreDirectory;
        this.backupsDirectory = backupsDirectory;
    }

    private synchronized void persistence(File file, Account account, boolean isEncrypt) throws IOException {
        if (account != null) {
            try {
                String keyStoreData = generateKeyStoreData(account, isEncrypt);
                LogUtil.getInstance().print("keyStoreData:%s" + keyStoreData);
                if (!TextUtils.isEmpty(keyStoreData)) {
                    BufferedWriter bufferedWriter = null;
                    try {
                        bufferedWriter = new BufferedWriter(new FileWriter(file));
                        bufferedWriter.write(keyStoreData);
                        bufferedWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        deleteAccount(account.getAddress2());
                        throw new IOException("Failed to persistence keyStore data!");
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
            } catch (NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException e) {
                e.printStackTrace();
                deleteAccount(account.getAddress2());
                throw new IOException("Failed to generator keystore data!");
            }
        } else {
            LogUtil.getInstance().print("The account is null!");
        }
    }

    public synchronized Account generateAccount(File file, int mode, boolean isEncrypt, String password) throws IOException, JSONException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        if (file != null && file.exists()) {
            LogUtil.getInstance().print(String.format("file:%s", file));
            try {
                Account account = new Account();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String data;
                while ((data = bufferedReader.readLine()) != null) {
                    JSONObject jsonObject1 = JSON.parseObject(data);
                    account.setAddress1(jsonObject1.getString("address"));
                    account.setDevice(jsonObject1.getString("device"));
                    account.setTime1(jsonObject1.getString("time"));
                    JSONObject jsonObject2 = jsonObject1.getJSONObject("crypto");
                    account.setCipher(jsonObject2.getString("cipher"));
                    switch (mode) {
                        case Cipher.ENCRYPT_MODE: {
                            JSONObject jsonObject3 = jsonObject2.getJSONObject("ciphertext");
                            if (!isEncrypt) {
                                account.setAccountName(jsonObject3.getString("account_name"));
                                account.setPrivateKey(jsonObject3.getString("private_key"));
                                account.setAddress2(jsonObject3.getString("address"));
                                account.setPassword(jsonObject3.getString("password"));
                            } else {
                                if (!TextUtils.isEmpty(password)) {
                                    byte[] bytes = SecurityUtil.getInstance().encryptAESEBC(jsonObject3.toJSONString(), password);
                                    if (bytes != null && bytes.length > 0) {
                                        account.setCipherText(Hex.toHexString(bytes));
                                    } else {
                                        throw new IOException("Failure of account generate, the password has error!");
                                    }
                                } else {
                                    throw new IOException("Failure of account generate, the password is null!");
                                }
                            }
                            LogUtil.getInstance().print(String.format("encrypt mode data:%s", data));
                            break;
                        }
                        case Cipher.DECRYPT_MODE: {
                            if (!TextUtils.isEmpty(password)) {
                                byte[] bytes = SecurityUtil.getInstance().decryptAESEBC(Hex.decode(jsonObject2.getString("ciphertext")), password);
                                if (bytes != null && bytes.length > 0) {
                                    account.setCipherText(new String(bytes, Regex.UTF_8.getRegext()));
                                } else {
                                    throw new IOException("Failure of account generate, the password has error!");
                                }
                            } else {
                                throw new IOException("Failure of account generate, the password is null!");
                            }
                            LogUtil.getInstance().print(String.format("decrypt mode data:%s", data));
                            break;
                        }
                        default: {
                            JSONObject jsonObject3 = jsonObject2.getJSONObject("ciphertext");
                            account.setAccountName(jsonObject3.getString("account_name"));
                            account.setPrivateKey(jsonObject3.getString("private_key"));
                            account.setAddress2(jsonObject3.getString("address"));
                            account.setPassword(jsonObject3.getString("password"));
                            LogUtil.getInstance().print(String.format("default mode data:%s", data));
                            break;
                        }
                    }
                }
                return account;
            } catch (JSONException e) {
                file.delete();
                e.printStackTrace();
                throw new IOException("Failure of account generate, the file format is unavailable!");
            }
        } else {
            return null;
        }

    }

    private synchronized String generateKeyStoreData(Account account, boolean isEncrypt) throws NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException {
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("address", account.getAddress1());
        jsonObject1.put("time", account.getTime1());
        jsonObject1.put("device", account.getDevice());
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("cipher", account.getCipher());
        if (!isEncrypt) {
            JSONObject jsonObject3 = new JSONObject();
            jsonObject3.put("account_name", account.getAccountName());
            jsonObject3.put("private_key", account.getPrivateKey());
            jsonObject3.put("address", account.getAddress2());
            jsonObject3.put("password", account.getPassword());
            jsonObject2.put("ciphertext", jsonObject3);
        } else {
            jsonObject2.put("ciphertext", account.getCipherText());
        }
        jsonObject1.put("crypto", jsonObject2);
        return jsonObject1.toJSONString();
    }

    private synchronized File generateAccountFile(String directoryPath, Account account, boolean isBackups) throws IOException {
        if (!TextUtils.isEmpty(directoryPath) && account != null) {
            File file;
            if (isBackups) {
                file = new File(directoryPath + Regex.LEFT_SLASH.getRegext() + account.getTime1() + Regex.DOUBLE_MINUS.getRegext() + account.getAddress1());
            } else {
                DateFormat dateFormat = new SimpleDateFormat(Regex.UTC_DATE_FORMAT_ALL.getRegext());
                dateFormat.setTimeZone(TimeZone.getTimeZone(Regex.UTC.getRegext()));
                String fileName = Regex.UTC.getRegext() + Regex.DOUBLE_MINUS.getRegext() + dateFormat.format(new Date()) + Regex.DOUBLE_MINUS.getRegext() + account.getAddress2();
                file = new File(directoryPath + Regex.LEFT_SLASH.getRegext() + fileName);
            }
            if (file.createNewFile()) {
                if (isBackups) {
                    if (file.exists()) {
                        persistence(file, account, isBackups);
                    } else {
                        throw new IOException("Failure of account create, there is a error in the directory!");
                    }
                }
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

//    public synchronized Account editAccount(String address, String password, String cipher, String cipherText, String deviceId) throws IOException {
//        if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
//            Account account = new Account(address, password, cipher, cipherText, deviceId);
//            FilePath file = generateAccountFile(keystoreDirectory.getAbsolutePath(), account);
//            if (file != null && file.exists()) {
//                persistence(file, account, false);
//            } else {
//                throw new IOException("Failure of account create, there is a error in the directory!");
//            }
//            return account;
//        } else {
//            throw new IOException("Failure of account create, there is a error in the keystore directory!");
//        }
//    }

    public synchronized Account createAccount(String deviceId, String timestamp1, String cipher, String accountName, String privateKey, String password, String timestamp2, boolean isEncrypt) throws IOException {
        LogUtil.getInstance().print(String.format("The password of create account is:%s", password));
        if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
            try {
                Account account = new Account(AddressFactory.generatorAddress(ECKeyPairFactory.generatePublicKey(new BigInteger(privateKey, 16), false), com.dasset.wallet.core.ecc.Constant.AddressType.HYC)
                        , deviceId
                        , timestamp1
                        , cipher
                        , accountName
                        , privateKey
                        , AddressFactory.generatorAddress(ECKeyPairFactory.generatePublicKey(new BigInteger(privateKey, 16), false), com.dasset.wallet.core.ecc.Constant.AddressType.HYC)
                        , password
                        , timestamp2);
                File file = generateAccountFile(keystoreDirectory.getAbsolutePath(), account, false);
                if (file != null && file.exists()) {
                    persistence(file, account, isEncrypt);
                } else {
                    throw new IOException("Failure of account create, there is a error in the directory!");
                }
                return account;
            } catch (NoSuchProviderException | InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            throw new IOException("Failure of account create, there is a error in the keystore directory!");
        }
    }

    public synchronized Account editAccount(File file, String deviceId, String timestamp1, String cipher, String accountName, String privateKey, String password, String timestamp2, boolean isEncrypt) throws IOException {
        try {
            Account account = new Account(AddressFactory.generatorAddress(ECKeyPairFactory.generatePublicKey(new BigInteger(privateKey, 16), false), com.dasset.wallet.core.ecc.Constant.AddressType.HYC)
                    , deviceId
                    , timestamp1
                    , cipher
                    , accountName
                    , privateKey
                    , AddressFactory.generatorAddress(ECKeyPairFactory.generatePublicKey(new BigInteger(privateKey, 16), false), com.dasset.wallet.core.ecc.Constant.AddressType.HYC)
                    , password
                    , timestamp2);
            if (file != null && file.exists()) {
                persistence(file, account, isEncrypt);
            } else {
                throw new IOException("Failure of account edit, there is a error in the keystore directory!");
            }
            return account;
        } catch (NoSuchProviderException | InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

//    public synchronized Account createBackupsAccount(FilePath backupsDirectoryFile, String accountName, String privateKey, String publicKey, String address, String password) throws IOException {
//        Account account = new Account(accountName, privateKey, publicKey, address, password);
//        if (backupsDirectoryFile != null && backupsDirectoryFile.exists()) {
//            persistence(backupsDirectoryFile, account, false);
//        } else {
//            throw new IOException("Failure of account create, there is a error in the directory!");
//        }
//        return account;
//    }

//    public synchronized void deleteAccount(FilePath keystoreDirectoryFile) throws IOException, PasswordException {
//        Account keystoreDirectoryAccount = generateAccount(keystoreDirectoryFile);
//        if (keystoreDirectoryAccount != null) {
//            if (keystoreDirectoryFile.delete()) {
//                LogUtil.getInstance().print(String.format("Keystore directory account %s has been deleted!", keystoreDirectoryAccount.getAddress2()));
//            } else {
//                throw new IOException("Failure of keystore directory account delete!");
//            }
//        } else {
//            throw new IOException("Failure of account delete, the keystore directory account is null!");
//        }
//    }

    public synchronized void deleteAccount(String address) throws IOException {
        if (!TextUtils.isEmpty(address)) {
            if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
                File[] keystoreDirectoryFiles = keystoreDirectory.listFiles();
                LogUtil.getInstance().print(String.format("keystore directory file number is %s", keystoreDirectoryFiles.length));
                if (keystoreDirectoryFiles.length != 0) {
                    for (File keystoreDirectoryFile : keystoreDirectoryFiles) {
                        if (keystoreDirectoryFile.getName().contains(address)) {
                            if (keystoreDirectoryFile.getName().contains(address)) {
                                if (keystoreDirectoryFile.delete()) {
                                    LogUtil.getInstance().print(String.format("Keystore directory account %s has been deleted!", address));
                                } else {
                                    throw new IOException("Failure of keystore directory account delete!");
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
        } else {
            throw new IOException("Failure of account delete, the account address is null!");
        }
    }

//    public synchronized void deleteAccount(String address, String password) throws IOException, PasswordException {
//        if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
//            FilePath[] keystoreDirectoryFiles = keystoreDirectory.listFiles();
//            LogUtil.getInstance().print(String.format("keystore directory file number is %s", keystoreDirectoryFiles.length));
//            if (keystoreDirectoryFiles.length != 0) {
//                for (FilePath keystoreDirectoryFile : keystoreDirectoryFiles) {
//                    if (keystoreDirectoryFile.getName().contains(address)) {
//                        if (keystoreDirectoryFile.getName().contains(address)) {
//                            Account keystoreDirectoryAccount = generateAccount(keystoreDirectoryFile);
//                            if (keystoreDirectoryAccount != null) {
//                                if (TextUtils.equals(password, keystoreDirectoryAccount.getPassword())) {
//                                    if (keystoreDirectoryFile.delete()) {
//                                        LogUtil.getInstance().print(String.format("Keystore directory account %s has been deleted!", keystoreDirectoryAccount.getAddress2()));
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

//    private synchronized void deleteBackupsAccount(FilePath backupsDirectoryFile) throws IOException, PasswordException {
//        Account backupsDirectoryAccount = generateAccount(backupsDirectoryFile);
//        if (backupsDirectoryAccount != null) {
//            if (backupsDirectoryFile.delete()) {
//                LogUtil.getInstance().print(String.format("Backups directory account %s has been deleted!", backupsDirectoryAccount.getAddress2()));
//            } else {
//                throw new IOException("Failure of backups directory account delete!");
//            }
//        } else {
//            throw new IOException("Failure of account delete, the backups directory account is null!");
//        }
//    }

    public synchronized void deleteBackupsAccount(String address) throws IOException {
        if (!TextUtils.isEmpty(address)) {
            if (backupsDirectory != null && backupsDirectory.exists() && backupsDirectory.isDirectory()) {
                File[] backupsDirectoryFiles = backupsDirectory.listFiles();
                LogUtil.getInstance().print(String.format("Backups directory file number is %s", backupsDirectoryFiles.length));
                if (backupsDirectoryFiles.length != 0) {
                    for (File backupsDirectoryFile : backupsDirectoryFiles) {
                        if (backupsDirectoryFile.getName().contains(address)) {
                            if (backupsDirectoryFile.getName().contains(address)) {
                                if (backupsDirectoryFile.delete()) {
                                    LogUtil.getInstance().print(String.format("Backups directory account %s has been deleted!", address));
                                } else {
                                    throw new IOException("Failure of backups directory account delete!");
                                }
                            }
                        }
                    }
                } else {
                    throw new IOException("Failure of account delete, the backups directory is empty!");
                }
            } else {
                throw new IOException("Failure of account delete, there is a error in the backups directory file!");
            }
        } else {
            throw new IOException("Failure of account export, the account address is null!");
        }
    }

//    public synchronized void exportAccount(FilePath directoryPath, String address, String password) throws IOException, PasswordException {
//        if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
//            FilePath[] files = keystoreDirectory.listFiles();
//            LogUtil.getInstance().print(String.format("directory file number is %s", files.length));
//            if (files.length != 0) {
//                for (FilePath file : files) {
//                    if (file.getName().contains(address)) {
//                        Account account = generateAccountFile(file);
//                        if (TextUtils.equals(password, account.getPassword())) {
//                            IOUtil.getInstance().copyFile(file.getAbsolutePath(), new FilePath(directoryPath, file.getName()).getAbsolutePath());
//                        } else {
//                            throw new PasswordException("Please enter the correct password!");
//                        }
//                    }
//                }
//            } else {
//                throw new IOException("Failure of account export, the directory is empty!");
//            }
//        } else {
//            throw new IOException("Failure of account export, there is a error in the directory file!");
//        }
//    }

    public synchronized File exportAccount(String address, String password) throws IOException {
        if (!TextUtils.isEmpty(address)) {
            if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
                File[] files = keystoreDirectory.listFiles();
                LogUtil.getInstance().print(String.format("directory file number is %s", files.length));
                if (files.length != 0) {
                    for (File file : files) {
                        if (file.getName().contains(address)) {
                            try {
                                Account originalAccount = generateAccount(file, Cipher.ENCRYPT_MODE, false, null);
                                LogUtil.getInstance().print(String.format("original account:%s", originalAccount.toString()));
                                Account encryptAccount = generateAccount(file, Cipher.ENCRYPT_MODE, true, password);
                                LogUtil.getInstance().print(String.format("encrypt account:%s", encryptAccount.toString()));
                                return generateAccountFile(backupsDirectory.getAbsolutePath(), encryptAccount, true);
                            } catch (IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                                e.printStackTrace();
                                throw new IOException("Failure of account export.");
                            }
                        }
                    }
                } else {
                    throw new IOException("Failure of account export, the directory is empty!");
                }
            } else {
                throw new IOException("Failure of account export, there is a error in the directory file!");
            }
        } else {
            throw new IOException("Failure of account export, the account address is null!");
        }
        return null;
    }

    public synchronized void importAccount(File file, String password) throws IOException, JSONException {
        LogUtil.getInstance().print(String.format("The password of import account is:%s", password));
        try {
            if (file != null && file.exists()) {
                if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
                    File[] keystoreFiles = keystoreDirectory.listFiles();
                    LogUtil.getInstance().print(String.format("Keystore directory file number is %s", keystoreFiles.length));
                    if (keystoreFiles.length != 0) {
                        for (File keystoreFile : keystoreFiles) {
                            if (verifyAccount(file, keystoreFile, password)) {
                                Account account = generateAccount(file, Cipher.DECRYPT_MODE, false, password);
                                if (account != null) {
                                    JSONObject jsonObject = JSON.parseObject(account.getCipherText());
                                    account.setAccountName(jsonObject.getString("account_name"));
                                    account.setPrivateKey(jsonObject.getString("private_key"));
                                    account.setAddress2(jsonObject.getString("address"));
                                    account.setPassword(password);
                                    account.setTime2(jsonObject.getString("time"));
                                    if (verifyAccount(account)) {
                                        generateAccountFile(keystoreDirectory.getAbsolutePath(), account, true);
                                        //IOUtil.getInstance().copyFile(file.getAbsolutePath(), new FilePath(keystoreDirectory.getAbsoluteFile(), file.getName()).getAbsolutePath());
                                    }
                                }
                            }
                        }
                    } else {
                        if (verifyAccount(file, null, password)) {
                            Account account = generateAccount(file, Cipher.DECRYPT_MODE, false, password);
                            if (account != null) {
                                LogUtil.getInstance().print("jsonObject:" + account.getCipherText());
                                JSONObject jsonObject = JSON.parseObject(account.getCipherText());
                                account.setAccountName(jsonObject.getString("account_name"));
                                account.setPrivateKey(jsonObject.getString("private_key"));
                                account.setAddress2(jsonObject.getString("address"));
                                account.setPassword(password);
                                account.setTime2(jsonObject.getString("time"));
                                if (verifyAccount(account)) {
                                    createAccount(account.getDevice(), account.getTime2(), account.getCipher(), account.getAccountName(), account.getPrivateKey(), password, account.getTime2(), false);
                                }
                            }
                        } else {
                            throw new NullPointerException("Failure of account import, account data is null!");
                        }
                    }
                } else {
                    throw new IOException("Failure of account import, there is a error in the keystore directory file!");
                }
            } else {
                throw new IOException("Failure of account import, there is a error in the keystore directory file!");
            }
        } catch (InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException e) {
            e.printStackTrace();
            throw new IOException("Failure of account import!");
        }
    }

    private synchronized boolean verifyAccount(File externalFile, File keystoreFile, String password) throws IOException, JSONException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {
        Account externalAccount;
        Account keystoreAccount = null;
        if (externalFile != null && externalFile.exists()) {
            externalAccount = generateAccount(externalFile, Cipher.DECRYPT_MODE, false, password);
            if (keystoreFile != null && keystoreFile.exists()) {
                keystoreAccount = generateAccount(keystoreFile, -1, false, null);
            }
            if (externalAccount != null && keystoreAccount != null) {
                if (externalFile.getName().startsWith(Regex.UTC.getRegext())) {
                    if (!externalAccount.getAddress1().equals(keystoreAccount.getAddress2())) {
                        return true;
                    } else {
                        throw new IOException("Failure of account import, the adress of external account is the same as the address of keystore account!");
                    }
                } else {
                    throw new IOException("Failure of account import, the file format is unavailable!");
                }
            } else if (externalAccount != null) {
                return true;
            } else {
                throw new IOException("Failure of account import, the external account is not exsist!");
            }
        } else {
            throw new IOException("Failure of account import, the external file or keystore file is not exsist!");
        }
    }

    private synchronized boolean verifyAccount(Account account) throws IOException {
        if (account != null) {
            if (TextUtils.equals(account.getAddress1(), account.getAddress2())) {
                return true;
            } else {
                throw new IOException("Failure of account import, the adress of external account has error!");
            }
        } else {
            throw new IOException("Failure of account import, the external account is not exsist!");
        }
    }

    public synchronized Account renameAccount(String address, String accountName) throws IOException {
        if (!TextUtils.isEmpty(address)) {
            if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
                File[] keystoreDirectoryFiles = keystoreDirectory.listFiles();
                LogUtil.getInstance().print(String.format("Beystore directory file number is %s", keystoreDirectoryFiles.length));
                if (keystoreDirectoryFiles.length != 0) {
                    for (File keystoreDirectoryFile : keystoreDirectoryFiles) {
                        if (keystoreDirectoryFile.getName().contains(address)) {
                            if (keystoreDirectoryFile.getName().contains(address)) {
                                try {
                                    Account account = generateAccount(keystoreDirectoryFile, Cipher.ENCRYPT_MODE, false, null);
                                    if (account != null) {
                                        return editAccount(keystoreDirectoryFile, account.getDevice(), account.getTime1(), account.getCipher(), accountName, account.getPrivateKey(), account.getPassword(), account.getTime2(), false);
                                    } else {
                                        throw new IOException("Failure of account rename, the keystore directory account is null!");
                                    }
                                } catch (IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                                    e.printStackTrace();
                                    throw new IOException("Failure of account rename!");
                                }
                            }
                        }
                    }
                } else {
                    throw new IOException("Failure of account rename, the keystore directory is empty!");
                }
            } else {
                throw new IOException("Failure of account rename, there is a error in the keystore directory file!");
            }
        } else {
            throw new IOException("Failure of account rename, the account address is null!");
        }
        return null;
    }

//    public synchronized void renameAccount(String address, String accountName) throws IOException, PasswordException {
//        if (keystoreDirectory != null && keystoreDirectory.exists() && keystoreDirectory.isDirectory()) {
//            FilePath[] keystoreDirectoryFiles = keystoreDirectory.listFiles();
//            LogUtil.getInstance().print(String.format("Beystore directory file number is %s", keystoreDirectoryFiles.length));
//            if (keystoreDirectoryFiles.length != 0) {
//                for (FilePath keystoreDirectoryFile : keystoreDirectoryFiles) {
//                    if (keystoreDirectoryFile.getName().contains(address)) {
//                        if (keystoreDirectoryFile.getName().contains(address)) {
//                            Account keystoreDirectoryAccount = generateAccountFile(keystoreDirectoryFile);
//                            if (keystoreDirectoryAccount != null) {
//                                editAccount(keystoreDirectoryFile, accountName, keystoreDirectoryAccount.getPrivateKey(), keystoreDirectoryAccount.getPublicKey(), keystoreDirectoryAccount.getAddress2(), keystoreDirectoryAccount.getPassword());
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

//    public synchronized void renameBackupsAccount(String address, String accountName) throws IOException, PasswordException {
//        if (backupsDirectory != null && backupsDirectory.exists() && backupsDirectory.isDirectory()) {
//            FilePath[] backupsDirectoryFiles = backupsDirectory.listFiles();
//            LogUtil.getInstance().print(String.format("Backups directory file number is %s", backupsDirectoryFiles.length));
//            if (backupsDirectoryFiles.length != 0) {
//                for (FilePath backupsDirectoryFile : backupsDirectoryFiles) {
//                    if (backupsDirectoryFile.getName().contains(address)) {
//                        if (backupsDirectoryFile.getName().contains(address)) {
//                            Account keystoreDirectoryAccount = generateAccountFile(backupsDirectoryFile);
//                            if (keystoreDirectoryAccount != null) {
//                                editAccount(backupsDirectoryFile, accountName, keystoreDirectoryAccount.getPrivateKey(), keystoreDirectoryAccount.getPublicKey(), keystoreDirectoryAccount.getAddress2(), keystoreDirectoryAccount.getPassword());
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
}
