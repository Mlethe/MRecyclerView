package com.mlethe.recyclerview.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.normal_btn).setOnClickListener(this);
        findViewById(R.id.view_pager_btn).setOnClickListener(this);
        findViewById(R.id.header_footer_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.normal_btn) {
            Intent intent = new Intent(this, NormalActivity.class);
            startActivity(intent);
        } else if (id == R.id.view_pager_btn) {
            Intent intent = new Intent(this, ViewPagerActivity.class);
            startActivity(intent);
        } else if (id == R.id.header_footer_btn) {
            Intent intent = new Intent(this, HeaderFooterActivity.class);
            startActivity(intent);
        }
    }
}
