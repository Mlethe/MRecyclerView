package com.mlethe.library.recyclerview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.mlethe.library.recyclerview.adapter.BaseAdapter;
import com.mlethe.library.recyclerview.load.BaseLoadCreator;
import com.mlethe.library.recyclerview.load.DefaultLoadCreator;
import com.mlethe.library.recyclerview.refresh.BaseRefreshCreator;
import com.mlethe.library.recyclerview.refresh.DefaultRefreshCreator;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.collection.SparseArrayCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;


public class MRecyclerView extends RecyclerView {

    private static final int BASE_ITEM_TYPE_HEADER = Integer.MAX_VALUE - 20000;
    private static final int BASE_ITEM_TYPE_FOOTER = Integer.MAX_VALUE - 10000;

    private SparseArrayCompat<View> mHeaderViews = new SparseArrayCompat<>();
    private SparseArrayCompat<View> mFooterViews = new SparseArrayCompat<>();

    // 刷新view
    private static final int REFRESH_VIEW = Integer.MAX_VALUE - 1;
    private OnRefreshListener mRefreshListener;
    private LinearLayout mRefreshHeader;
    private boolean isRefreshEnabled = true;
    private BaseRefreshCreator mRefreshCreator;
    private int mMeasuredHeight;
    private float mLastY = -1;
    private static final float DRAG_RATE = 3;
    // 默认状态
    public static final int STATE_NORMAL = 0x0011;
    // 松开刷新状态
    public static final int STATE_RELEASE_TO_REFRESH = 0x0022;
    // 正在刷新状态
    public static final int STATE_REFRESHING = 0x0033;
    // 刷新完成
    public static final int STATE_DONE = 0x0044;
    // 刷新失败
    public static final int STATE_FAIL = 0x0055;
    // 当前状态
    private int mState = STATE_NORMAL;

    // 加载更多view
    private static final int LOADING_VIEW = Integer.MAX_VALUE - 2;
    // 加载更多初始值  isLoadDisabled-是否有加载更多的能力  false 否 true 是
    private boolean isLoadDisabled = true, isLoading = false, isLoadNoData = false, isLoadFail = false;
    private BaseLoadCreator mLoadViewCreator;
    //    private View mLoadView;
    private OnLoadListener mLoadListener;

    private WrapAdapter mWrapAdapter;

    // 父布局
    private ViewGroup mParent;
    private FrameLayout mLayout;
    // 空数据、加载、加载失败view
    private View mEmptyView, mLoadingView, mErrorView;
    private final AdapterDataObserver mDataObserver = new DataObserver();
    private AppBarStateChangeListener.State appbarState = AppBarStateChangeListener.State.EXPANDED;

    // limit number to call load more
    // 控制多出多少条的时候调用 onLoadMore
    private int limitNumberToCallLoadMore = 1;

    /**
     * 是不是手动刷新  true 是， false 不是
     */
    private boolean isManualRefresh = false;

    public MRecyclerView(Context context) {
        this(context, null);
    }

    public MRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (isRefreshEnabled) {
            if (mRefreshCreator == null) {
                mRefreshCreator = new DefaultRefreshCreator();
            }
            mRefreshHeader = new LinearLayout(getContext());
            initRefreshView(mRefreshCreator);
        }
        mLoadViewCreator = new DefaultLoadCreator();
    }

    // set the number to control call load more,see the demo on linearActivity
    public void setLimitNumberToCallLoadMore(int limitNumberToCallLoadMore) {
        this.limitNumberToCallLoadMore = limitNumberToCallLoadMore;
    }

    private void initRefreshView(BaseRefreshCreator refreshViewCreator) {
        // 初始情况，设置下拉刷新view高度为0
        View mContainer = refreshViewCreator.getRefreshView(getContext(), null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        mRefreshHeader.setLayoutParams(lp);
        mRefreshHeader.setPadding(0, 0, 0, 0);
        mRefreshHeader.removeAllViews();
        // 设置下拉刷新view高度为0
        mRefreshHeader.addView(mContainer, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0));
        mRefreshHeader.setGravity(Gravity.BOTTOM);
        mRefreshHeader.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mMeasuredHeight = mRefreshHeader.getMeasuredHeight();
    }

    /**
     * call it when you finish the activity,
     * when you call this,better don't call some kind of functions like
     * RefreshHeader,because the reference of mRefreshHeader is NULL.
     */
    private void release() {
        mRefreshHeader = null;
        if (mRefreshCreator != null) {
            mRefreshCreator.destroy();
            mRefreshCreator = null;
        }
        if (mHeaderViews != null) {
            mHeaderViews.clear();
        }
        if (mFooterViews != null) {
            mFooterViews.clear();
        }
        if (mLoadViewCreator != null) {
            mLoadViewCreator.destroy();
            mLoadViewCreator = null;
        }
        setAdapter(null);
        mParent = null;
        mLayout = null;
        mEmptyView = null;
        mLoadingView = null;
        mErrorView = null;
    }

    /**
     * 设置是否支持刷新  false 否 true 是
     *
     * @param enabled
     */
    public MRecyclerView setRefreshEnabled(boolean enabled) {
        isRefreshEnabled = enabled;
        return this;
    }

    /**
     * 设置刷新处理类
     *
     * @param refreshViewCreator
     */
    public MRecyclerView setRefreshViewCreator(BaseRefreshCreator refreshViewCreator) {
        this.mRefreshCreator = refreshViewCreator;
        if (mRefreshCreator != null && mRefreshHeader != null) {
            initRefreshView(mRefreshCreator);
        }
        return this;
    }

    /**
     * 正在刷新
     */
    public void refresh() {
        if (isRefreshEnabled && mRefreshListener != null) {
            isManualRefresh = true;
            setState(STATE_REFRESHING);
        }
    }

    /**
     * 刷新完成
     */
    public void refreshComplete() {
        refreshComplete(STATE_DONE);
    }

    /**
     * 刷新失败
     */
    public void refreshFail() {
        refreshComplete(STATE_FAIL);
    }

    private void refreshComplete(int state) {
        setState(state);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                reset();
            }
        }, 200);
    }

    /**
     * 加载完成
     */
    public void loadMoreComplete() {
        isLoading = false;
        if (mLoadViewCreator != null)
            mLoadViewCreator.onLoadStop();
    }

    public void disable() {
        isLoadDisabled = false;
    }

    private void setNoMore(boolean noMore) {
        isLoading = false;
        isLoadFail = false;
        isLoadNoData = noMore;
        if (noMore) {
            mLoadViewCreator.setNoData();
        }
    }

    /**
     * 加载中
     */
    private void loading() {
        isLoadFail = false;
        isLoadNoData = false;
        isLoading = true;
        if (mLoadViewCreator != null)
            mLoadViewCreator.onLoading();
    }

    /**
     * 加载失败
     */
    public void loadFail() {
        isLoadFail = true;
        isLoadNoData = false;
        isLoading = false;
        if (mLoadViewCreator != null)
            mLoadViewCreator.onLoadFail();
    }

    /**
     * 加载没有数据
     */
    public void setNoData() {
        setNoMore(true);
    }

    public MRecyclerView setLoadEnabled(boolean enabled) {
        isLoadDisabled = enabled;
        return this;
    }

    private void resetLoad() {
        isLoadFail = false;
        isLoadNoData = false;
        isLoading = false;
    }

    /**
     * 添加头部
     *
     * @param view
     */
    public MRecyclerView addHeaderView(View view) {
        mHeaderViews.put(mHeaderViews.size() + BASE_ITEM_TYPE_HEADER, view);
        if (mWrapAdapter != null)
            mWrapAdapter.notifyItemInserted(getRefreshCount() + getHeadersCount());
        return this;
    }

    /**
     * 添加底部
     *
     * @param view
     */
    public MRecyclerView addFooterView(View view) {
        mFooterViews.put(mFooterViews.size() + BASE_ITEM_TYPE_FOOTER, view);
        if (mWrapAdapter != null)
            mWrapAdapter.notifyItemInserted(getRefreshCount() + getHeadersCount() + mWrapAdapter.getOriginalAdapter().getItemCount() + getFootersCount());
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
        if (mWrapAdapter != null) {
            int position = getRefreshCount() + index;
            mWrapAdapter.getOriginalAdapter().notifyItemRemoved(position);
            mWrapAdapter.getOriginalAdapter().notifyItemRangeChanged(position, mWrapAdapter.getItemCount() - position);
        }
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
        if (mWrapAdapter != null) {
            int position = getRefreshCount() + getHeadersCount() + mWrapAdapter.getOriginalAdapter().getItemCount() + index;
            mWrapAdapter.getOriginalAdapter().notifyItemRemoved(position);
            mWrapAdapter.getOriginalAdapter().notifyItemRangeChanged(position, mWrapAdapter.getItemCount() - position);
        }
    }

    /**
     * 获取头部数量
     *
     * @return
     */
    public int getHeadersCount() {
        return mHeaderViews.size();
    }

    /**
     * 获取底部数量
     *
     * @return
     */
    public int getFootersCount() {
        return mFooterViews.size();
    }

    /**
     * 获取刷新头部数量
     *
     * @return
     */
    public int getRefreshCount() {
        if (isRefreshEnabled && mRefreshHeader != null) {
            return 1;
        }
        return 0;
    }

    /**
     * 获取加载更多底部数量
     *
     * @return
     */
    public int getLoadingCount() {
        if (isLoadDisabled && mLoadViewCreator != null) {
            return 1;
        }
        return 0;
    }

    public MRecyclerView setEmptyViewOnclickListener(View.OnClickListener onclickListener) {
        mEmptyView.setOnClickListener(onclickListener);
        return this;
    }

    public MRecyclerView setView(View loadingView, View emptyView, View errorView) {
        this.mLoadingView = loadingView;
        this.mEmptyView = emptyView;
        this.mErrorView = errorView;
        return this;
    }

    public MRecyclerView setView(@LayoutRes int loadingLayoutId, @LayoutRes int emptyLayoutId, @LayoutRes int errorLayoutId) {
        this.mLoadingView = add(loadingLayoutId);
        this.mEmptyView = add(emptyLayoutId);
        this.mErrorView = add(errorLayoutId);
        return this;
    }

    public void showLoadingView() {
        if (mLoadingView != null) {
            show(mLoadingView);
        }
    }

    public void showEmptyView() {
        showEmptyView(null);
    }

    public void showEmptyView(View.OnClickListener listener) {
        if (mEmptyView != null) {
            if (listener != null) {
                mEmptyView.setOnClickListener(listener);
            }
            show(mEmptyView);
        }
    }

    public void showErrorView() {
        showErrorView(null);
    }

    public void showErrorView(View.OnClickListener listener) {
        if (mErrorView != null) {
            if (listener != null) {
                mErrorView.setOnClickListener(listener);
            }
            show(mErrorView);
        }
    }

    public View getEmptyView() {
        return mEmptyView;
    }

    public View getLoadingView() {
        return mLoadingView;
    }

    public View getErrorView() {
        return mErrorView;
    }

    /**
     * 添加view
     *
     * @param layoutId
     * @return
     */
    private View add(@LayoutRes int layoutId) {
        if (layoutId != 0) {
            createFrameLayout();
            return LayoutInflater.from(getContext()).inflate(layoutId, mLayout, false);
        }
        return null;
    }

    /**
     * 创建FrameLayout
     */
    private void createFrameLayout() {
        if (mLayout == null) {
            mLayout = new FrameLayout(this.getContext());
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            mLayout.setLayoutParams(layoutParams);
        }
    }

    /**
     * 显示view
     *
     * @param view
     */
    private void show(View view) {
        if (view != null) {
            if (mParent == null) {
                this.mParent = (ViewGroup) this.getParent();
            }
            createFrameLayout();
            mLayout.removeAllViews();
            mLayout.addView(view, 0);
            if (mParent.indexOfChild(mLayout) < 0) {
                mParent.addView(mLayout, 0);
            }
            mLayout.setVisibility(View.VISIBLE);
            this.setVisibility(View.GONE);
        }
    }

    /**
     * 隐藏
     */
    public void hide() {
        if (mLayout != null) {
            mLayout.setVisibility(View.GONE);
            this.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置父布局
     *
     * @param parent
     */
    public MRecyclerView setParent(ViewGroup parent) {
        this.mParent = parent;
        return this;
    }

    public MRecyclerView setOnRefreshListener(OnRefreshListener listener) {
        mRefreshListener = listener;
        return this;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    public MRecyclerView setOnLoadListener(OnLoadListener listener) {
        mLoadListener = listener;
        return this;
    }

    public interface OnLoadListener {
        void onRetry();

        void onLoadMore();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (mWrapAdapter != null) {
            if (adapter != null)
                adapter.unregisterAdapterDataObserver(mDataObserver);
            mWrapAdapter = null;
        }
        mWrapAdapter = new WrapAdapter(adapter);
        super.setAdapter(mWrapAdapter);
        if (adapter != null)
            adapter.registerAdapterDataObserver(mDataObserver);
//        mDataObserver.onChanged();
    }

    //避免用户自己调用getAdapter() 引起的ClassCastException
    @Override
    public Adapter getAdapter() {
        if (mWrapAdapter != null)
            return mWrapAdapter.getOriginalAdapter();
        else
            return null;
    }
/*
    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if(mWrapAdapter != null){
            if (layout instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) layout);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (mWrapAdapter.isHeader(position) || mWrapAdapter.isFooter(position) || mWrapAdapter.isRefreshHeader(position))
                                ? gridManager.getSpanCount() : 1;
                    }
                });
            }
        }
    }*/

    /**
     * ===================== try to adjust the position for XR when you call those functions below ======================
     */
    // which cause "Called attach on a child which is not detached" exception info.
    // {reason analyze @link:http://www.cnblogs.com/linguanh/p/5348510.html}
    // by lgh on 2017-11-13 23:55

    // example: listData.remove(position); You can also see a demo on LinearActivity
    public <T> void notifyItemRemoved(List<T> listData, int position) {
        if (mWrapAdapter.adapter == null)
            return;
        int headerSize = getHeaders_includingRefreshCount();
        int adjPos = position + headerSize;
        mWrapAdapter.adapter.notifyItemRemoved(adjPos);
        mWrapAdapter.adapter.notifyItemRangeChanged(headerSize, listData.size(), new Object());
    }

    public <T> void notifyItemInserted(List<T> listData, int position) {
        if (mWrapAdapter.adapter == null)
            return;
        int headerSize = getHeaders_includingRefreshCount();
        int adjPos = position + headerSize;
        mWrapAdapter.adapter.notifyItemInserted(adjPos);
        mWrapAdapter.adapter.notifyItemRangeChanged(headerSize, listData.size(), new Object());
    }

    public void notifyItemChanged(int position) {
        if (mWrapAdapter.adapter == null)
            return;
        int adjPos = position + getHeaders_includingRefreshCount();
        mWrapAdapter.adapter.notifyItemChanged(adjPos);
    }

    public void notifyItemChanged(int position, Object o) {
        if (mWrapAdapter.adapter == null)
            return;
        int adjPos = position + getHeaders_includingRefreshCount();
        mWrapAdapter.adapter.notifyItemChanged(adjPos, o);
    }

    public int getHeaders_includingRefreshCount() {
        return getHeadersCount() + getRefreshCount();
    }

    /**
     * ======================================================= end =======================================================
     */

    private void reset() {
        smoothScrollTo(0);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                setState(STATE_NORMAL);
            }
        }, 500);
    }

    private void setState(int state) {
        setState(state, 0);
    }

    private void setState(int state, int height) {
//        if (state == mState) return ;
        switch (state) {
            case STATE_NORMAL:  // 下拉刷新
            case STATE_RELEASE_TO_REFRESH:  // 释放立即刷新
                if (mRefreshCreator != null)
                    mRefreshCreator.onPull(height, mMeasuredHeight, state);
                break;
            case STATE_REFRESHING:  // 正在刷新...
                resetLoad();
                mRefreshCreator.onRefreshing();
                smoothScrollTo(mMeasuredHeight);
                if (isManualRefresh) {
                    isManualRefresh = false;
                    if (mRefreshListener != null) {
                        mRefreshListener.onRefresh();
                    }
                }
                break;
            case STATE_DONE:    // 刷新完成
                if (mRefreshCreator != null)
                    mRefreshCreator.onRefreshStop();
                break;
            case STATE_FAIL:    // 刷新失败
                if (mRefreshCreator != null)
                    mRefreshCreator.onRefreshFail();
                break;
            default:
        }
        mState = state;
    }

    /**
     * 回滚到指定位置
     *
     * @param destHeight
     */
    private void smoothScrollTo(int destHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight);
        animator.setDuration(300).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setVisibleHeight((int) animation.getAnimatedValue());
            }
        });
        animator.start();
    }

    /**
     * 设置显示高度
     *
     * @param height
     */
    private void setVisibleHeight(int height) {
        if (height < 0) height = 0;
        if (mRefreshHeader == null)
            return;
        View childView = mRefreshHeader.getChildAt(0);
        ViewGroup.LayoutParams lp = childView.getLayoutParams();
        lp.height = height;
        childView.setLayoutParams(lp);
    }

    /**
     * 获取显示高度
     */
    private int getVisibleHeight() {
        if (mRefreshHeader == null)
            return 0;
        View childView = mRefreshHeader.getChildAt(0);
        ViewGroup.LayoutParams lp = childView.getLayoutParams();
        return lp.height;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                if (isOnTop() && isRefreshEnabled && appbarState == AppBarStateChangeListener.State.EXPANDED) {
                    if (mRefreshHeader == null)
                        break;
                    onMove(deltaY / DRAG_RATE);
                    if (getVisibleHeight() > 0 && (mState == STATE_NORMAL || mState == STATE_RELEASE_TO_REFRESH)) {
                        return false;
                    }
                }
                break;
            default:
                mLastY = -1; // reset
                if (isOnTop() && isRefreshEnabled && appbarState == AppBarStateChangeListener.State.EXPANDED) {
                    if (mRefreshHeader != null && releaseAction()) {
                        if (mRefreshListener != null) {
                            mRefreshListener.onRefresh();
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void onMove(float delta) {
        if (getVisibleHeight() > 0 || delta > 0) {
            int visibleHeight = (int) delta + getVisibleHeight();
            setVisibleHeight(visibleHeight);
            if (mState == STATE_NORMAL || mState == STATE_RELEASE_TO_REFRESH) { // 未处于刷新状态，更新箭头
                if (visibleHeight > mMeasuredHeight) {
                    setState(STATE_RELEASE_TO_REFRESH, visibleHeight);
                } else {
                    setState(STATE_NORMAL, visibleHeight);
                }
            }
        }
    }

    private boolean releaseAction() {
        boolean isOnRefresh = false;
        int height = getVisibleHeight();
        if (height == 0) // not visible.
            isOnRefresh = false;

        if (getVisibleHeight() > mMeasuredHeight && (mState == STATE_NORMAL || mState == STATE_RELEASE_TO_REFRESH)) {
            setState(STATE_REFRESHING);
            isOnRefresh = true;
        }
        // refreshing and header isn't shown fully. do nothing.
        if (mState == STATE_REFRESHING && height <= mMeasuredHeight) {
            //return;
        }
        if (mState != STATE_REFRESHING) {
            smoothScrollTo(0);
        }

        if (mState == STATE_REFRESHING) {
            int destHeight = mMeasuredHeight;
            smoothScrollTo(destHeight);
        }

        return isOnRefresh;
    }

    private boolean isOnTop() {
        if (mRefreshHeader == null)
            return false;
        if (mRefreshHeader.getParent() != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
//        Log.e("TAG", "onScrollStateChanged: state->" + state + "     mLoadListener->" + (mLoadListener != null) + "     isLoading->" + isLoading + "    isLoadDisabled->" +isLoadDisabled);
        if (state == RecyclerView.SCROLL_STATE_IDLE && mLoadListener != null && !isLoading && isLoadDisabled) {
            LayoutManager layoutManager = getLayoutManager();
            int lastVisibleItemPosition;
            if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                lastVisibleItemPosition = findMax(into);
            } else {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }
            int adjAdapterItemCount = layoutManager.getItemCount() + getHeaders_includingRefreshCount() + getLoadingCount();
//            Log.e("aaaaa", "adjAdapterItemCount->" + adjAdapterItemCount + " getItemCount->" + layoutManager.getItemCount() + "   getChildCount->" + layoutManager.getChildCount());

            int status = STATE_DONE;

            if (mRefreshHeader != null) {
                status = mState;
            }
            if (
                    layoutManager.getChildCount() > 0
                            && lastVisibleItemPosition >= adjAdapterItemCount - limitNumberToCallLoadMore
                            && adjAdapterItemCount >= layoutManager.getChildCount()
                            && !isLoadNoData
                            && (status == STATE_NORMAL || status == STATE_RELEASE_TO_REFRESH)
            ) {
                loading();
                mLoadListener.onLoadMore();
            }
        }
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private class DataObserver extends AdapterDataObserver {
        @Override
        public void onChanged() {
            if (mWrapAdapter != null) {
                mWrapAdapter.notifyDataSetChanged();
            }
            dataChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            if (mWrapAdapter != null)
                mWrapAdapter.notifyItemRangeInserted(positionStart + getHeaders_includingRefreshCount(), itemCount);
            dataChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            if (mWrapAdapter != null) {
                if (itemCount > 1) {
                    mWrapAdapter.notifyItemRangeChanged(positionStart + getHeaders_includingRefreshCount(), itemCount + getFootersCount() + getLoadingCount());
                } else {
                    mWrapAdapter.notifyItemRangeChanged(positionStart + getHeaders_includingRefreshCount(), itemCount);
                }
            }
            dataChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            if (mWrapAdapter != null) {
                if (itemCount > 1) {
                    mWrapAdapter.notifyItemRangeChanged(positionStart + getHeaders_includingRefreshCount(), itemCount + getFootersCount() + getLoadingCount(), payload);
                } else {
                    mWrapAdapter.notifyItemRangeChanged(positionStart + getHeaders_includingRefreshCount(), itemCount, payload);
                }
            }
            dataChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            if (mWrapAdapter != null)
                mWrapAdapter.notifyItemRangeRemoved(positionStart + getHeaders_includingRefreshCount(), itemCount);
            dataChanged();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            if (mWrapAdapter != null)
                mWrapAdapter.notifyItemMoved(fromPosition + getHeaders_includingRefreshCount(), toPosition + getHeaders_includingRefreshCount());
            dataChanged();
        }

        private void dataChanged() {
            if (mWrapAdapter != null && mLayout != null) {
                int itemCount = mWrapAdapter.getOriginalAdapter().getItemCount();
                if (itemCount != 0) {
                    hide();
                } else {
                    showEmptyView();
                }
            }
        }
    }

    private class WrapAdapter extends Adapter<ViewHolder> {

        private Adapter adapter;

        public WrapAdapter(Adapter adapter) {
            this.adapter = adapter;
        }

        public Adapter getOriginalAdapter() {
            return this.adapter;
        }

        /**
         * 根据当前位置获取不同的viewType
         */
        @Override
        public int getItemViewType(int position) {
            if (isRefreshViewPos(position)) {
                return REFRESH_VIEW;
            }
            if (isHeaderViewPos(position)) {
                return mHeaderViews.keyAt(position - getRefreshCount());
            }
            if (isFooterViewPos(position)) {
                return mFooterViews.keyAt(position - getRefreshCount() - getHeadersCount() - adapter.getItemCount());
            }
            if (isLoadViewPos(position)) {
                return LOADING_VIEW;
            }
            if (adapter != null)
                return adapter.getItemViewType(position - getHeaders_includingRefreshCount());
            else
                return 0;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == REFRESH_VIEW) {
                ViewHolder holder = new SimpleViewHolder(mRefreshHeader);
                return holder;
            } else if (mHeaderViews.get(viewType) != null) {
                ViewHolder holder = new SimpleViewHolder(mHeaderViews.get(viewType));
                return holder;
            } else if (mFooterViews.get(viewType) != null) {
                ViewHolder holder = new SimpleViewHolder(mFooterViews.get(viewType));
                return holder;
            } else if (viewType == LOADING_VIEW) {
                View loadView = mLoadViewCreator.getLoadView(getContext(), parent);
                ViewHolder holder = new SimpleViewHolder(loadView);
                return holder;
            }
            if (adapter != null)
                return adapter.createViewHolder(parent, viewType);
            else
                return null;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (holder.getItemViewType() == REFRESH_VIEW || isHeaderViewPos(position) || isFooterViewPos(position)) {
                return;
            }
            if (holder.getItemViewType() == LOADING_VIEW) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mLoadListener != null && isLoadFail) {
                            loading();
                            mLoadListener.onRetry();
                        }
                    }
                });
                return;
            }
            int realPos = position - getHeaders_includingRefreshCount();
            if (adapter != null)
                adapter.onBindViewHolder(holder, realPos);
        }

        @Override
        public int getItemCount() {
            return getRefreshCount() + getHeadersCount() + adapter.getItemCount() + getFootersCount() + getLoadingCount();
        }

        @Override
        public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
            LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();

                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        int viewType = getItemViewType(position);
                        if (viewType == REFRESH_VIEW || mHeaderViews.get(viewType) != null || mFooterViews.get(viewType) != null || viewType == LOADING_VIEW) {
                            return gridLayoutManager.getSpanCount();
                        }
                        if (spanSizeLookup != null) {
                            return spanSizeLookup.getSpanSize(position - getHeaders_includingRefreshCount());
                        }
                        return 1;
                    }
                });
                gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount());
            }
            if (adapter != null)
                adapter.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            int position = holder.getLayoutPosition();
            if (holder.getItemViewType() == REFRESH_VIEW || isHeaderViewPos(position) || isFooterViewPos(position) || holder.getItemViewType() == LOADING_VIEW) {
                ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                    StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                    p.setFullSpan(true);
                }
            }
            if (adapter != null)
                adapter.onViewAttachedToWindow(holder);
        }

        @Override
        public long getItemId(int position) {
            if (adapter != null && position >= getHeadersCount() + 1) {
                int adjPosition = position - (getHeadersCount() + 1);
                if (adjPosition < adapter.getItemCount()) {
                    return adapter.getItemId(adjPosition);
                }
            }
            return -1;
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            if (adapter != null)
                adapter.onDetachedFromRecyclerView(recyclerView);
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            if (adapter != null)
                adapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            if (adapter != null)
                adapter.onViewRecycled(holder);
        }

        @Override
        public boolean onFailedToRecycleView(ViewHolder holder) {
            if (adapter != null)
                return adapter.onFailedToRecycleView(holder);
            else
                return false;
        }

        /*@Override
        public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            if (adapter != null)
                adapter.unregisterAdapterDataObserver(observer);
        }

        @Override
        public void registerAdapterDataObserver(AdapterDataObserver observer) {
            if (adapter != null)
                adapter.registerAdapterDataObserver(observer);
        }*/

        private class SimpleViewHolder extends ViewHolder {
            public SimpleViewHolder(View itemView) {
                super(itemView);
            }
        }

        /**
         * 判断是否是refreshView
         *
         * @param position
         * @return
         */
        private boolean isRefreshViewPos(int position) {
            return isRefreshEnabled && mRefreshHeader != null && position == 0;
        }

        /**
         * 判断是不是头部
         *
         * @param position
         * @return
         */
        private boolean isHeaderViewPos(int position) {
            return position >= getRefreshCount() && position < getRefreshCount() + getHeadersCount();
        }

        /**
         * 判断是不底部
         *
         * @param position
         * @return
         */
        private boolean isFooterViewPos(int position) {
            return position >= getRefreshCount() + getHeadersCount() + adapter.getItemCount() && position < getRefreshCount() + getHeadersCount() + adapter.getItemCount() + getFootersCount();
        }

        /**
         * 判断是否是loadView
         *
         * @param position
         * @return
         */
        protected boolean isLoadViewPos(int position) {
            return position == getItemCount() - 1 && mLoadViewCreator != null && isLoadDisabled;
        }

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //解决和CollapsingToolbarLayout冲突的问题
        AppBarLayout appBarLayout = null;
        ViewParent p = getParent();
        while (p != null) {
            if (p instanceof CoordinatorLayout) {
                break;
            }
            p = p.getParent();
        }
        if (p instanceof CoordinatorLayout) {
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) p;
            final int childCount = coordinatorLayout.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                final View child = coordinatorLayout.getChildAt(i);
                if (child instanceof AppBarLayout) {
                    appBarLayout = (AppBarLayout) child;
                    break;
                }
            }
            if (appBarLayout != null) {
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, State state) {
                        appbarState = state;
                    }
                });
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        release();
        super.onDetachedFromWindow();
    }

    /**
     * add by LinGuanHong below
     */
    private int scrollDyCounter = 0;

    @Override
    public void scrollToPosition(int position) {
        super.scrollToPosition(position);
        /** if we scroll to position 0, the scrollDyCounter should be reset */
        if (position == 0) {
            scrollDyCounter = 0;
        }
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        if (scrollAlphaChangeListener == null) {
            return;
        }
        int height = scrollAlphaChangeListener.setLimitHeight();
        scrollDyCounter = scrollDyCounter + dy;
        if (scrollDyCounter <= 0) {
            scrollAlphaChangeListener.onAlphaChange(0);
        } else if (scrollDyCounter <= height && scrollDyCounter > 0) {
            float scale = (float) scrollDyCounter / height; /** 255/height = x/255 */
            float alpha = (255 * scale);
            scrollAlphaChangeListener.onAlphaChange((int) alpha);
        } else {
            scrollAlphaChangeListener.onAlphaChange(255);
        }
    }

    private ScrollAlphaChangeListener scrollAlphaChangeListener;

    public void setScrollAlphaChangeListener(
            ScrollAlphaChangeListener scrollAlphaChangeListener
    ) {
        this.scrollAlphaChangeListener = scrollAlphaChangeListener;
    }

    public interface ScrollAlphaChangeListener {
        void onAlphaChange(int alpha);

        /**
         * you can handle the alpha insert it
         */
        int setLimitHeight(); /** set a height for the begging of the alpha start to change */
    }
}
