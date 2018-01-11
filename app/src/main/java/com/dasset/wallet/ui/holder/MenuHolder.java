package com.dasset.wallet.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dasset.wallet.R;
import com.dasset.wallet.base.sticky.holder.BaseViewHolder;
import com.dasset.wallet.components.utils.ViewUtil;

public class MenuHolder extends BaseViewHolder {

    public TextView tvMenu;

    public MenuHolder(View itemView) {
        super(itemView);
        tvMenu = ViewUtil.getInstance().findView(itemView, R.id.tvMenu);
    }
}
