package com.wolf.xscript.activity;

import android.support.v7.app.AppCompatActivity;

import com.wolf.util.tint.StatusBarUtil;
import com.wolf.xscript.R;

/**
 * Created by GreyWolf on 2016/5/18.
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setStatusBar();
    }

    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary));
    }
}
