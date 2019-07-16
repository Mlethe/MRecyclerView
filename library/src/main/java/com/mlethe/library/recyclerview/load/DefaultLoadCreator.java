package com.mlethe.library.recyclerview.load;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mlethe.library.recyclerview.R;
import com.mlethe.library.recyclerview.progress.AVLoadingIndicatorView;
import com.mlethe.library.recyclerview.progress.ProgressStyle;
import com.mlethe.library.recyclerview.progress.SimpleViewSwitcher;

/**
 * Created by Mlethe on 2017/1/3.
 * 默认样式的加载底部辅助类
 * 如淘宝、京东、不同的样式可以自己去实现
 */

public class DefaultLoadCreator extends BaseLoadCreator {

    private View loadView;
    private SimpleViewSwitcher progressCon;
    private TextView mText;

    @Override
    public View getLoadView(Context context, ViewGroup parent) {
        loadView = LayoutInflater.from(context).inflate(R.layout.default_loading_footer, parent, false);
        progressCon = loadView.findViewById(R.id.recycler_foot_progress);
        AVLoadingIndicatorView progressView = new  AVLoadingIndicatorView(context);
        progressView.setIndicatorColor(0xffB5B5B5);
        progressView.setIndicatorId(ProgressStyle.BallSpinFadeLoader);
        progressCon.setView(progressView);

        mText  = loadView.findViewById(R.id.recycler_foot_more);
        mText.setText(R.string.recycler_loading);
        loadView.setVisibility(View.GONE);
        return loadView;
    }

    @Override
    public void onLoading() {
        mText.setText(R.string.loading);
        loadView.setVisibility(View.VISIBLE);
        progressCon.setVisibility(View.VISIBLE);
    }


    @Override
    public void onLoadStop() {
        loadView.setVisibility(View.GONE);
        progressCon.setVisibility(View.GONE);
        mText.setText(R.string.loading_done);
        loadView.setVisibility(View.GONE);
    }

    @Override
    public void onLoadFail() {
        progressCon.setVisibility(View.GONE);
        mText.setText(R.string.recycler_footer_network_error);
    }

    @Override
    public void setNoData() {
        progressCon.setVisibility(View.GONE);
        mText.setText(R.string.recycler_footer_end);
    }

    @Override
    public void destroy() {
        progressCon = null;
        mText = null;
        loadView = null;
    }
}
