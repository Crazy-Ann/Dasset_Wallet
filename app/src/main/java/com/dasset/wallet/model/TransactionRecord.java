package com.dasset.wallet.model;

import android.os.Parcel;

import com.dasset.wallet.BuildConfig;
import com.dasset.wallet.base.http.model.BaseEntity;
import com.dasset.wallet.components.widget.sticky.listener.OnGroupListener;


public class TransactionRecord extends BaseEntity implements OnGroupListener {

    private String assetName;
    private String assetType;
    private String assetAmount;
    private String transactionDate;

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getAssetAmount() {
        return assetAmount;
    }

    public void setAssetAmount(String assetAmount) {
        this.assetAmount = assetAmount;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Override
    public String toString() {
        if (BuildConfig.DEBUG) {
            return "TransactionRecord{" +
                    "assetName='" + assetName + '\'' +
                    ", assetType='" + assetType + '\'' +
                    ", assetAmount='" + assetAmount + '\'' +
                    ", transactionDate='" + transactionDate + '\'' +
                    '}';
        } else {
            return super.toString();
        }
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.assetName);
        dest.writeString(this.assetType);
        dest.writeString(this.assetAmount);
        dest.writeString(this.transactionDate);
    }

    public TransactionRecord() {}

    protected TransactionRecord(Parcel in) {
        super(in);
        this.assetName = in.readString();
        this.assetType = in.readString();
        this.assetAmount = in.readString();
        this.transactionDate = in.readString();
    }

    public static final Creator<TransactionRecord> CREATOR = new Creator<TransactionRecord>() {
        @Override
        public TransactionRecord createFromParcel(Parcel source) {return new TransactionRecord(source);}

        @Override
        public TransactionRecord[] newArray(int size) {return new TransactionRecord[size];}
    };

    @Override
    public String getGroupName() {
        return null;
    }

    @Override
    public long getGroupId() {
        return 0;
    }
}
