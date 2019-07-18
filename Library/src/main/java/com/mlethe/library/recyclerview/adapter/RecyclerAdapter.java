package com.mlethe.library.recyclerview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mlethe.library.recyclerview.adapter.listener.OnItemClickListener;
import com.mlethe.library.recyclerview.adapter.listener.OnLongClickListener;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Mlethe on 2018/1/11.
 * Description: 通用的Adapter
 */
public abstract class RecyclerAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    protected Context mContext;
    protected LayoutInflater mInflater;
    //数据怎么办？
    protected List<T> mData;
    // 布局怎么办？
    private int mLayoutId;

    // 多布局支持
    private MultiTypeSupport mMultiTypeSupport;

    public RecyclerAdapter(Context context, List<T> data, int layoutId) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mData = data;
        this.mLayoutId = layoutId;
    }

    /**
     * 多布局支持
     */
    public RecyclerAdapter(Context context, List<T> data, MultiTypeSupport<T> multiTypeSupport) {
        this(context, data, -1);
        this.mMultiTypeSupport = multiTypeSupport;
    }

    /**
     * 根据当前位置获取不同的viewType
     */
    @Override
    public int getItemViewType(int position) {
        // 多布局支持
        if (mMultiTypeSupport != null) {
            return mMultiTypeSupport.getLayoutId(mData.get(position), position);
        }
        return super.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 多布局支持
        if (mMultiTypeSupport != null) {
            mLayoutId = viewType;
        }
        // 先inflate数据
        View itemView = mInflater.inflate(mLayoutId, parent, false);
        // 返回ViewHolder
        ViewHolder holder = new ViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // 设置点击和长按事件
        if (mItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(position);
                }
            });
        }
        if (mLongClickListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return mLongClickListener.onLongClick(position);
                }
            });
        }
        // 绑定怎么办？回传出去
        convert(holder, mData.get(position), position);
    }

    /**
     * 利用抽象方法回传出去，每个不一样的Adapter去设置
     *
     * @param item 当前的数据
     */
    public abstract void convert(ViewHolder holder, T item, int position);

    @Override
    public int getItemCount() {
        return getRealItemCount();
    }

    /**
     * 获取真实的数据条数
     *
     * @return
     */
    protected int getRealItemCount() {
        return mData.size();
    }

    /**
     * 销毁
     */
    public void clear() {
        mContext = null;
        mInflater = null;
        if (mData != null) {
            mData.clear();
        }
        mData = null;
        mLayoutId = 0;
        mMultiTypeSupport = null;
        mItemClickListener = null;
        mLongClickListener = null;
    }

    /**
     * 通知观察者移动数据
     *
     * @param fromPosition 开始位置
     * @param toPosition   结束位置
     */
    public final void notifyDataMoved(int fromPosition, int toPosition) {
        mData.add(toPosition, mData.get(fromPosition));
        mData.remove(fromPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    /***************
     * 给条目设置点击和长按事件
     *********************/
    public OnItemClickListener mItemClickListener;
    public OnLongClickListener mLongClickListener;

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    public void setOnLongClickListener(OnLongClickListener longClickListener) {
        this.mLongClickListener = longClickListener;
    }
}
