package com.mlethe.library.recyclerview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;

import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * Created by Mlethe on 2018/12/28.
 * 添加头部 、添加底部、设置emptyView、errorView、loadingView
 */
public abstract class WrapAdapter<T> extends BaseAdapter<T, RecyclerView.ViewHolder> {

    private static final int BASE_ITEM_TYPE_HEADER = Integer.MAX_VALUE - 20000;
    private static final int BASE_ITEM_TYPE_FOOTER = Integer.MAX_VALUE - 10000;
    public static final int EMPTY_VIEW = Integer.MAX_VALUE - 3;

    private FrameLayout mEmptyLayout;
    private boolean mIsUseEmpty = true;

    private SparseArrayCompat<View> mHeaderViews = new SparseArrayCompat<>();
    private SparseArrayCompat<View> mFooterViews = new SparseArrayCompat<>();

    public WrapAdapter(Context context, List<T> data, int layoutId) {
        super(context, data, layoutId);
    }

    public WrapAdapter(Context context, List<T> data, MultiTypeSupport<T> multiTypeSupport) {
        super(context, data, multiTypeSupport);
    }

    /**
     * 根据当前位置获取不同的viewType
     */
    @Override
    public int getItemViewType(int position) {
        if (getEmptyViewCount() == 1) {
            return EMPTY_VIEW;
        } else if (isHeaderViewPos(position)) {
            return mHeaderViews.keyAt(position);
        } else if (isFooterViewPos(position)) {
            return mFooterViews.keyAt(position - mHeaderViews.size() - getRealItemCount());
        }
        return super.getItemViewType(position - mHeaderViews.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == EMPTY_VIEW) {
            ViewHolder holder = new ViewHolder(mEmptyLayout);
            return holder;
        } else if (mHeaderViews.get(viewType) != null) {
            SimpleViewHolder holder = new SimpleViewHolder(mHeaderViews.get(viewType));
            return holder;
        } else if (mFooterViews.get(viewType) != null) {
            SimpleViewHolder holder = new SimpleViewHolder(mFooterViews.get(viewType));
            return holder;
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    protected ViewHolder createViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    public final void convert(RecyclerView.ViewHolder holder, T item, int position) {
        convert((ViewHolder) holder, item, position);
    }

    /**
     * 利用抽象方法回传出去，每个不一样的Adapter去设置
     *
     * @param item 当前的数据
     */
    public abstract void convert(ViewHolder holder, T item, int position);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == EMPTY_VIEW || isHeaderViewPos(position) || isFooterViewPos(position)) {
            return;
        }
        int realPos = position - mHeaderViews.size();
        super.onBindViewHolder(holder, realPos);
    }

    @Override
    public int getItemCount() {
        int count;
        if (getEmptyViewCount() == 1) {
            count = 1;
        } else {
            count = mHeaderViews.size() + getRealItemCount() + mFooterViews.size();
        }
        return count;
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int viewType = getItemViewType(position);
                    if (viewType == EMPTY_VIEW) {
                        return gridLayoutManager.getSpanCount();
                    }
                    if (spanSizeLookup != null) {
                        return spanSizeLookup.getSpanSize(position);
                    }
                    return 1;
                }
            });
            gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount());
        }

    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (holder.getItemViewType() == EMPTY_VIEW) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }

    @Override
    public void release() {
        super.release();
        mHeaderViews.clear();
        mFooterViews.clear();
        mEmptyLayout = null;
    }

    /**
     * 设置emptyView、errorView、loadingView
     *
     * @param layoutResId
     * @param viewGroup
     */
    public void setEmptyView(int layoutResId, ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutResId, viewGroup, false);
        setEmptyView(view);
    }

    /**
     * 设置emptyView、errorView、loadingView
     * @param layoutResId
     * @param viewGroup
     * @param listener
     */
    public void setEmptyView(int layoutResId, ViewGroup viewGroup, View.OnClickListener listener) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutResId, viewGroup, false);
        view.setOnClickListener(listener);
        setEmptyView(view);
    }

    /**
     * 设置emptyView、errorView、loadingView
     *
     * @param emptyView
     */
    public void setEmptyView(View emptyView) {
        boolean insert = false;
        if (mEmptyLayout == null) {
            mEmptyLayout = new FrameLayout(emptyView.getContext());
            final RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
            final ViewGroup.LayoutParams lp = emptyView.getLayoutParams();
            if (lp != null) {
                layoutParams.width = lp.width;
                layoutParams.height = lp.height;
            }
            mEmptyLayout.setLayoutParams(layoutParams);
            insert = true;
        }
        mEmptyLayout.removeAllViews();
        mEmptyLayout.addView(emptyView);
        mIsUseEmpty = true;
        if (insert) {
            if (getEmptyViewCount() == 1) {
                notifyItemInserted(0);
            }
        }
    }

    /**
     * Set whether to use empty view
     *
     * @param isUseEmpty
     */
    public void isUseEmpty(boolean isUseEmpty) {
        mIsUseEmpty = isUseEmpty;
    }

    /**
     * if show empty view will be return 1 or not will be return 0
     *
     * @return
     */
    public int getEmptyViewCount() {
        if (mEmptyLayout == null || mEmptyLayout.getChildCount() == 0) {
            return 0;
        }
        if (!mIsUseEmpty) {
            return 0;
        }
        if (getRealItemCount() != 0) {
            return 0;
        }
        return 1;
    }

    /**
     * 添加头部
     *
     * @param view
     */
    public WrapAdapter addHeaderView(View view) {
        mHeaderViews.put(mHeaderViews.size() + BASE_ITEM_TYPE_HEADER, view);
        notifyItemInserted(mHeaderViews.size() - 1);
        return this;
    }

    /**
     * 添加底部
     *
     * @param view
     */
    public WrapAdapter addFooterView(View view) {
        mFooterViews.put(mFooterViews.size() + BASE_ITEM_TYPE_FOOTER, view);
        notifyItemInserted(mHeaderViews.size() + getRealItemCount() + mFooterViews.size() - 1);
        return this;
    }

    /**
     * 移除头部
     *
     * @param view
     */
    public void removeHeaderView(View view) {
        int index = mHeaderViews.indexOfValue(view);
        if (index < 0) return;
        mHeaderViews.removeAt(index);
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, getItemCount() - index);
    }

    /**
     * 移除底部
     *
     * @param view
     */
    public void removeFooterView(View view) {
        int index = mFooterViews.indexOfValue(view);
        if (index < 0) return;
        mFooterViews.removeAt(index);
        int position = mHeaderViews.size() + getRealItemCount() + index - 1;
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount() - position);
    }

    /**
     * 判断是不是头部
     *
     * @param position
     * @return
     */
    private boolean isHeaderViewPos(int position) {
        int size = mHeaderViews.size();
        if (size == 0) {
            return false;
        }
        return position >= 0 && position < size;
    }

    /**
     * 判断是不底部
     *
     * @param position
     * @return
     */
    private boolean isFooterViewPos(int position) {
        int size = mFooterViews.size();
        if (size == 0) {
            return false;
        }
        return position >= mHeaderViews.size() + getRealItemCount() && position < mHeaderViews.size() + getRealItemCount() + size;
    }

    private class SimpleViewHolder extends RecyclerView.ViewHolder {
        public SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }

}
