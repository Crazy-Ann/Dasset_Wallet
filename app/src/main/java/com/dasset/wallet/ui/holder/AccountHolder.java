package com.dasset.wallet.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dasset.wallet.R;
import com.dasset.wallet.base.sticky.holder.BaseViewHolder;
import com.dasset.wallet.components.utils.ViewUtil;

public class AccountHolder extends BaseViewHolder {

    public TextView tvAddress;
    public TextView tvAccountName;

    public AccountHolder(View itemView) {
        super(itemView);
        tvAddress = ViewUtil.getInstance().findView(itemView, R.id.tvAddress);
        tvAccountName = ViewUtil.getInstance().findView(itemView, R.id.tvAccountName);
    }
}
