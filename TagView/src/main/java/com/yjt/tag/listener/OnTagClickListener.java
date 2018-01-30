package com.yjt.tag.listener;

import android.view.View;

import com.yjt.tag.layout.FlowLayout;

public interface OnTagClickListener {

    boolean onTagClick(View view, int position, FlowLayout parent);
}
