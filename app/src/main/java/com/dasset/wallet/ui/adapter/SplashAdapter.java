package com.dasset.wallet.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dasset.wallet.components.utils.GlideUtil;
import com.dasset.wallet.components.utils.LogUtil;

import java.util.List;

public class SplashAdapter<T extends View> extends PagerAdapter {

    private Context context;
    private List<ArrayMap<String, T>> arrayMaps;

    public SplashAdapter(Context context, List<ArrayMap<String, T>> arrayMaps) {
        super();
        this.context = context;
        this.arrayMaps = arrayMaps;
    }

    @Override
    public int getCount() {
        return arrayMaps.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = (ImageView) arrayMaps.get(position).valueAt(position);
        LogUtil.getInstance().print("url:" + arrayMaps.get(position).keyAt(position));
        LogUtil.getInstance().print("view:" + imageView);
        GlideUtil.getInstance().with(context, arrayMaps.get(position).keyAt(position), null, null, DiskCacheStrategy.NONE, imageView);
        container.addView(imageView);
        return imageView;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}
