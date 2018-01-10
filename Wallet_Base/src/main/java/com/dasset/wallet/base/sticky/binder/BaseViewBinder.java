package com.dasset.wallet.base.sticky.binder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.dasset.wallet.base.sticky.listener.OnViewBinderListener;
import com.dasset.wallet.base.sticky.listener.OnViewClickListener;

public abstract class BaseViewBinder implements OnViewBinderListener, View.OnClickListener {

    protected OnViewClickListener onViewClickListener;
    private int position;

    public void setOnViewClickListener(OnViewClickListener onViewClickListener) {
        this.onViewClickListener = onViewClickListener;
    }

    @Override
    public void onClick(View view) {
        if (onViewClickListener != null) {
            onViewClickListener.onViewClick(position, view);
        }
    }

    @Override
    public abstract void bind(RecyclerView.ViewHolder viewHolder, Object object, int position, boolean checkable);

    @Override
    public abstract RecyclerView.ViewHolder getViewHolder(int type);
}
