package com.mlethe.library.recyclerview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mlethe.library.recyclerview.adapter.listener.OnConvertListener;
import com.mlethe.library.recyclerview.adapter.listener.OnItemClickListener;
import com.mlethe.library.recyclerview.adapter.listener.OnLongClickListener;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Mlethe on 2018/1/11.
 */
public abstract class RecyclerAdapter<T> extends BaseAdapter<T, ViewHolder> {

    public RecyclerAdapter(Context context, List<T> data, int layoutId) {
        super(context, data, layoutId);
    }

    public RecyclerAdapter(Context context, List<T> data, MultiTypeSupport<T> multiTypeSupport) {
        super(context, data, multiTypeSupport);
    }

    @Override
    protected ViewHolder createViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

}
