package com.dasset.wallet.base.sticky.listener;


import android.support.v7.widget.RecyclerView;

public interface OnViewBinderListener {

    void bind(RecyclerView.ViewHolder viewHolder, Object object, int position, boolean checkable);

    RecyclerView.ViewHolder getViewHolder(int type);
}
