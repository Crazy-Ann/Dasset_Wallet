package com.dasset.wallet.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.dasset.wallet.R;
import com.dasset.wallet.base.sticky.adapter.FixedStickyHeaderAdapter;
import com.dasset.wallet.base.sticky.binder.BaseViewBinder;
import com.dasset.wallet.components.utils.ViewUtil;
import com.dasset.wallet.model.AccountInfo;
import com.dasset.wallet.ui.holder.AccountHolder;

public class AccountAdapter extends FixedStickyHeaderAdapter<AccountInfo, AccountHolder> {

    public AccountAdapter(Context ctx, BaseViewBinder baseViewBinder, boolean groupable) {
        super(ctx, baseViewBinder, groupable);
    }

    @Override
    protected void onBindHeaderOrFooter(RecyclerView.ViewHolder viewHolder, Object object) {
        super.onBindHeaderOrFooter(viewHolder, object);
        switch (((FixedStickyView) object).fixedStickyViewType) {
            case R.layout.holder_add_account:
                ViewUtil.getInstance().findViewAttachOnclick(viewHolder.itemView, R.id.llAddAccount, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onHeaderOrFooterItemClickListener != null) {
                            onHeaderOrFooterItemClickListener.onHeaderOrFooterItemClick(R.id.llAddAccount);
                        }
                    }
                });
                break;
            default:
                break;
        }
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
