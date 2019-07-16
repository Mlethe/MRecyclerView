package com.mlethe.library.recyclerview.refresh;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Darren on 2017/1/3.
 * Email: 240336124@qq.com
 * Description: 下拉刷新的辅助类为了匹配所有效果
 */

public abstract class BaseRefreshCreator {

    /**
     * 获取下拉刷新的View
     *
     * @param context 上下文
     * @param parent  RecyclerView
     */
    public abstract View getRefreshView(Context context, ViewGroup parent);

    /**
     * 正在下拉
     * @param currentDragHeight   当前拖动的高度
     * @param viewHeight  总的刷新高度
     * @param state 当前状态
     */
    public abstract void onPull(int currentDragHeight, int viewHeight, int state);

    /**
     * 释放刷新
     */
    public abstract void onRelease();

    /**
     * 正在刷新中
     */
    public abstract void onRefreshing();

    /**
     * 停止刷新
     */
    public abstract void onRefreshStop();

    /**
     * 刷新失败
     */
    public abstract void onRefreshFail();

    /**
     * 销毁
     */
    public abstract void destroy();
}
