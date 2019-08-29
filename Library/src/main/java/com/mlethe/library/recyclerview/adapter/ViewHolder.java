package com.mlethe.library.recyclerview.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Mlethe on 2018/1/11.
 * Description: RecyclerView的ViewHolder
 */
public class ViewHolder extends RecyclerView.ViewHolder {

    // 用来存放子View减少findViewById的次数
    private SparseArray<View> mViews;

    public ViewHolder(View itemView) {
        super(itemView);
        mViews = new SparseArray<>();
    }

    /**
     * 设置TextView文本
     *
     * @param viewId
     * @param text
     * @return
     */
    public ViewHolder setText(int viewId, CharSequence text) {
        TextView tv = getView(viewId);
        tv.setText(text);
        return this;
    }

    /**
     * 设置TextView文本
     *
     * @param viewId
     * @param resid
     * @return
     */
    public ViewHolder setText(int viewId, @StringRes int resid) {
        TextView tv = getView(viewId);
        tv.setText(resid);
        return this;
    }

    /**
     * 设置TextView文本
     *
     * @param viewId
     * @param value
     * @return
     */
    public ViewHolder setText(int viewId, float value) {
        setText(viewId, String.valueOf(value));
        return this;
    }

    /**
     * 设置TextView文本
     *
     * @param viewId
     * @param value
     * @return
     */
    public ViewHolder setText(int viewId, double value) {
        setText(viewId, String.valueOf(value));
        return this;
    }

    /**
     * 设置TextView文本
     *
     * @param viewId
     * @param value
     * @return
     */
    public ViewHolder setText(int viewId, long value) {
        setText(viewId, String.valueOf(value));
        return this;
    }

    /**
     * 通过id获取view
     */
    public <T extends View> T getView(@IdRes int viewId) {
        // 先从缓存中找
        View view = mViews.get(viewId);
        if (view == null) {
            // 直接从ItemView中找
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 设置背景
     *
     * @param viewId
     * @param background
     * @return
     */
    public ViewHolder setBackground(int viewId, Drawable background) {
        getView(viewId).setBackground(background);
        return this;
    }

    /**
     * 设置背景资源
     *
     * @param viewId
     * @param resid
     * @return
     */
    public ViewHolder setBackgroundResource(int viewId, @DrawableRes int resid) {
        getView(viewId).setBackgroundResource(resid);
        return this;
    }

    /**
     * 设置背景颜色
     *
     * @param viewId
     * @param color
     * @return
     */
    public ViewHolder setBackgroundColor(int viewId, @ColorInt int color) {
        getView(viewId).setBackgroundColor(color);
        return this;
    }

    /**
     * 设置View的Visibility
     */
    public ViewHolder setViewVisibility(int viewId, int visibility) {
        getView(viewId).setVisibility(visibility);
        return this;
    }

    /**
     * 获取View的Visibility
     *
     * @param viewId
     * @return
     */
    public int getViewVisibility(int viewId) {
        return getView(viewId).getVisibility();
    }

    /**
     * 设置ImageView的资源
     */
    public ViewHolder setImageResource(int viewId, int resourceId) {
        ImageView imageView = getView(viewId);
        imageView.setImageResource(resourceId);
        return this;
    }

    /**
     * 设置ImageView Bitmap
     * @param viewId
     * @param bitmap
     * @return
     */
    public ViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView imageView = getView(viewId);
        imageView.setImageBitmap(bitmap);
        return this;
    }

    /**
     * 设置View的点击事件
     *
     * @param viewId
     * @param listener
     * @return
     */
    public ViewHolder setOnClickListener(int viewId, View.OnClickListener listener) {
        getView(viewId).setOnClickListener(listener);
        return this;
    }

    /**
     * 设置View的长按事件
     *
     * @param viewId
     * @param listener
     * @return
     */
    public ViewHolder setOnLongClickListener(int viewId, View.OnLongClickListener listener) {
        getView(viewId).setOnLongClickListener(listener);
        return this;
    }

    /**
     * 设置条目点击事件
     */
    public ViewHolder setOnItemClickListener(View.OnClickListener listener) {
        itemView.setOnClickListener(listener);
        return this;
    }

    /**
     * 设置条目长按事件
     */
    public ViewHolder setOnItemLongClickListener(View.OnLongClickListener listener) {
        itemView.setOnLongClickListener(listener);
        return this;
    }

    /**
     * 设置图片通过路径,这里稍微处理得复杂一些，因为考虑加载图片的第三方可能不太一样
     * 也可以直接写死
     */
    public ViewHolder setImageByUrl(int viewId, HolderImageLoader imageLoader) {
        ImageView imageView = getView(viewId);
        if (imageLoader == null) {
            throw new NullPointerException("imageLoader is null!");
        }
        imageLoader.displayImage(imageView.getContext(), imageView, imageLoader.getImagePath());
        return this;
    }

    /**
     * 设置字体颜色
     *
     * @param viewId
     * @param color
     */
    public ViewHolder setTextColor(int viewId, int color) {
        TextView textView = getView(viewId);
        textView.setTextColor(color);
        return this;
    }

    /**
     * 图片加载，这里稍微处理得复杂一些，因为考虑加载图片的第三方可能不太一样
     * 也可以不写这个类
     */
    public static abstract class HolderImageLoader {
        private String mImagePath;

        public HolderImageLoader(String imagePath) {
            this.mImagePath = imagePath;
        }

        public String getImagePath() {
            return mImagePath;
        }

        public abstract void displayImage(Context context, ImageView imageView, String imagePath);
    }
}
