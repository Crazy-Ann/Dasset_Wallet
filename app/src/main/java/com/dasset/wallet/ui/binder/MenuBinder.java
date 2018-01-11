package com.dasset.wallet.ui.binder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.dasset.wallet.R;
import com.dasset.wallet.base.sticky.binder.BaseViewBinder;
import com.dasset.wallet.model.Menu;
import com.dasset.wallet.ui.holder.AccountHolder;
import com.dasset.wallet.ui.holder.MenuHolder;

public class MenuBinder extends BaseViewBinder {

    private Context context;
    private RecyclerView recyclerView;

    public MenuBinder(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
    }

    @Override
    public void bind(RecyclerView.ViewHolder viewHolder, Object object, int position, boolean checkable) {
        if (viewHolder instanceof MenuHolder) {
            MenuHolder menuHolder = (MenuHolder) viewHolder;
            Menu menu = (Menu) object;
            menuHolder.tvMenu.setText(menu.getMenuName());
        }
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(int type) {
        return new MenuHolder(LayoutInflater.from(context).inflate(R.layout.holder_menu, recyclerView, false));
    }
}
