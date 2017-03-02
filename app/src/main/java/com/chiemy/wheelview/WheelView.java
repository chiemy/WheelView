package com.chiemy.wheelview;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created: chiemy
 * Date: 17/3/1
 * Description:
 */

public class WheelView extends FrameLayout {
    private static final int DEFAULT_OFFSET = 1;
    // 偏移量
    private int mOffset = DEFAULT_OFFSET;
    private int mDisplayItemCount;
    private RecyclerView mRecyclerView;
    private WrapAdapter mWrapAdapter;
    private int mItemHeight;
    private OnWheelChangedListener mOnWheelChangedListener;
    private LinearLayoutManager mLayoutManager;

    private int mSelectedPosition;

    public WheelView(Context context) {
        super(context);
        init();
    }

    public WheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WheelView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mRecyclerView = new RecyclerView(getContext());
        addView(mRecyclerView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int firstVisiblePosition = mLayoutManager.findFirstCompletelyVisibleItemPosition();
                    if (mSelectedPosition == firstVisiblePosition) {
                        return;
                    }
                    mSelectedPosition = firstVisiblePosition;
                    if (mOnWheelChangedListener != null) {
                        mOnWheelChangedListener.onSelected(WheelView.this, mSelectedPosition);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        mRecyclerView.addOnItemTouchListener(new WheelItemTouchListener(getContext(), mRecyclerView) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                int position = vh.getAdapterPosition();
                setSelectedPosition(vh.getAdapterPosition() - mOffset);
                if (position > mSelectedPosition) {
                } else {
                }
            }
        });

        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);

        mDisplayItemCount = mOffset * 2 + 1;
    }

    private static abstract class WheelItemTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector mGestureDetector;

        WheelItemTouchListener(Context context, final RecyclerView recyclerView) {
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null) {
                        RecyclerView.ViewHolder vh = recyclerView.getChildViewHolder(child);
                        onItemClick(vh);
                        return true;
                    }
                    return false;
                }
            });

        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            mGestureDetector.onTouchEvent(e);
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            mGestureDetector.onTouchEvent(e);
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

        public abstract void onItemClick(RecyclerView.ViewHolder vh);

    }

    public void setAdapter(WheelAdapter adapter) {
        if (adapter == null) {
            return;
        }

        mItemHeight = getViewMeasuredHeight(adapter.onCreateItemView(mRecyclerView));
        int wheelHeight = mItemHeight * mDisplayItemCount;

        setWheelHeight(wheelHeight);

        mWrapAdapter = new WrapAdapter(adapter, mItemHeight, mOffset);
        mRecyclerView.setAdapter(mWrapAdapter);
        setSelectedPosition(mSelectedPosition);
    }

    private int getViewMeasuredHeight(View view) {
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, View.MeasureSpec.AT_MOST);
        view.measure(width, expandSpec);
        return view.getMeasuredHeight();
    }

    private void setWheelHeight(int wheelHeight) {
        LayoutParams params = (LayoutParams) mRecyclerView.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.MATCH_PARENT, wheelHeight);
            mRecyclerView.setLayoutParams(params);
        } else {
            params.width = LayoutParams.MATCH_PARENT;
            params.height = wheelHeight;
        }
    }

    /**
     * 显示个数
     *
     * @param offset
     */
    public void setDisplayOffset(int offset) {
        mOffset = offset;
        mDisplayItemCount = mOffset * 2 + 1;
        if (mWrapAdapter != null) {
            setWheelHeight(mItemHeight * mDisplayItemCount);
            mWrapAdapter.setOffset(mOffset);
        }
    }

    public void setOnWheelChangedListener(OnWheelChangedListener onWheelChangedListener) {
        mOnWheelChangedListener = onWheelChangedListener;
    }

    public void setSelectedPosition(int selectedPosition) {
        if (mLayoutManager.getItemCount() == 0
                || selectedPosition < 0
                || selectedPosition >= mLayoutManager.getItemCount()) {
            return;
        }
        int distance = (selectedPosition - mSelectedPosition) * mItemHeight;
        mSelectedPosition = selectedPosition;

        mRecyclerView.smoothScrollBy(0, distance);
        if (mOnWheelChangedListener != null) {
            mOnWheelChangedListener.onSelected(WheelView.this, mSelectedPosition);
        }
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public interface OnWheelChangedListener {
        void onSelected(WheelView wheelView, int position);
    }

    private static class WrapAdapter extends RecyclerView.Adapter<WheelViewHolder> {
        private static final int TYPE_PLACEHOLDER = 1;
        private static final int TYPE_ITEM = 2;

        private WheelAdapter mWheelAdapter;
        private int mOffset;
        private int mItemHeight;

        WrapAdapter(WheelAdapter adapter, int itemHeight, int offset) {
            mWheelAdapter = adapter;
            mWheelAdapter.setDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    notifyDataSetChanged();
                }
            });
            mItemHeight = itemHeight;
            mOffset = offset;
        }

        public void setOffset(int offset) {
            mOffset = offset;
            this.notifyDataSetChanged();
        }

        @Override
        public WheelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            WheelViewHolder vh;
            if (viewType == TYPE_PLACEHOLDER) {
                vh = new WheelViewHolder(createPlaceHolderView(parent.getContext(), mItemHeight));
            } else {
                View view = mWheelAdapter.onCreateItemView(parent);
                vh = new WheelViewHolder(view);
            }
            return vh;
        }

        @Override
        public void onBindViewHolder(WheelViewHolder holder, int position) {
            if (holder.getItemViewType() == TYPE_ITEM) {
                mWheelAdapter.onBindView(holder.itemView, position - mOffset);
            }
        }

        @Override
        public int getItemCount() {
            return mOffset * 2 + mWheelAdapter.getCount();
        }

        @Override
        public int getItemViewType(int position) {
            int type;
            if (position < mOffset
                    || position >= (mOffset + mWheelAdapter.getCount())) {
                type = TYPE_PLACEHOLDER;
            } else {
                type = TYPE_ITEM;
            }
            return type;
        }

        private View createPlaceHolderView(Context context, int height) {
            View space = new View(context);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            space.setLayoutParams(lp);
            return space;
        }

    }

    private static class WheelViewHolder extends RecyclerView.ViewHolder {

        public WheelViewHolder(View itemView) {
            super(itemView);
        }

    }
}
