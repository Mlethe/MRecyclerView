package com.mlethe.recyclerview.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

public class ViewPagerActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String[] mTitles;
    private SparseArray<Fragment> fragments = new SparseArray<>();
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);
        tabLayout = findViewById(R.id.test_tab);
        viewPager = findViewById(R.id.test_vp);
        mTitles = new String[]{"页面一","页面二","页面三","页面四","页面五"};
        setView();
    }

    /**
     * 初始化view
     */
    private void setView(){
        fragments.clear();
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        // 设置预加载界面个数
        viewPager.setOffscreenPageLimit(0);
        tabLayout.setupWithViewPager(viewPager);
        initTab();
        adapter.notifyDataSetChanged();
    }

    private void initTab() {
        tabLayout.removeAllTabs();
        for (int i = 0; i < mTitles.length; i++) {
            tabLayout.addTab(tabLayout.newTab().setText(mTitles[i]));
        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = fragments.get(position);//从集合中取出Fragment
            if (fragment == null){
                fragment = new TestFragment();
                int type = 0;
                if (position == 0) {
                    type = 0;
                } else if (position == 1) {
                    type = 1;
                } else if (position == 2) {
                    type = 5;
                } else if (position == 3) {
                    type = 7;
                }
                Bundle bundle = new Bundle();
                bundle.putInt("type", type);//这里的values就是我们要传的值
                fragment.setArguments(bundle);
                fragments.put(position, fragment);//存入集合中
            }
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }

}
