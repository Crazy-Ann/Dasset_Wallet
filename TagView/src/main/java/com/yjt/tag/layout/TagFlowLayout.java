package com.yjt.tag.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.yjt.tag.R;
import com.yjt.tag.TagAdapter;
import com.yjt.tag.TagView;
import com.yjt.tag.constant.Constant;
import com.yjt.tag.listener.OnTagClickListener;
import com.yjt.tag.listener.OnTagDataChangedListener;
import com.yjt.tag.listener.OnTagSelectedPositionsListener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TagFlowLayout extends FlowLayout implements OnTagDataChangedListener {

    private TagAdapter tagAdapter;
    private int selectedCount = -1;
    private Set<Integer> selectedPositions = new HashSet<>();
    private OnTagSelectedPositionsListener onTagSelectedPositionsListener;
    private OnTagClickListener onTagClickListener;

    public TagFlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TagFlowLayout);
        this.selectedCount = typedArray.getInt(R.styleable.TagFlowLayout_max_selected_count, -1);
        typedArray.recycle();
    }

    public TagFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagFlowLayout(Context context) {
        this(context, null);
    }

    public void setOnTagSelectedPositionsListener(OnTagSelectedPositionsListener onTagSelectedPositionsListener) {
        this.onTagSelectedPositionsListener = onTagSelectedPositionsListener;
    }

    public void setOnTagClickListener(OnTagClickListener onTagClickListener) {
        this.onTagClickListener = onTagClickListener;
    }

    public TagAdapter getAdapter() {
        return tagAdapter;
    }

    public void setSelectedCount(int count) {
        if (this.selectedPositions.size() > count) {
            this.selectedPositions.clear();
        }
        this.selectedCount = count;
    }

    public Set<Integer> getSelectedPositions() {
        return this.selectedPositions;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < getChildCount(); i++) {
            TagView tagView = (TagView) getChildAt(i);
            if (tagView.getVisibility() == View.GONE) {
                continue;
            }
            if (tagView.getTagView().getVisibility() == View.GONE) {
                tagView.setVisibility(View.GONE);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setAdapter(TagAdapter tagAdapter) {
        this.tagAdapter = tagAdapter;
        this.tagAdapter.setOnDataChangedListener(this);
        this.selectedPositions.clear();
        changeAdapter();
    }

    private void changeAdapter() {
        removeAllViews();
        TagAdapter tagAdapter = this.tagAdapter;
        TagView tagView;
        HashSet selectedPosition = this.tagAdapter.getSelectedPositions();
        for (int i = 0; i < tagAdapter.getCount(); i++) {
            View view = tagAdapter.getView(this, i, tagAdapter.getItem(i));
            tagView = new TagView(getContext());
            view.setDuplicateParentStateEnabled(true);
            if (view.getLayoutParams() != null) {
                tagView.setLayoutParams(view.getLayoutParams());
            } else {
                MarginLayoutParams marginLayoutParams = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                marginLayoutParams.setMargins(dip2px(getContext(), 5), dip2px(getContext(), 5), dip2px(getContext(), 5), dip2px(getContext(), 5));
                tagView.setLayoutParams(marginLayoutParams);
            }
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            view.setLayoutParams(layoutParams);
            tagView.addView(view);
            addView(tagView);
            if (selectedPosition.contains(i)) {
                setChildSelected(i, tagView, true);
            }
            if (this.tagAdapter.setSelected(i, tagAdapter.getItem(i))) {
                setChildSelected(i, tagView, true);
            }
            view.setClickable(false);
            final TagView finalTagView = tagView;
            final int finalI = i;
            tagView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    select(finalTagView, finalI);
                    if (onTagClickListener != null) {
                        onTagClickListener.onTagClick(finalTagView, finalI, TagFlowLayout.this);
                    }
                }
            });
        }
        this.selectedPositions.addAll(selectedPosition);
    }

    private void setChildSelected(int position, TagView view, boolean isSelected) {
        view.setChecked(isSelected);
        if (isSelected) {
            this.tagAdapter.onSelected(position, view.getTagView());
        } else {
            this.tagAdapter.unSelected(position, view.getTagView());
        }
    }

    private void select(TagView child, int position) {
        if (!child.isChecked()) {
            if (selectedCount == 1 && selectedPositions.size() == 1) {
                Iterator<Integer> iterator = selectedPositions.iterator();
                Integer selectedPosition = iterator.next();
                TagView tagView = (TagView) getChildAt(selectedPosition);
                setChildSelected(selectedPosition, tagView, false);
                setChildSelected(position, child, true);
                this.selectedPositions.remove(selectedPosition);
                this.selectedPositions.add(position);
            } else {
                if (selectedCount > 0 && selectedPositions.size() >= selectedCount) {
                    return;
                }
                setChildSelected(position, child, true);
                selectedPositions.add(position);
            }
        } else {
            setChildSelected(position, child, false);
            selectedPositions.remove(position);
        }
        if (onTagSelectedPositionsListener != null) {
            onTagSelectedPositionsListener.onTagSelectedPositions(selectedPositions);
        }
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constant.DEFAULT, super.onSaveInstanceState());
        StringBuilder stringBuilder = new StringBuilder();
        if (selectedPositions.size() > 0) {
            for (int selectedPosition : selectedPositions) {
                stringBuilder.append(selectedPosition).append("|");
            }
            stringBuilder = new StringBuilder(stringBuilder.substring(0, stringBuilder.length() - 1));
        }
        bundle.putString(Constant.SELECTED_POSITION, stringBuilder.toString());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            String selectedPositions = bundle.getString(Constant.SELECTED_POSITION);
            if (!TextUtils.isEmpty(selectedPositions)) {
                for (String selectedPosition : selectedPositions.split("\\|")) {
                    int position = Integer.parseInt(selectedPosition);
                    this.selectedPositions.add(position);
                    TagView tagView = (TagView) getChildAt(position);
                    if (tagView != null) {
                        setChildSelected(position, tagView, true);
                    }
                }
            }
            super.onRestoreInstanceState(bundle.getParcelable(Constant.DEFAULT));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void onTagDataChange() {
        this.selectedPositions.clear();
        changeAdapter();
    }
}
