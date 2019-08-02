package com.mlethe.recyclerview.demo;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mlethe.library.recyclerview.MRecyclerView;
import com.mlethe.library.recyclerview.adapter.RecyclerAdapter;
import com.mlethe.library.recyclerview.adapter.ViewHolder;
import com.mlethe.library.recyclerview.decration.LinearItemDecoration;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TestFragment extends Fragment implements MRecyclerView.OnRefreshListener, MRecyclerView.OnLoadListener {

    private MRecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private List<String> dataList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView = getView().findViewById(R.id.test_mrv);
        adapter = new RecyclerViewAdapter(getContext(), dataList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new LinearItemDecoration(getContext(), R.drawable.line_1dp));
        recyclerView.setOnRefreshListener(this)
                .setOnLoadListener(this)
                .setAdapter(adapter);
        recyclerView.refresh();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<String> data = getData(15, 1);
                dataList.clear();
                dataList.addAll(data);
                recyclerView.refreshComplete();
                adapter.notifyDataSetChanged();
            }
        }, 2000);
    }

    @Override
    public void onRetry() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<String> data = getData(10, 3);
                dataList.addAll(data);
                recyclerView.loadMoreComplete();
                adapter.notifyItemRangeInserted(dataList.size(), 10);
            }
        }, 2000);
    }

    @Override
    public void onLoadMore() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<String> data = getData(10, 2);
                dataList.addAll(data);
                recyclerView.loadMoreComplete();
                adapter.notifyItemRangeInserted(dataList.size(), 10);
            }
        }, 2000);
    }

    public List<String> getData(int index, int type) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < index; i++) {
            if (type == 1) {
                list.add("刷新" + (i + 1));
            } else if (type == 2){
                list.add("重新加载" + (i + 1));
            } else {
                list.add("加载" + (i + 1));
            }
        }
        return list;
    }

    private class RecyclerViewAdapter extends RecyclerAdapter<String> {

        public RecyclerViewAdapter(Context context, List<String> data) {
            super(context, data, R.layout.item_text);
        }

        @Override
        public void convert(ViewHolder holder, String item, int position) {
            holder.setText(R.id.text_view, item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("TAG", "onDestroy: ");
    }
}
