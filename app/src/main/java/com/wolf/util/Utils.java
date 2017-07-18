package com.wolf.util;

import android.content.Context;
import android.util.Log;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by GreyWolf on 2016/5/14.
 */
public class Utils {
    public static final String XTAG = "xcompile";

    public static void log(String str) {
        Log.i(XTAG, str);
    }

    public static void deleteFile(File f) {
        if (f.isDirectory()) {
            for (File sub : f.listFiles()) {
                deleteFile(sub);
            }
        }
        f.delete();
    }

    public static void showMsg(Context context, String title, String content) {

        SweetAlertDialog dialog = new SweetAlertDialog(context).setTitleText(title).setContentText(content);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
}
