package com.dasset.wallet.ui.binder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.dasset.wallet.R;
import com.dasset.wallet.base.sticky.binder.BaseViewBinder;
import com.dasset.wallet.model.TransactionRecord;
import com.dasset.wallet.ui.holder.TransactionRecordHolder;

public class TransactionRecordBinder extends BaseViewBinder {

    private Context context;
    private RecyclerView recyclerView;

    public TransactionRecordBinder(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
    }

    @Override
    public void bind(RecyclerView.ViewHolder viewHolder, Object object, int position, boolean checkable) {
        TransactionRecordHolder transactionRecordHolder = (TransactionRecordHolder) viewHolder;
        TransactionRecord transactionRecord = (TransactionRecord) object;
        transactionRecordHolder.tvAssetName.setText(transactionRecord.getAssetName());
        transactionRecordHolder.tvAssetType.setText(transactionRecord.getAssetType());
        transactionRecordHolder.tvAssetAmount.setText(transactionRecord.getAssetAmount());
        transactionRecordHolder.tvTransactionDate.setText(transactionRecord.getTransactionDate());
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(int type) {
        return new TransactionRecordHolder(LayoutInflater.from(context).inflate(R.layout.holder_transaction_record, recyclerView, false));
    }

    @Override
    public void onClick(View v) {
        
    }
}
