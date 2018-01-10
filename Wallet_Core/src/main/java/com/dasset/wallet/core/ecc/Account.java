package com.dasset.wallet.core.ecc;

import android.os.Parcel;
import android.os.Parcelable;

import com.dasset.wallet.core.BuildConfig;

public final class Account implements Parcelable {

    private String address1;
    private String device;
    private String time1;
    private String cipher;
    private String cipherText;
    private String accountName;
    private String privateKey;
    private String address2;
    private String password;
    private String time2;

    public Account() { }

    public Account(String address1, String device, String time1, String cipher, String accountName, String privateKey, String address2, String password, String time2) {
        this.address1 = address1;
        this.device = device;
        this.time1 = time1;
        this.cipher = cipher;
        this.accountName = accountName;
        this.privateKey = privateKey;
        this.address2 = address2;
        this.password = password;
        this.time2 = time2;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getTime1() {
        return time1;
    }

    public void setTime1(String time1) {
        this.time1 = time1;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getAddress2() {
        return address2;
    }

    public String getPassword() {
        return password;
    }

    public String getTime2() {
        return time2;
    }

    @Override
    public String toString() {
        if (BuildConfig.DEBUG) {
            return "Account{" +
                    "address1='" + address1 + '\'' +
                    ", device='" + device + '\'' +
                    ", time1='" + time1 + '\'' +
                    ", cipher='" + cipher + '\'' +
                    ", cipherText='" + cipherText + '\'' +
                    ", accountName='" + accountName + '\'' +
                    ", privateKey='" + privateKey + '\'' +
                    ", address2='" + address2 + '\'' +
                    ", password='" + password + '\'' +
                    ", time2='" + time2 + '\'' +
                    '}';
        } else {
            return super.toString();
        }
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTime2(String time2) {
        this.time2 = time2;
    }

    public String getCipher() {
        return cipher;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }

    public String getCipherText() {
        return cipherText;
    }

    public void setCipherText(String cipherText) {
        this.cipherText = cipherText;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.address1);
        dest.writeString(this.device);
        dest.writeString(this.time1);
        dest.writeString(this.cipher);
        dest.writeString(this.cipherText);
        dest.writeString(this.accountName);
        dest.writeString(this.privateKey);
        dest.writeString(this.address2);
        dest.writeString(this.password);
        dest.writeString(this.time2);
    }

    protected Account(Parcel in) {
        this.address1 = in.readString();
        this.device = in.readString();
        this.time1 = in.readString();
        this.cipher = in.readString();
        this.cipherText = in.readString();
        this.accountName = in.readString();
        this.privateKey = in.readString();
        this.address2 = in.readString();
        this.password = in.readString();
        this.time2 = in.readString();
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel source) {return new Account(source);}

        @Override
        public Account[] newArray(int size) {return new Account[size];}
    };
}
