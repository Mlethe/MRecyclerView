package com.mlethe.library.recyclerview.adapter.listener;

import androidx.recyclerview.widget.RecyclerView;

public interface OnConvertListener<VH extends RecyclerView.ViewHolder> {
    void convert(VH holder, int position);
}
