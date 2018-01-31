package com.yjt.tag;

import android.view.View;

import com.yjt.tag.layout.FlowLayout;
import com.yjt.tag.listener.OnTagDataChangedListener;
import com.yjt.tag.listener.OnTagSelectedPositionListener;
import com.yjt.tag.listener.OnTagUnselectedPositionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TagAdapter<T> {

    private List<T> tagDatas;
    private OnTagDataChangedListener onTagDataChangedListener;
    private OnTagSelectedPositionListener onTagSelectedPositionListener;
    private OnTagUnselectedPositionListener onTagUnselectedPositionListener;
    private HashSet<Integer> selectedPositions = new HashSet<>();

    public TagAdapter(List<T> tagDatas) {
        this.tagDatas = tagDatas;
    }

    @Deprecated
    public TagAdapter(T[] datas) {
        tagDatas = new ArrayList<>(Arrays.asList(datas));
    }

    public void setOnDataChangedListener(OnTagDataChangedListener onTagDataChangedListener) {
        this.onTagDataChangedListener = onTagDataChangedListener;
    }

    public void setOnTagSelectedPositionListener(OnTagSelectedPositionListener onTagSelectedPositionListener) {
        this.onTagSelectedPositionListener = onTagSelectedPositionListener;
    }

    public void setOnTagUnselectedPositionListener(OnTagUnselectedPositionListener onTagUnselectedPositionListener) {
        this.onTagUnselectedPositionListener = onTagUnselectedPositionListener;
    }

    public void setSelectedPositions(int... selectedPositions) {
        Set<Integer> positions = new HashSet<>();
        for (int position : selectedPositions) {
            positions.add(position);
        }
        setSelectedPositions(positions);
    }

    public void setSelectedPositions(Set<Integer> positions) {
        selectedPositions.clear();
        if (positions != null) {
            selectedPositions.addAll(positions);
        }
        notifyDataChanged();
    }

    public HashSet<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    public int getCount() {
        return tagDatas == null ? 0 : tagDatas.size();
    }

    public void notifyDataChanged() {
        if (onTagDataChangedListener != null)
            onTagDataChangedListener.onTagDataChange();
    }

    public T getItem(int position) {
        return tagDatas.get(position);
    }

    public abstract View getView(FlowLayout parent, int position, T t);

    public void onSelected(int position, View view) {
        if (onTagSelectedPositionListener != null) {
            onTagSelectedPositionListener.onTagSelectedPosition(position, view);
        }
    }

    public void unSelected(int position, View view) {
        if (onTagUnselectedPositionListener != null) {
            onTagUnselectedPositionListener.onTagUnselectedPosition(position, view);
        }
    }

    public boolean setSelected(int position, T t) {
        return false;
    }
}
