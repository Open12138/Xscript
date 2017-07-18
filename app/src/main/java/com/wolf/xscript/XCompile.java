package com.wolf.xscript;

import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.devtools.simple.compiler.Main;
import com.google.devtools.simple.compiler.XInit;
import com.wolf.util.Utils;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by GreyWolf on 2016/5/15.
 */
public class XCompile {
    public static final String propertiesDir = "xscript";//存放project.properties 文件
    public static SweetAlertDialog pDialog = null;
    public static long currtime = System.currentTimeMillis();

    public static void compile(final Context context, final String path) {
        currtime = System.currentTimeMillis();
        curr = 0;
        pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE).setTitleText("编译中...");
        pDialog.show();
        pDialog.setCancelable(false);
        final String propertiespath = path + File.separator + XCompile.propertiesDir + File.separator + "project.properties";
        final Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Main.FAILED:
                        pDialog.setTitleText("编译失败")
                                .setContentText(Main.sb.toString())
                                .setConfirmText("复制")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                        clipboard.setText(Main.sb.toString());
                                        sweetAlertDialog.dismissWithAnimation();
                                        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
//                        showMsg(context, "编译失败~", Main.sb.toString());
                        break;
                    case Main.STATE:
//                        dialog.setMessage(msg.obj.toString());
                        pDialog.setContentText(msg.obj.toString());
                        int co = getColor(context);
                        if (co != 0) {
                            pDialog.getProgressHelper().setBarColor(co);
                        }
                        break;
                    case Main.SUCESS:
                        pDialog.setTitleText("编译成功!")
                                .setConfirmText("OK")
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

//                        showMsg(context, "编译成功!", Main.sb.toString());
                        Intent intent = new Intent("android.intent.action.VIEW");
                        intent.setDataAndType(Uri.fromFile(new File(msg.obj.toString())), "application/vnd.android.package-archive");
                        context.startActivity(intent);
                        break;
                }
            }
        };
        new Thread("compile") {
            @Override
            public void run() {
                Main.xCompile(context, propertiespath, h);
            }
        }.start();
    }

    public static void showMsg(final Context context, String title, final String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(content)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("复制", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(content);
                        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .create().show();
    }

    public static int curr = 0;

    public static int getColor(Context context) {
        final int[] colors = {
                context.getResources().getColor(R.color.blue_btn_bg_color),
                context.getResources().getColor(R.color.material_deep_teal_50),
                context.getResources().getColor(R.color.success_stroke_color),
                context.getResources().getColor(R.color.material_deep_teal_20),
                context.getResources().getColor(R.color.material_blue_grey_80),
                context.getResources().getColor(R.color.warning_stroke_color),
                context.getResources().getColor(R.color.success_stroke_color)
        };
        if ((System.currentTimeMillis() - currtime) >= 1000) {
            if (curr > colors.length - 1)
                curr = 0;
            return colors[curr++];
        }
        return 0;
    }
}
