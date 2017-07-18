package com.wolf.xscript;

import android.app.Application;

import com.google.devtools.simple.compiler.XInit;
import com.wolf.util.UnzipAssets;

import java.io.IOException;


/**
 * Created by GreyWolf on 2016/5/14.
 */
public class XscriptApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        XInit.init(getApplicationContext());
        //初始化文件
    }
}
