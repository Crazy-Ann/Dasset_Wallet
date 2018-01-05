package com.dasset.wallet.core.ecc;

import android.os.Parcel;
import android.os.Parcelable;

import com.dasset.wallet.components.constant.Regex;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Account implements Parcelable {

    private String serialNumber;
    private String accountName;
    private String privateKey;
    private String publicKey;
    private String address;
    private String password;
    private String timestamp;

    public Account() { }

    public Account(String serialNumber, String accountName, String privateKey, String publicKey, String address, String password) {
        this.serialNumber = serialNumber;
        this.accountName = accountName;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
        this.password = password;
        this.timestamp = new SimpleDateFormat(Regex.DATE_FORMAT_ALL.getRegext(), Locale.getDefault()).format(new Date(System.currentTimeMillis()));
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getAddress() {
        return address;
    }

    public String getPassword() {
        return password;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.serialNumber);
        dest.writeString(this.accountName);
        dest.writeString(this.privateKey);
        dest.writeString(this.publicKey);
        dest.writeString(this.address);
        dest.writeString(this.password);
        dest.writeString(this.timestamp);
    }

    protected Account(Parcel in) {
        this.serialNumber = in.readString();
        this.accountName = in.readString();
        this.privateKey = in.readString();
        this.publicKey = in.readString();
        this.address = in.readString();
        this.password = in.readString();
        this.timestamp = in.readString();
    }

    public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel source) {return new Account(source);}

        @Override
        public Account[] newArray(int size) {return new Account[size];}
    };

    @Override
    public String toString() {
        return "Account{" +
                "serialNumber='" + serialNumber + '\'' +
                ", accountName='" + accountName + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", address='" + address + '\'' +
                ", password='" + password + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
