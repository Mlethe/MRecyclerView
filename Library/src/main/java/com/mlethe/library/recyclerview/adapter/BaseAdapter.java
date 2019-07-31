package com.mlethe.library.recyclerview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mlethe.library.recyclerview.adapter.listener.OnConvertListener;
import com.mlethe.library.recyclerview.adapter.listener.OnItemClickListener;
import com.mlethe.library.recyclerview.adapter.listener.OnLongClickListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * 通用的Adapter
 * Created by Mlethe on 2019/07/31.
 * @param <T>
 * @param <VH>
 */
public abstract class BaseAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected Context mContext;
    protected LayoutInflater mInflater;
    //数据怎么办？
    protected List<T> mData;
    // 布局怎么办？
    private int mLayoutId;

    // 多布局支持
    private MultiTypeSupport mMultiTypeSupport;

    private OnConvertListener mOnConvertListener;
    /***************
     * 给条目设置点击和长按事件
     *********************/
    public OnItemClickListener mItemClickListener;
    public OnLongClickListener mLongClickListener;

    public BaseAdapter(Context context, List<T> data, int layoutId) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mData = data;
        this.mLayoutId = layoutId;
    }

    /**
     * 多布局支持
     */
    public BaseAdapter(Context context, List<T> data, MultiTypeSupport<T> multiTypeSupport) {
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

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 多布局支持
        if (mMultiTypeSupport != null) {
            mLayoutId = viewType;
        }
        // 先inflate数据
        View itemView = mInflater.inflate(mLayoutId, parent, false);
        // 返回ViewHolder
        VH holder = createViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, final int position) {
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
        if (mOnConvertListener != null) {
            mOnConvertListener.convert(holder, position);
        }
        // 绑定怎么办？回传出去
        convert(holder, mData.get(position), position);
    }

    /**
     * 利用抽象方法回传出去，每个不一样的Adapter去设置
     *
     * @param item 当前的数据
     */
    public abstract void convert(VH holder, T item, int position);

    @Override
    public int getItemCount() {
        return getRealItemCount();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        release();
        super.onDetachedFromRecyclerView(recyclerView);
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
     * 释放内存
     */
    protected void release() {
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
     * 创建ViewHolder
     *
     * @param itemView
     * @return
     */
    protected VH createViewHolder(View itemView) {
        VH obj = null;
        try {
            Class<VH> clazz = getPClass();
            // 参数类型数组
            Class[] parameterTypes = {View.class};
            // 根据参数类型获取相应的构造函数
            Constructor constructor = clazz.getConstructor(parameterTypes);
            // 参数数组
            Object[] parameters = {itemView};
            obj = (VH) constructor.newInstance(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    private Class<VH> getPClass() {
        return (Class<VH>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * 设置绑定事件
     * @param onConvertListener
     */
    public BaseAdapter setOnConvertListener(OnConvertListener onConvertListener) {
        this.mOnConvertListener = onConvertListener;
        return this;
    }

    /**
     * 设置点击事件
     * @param itemClickListener
     * @return
     */
    public BaseAdapter setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
        return this;
    }

    /**
     * 设置长按事件
     * @param longClickListener
     * @return
     */
    public BaseAdapter setOnLongClickListener(OnLongClickListener longClickListener) {
        this.mLongClickListener = longClickListener;
        return this;
    }
}
