package com.dasset.wallet.model;

import android.os.Parcel;

import com.dasset.wallet.BuildConfig;
import com.dasset.wallet.base.http.model.BaseEntity;
import com.dasset.wallet.core.wallet.hd.HDAccount;

import java.util.List;

public class WalletInfo extends BaseEntity {

    private String walletName;
    private HDAccount hdAccount;
    private List<String> MnemonicCodes;

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public HDAccount getHdAccount() {
        return hdAccount;
    }

    public void setHdAccount(HDAccount hdAccount) {
        this.hdAccount = hdAccount;
    }

    public List<String> getMnemonicCodes() {
        return MnemonicCodes;
    }

    public void setMnemonicCodes(List<String> mnemonicCodes) {
        MnemonicCodes = mnemonicCodes;
    }

    @Override
    public String toString() {
        if (BuildConfig.DEBUG) {
            return "WalletInfo{" +
                    "walletName=" + walletName +
                    ", hdAccount=" + hdAccount.getAddress() +
                    ", MnemonicCodes=" + MnemonicCodes +
                    '}';
        } else {
            return super.toString();
        }
    }


    public WalletInfo() {}

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.walletName);
        dest.writeParcelable(this.hdAccount, flags);
        dest.writeStringList(this.MnemonicCodes);
    }

    protected WalletInfo(Parcel in) {
        super(in);
        this.walletName = in.readString();
        this.hdAccount = in.readParcelable(HDAccount.class.getClassLoader());
        this.MnemonicCodes = in.createStringArrayList();
    }

    public static final Creator<WalletInfo> CREATOR = new Creator<WalletInfo>() {
        @Override
        public WalletInfo createFromParcel(Parcel source) {return new WalletInfo(source);}

        @Override
        public WalletInfo[] newArray(int size) {return new WalletInfo[size];}
    };
}
