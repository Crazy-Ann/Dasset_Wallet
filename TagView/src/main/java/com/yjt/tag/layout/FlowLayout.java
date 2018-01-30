package com.yjt.tag.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.yjt.tag.R;
import com.yjt.tag.constant.Constant;

import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {

    protected List<List<View>> allViewa = new ArrayList<>();
    private List<View> views = new ArrayList<>();
    protected List<Integer> lineHeight = new ArrayList<>();
    protected List<Integer> lineWidth = new ArrayList<>();
    private int gravity;

    public FlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TagFlowLayout);
        gravity = typedArray.getInt(R.styleable.TagFlowLayout_tag_gravity, Constant.TAG_GRAVITY_LEFT);
        typedArray.recycle();
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context) {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        // wrap_content
        int width = 0;
        int height = 0;
        int lineWidth = 0;
        int lineHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                if (i == getChildCount() - 1) {
                    width = Math.max(lineWidth, width);
                    height += lineHeight;
                }
                continue;
            }
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams marginLayoutParams = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin;
            int childHeight = child.getMeasuredHeight() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin;
            if (lineWidth + childWidth > sizeWidth - getPaddingLeft() - getPaddingRight()) {
                width = Math.max(width, lineWidth);
                lineWidth = childWidth;
                height += lineHeight;
                lineHeight = childHeight;
            } else {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
            if (i == getChildCount() - 1) {
                width = Math.max(lineWidth, width);
                height += lineHeight;
            }
        }
        setMeasuredDimension(modeWidth == MeasureSpec.EXACTLY ? sizeWidth : width + getPaddingLeft() + getPaddingRight(), modeHeight == MeasureSpec.EXACTLY ? sizeHeight : height + getPaddingTop() + getPaddingBottom());
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.allViewa.clear();
        this.lineHeight.clear();
        this.lineWidth.clear();
        this.views.clear();
        int width = getWidth();
        int lineWidth = 0;
        int lineHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            MarginLayoutParams marginLayoutParams = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            if (childWidth + lineWidth + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin > width - getPaddingLeft() - getPaddingRight()) {
                this.lineHeight.add(lineHeight);
                this.allViewa.add(views);
                this.lineWidth.add(lineWidth);
                lineWidth = 0;
                lineHeight = childHeight + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin;
                this.views = new ArrayList<>();
            }
            lineWidth += childWidth + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin);
            this.views.add(child);
        }
        this.lineHeight.add(lineHeight);
        this.lineWidth.add(lineWidth);
        this.allViewa.add(views);
        int left = getPaddingLeft();
        int top = getPaddingTop();
        for (int i = 0; i < this.allViewa.size(); i++) {
            this.views = this.allViewa.get(i);
            lineHeight = this.lineHeight.get(i);
            // set gravity
            switch (this.gravity) {
                case Constant.TAG_GRAVITY_LEFT:
                    left = getPaddingLeft();
                    break;
                case Constant.TAG_GRAVITY_CENTER:
                    left = (width - this.lineWidth.get(i)) / 2 + getPaddingLeft();
                    break;
                case Constant.TAG_GRAVITY_RIGHT:
                    left = width - this.lineWidth.get(i) + getPaddingLeft();
                    break;
                default:
                    break;
            }
            for (int j = 0; j < this.views.size(); j++) {
                View child = this.views.get(j);
                if (child.getVisibility() == View.GONE) {
                    continue;
                }
                MarginLayoutParams marginLayoutParams = (MarginLayoutParams) child.getLayoutParams();
                int lc = left + marginLayoutParams.leftMargin;
                int tc = top + marginLayoutParams.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();
                child.layout(lc, tc, rc, bc);
                left += child.getMeasuredWidth() + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin;
            }
            top += lineHeight;
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }
}
