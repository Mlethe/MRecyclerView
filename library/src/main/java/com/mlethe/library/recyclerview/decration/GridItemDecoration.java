package com.mlethe.library.recyclerview.decration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.mlethe.library.recyclerview.MRecyclerView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * GridView样式的分割线
 * Created by Mlethe on 2018/1/11.
 */

public class GridItemDecoration extends RecyclerView.ItemDecoration {
    // 用的是 系统的一个属性  android.R.attrs.listDriver
    private Drawable mDivider;

    public GridItemDecoration(Context context, int drawbleResourceId) {
        // 获取Drawable
        mDivider = ContextCompat.getDrawable(context, drawbleResourceId);
    }

    /**
     * 基本操作就是留出分割线位置
     *
     * @param outRect
     * @param view
     * @param parent
     * @param state
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        // 留出分割线的位置  下边和右边
        //outRect.bottom = mDivider.getIntrinsicHeight();
        //outRect.right = mDivider.getIntrinsicWidth();

        int position = parent.getChildAdapterPosition(view);
        if (isMRecyclerView(position, parent, state)) {
            return;
        }

        // 留出分割线的位置  下边和右边 如果是最底部那么不要留，最右边也不要留
        int height = mDivider.getIntrinsicHeight();
        int width = mDivider.getIntrinsicWidth();
        if (isFirstColumn(position, parent)) {   //第一列  当前位置%列数 == 0
            width = 0;
        }
        if (isFirstRow(position, parent)) { //第一行
            height = 0;
        }
        outRect.top = height;
        outRect.left = width;
    }

    /**
     * 处理是MRecyclerView的问题
     * @param parent
     * @param position
     * @return
     */
    private boolean isMRecyclerView(int position, RecyclerView parent, RecyclerView.State state) {
        if (parent instanceof MRecyclerView){
            MRecyclerView recyclerView = (MRecyclerView) parent;
            if (position < recyclerView.getHeadersCount() + recyclerView.getRefreshCount()) {
                return true;
            }
            int minimum = recyclerView.getHeadersCount() + recyclerView.getAdapter().getItemCount() + recyclerView.getRefreshCount();
            int maximal = state.getItemCount();
            if (position >= minimum && position <= maximal) {
                return true;
            }
        }
        return false;
    }

    /**
     * 绘制分割线
     *
     * @param c
     * @param parent
     * @param state
     */
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        // 绘制
        drawHorizontal(c, parent, state);
        drawVertical(c, parent, state);
    }

    /**
     * 绘制垂直方向
     *
     * @param canvas
     * @param parent
     */
    private void drawVertical(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View childView = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(childView);
            if (isMRecyclerView(position, parent, state)) {
                continue;
            }
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) childView.getLayoutParams();
            int bottom = childView.getBottom() + params.bottomMargin;
            int top = childView.getTop() - params.topMargin;
            int right = childView.getLeft();
            int left = right - mDivider.getIntrinsicWidth();
            // 计算水平分割线的位置
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(canvas);
        }
    }

    /**
     * 绘制水平方向
     *
     * @param canvas
     * @param parent
     */
    private void drawHorizontal(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View childView = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(childView);
            if (isMRecyclerView(position, parent, state)) {
                continue;
            }
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) childView.getLayoutParams();
            int left = childView.getLeft() - params.leftMargin;
            int right = childView.getRight() + mDivider.getIntrinsicWidth() + params.rightMargin;
            int bottom = childView.getTop();
            int top = bottom - mDivider.getIntrinsicHeight();
            // 计算水平分割线的位置
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(canvas);
        }
    }

    /**
     * 第一列
     *
     * @return
     */
    public boolean isFirstColumn(int position, RecyclerView parent) {
        int spanCount = getSpanCount(parent);

        if (parent instanceof MRecyclerView) {
            MRecyclerView recyclerView = (MRecyclerView) parent;
            return (position - recyclerView.getRefreshCount() - recyclerView.getHeadersCount()) % spanCount == 0;    // 当前位置%列数 == 0
        } else {
            return position % spanCount == 0;    //当前位置%列数 == 0
        }
    }

    /**
     * 获取RecyclerView的列数
     *
     * @param parent
     * @return
     */
    private int getSpanCount(RecyclerView parent) {
        // 获取列数  GridLayout
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            int spanCount = gridLayoutManager.getSpanCount();
            return spanCount;
        }
        return 1;
    }

    /**
     * 第一行
     *
     * @return
     */
    public boolean isFirstRow(int position, RecyclerView parent) {
        // 列数
        int spanCount = getSpanCount(parent);

        if (parent instanceof MRecyclerView) {
            MRecyclerView recyclerView = (MRecyclerView) parent;
            // 当前的位置 < 列数
            return position - recyclerView.getRefreshCount() - recyclerView.getHeadersCount() < spanCount;
        } else {
            // 当前的位置 < 列数
            return position  < spanCount;
        }
    }
}
