package com.mlethe.library.recyclerview.decration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.mlethe.library.recyclerview.MRecyclerView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Linear分割线（头部和底部自定义）
 * Created by Mlethe on 2018/10/23.
 */

public class LinearHeaderFooterDecoration extends RecyclerView.ItemDecoration{
    // 用的是 系统的一个属性  android.R.attrs.listDriver
    private Drawable mHeaderDivider, mDivider, mFooterDivider;
    private int mOrientation;

    public LinearHeaderFooterDecoration(Context context, int headerDrawableResId, int drawableResourceId, int footerDrawableResId) {
        // 获取Drawable
        mHeaderDivider = ContextCompat.getDrawable(context,headerDrawableResId);
        mDivider = ContextCompat.getDrawable(context,drawableResourceId);
        mFooterDivider = ContextCompat.getDrawable(context,footerDrawableResId);
    }

    /**
     * 基本操作就是留出分割线位置
     * @param outRect
     * @param view
     * @param parent
     * @param state
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        // 代表在每个底部的位置留出10px来绘制分割线 最后一个位置不需要分割线

        int position = parent.getChildAdapterPosition(view);

        if (isMRecyclerView(position, parent, state)) {
            return;
        }
        mOrientation = ((LinearLayoutManager) parent.getLayoutManager()).getOrientation();
        int childCount = parent.getChildCount();
        if (mOrientation == LinearLayoutManager.HORIZONTAL) {
            if (position == 0) {
                outRect.left = mHeaderDivider.getIntrinsicWidth();
            }
            if (position == childCount - 1) {
                outRect.right = mFooterDivider.getIntrinsicWidth();
            } else {
                outRect.right = mDivider.getIntrinsicWidth();
            }
        } else if (mOrientation == LinearLayoutManager.VERTICAL) {
            if (position == 0) {
                outRect.top = mHeaderDivider.getIntrinsicHeight();
            }
            if (position == childCount - 1) {
                outRect.bottom = mFooterDivider.getIntrinsicHeight();
            } else {
                outRect.bottom = mDivider.getIntrinsicHeight();
            }
        }
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
            if (position <= recyclerView.getHeadersCount() + recyclerView.getRefreshCount()) {
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
     * @param canvas
     * @param parent
     * @param state
     */
    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
//        super.onDraw(canvas, parent, state);

        if (mOrientation == LinearLayoutManager.HORIZONTAL) {
            drawHorizontalDividers(canvas, parent, state);
        } else if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVerticalDividers(canvas, parent, state);
        }
    }

    /**
     * Adds dividers to a RecyclerView with a LinearLayoutManager or its
     * subclass oriented horizontally.
     *
     * @param canvas The {@link Canvas} onto which horizontal dividers will be
     *               drawn
     * @param parent The RecyclerView onto which horizontal dividers are being
     *               added
     */
    private void drawHorizontalDividers(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        canvas.save();
        int parentTop = parent.getPaddingTop();
        int parentBottom = parent.getHeight() - parent.getPaddingBottom();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            int position = parent.getChildAdapterPosition(child);
            if (isMRecyclerView(position, parent, state)) {
                continue;
            }

            if (position == 0) {
                int parentRight = child.getLeft();
                int parentLeft = parentRight - mHeaderDivider.getIntrinsicWidth();

                mHeaderDivider.setBounds(parentLeft, parentTop, parentRight, parentBottom);
                mHeaderDivider.draw(canvas);
            }
            if (position == childCount - 1) {
                int parentLeft = child.getRight();
                int parentRight = parentLeft + mFooterDivider.getIntrinsicWidth();

                mFooterDivider.setBounds(parentLeft, parentTop, parentRight, parentBottom);
                mFooterDivider.draw(canvas);
            } else {
                int parentLeft = child.getRight();
                int parentRight = parentLeft + mDivider.getIntrinsicWidth();

                mDivider.setBounds(parentLeft, parentTop, parentRight, parentBottom);
                mDivider.draw(canvas);
            }
        }
        canvas.restore();
    }

    /**
     * Adds dividers to a RecyclerView with a LinearLayoutManager or its
     * subclass oriented vertically.
     *
     * @param canvas The {@link Canvas} onto which vertical dividers will be
     *               drawn
     * @param parent The RecyclerView onto which vertical dividers are being
     *               added
     */
    private void drawVerticalDividers(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        canvas.save();
        int parentLeft = parent.getPaddingLeft();
        int parentRight = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            int position = parent.getChildAdapterPosition(child);
            if (isMRecyclerView(position, parent, state)) {
                continue;
            }

            if (position == 0) {
                int parentBottom = child.getTop();
                int parentTop = parentBottom - mHeaderDivider.getIntrinsicHeight();

                mHeaderDivider.setBounds(parentLeft, parentTop, parentRight, parentBottom);
                mHeaderDivider.draw(canvas);
            }
            if (position == childCount - 1) {
                int parentTop = child.getBottom();
                int parentBottom = parentTop + mFooterDivider.getIntrinsicHeight();

                mFooterDivider.setBounds(parentLeft, parentTop, parentRight, parentBottom);
                mFooterDivider.draw(canvas);
            } else {
                int parentTop = child.getBottom();
                int parentBottom = parentTop + mDivider.getIntrinsicHeight();

                mDivider.setBounds(parentLeft, parentTop, parentRight, parentBottom);
                mDivider.draw(canvas);
            }
        }
        canvas.restore();
    }

}
