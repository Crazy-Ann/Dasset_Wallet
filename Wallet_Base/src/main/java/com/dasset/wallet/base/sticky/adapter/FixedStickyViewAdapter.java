package com.dasset.wallet.base.sticky.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.dasset.wallet.base.sticky.binder.BaseViewBinder;
import com.dasset.wallet.base.sticky.listener.OnEventClickListener;
import com.dasset.wallet.base.sticky.listener.OnItemClickListener;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public abstract class FixedStickyViewAdapter<T, V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter {


    public static final  int TYPE_HEADER_VIEW       = 0x5001;
    public static final  int TYPE_CONTENT_VIEW      = 0x5003;
    public static final  int TYPE_FOOTER_VIEW       = 0x5004;
    private static final int NOTIFY_TIP_UNAVAILABLE = -1;
    private int notifyTip;

    protected ArrayList<FixedStickyView> headerViews = Lists.newArrayList();
    protected ArrayList<FixedStickyView> footerViews = Lists.newArrayList();

    private SparseArray<FixedViewHoldGenerator> generators = new SparseArray<>();
    private BaseViewBinder baseViewBinder;

    protected List<T> items = Lists.newArrayList();
    protected OnItemClickListener  onItemClickListener;
    protected OnEventClickListener onEventClickListener;

    public FixedStickyViewAdapter(BaseViewBinder binder) {
        this.baseViewBinder = binder;
        notifyTip = NOTIFY_TIP_UNAVAILABLE;
    }

    public FixedStickyViewAdapter setData(List<T> datas) {
        if (datas != null && datas.size() > 0) {
            items.clear();
            items.addAll(datas);
        } else {
            items.clear();
        }
        notifyDataSetChanged();
        return this;
    }

    public void addItems(Activity activity, List<T> items) {
        this.items.addAll(items);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void clear() {
        items.clear();
        headerViews.clear();
        footerViews.clear();
        generators.clear();
        notifyDataSetChanged();
    }

    public void bindDataToHeaderOrFooter(int id, Object object, int viewType) {
        List<FixedStickyView> views = null;
        if (viewType == TYPE_HEADER_VIEW) {
            views = headerViews;
        } else if (viewType == TYPE_FOOTER_VIEW) {
            views = footerViews;
        }
        if (views != null && views.size() > 0) {
            for (int i = 0; i < views.size(); i++) {
                FixedStickyView view = views.get(i);
                if (view.id == id) {
                    view.object = object;
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    public void addFooterView(int id, int layoutId, int viewType, int fixedStickyViewType, FixedViewHoldGenerator generator) {
        final FixedStickyView info = new FixedStickyView();
        info.id = id;
        info.object = null;
        info.layoutId = layoutId;
        info.viewType = viewType;
        info.fixedStickyViewType = fixedStickyViewType;
        if (generators.get(fixedStickyViewType) == null) {
            generators.append(fixedStickyViewType, generator);
        }
        footerViews.add(info);
        notifyDataSetChanged();
    }

    public boolean hasFooterView(int id) {
        for (int i = 0; i < footerViews.size(); i++) {
            if (footerViews.get(i).id == id) {
                return true;
            }
        }
        return false;
    }

    public Object getItem(int position) {
        if (position < headerViews.size()) {
            return headerViews.get(position);
        }
        if (position >= headerViews.size() && position < headerViews.size() + items.size()) {
            return items.get(position - headerViews.size());
        }
        return footerViews.get(position - headerViews.size() - items.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_CONTENT_VIEW:
                return baseViewBinder.getViewHolder(viewType);
            default:
                return generators.get(viewType).generate();
        }
    }

    public void setNotifyTip(int notifyTip) {
        this.notifyTip = notifyTip;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object obj = getItem(position);
        if (obj instanceof FixedStickyView) {
            onBindHeaderOrFooter(holder, obj);
        } else {
            baseViewBinder.bind(holder, obj, position, notifyTip == position);
        }
        final int itemPosition = position;
        if (holder.getItemViewType() == TYPE_CONTENT_VIEW) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(itemPosition);
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        FixedStickyView view;
        if (position < headerViews.size()) {
            view = headerViews.get(position);
            return view.fixedStickyViewType;
        } else if ((position >= headerViews.size() && position < headerViews.size() + items.size())) {
            return TYPE_CONTENT_VIEW;
        } else {
            view = footerViews.get(position - headerViews.size() - items.size());
            return view.fixedStickyViewType;
        }
    }

    @Override
    public int getItemCount() {
        return items.size() + headerViews.size() + footerViews.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnEventClickListener(OnEventClickListener onEventClickListener) {
        this.onEventClickListener = onEventClickListener;
    }

    protected abstract void onBindHeaderOrFooter(RecyclerView.ViewHolder holder, Object object);

    public static class FixedStickyView {
        public int    id;
        public int    viewType;
        public int    fixedStickyViewType;
        public int    layoutId;
        public Object object;
    }

    public static class FixedViewHoldGenerator {
        public RecyclerView.ViewHolder generate() {
            return null;
        }
    }
}
