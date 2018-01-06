package com.dasset.wallet.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dasset.wallet.R;
import com.dasset.wallet.base.sticky.holder.BaseViewHolder;
import com.dasset.wallet.components.utils.ViewUtil;

public class TransactionRecordHolder extends BaseViewHolder {

    public TextView tvAssetName;
    public TextView tvAssetType;
    public TextView tvAssetAmount;
    public TextView tvTransactionDate;

    public TransactionRecordHolder(View itemView) {
        super(itemView);
        tvAssetName = ViewUtil.getInstance().findView(itemView, R.id.tvAssetName);
        tvAssetType = ViewUtil.getInstance().findView(itemView, R.id.tvAssetType);
        tvAssetAmount = ViewUtil.getInstance().findView(itemView, R.id.tvAssetAmount);
        tvTransactionDate = ViewUtil.getInstance().findView(itemView, R.id.tvTransactionDate);
    }
}
