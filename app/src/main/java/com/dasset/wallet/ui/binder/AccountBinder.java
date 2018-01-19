package com.dasset.wallet.ui.binder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.dasset.wallet.R;
import com.dasset.wallet.base.sticky.binder.BaseViewBinder;
import com.dasset.wallet.model.AccountInfo;
import com.dasset.wallet.ui.holder.AccountHolder;
import com.dasset.wallet.ui.holder.AddAccountHolder;

public class AccountBinder extends BaseViewBinder {

    private Context context;
    private RecyclerView recyclerView;

    private RecyclerView.ViewHolder viewHolder;
    private int position;

    public AccountBinder(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
    }

    @Override
    public void bind(RecyclerView.ViewHolder viewHolder, Object object, int position, boolean checkable) {
        this.viewHolder = viewHolder;
        this.position = position;
        if (viewHolder instanceof AccountHolder) {
            AccountHolder accountHolder = (AccountHolder) viewHolder;
            AccountInfo accountInfo = (AccountInfo) object;
            accountHolder.tvAddress.setText(accountInfo.getAddress2());
            accountHolder.tvAccountName.setText(String.format("账户%s", accountInfo.getAccountName()));
            accountHolder.setOnViewClickListener(position, onViewClickListener);
        } else if (viewHolder instanceof AddAccountHolder) {
            //TODO
        }
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(int type) {
        return new AccountHolder(LayoutInflater.from(context).inflate(R.layout.holder_account, recyclerView, false));
    }


    @Override
    public void onClick(View view) {
        if (onViewClickListener != null) {
            onViewClickListener.onViewClick(position, view, viewHolder);
        }
    }
}
