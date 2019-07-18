package com.mlethe.library.recyclerview.load;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Mlethe on 2017/1/3.
 * 加载更多的辅助类为了匹配所有效果
 */

public abstract class BaseLoadCreator {

    /**
     * 获取上拉加载更多的View
     *
     * @param context 上下文
     * @param parent  RecyclerView
     */
    public abstract View getLoadView(Context context, ViewGroup parent);

    /**
     * 正在加载中
     */
    public abstract void onLoading();

    /**
     * 停止加载
     */
    public abstract void onLoadStop();

    /**
     * 加载失败
     */
    public abstract void onLoadFail();

    /**
     * UI处理没有更多数据
     */
    public abstract void setNoData();

    /**
     * 销毁
     */
    public abstract void destroy();
}
