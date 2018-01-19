package com.dasset.wallet.base.sticky.listener;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public interface OnViewClickListener {

    void onViewClick(int position, View view, RecyclerView.ViewHolder viewHolder);
}
