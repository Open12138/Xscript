package com.wolf.xscript.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wolf.xscript.R;

import net.youmi.android.spot.SpotManager;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("关于Xscript");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=" + "3100816665" + "&version=1")));
                } catch (Exception e) {
                    Toast.makeText(AboutActivity.this, "你QQ版本过低或未安装QQ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        TextView tv = (TextView) findViewById(R.id.suport);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpotManager.getInstance(AboutActivity.this).showSpotAds(AboutActivity.this);
            }
        });
    }
}
