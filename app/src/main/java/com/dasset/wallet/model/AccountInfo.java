package com.dasset.wallet.model;

import android.os.Parcel;

import com.dasset.wallet.BuildConfig;
import com.dasset.wallet.base.http.model.BaseEntity;
import com.dasset.wallet.components.widget.sticky.listener.OnGroupListener;

public class AccountInfo extends BaseEntity implements OnGroupListener {

    private String accountName;
    private String privateKey;
    private String publicKey;
    private String address;
    private String password;
    private String timestamp;

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

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        if (BuildConfig.DEBUG) {
            return "AccountInfo{" +
                    "accountName='" + accountName + '\'' +
                    ", privateKey='" + privateKey + '\'' +
                    ", publicKey='" + publicKey + '\'' +
                    ", address='" + address + '\'' +
                    ", password='" + password + '\'' +
                    ", timestamp='" + timestamp + '\'' +
                    '}';
        } else {
            return super.toString();
        }
    }

    public AccountInfo() {}

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
        dest.writeString(this.accountName);
        dest.writeString(this.privateKey);
        dest.writeString(this.publicKey);
        dest.writeString(this.address);
        dest.writeString(this.password);
        dest.writeString(this.timestamp);
    }

    protected AccountInfo(Parcel in) {
        super(in);
        this.accountName = in.readString();
        this.privateKey = in.readString();
        this.publicKey = in.readString();
        this.address = in.readString();
        this.password = in.readString();
        this.timestamp = in.readString();
    }

    public static final Creator<AccountInfo> CREATOR = new Creator<AccountInfo>() {
        @Override
        public AccountInfo createFromParcel(Parcel source) {return new AccountInfo(source);}

        @Override
        public AccountInfo[] newArray(int size) {return new AccountInfo[size];}
    };
}
