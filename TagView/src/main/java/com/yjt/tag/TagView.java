package com.yjt.tag;

import android.content.Context;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;

public class TagView extends FrameLayout implements Checkable {

    private boolean isSelected;
    private static final int[] CHECK_STATE = new int[]{android.R.attr.state_checked};

    public TagView(Context context) {
        super(context);
    }

    public View getTagView() {
        return getChildAt(0);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        int[] states = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(states, CHECK_STATE);
        }
        return states;
    }

    @Override
    public void setChecked(boolean selected) {
        if (this.isSelected != selected) {
            this.isSelected = selected;
            refreshDrawableState();
        }
    }

    @Override
    public boolean isChecked() {
        return isSelected;
    }

    @Override
    public void toggle() {
        setChecked(!isSelected);
    }
}
