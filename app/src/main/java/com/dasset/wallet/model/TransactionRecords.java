package com.dasset.wallet.model;

import android.os.Parcel;

import com.dasset.wallet.BuildConfig;
import com.dasset.wallet.base.http.model.BaseEntity;

import java.util.List;


public class TransactionRecords extends BaseEntity {

    private List<TransactionRecord> transactionRecords;

    public List<TransactionRecord> getTransactionRecords() {
        return transactionRecords;
    }

    public void setTransactionRecords(List<TransactionRecord> transactionRecords) {
        this.transactionRecords = transactionRecords;
    }

    public TransactionRecords() {}

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedList(this.transactionRecords);
    }

    protected TransactionRecords(Parcel in) {
        super(in);
        this.transactionRecords = in.createTypedArrayList(TransactionRecord.CREATOR);
    }

    public static final Creator<TransactionRecords> CREATOR = new Creator<TransactionRecords>() {
        @Override
        public TransactionRecords createFromParcel(Parcel source) {return new TransactionRecords(source);}

        @Override
        public TransactionRecords[] newArray(int size) {return new TransactionRecords[size];}
    };
}
