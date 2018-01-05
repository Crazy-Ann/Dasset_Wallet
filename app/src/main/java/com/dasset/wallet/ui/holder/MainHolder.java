package com.dasset.wallet.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dasset.wallet.R;
import com.dasset.wallet.base.sticky.holder.BaseViewHolder;
import com.dasset.wallet.components.utils.ViewUtil;

public class MainHolder extends BaseViewHolder {

    public TextView tvAddress;
    public ImageView ivAddress;
    public TextView tvSerialNumber;

    public MainHolder(View itemView) {
        super(itemView);
        tvAddress = ViewUtil.getInstance().findView(itemView, R.id.tvAddress);
        ivAddress = ViewUtil.getInstance().findView(itemView, R.id.ivAddress);
        tvSerialNumber = ViewUtil.getInstance().findView(itemView, R.id.tvSerialNumber);
    }
}
