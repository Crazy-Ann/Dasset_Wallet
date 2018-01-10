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
    public ImageView ivAddressQRCode;

    public AccountHolder(View itemView) {
        super(itemView);
        tvAddress = ViewUtil.getInstance().findViewAttachOnclick(itemView, R.id.tvAddress, this);
        tvAccountName = ViewUtil.getInstance().findView(itemView, R.id.tvAccountName);
        ivAddressQRCode = ViewUtil.getInstance().findViewAttachOnclick(itemView, R.id.ivAddressQRCode, this);
    }
}
