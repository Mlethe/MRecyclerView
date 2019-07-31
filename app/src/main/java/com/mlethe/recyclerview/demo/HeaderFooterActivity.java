package com.mlethe.recyclerview.demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.mlethe.library.recyclerview.adapter.ViewHolder;
import com.mlethe.library.recyclerview.adapter.WrapAdapter;
import com.mlethe.library.recyclerview.decration.LinearItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class HeaderFooterActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private List<String> dataList = new ArrayList<>();
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_header_footer);
        recyclerView = findViewById(R.id.header_footer_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new LinearItemDecoration(this, R.drawable.line_1dp));
        adapter = new RecyclerViewAdapter(this, dataList);
        recyclerView.setAdapter(adapter);
        adapter.setEmptyView(R.layout.empty_view, recyclerView, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.setEmptyView(R.layout.loading_view, recyclerView);
                mHandler.postDelayed(() -> {
                    List<String> data = getData(15, 1);
                    dataList.clear();
                    dataList.addAll(data);
                    adapter.notifyDataSetChanged();
                }, 2000);
            }
        });
        View header = LayoutInflater.from(this).inflate(R.layout.header_view, recyclerView, false);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.removeHeaderView(header);
            }
        });
        View footer = LayoutInflater.from(this).inflate(R.layout.footer_view, recyclerView, false);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.removeFooterView(footer);
            }
        });
        adapter.addHeaderView(header)
                .addFooterView(footer);
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

    private class RecyclerViewAdapter extends WrapAdapter<String> {

        public RecyclerViewAdapter(Context context, List<String> data) {
            super(context, data, R.layout.item_text);
        }

        @Override
        public void convert(ViewHolder holder, String item, int position) {
            holder.setText(R.id.text_view, item);
        }
    }

    @Override
    protected void onDestroy() {
        recyclerView.setAdapter(null);
        super.onDestroy();
    }
}
