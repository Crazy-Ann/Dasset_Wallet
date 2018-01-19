package com.dasset.wallet.base.sticky.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.dasset.wallet.base.sticky.listener.OnViewClickListener;

public class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    protected OnViewClickListener onViewClickListener;
    private int position;

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public void setOnViewClickListener(int position, OnViewClickListener onViewClickListener) {
        this.onViewClickListener = onViewClickListener;
        this.position = position;
    }

    @Override
    public void onClick(View view) {
        if (onViewClickListener != null) {
            onViewClickListener.onViewClick(position, view, this);
        }
    }
}
