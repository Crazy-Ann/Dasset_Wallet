package com.dasset.wallet.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.dasset.wallet.base.sticky.adapter.FixedStickyViewAdapter;
import com.dasset.wallet.base.sticky.binder.BaseViewBinder;
import com.dasset.wallet.model.Menu;
import com.dasset.wallet.ui.holder.MenuHolder;

public class MenuAdapter extends FixedStickyViewAdapter<Menu, MenuHolder> {

    public MenuAdapter(BaseViewBinder baseViewBinder) {
        super(baseViewBinder);
    }

    @Override
    protected void onBindHeaderOrFooter(RecyclerView.ViewHolder holder, Object object) {

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
        final int itemPosition = position;
        if (viewHolder.getItemViewType() == TYPE_CONTENT_VIEW) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(itemPosition, view, viewHolder);
                    }
                }
            });
        }
    }
}