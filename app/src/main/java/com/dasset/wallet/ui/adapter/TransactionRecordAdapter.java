package com.dasset.wallet.ui.adapter;

import android.support.v7.widget.RecyclerView;

import com.dasset.wallet.base.sticky.adapter.FixedStickyViewAdapter;
import com.dasset.wallet.base.sticky.binder.BaseViewBinder;
import com.dasset.wallet.model.TransactionRecord;
import com.dasset.wallet.ui.holder.TransactionRecordHolder;

public class TransactionRecordAdapter extends FixedStickyViewAdapter<TransactionRecord, TransactionRecordHolder> {

    public TransactionRecordAdapter(BaseViewBinder baseViewBinder) {
        super(baseViewBinder);
    }

    @Override
    protected void onBindHeaderOrFooter(RecyclerView.ViewHolder holder, Object object) {
        
    }

}