package com.google.devtools.simple.compiler;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 初始化一些文件及目录
 * Created by GreyWolf on 2016/5/14.
 */
public class XInit {
    public static final String[] files = {"aapt", "xruntime.jar", "android.jar"};
    public static final String ProjectDir = Environment.getExternalStorageDirectory().getPath() + File.separator + "XProject";
    public static String XRunTime;//xruntime.jar路径
    public static String AAPT;//aapt路径
    public static String ANDROID_JAR;//android.jar路径

    /**
     * 初始化文件
     *
     * @param context
     */
    public static void init(Context context) {
        if (!new File(ProjectDir).exists())
            new File(ProjectDir).mkdirs();
        for (String s : files) {
            copy(context, s);
        }
        XRunTime = context.getFilesDir().getPath() + File.separator + "xruntime.jar";
        AAPT = context.getFilesDir().getPath() + File.separator + "aapt";
        ANDROID_JAR = context.getFilesDir().getPath() + File.separator + "android.jar";
    }

    public static void copy(Context context, String name) {
        File dir = context.getFilesDir();
        String path = dir.getAbsolutePath() + File.separator + name;
        if (new File(path).exists())
            return;
        copyAssets(context, name, dir.getAbsolutePath());
    }

    public static void copyAssets(Context context, String assetsfilename, String outdir) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = context.getAssets().open(assetsfilename);
            out = new FileOutputStream(new File(outdir + File.separator + assetsfilename));
            byte[] b = new byte[1024 * 8];
            int n;
            while ((n = in.read(b)) != -1) {
                out.write(b, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
