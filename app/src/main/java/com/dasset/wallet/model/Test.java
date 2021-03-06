package com.dasset.wallet.model;

import android.annotation.SuppressLint;
import android.os.Parcel;

import com.dasset.wallet.base.http.model.BaseEntity;

@SuppressLint("ParcelCreator")
public class Test extends BaseEntity {

    private String privateKey;
    private String publicKey;
    private String address;
    private String signatureR;
    private String signatureS;
    private String signature;
    private boolean signatureResult;

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

    public String getSignatureR() {
        return signatureR;
    }

    public void setSignatureR(String signatureR) {
        this.signatureR = signatureR;
    }

    public String getSignatureS() {
        return signatureS;
    }

    public void setSignatureS(String signatureS) {
        this.signatureS = signatureS;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public boolean getSignatureResult() {
        return signatureResult;
    }

    public void setSignatureResult(boolean signatureResult) {
        this.signatureResult = signatureResult;
    }
    
    public Test() {}

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.privateKey);
        dest.writeString(this.publicKey);
        dest.writeString(this.address);
        dest.writeString(this.signatureR);
        dest.writeString(this.signatureS);
        dest.writeString(this.signature);
        dest.writeByte(this.signatureResult ? (byte) 1 : (byte) 0);
    }

    protected Test(Parcel in) {
        super(in);
        this.privateKey = in.readString();
        this.publicKey = in.readString();
        this.address = in.readString();
        this.signatureR = in.readString();
        this.signatureS = in.readString();
        this.signature = in.readString();
        this.signatureResult = in.readByte() != 0;
    }

    public static final Creator<Test> CREATOR = new Creator<Test>() {
        @Override
        public Test createFromParcel(Parcel source) {return new Test(source);}

        @Override
        public Test[] newArray(int size) {return new Test[size];}
    };
}
