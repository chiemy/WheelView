package com.chiemy.wheelview;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created: chiemy
 * Date: 17/3/1
 * Description:
 */

public abstract class WheelAdapter<T> {
    private List<T> mData;
    private DataSetObserver mDataObserver;

    public void setData(List<T> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public List<T> getData() {
        return mData;
    }

    public T getItem(int position) {
        return mData != null ? mData.get(position) : null;
    }

    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    public abstract View onCreateItemView(ViewGroup parent);

    public abstract void onBindView(View view, int position);

    public void notifyDataSetChanged() {
        if (mDataObserver != null) {
            mDataObserver.onChanged();
        }
    }

    public void setDataSetObserver(DataSetObserver observer) {
        mDataObserver = observer;
    }

}
