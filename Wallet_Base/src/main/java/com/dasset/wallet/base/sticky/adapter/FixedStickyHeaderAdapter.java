package com.dasset.wallet.base.sticky.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dasset.wallet.base.R;
import com.dasset.wallet.base.sticky.binder.BaseViewBinder;
import com.dasset.wallet.base.sticky.holder.BaseViewHolder;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.components.widget.sticky.listener.OnGroupListener;
import com.dasset.wallet.components.widget.sticky.listener.StickyRecyclerHeadersAdapter;


public class FixedStickyHeaderAdapter<T extends OnGroupListener, V extends RecyclerView.ViewHolder> extends FixedStickyViewAdapter<T, V> implements StickyRecyclerHeadersAdapter {

    protected Context context;
    protected boolean groupable;

    protected static final int GROUP_ID_UNAVAILABLE = -1;

    public FixedStickyHeaderAdapter(Context ctx, BaseViewBinder baseViewBinder, boolean groupable) {
        super(baseViewBinder);
        this.context = ctx;
        this.groupable = groupable;
    }

    @Override
    protected void onBindHeaderOrFooter(RecyclerView.ViewHolder holder, Object object) {

    }

    @Override
    public long getHeaderId(int position) {
        if (!groupable) {
            return GROUP_ID_UNAVAILABLE;
        }
        Object object = getItem(position);
        if (object != null) {
            int headers = headerViews.size();
            int items   = this.items.size();
            if (position < headers || position >= headers + items) {
                return GROUP_ID_UNAVAILABLE;
            }
            return ((OnGroupListener) object).getGroupId();
        }
        return GROUP_ID_UNAVAILABLE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return new BaseViewHolder(LayoutInflater.from(context).inflate(R.layout.view_header, parent, false));
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (groupable) {
            TextView tvHeader = ViewUtil.getInstance().findView(holder.itemView, R.id.tvDate);
            if (getHeaderId(position) != GROUP_ID_UNAVAILABLE) {
                tvHeader.setText(((OnGroupListener) getItem(position)).getGroupName());
            }
        }
    }
}
