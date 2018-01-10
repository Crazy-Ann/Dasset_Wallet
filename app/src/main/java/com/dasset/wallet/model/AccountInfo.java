package com.dasset.wallet.model;

import android.os.Parcel;

import com.dasset.wallet.BuildConfig;
import com.dasset.wallet.base.http.model.BaseEntity;
import com.dasset.wallet.components.widget.sticky.listener.OnGroupListener;

public class AccountInfo extends BaseEntity implements OnGroupListener {

    private String address1;
    private String device;
    private String timestamp1;
    private String cipher;
    private String cipherText;
    private String accountName;
    private String privateKey;
    private String address2;
    private String password;
    private String timestamp2;

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getTimestamp1() {
        return timestamp1;
    }

    public void setTimestamp1(String timestamp1) {
        this.timestamp1 = timestamp1;
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

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTimestamp2() {
        return timestamp2;
    }

    public void setTimestamp2(String timestamp2) {
        this.timestamp2 = timestamp2;
    }

    @Override
    public String toString() {
        if (BuildConfig.DEBUG) {
            return "AccountInfo{" +
                    "address1='" + address1 + '\'' +
                    ", device='" + device + '\'' +
                    ", timestamp1='" + timestamp1 + '\'' +
                    ", cipher='" + cipher + '\'' +
                    ", cipherText='" + cipherText + '\'' +
                    ", accountName='" + accountName + '\'' +
                    ", privateKey='" + privateKey + '\'' +
                    ", address2='" + address2 + '\'' +
                    ", password='" + password + '\'' +
                    ", timestamp2='" + timestamp2 + '\'' +
                    '}';
        } else {
            return super.toString();
        }
    }

    @Override
    public String getGroupName() {
        return null;
    }

    @Override
    public long getGroupId() {
        return 0;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.address1);
        dest.writeString(this.device);
        dest.writeString(this.timestamp1);
        dest.writeString(this.cipher);
        dest.writeString(this.cipherText);
        dest.writeString(this.accountName);
        dest.writeString(this.privateKey);
        dest.writeString(this.address2);
        dest.writeString(this.password);
        dest.writeString(this.timestamp2);
    }

    public AccountInfo() {}

    protected AccountInfo(Parcel in) {
        super(in);
        this.address1 = in.readString();
        this.device = in.readString();
        this.timestamp1 = in.readString();
        this.cipher = in.readString();
        this.cipherText = in.readString();
        this.accountName = in.readString();
        this.privateKey = in.readString();
        this.address2 = in.readString();
        this.password = in.readString();
        this.timestamp2 = in.readString();
    }

    public static final Creator<AccountInfo> CREATOR = new Creator<AccountInfo>() {
        @Override
        public AccountInfo createFromParcel(Parcel source) {return new AccountInfo(source);}

        @Override
        public AccountInfo[] newArray(int size) {return new AccountInfo[size];}
    };
}
