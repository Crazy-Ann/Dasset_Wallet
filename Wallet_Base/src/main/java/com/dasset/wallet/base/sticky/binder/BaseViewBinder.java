package com.dasset.wallet.base.sticky.binder;

import android.support.v7.widget.RecyclerView;

import com.dasset.wallet.base.sticky.listener.OnViewBinderListener;

public abstract class BaseViewBinder implements OnViewBinderListener {

    @Override
    public abstract void bind(RecyclerView.ViewHolder viewHolder, Object object, int position, boolean checkable);

    @Override
    public abstract RecyclerView.ViewHolder getViewHolder(int type);
}
