package com.mlethe.library.recyclerview.refresh;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.mlethe.library.recyclerview.R;
import com.mlethe.library.recyclerview.progress.AVLoadingIndicatorView;
import com.mlethe.library.recyclerview.progress.ProgressStyle;
import com.mlethe.library.recyclerview.progress.SimpleViewSwitcher;

import java.util.Date;

import static com.mlethe.library.recyclerview.MRecyclerView.STATE_NORMAL;
import static com.mlethe.library.recyclerview.MRecyclerView.STATE_RELEASE_TO_REFRESH;

/**
 * Created by Mlethe on 2017/1/3.
 * 默认样式的刷新头部辅助类
 *              如淘宝、京东、不同的样式可以自己去实现
 */

public class DefaultRefreshCreator extends BaseRefreshCreator {

    private String KEY_LAST_UPDATE_TIME = "LAST_UPDATE_TIME";

    private ImageView mArrowImageView;
    private SimpleViewSwitcher mProgressBar;
    private TextView mStatusTextView;
    private AVLoadingIndicatorView progressView;
    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;
    private Context mContent;

    private static final int ROTATE_ANIM_DURATION = 180;
    private TextView mHeaderTimeView;
    private SharedPreferences sharedPre;

    private int mState;

    private String textJust;
    private String secondsAgo;
    private String minuteAgo;
    private String hourAgo;
    private String dayAgo;
    private String monthAgo;
    private String yearAgo;

    @Override
    public View getRefreshView(Context context, ViewGroup parent) {
        this.mContent = context;
        KEY_LAST_UPDATE_TIME += context.getClass().getName();
        sharedPre = context.getSharedPreferences("ClassicsHeader", Context.MODE_PRIVATE);
        View refreshView = LayoutInflater.from(context).inflate(R.layout.default_refresh_header, parent, false);
        mArrowImageView = (ImageView)refreshView.findViewById(R.id.default_header_arrow);
        mStatusTextView = (TextView)refreshView.findViewById(R.id.default_refresh_status_tv);

        //init the progress view
        mProgressBar = (SimpleViewSwitcher)refreshView.findViewById(R.id.default_header_progressbar);
        progressView = new  AVLoadingIndicatorView(context);
        progressView.setIndicatorColor(0xffB5B5B5);
        progressView.setIndicatorId(ProgressStyle.BallSpinFadeLoader);
        if(mProgressBar != null)
            mProgressBar.setView(progressView);

        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);

        mHeaderTimeView = (TextView)refreshView.findViewById(R.id.last_refresh_time);
        textJust = context.getString(R.string.text_just);
        secondsAgo = context.getString(R.string.text_seconds_ago);
        minuteAgo = context.getString(R.string.text_minute_ago);
        hourAgo = context.getString(R.string.text_hour_ago);
        dayAgo = context.getString(R.string.text_day_ago);
        monthAgo = context.getString(R.string.text_month_ago);
        yearAgo = context.getString(R.string.text_year_ago);
        return refreshView;
    }

    @Override
    public void onPull(int currentDragHeight, int viewHeight, int state) {
        long sharedPreLong = sharedPre.getLong(KEY_LAST_UPDATE_TIME, System.currentTimeMillis());
        mHeaderTimeView.setText(String.format(mContent.getString(R.string.recycler_header_last_time), friendlyTime(new Date(sharedPreLong))));
        mArrowImageView.setVisibility(View.VISIBLE);
        if(mProgressBar != null){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
        if (mState == state) return;
        if (state == STATE_NORMAL){
            mArrowImageView.clearAnimation();
            mArrowImageView.startAnimation(mRotateDownAnim);
            mStatusTextView.setText(R.string.recycler_header_hint_normal);
        } else if (state == STATE_RELEASE_TO_REFRESH) {
            mArrowImageView.clearAnimation();
            mArrowImageView.startAnimation(mRotateUpAnim);
            mStatusTextView.setText(R.string.recycler_header_hint_release);
        }
        mState = state;
    }

    @Override
    public void onRelease() {

    }

    @Override
    public void onRefreshing() {
        mArrowImageView.clearAnimation();
        mArrowImageView.setVisibility(View.INVISIBLE);
        if(mProgressBar != null)
            mProgressBar.setVisibility(View.VISIBLE);
        mStatusTextView.setText(R.string.refreshing);
    }

    @Override
    public void onRefreshStop() {
        mArrowImageView.setVisibility(View.INVISIBLE);
        if(mProgressBar != null)
            mProgressBar.setVisibility(View.INVISIBLE);
        mStatusTextView.setText(R.string.refresh_done);
        saveLastRefreshTime(System.currentTimeMillis());
    }

    @Override
    public void onRefreshFail() {
        mArrowImageView.setVisibility(View.INVISIBLE);
        if(mProgressBar != null)
            mProgressBar.setVisibility(View.INVISIBLE);
        mStatusTextView.setText(R.string.refresh_fail);
        saveLastRefreshTime(System.currentTimeMillis());
    }

    @Override
    public void destroy(){
        mProgressBar = null;
        mContent = null;
        if(progressView != null){
            progressView.destroy();
            progressView = null;
        }
        if(mRotateUpAnim != null){
            mRotateUpAnim.cancel();
            mRotateUpAnim = null;
        }
        if(mRotateDownAnim != null){
            mRotateDownAnim.cancel();
            mRotateDownAnim = null;
        }
        sharedPre = null;
    }

    private void saveLastRefreshTime(long refreshTime){
        sharedPre.edit().putLong(KEY_LAST_UPDATE_TIME,refreshTime).commit();
    }

    public String friendlyTime(Date time) {
        //获取time距离当前的秒数
        int ct = (int)((System.currentTimeMillis() - time.getTime())/1000);

        if(ct == 0) {
            return textJust;
        }

        if(ct > 0 && ct < 60) {
            return ct + secondsAgo;
        }

        if(ct >= 60 && ct < 3600) {
            return Math.max(ct / 60,1) + minuteAgo;
        }
        if(ct >= 3600 && ct < 86400)
            return ct / 3600 + hourAgo;
        if(ct >= 86400 && ct < 2592000){ //86400 * 30
            int day = ct / 86400 ;
            return day + dayAgo;
        }
        if(ct >= 2592000 && ct < 31104000) { //86400 * 30
            return ct / 2592000 + monthAgo;
        }
        return ct / 31104000 + yearAgo;
    }
}
