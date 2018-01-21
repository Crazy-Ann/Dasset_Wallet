package com.dasset.wallet.core.wallet;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.dasset.wallet.components.BuildConfig;
import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.SecurityUtil;
import com.dasset.wallet.core.contant.Constant;
import com.google.common.collect.Lists;

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
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.Property;

public final class Storage {

    private Box<Account> accountBox;

    public Storage(BoxStore boxStore) throws StorageException {
        if (boxStore != null) {
            this.accountBox = boxStore.boxFor(Account.class);
        } else {
            throw new StorageException("The boxStore is null!");
        }
    }

    //TODO
    public synchronized Account insert(String deviceId, String timestamp1, String cipher, String accountName, String privateKey, String password, String timestamp2, boolean isEncrypt) throws StorageException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, IOException {
        if (accountBox != null) {
            for (Account account : query(Account_.time1)) {
                if (isEncrypt) {
                    if (TextUtils.equals(account.getAccountName(), accountName)) {
                        throw new StorageException("Failure of account create, the account name has already existed!");
                    }
                    if (!TextUtils.isEmpty(password)) {
                        Account newAccount = new Account();
                        newAccount.setAddress1(AddressFactory.generatorAddress(ECKeyPairFactory.generatePublicKey(new BigInteger(privateKey, 16), false), Constant.AddressType.HYC));
                        newAccount.setDevice(deviceId);
                        newAccount.setTime1(timestamp1);
                        newAccount.setCipher(cipher);
                        JSONObject jsonObject1 = new JSONObject();
                        JSONObject jsonObject2 = new JSONObject();
                        jsonObject2.put("account_name", accountName);
                        jsonObject2.put("private_key", privateKey);
                        jsonObject2.put("address", AddressFactory.generatorAddress(ECKeyPairFactory.generatePublicKey(new BigInteger(privateKey, 16), false), Constant.AddressType.HYC));
                        jsonObject2.put("time", timestamp2);
                        jsonObject1.put("ciphertext", jsonObject2.toJSONString());
                        byte[] bytes = SecurityUtil.getInstance().encryptAESEBC(jsonObject1.toJSONString(), password);
                        if (bytes != null && bytes.length > 0) {
                            newAccount.setCipherText(Hex.toHexString(bytes));
                            accountBox.put(newAccount);
                            LogUtil.getInstance().print(String.format("Account create success! [%s]", newAccount.toString()));
                            return newAccount;
                        } else {
                            throw new StorageException("Failure of account create, the password has error!");
                        }
                    } else {
                        throw new StorageException("Failure of account create, the password is null!");
                    }
                } else {
                    Account newAccount = new Account();
                    newAccount.setAddress1(AddressFactory.generatorAddress(ECKeyPairFactory.generatePublicKey(new BigInteger(privateKey, 16), false), Constant.AddressType.HYC));
                    newAccount.setDevice(deviceId);
                    newAccount.setTime1(timestamp1);
                    newAccount.setCipher(cipher);
                    newAccount.setCipherText(null);
                    newAccount.setAccountName(accountName);
                    newAccount.setPassword(password);
                    newAccount.setPrivateKey(privateKey);
                    newAccount.setAddress2(AddressFactory.generatorAddress(ECKeyPairFactory.generatePublicKey(new BigInteger(privateKey, 16), false), Constant.AddressType.HYC));
                    newAccount.setTime2(timestamp2);
                    accountBox.put(newAccount);
                    LogUtil.getInstance().print(String.format("Account create success! [%s]", newAccount.toString()));
                    return newAccount;
                }
            }
        } else {
            throw new StorageException("Failure of account create, the accountBox is null!");
        }
        return null;
    }

    public synchronized void delete(String address, String password, boolean isEncrypt) throws StorageException, NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        if (accountBox != null) {
            if (!TextUtils.isEmpty(address) && !TextUtils.isEmpty(password)) {
                for (Account account : query(Account_.time1)) {
                    if (isEncrypt) {
                        byte[] bytes = SecurityUtil.getInstance().decryptAESEBC(Hex.decode(account.getCipherText()), password);
                        if (bytes != null && bytes.length > 0) {
                            if (TextUtils.equals(JSONObject.parseObject(new String(bytes, Regex.UTF_8.getRegext())).getString("address"), address)) {
                                accountBox.remove(account);
                                LogUtil.getInstance().print(String.format("Account delete success! [%s]", account.toString()));
                            }
                        } else {
                            throw new StorageException("Failure of account delete, the password has error!");
                        }
                    } else {
                        if (TextUtils.equals(account.getAddress2(), address)) {
                            accountBox.remove(account);
                            LogUtil.getInstance().print(String.format("Account delete success! [%s]", account.toString()));
                        }
                    }
                }
            } else {
                throw new StorageException("Failure of account create, the address or password is null!");
            }
            throw new StorageException("Failure of account delete, the account address has not existed!");
        } else {
            throw new StorageException("Failure of account delete, the accountBox is null!");
        }
    }

    public synchronized Account update(String address, String accountName, String password, boolean isEncrypt) throws StorageException, NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        if (accountBox != null) {
            if (!TextUtils.isEmpty(address)) {
                for (Account account : query(Account_.time1)) {
                    if (isEncrypt) {
                        if (!TextUtils.isEmpty(password)) {
                            byte[] decryptBytes = SecurityUtil.getInstance().decryptAESEBC(Hex.decode(account.getCipherText()), password);
                            if (decryptBytes != null && decryptBytes.length > 0) {
                                JSONObject jsonObject = JSONObject.parseObject(new String(decryptBytes, Regex.UTF_8.getRegext()));
                                if (TextUtils.equals(jsonObject.getString("address"), address)) {
                                    if (TextUtils.equals(jsonObject.getString("account_name"), accountName)) {
                                        throw new StorageException("Failure of account update, the account name has already existed!");
                                    } else {
                                        JSONObject jsonObject1 = new JSONObject();
                                        jsonObject.put("account_name", accountName);
                                        jsonObject.put("private_key", jsonObject.getString("private_key"));
                                        jsonObject.put("address", jsonObject.getString("address"));
                                        jsonObject.put("time", jsonObject.getString("time"));
                                        jsonObject1.put("ciphertext", jsonObject.toJSONString());
                                        byte[] encryptBytes = SecurityUtil.getInstance().encryptAESEBC(jsonObject1.toJSONString(), password);
                                        if (encryptBytes != null && encryptBytes.length > 0) {
                                            account.setCipherText(Hex.toHexString(encryptBytes));
                                            accountBox.put(account);
                                            LogUtil.getInstance().print(String.format("Account create success! [%s]", account.toString()));
                                            return account;
                                        } else {
                                            throw new StorageException("Failure of account create, the password has error!");
                                        }
                                    }
                                }
                            } else {
                                throw new StorageException("Failure of account generate, the password has error!");
                            }
                        } else {
                            throw new StorageException("Failure of account create, the password is null!");
                        }
                    } else {
                        if (TextUtils.equals(account.getAddress2(), address)) {
                            account.setAccountName(accountName);
                            accountBox.put(account);
                            LogUtil.getInstance().print(String.format("Account create success! [%s]", account.toString()));
                            return account;
                        }
                    }
                }
            } else {
                throw new StorageException("Failure of keystore update, the address is null!");
            }
        } else {
            throw new StorageException("Failure of account update, the accountBox is null!");
        }
        return null;
    }

    public synchronized List<Account> query(Property property) {
        if (accountBox != null) {
            return accountBox.query().order(property).build().find();
        } else {
            LogUtil.getInstance().print("The accountBox is null!");
            return Lists.newArrayList();
        }
    }

    public synchronized void importKeyStore(File file, String password, boolean isEncrypt) throws StorageException, BadPaddingException, NoSuchAlgorithmException, IOException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException {
        if (file != null && file.exists()) {
            Account account = generateAccount(file, password, isEncrypt);
            if (verifyKeyStore(account, password, isEncrypt)) {
                if (accountBox != null) {
                    accountBox.put(account);
                } else {
                    throw new StorageException("Failure of account create, the accountBox is null!");
                }
            } else {
                throw new StorageException("Failure of keystore import, not through keystore verify!");
            }
        } else {
            throw new StorageException("Failure of keystore import, an error occurred in the keystore file!");
        }
    }

    public synchronized File exportKeyStore(File directory, String address, String password, boolean isEncrypt) throws StorageException, NoSuchPaddingException, IOException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            if (!TextUtils.isEmpty(address)) {
                for (Account account : query(Account_.time1)) {
                    if (isEncrypt) {
                        byte[] decryptBytes = SecurityUtil.getInstance().decryptAESEBC(Hex.decode(account.getCipherText()), password);
                        if (decryptBytes != null && decryptBytes.length > 0) {
                            if (!TextUtils.equals(address, JSONObject.parseObject(new String(decryptBytes, Regex.UTF_8.getRegext())).getString("address"))) {
                                return generateAccountFile(directory.getAbsolutePath(), account, password, isEncrypt);
                            }
                        }
                    } else {
                        if (!TextUtils.equals(address, account.getAddress2())) {
                            return generateAccountFile(directory.getAbsolutePath(), account, password, isEncrypt);
                        }
                    }
                }
            } else {
                throw new StorageException("Failure of keystore export, the address is null!");
            }
        } else {
            throw new StorageException("Failure of keystore import, an error occurred in the directory file!");
        }
        return null;
    }

    private synchronized boolean verifyKeyStore(Account externalAccount, String password, boolean isEncrypt) throws IOException, JSONException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, StorageException {
        if (externalAccount != null) {
            for (Account account : query(Account_.time1)) {
                if (isEncrypt) {
                    byte[] accountDecryptBytes = SecurityUtil.getInstance().decryptAESEBC(Hex.decode(account.getCipherText()), password);
                    byte[] externalAccountDecryptBytes = SecurityUtil.getInstance().decryptAESEBC(Hex.decode(externalAccount.getCipherText()), password);
                    if (accountDecryptBytes != null && accountDecryptBytes.length > 0 && externalAccountDecryptBytes != null && externalAccountDecryptBytes.length > 0) {
                        String accountDecryptData = new String(accountDecryptBytes, Regex.UTF_8.getRegext());
                        String externaAccountDecryptData = new String(externalAccountDecryptBytes, Regex.UTF_8.getRegext());
                        if (!TextUtils.equals(externalAccount.getAddress1(), JSONObject.parseObject(externaAccountDecryptData).getString("address"))) {
                            if (!TextUtils.equals(JSONObject.parseObject(accountDecryptData).getString("address"), JSONObject.parseObject(externaAccountDecryptData).getString("address"))) {
                                if (!TextUtils.equals(JSONObject.parseObject(accountDecryptData).getString("account_name"), JSONObject.parseObject(externaAccountDecryptData).getString("account_name"))) {
                                    return true;
                                } else {
                                    throw new StorageException("The account name has already existed!");
                                }
                            } else {
                                throw new StorageException("The account has already existed!");
                            }
                        } else {
                            throw new StorageException("The external account address has error!");
                        }
                    }
                } else {
                    if (!TextUtils.equals(externalAccount.getAddress1(), externalAccount.getAddress2())) {
                        if (!TextUtils.equals(account.getAddress2(), externalAccount.getAddress2())) {
                            if (!TextUtils.equals(account.getAccountName(), externalAccount.getAccountName())) {
                                return true;
                            } else {
                                throw new StorageException("The account name has already existed!");
                            }
                        } else {
                            throw new StorageException("The account has already existed!");
                        }
                    } else {
                        throw new StorageException("The external account address has error!");
                    }
                }
            }
        }
        return false;
    }

    private synchronized Account generateAccount(File file, String password, boolean isEncrypt) throws IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, StorageException {
        if (file != null && file.exists()) {
            LogUtil.getInstance().print(String.format("Generate account file is :%s", file));
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
                if (isEncrypt) {
                    byte[] decryptBytes = SecurityUtil.getInstance().decryptAESEBC(Hex.decode(jsonObject2.getString("ciphertext")), password);
                    if (decryptBytes != null && decryptBytes.length > 0) {
                        JSONObject jsonObject3 = JSONObject.parseObject(new String(decryptBytes, Regex.UTF_8.getRegext()));
                        account.setCipher(jsonObject3.getString("account_name"));
                        account.setCipher(jsonObject3.getString("private_key"));
                        account.setCipher(jsonObject3.getString("address"));
                        account.setCipher(jsonObject3.getString("time"));
                    } else {
                        throw new StorageException("Failure of account generate, the password has error!");
                    }
                    account.setCipherText(null);
                } else {
                    account.setCipherText(jsonObject2.getString("ciphertext"));
                }
            }
            return account;
        }
        return null;
    }

    private synchronized File generateAccountFile(String directoryPath, Account account, String password, boolean isEncrypt) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
        if (!TextUtils.isEmpty(directoryPath) && account != null) {
            File file = null;
            if (isEncrypt) {
                byte[] decryptBytes = SecurityUtil.getInstance().decryptAESEBC(Hex.decode(account.getCipherText()), password);
                if (decryptBytes != null && decryptBytes.length > 0) {
                    JSONObject jsonObject = JSONObject.parseObject(new String(decryptBytes, Regex.UTF_8.getRegext()));
                    file = new File(directoryPath + Regex.LEFT_SLASH.getRegext() + jsonObject.getString("time") + Regex.DOUBLE_MINUS.getRegext() + jsonObject.getString("address"));
                }
            } else {
                file = new File(directoryPath + Regex.LEFT_SLASH.getRegext() + account.getTime2() + Regex.DOUBLE_MINUS.getRegext() + account.getAddress2());
            }
            if (file != null && file.createNewFile()) {
                if (file.exists()) {
                    persistence(file, account, isEncrypt);
                } else {
                    throw new IOException("Failure of account generate, an error occurred when the file was created!");
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

    private synchronized String generateKeyStoreData(Account account, boolean isEncrypt) {
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("address", account.getAddress1());
        jsonObject1.put("time", account.getTime1());
        jsonObject1.put("device", account.getDevice());
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("cipher", account.getCipher());
        if (isEncrypt) {
            jsonObject2.put("ciphertext", account.getCipherText());
        } else {
            JSONObject jsonObject3 = new JSONObject();
            jsonObject3.put("account_name", account.getAccountName());
            jsonObject3.put("private_key", account.getPrivateKey());
            jsonObject3.put("address", account.getAddress2());
            jsonObject3.put("password", account.getPassword());
            jsonObject3.put("time", account.getTime2());
            jsonObject2.put("ciphertext", jsonObject3);
        }
        jsonObject1.put("crypto", jsonObject2);
        return jsonObject1.toJSONString();
    }

    private synchronized void persistence(File file, Account account, boolean isEncrypt) throws IOException {
        if (account != null) {
            String keyStoreData = generateKeyStoreData(account, isEncrypt);
            if (!TextUtils.isEmpty(keyStoreData)) {
                BufferedWriter bufferedWriter = null;
                try {
                    bufferedWriter = new BufferedWriter(new FileWriter(file));
                    bufferedWriter.write(keyStoreData);
                    bufferedWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    file.delete();
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
        } else {
            LogUtil.getInstance().print("The account is null!");
        }
    }

    private synchronized File[] directoryTraversal(File directory) {
        if (directory != null && directory.isDirectory() && directory.exists()) {
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
            LogUtil.getInstance().print(String.format("The %s has an error", directory));
        }
        return null;
    }
}
