package com.wolf.xscript.project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.devtools.simple.compiler.Project;
import com.wolf.util.Utils;
import com.wolf.xscript.R;
import com.wolf.xscript.XCompile;
import com.wolf.xscript.data.OneProjectData;

import java.io.File;

/**
 * Created by GreyWolf on 2016/5/19.
 */
public class LoadProject {
    public static OneProjectData load(Context context, String propertiefilepath) {
//        String propertiefilepath = propertiespath + File.separator + XCompile.propertiesDir
//                + File.separator + "project.properties";
        if (new File(propertiefilepath).exists() && new File(propertiefilepath).isFile()) {
            try {
                Project project = new Project(propertiefilepath, null);
                String name = project.getProjectName();//名字

                String icon_path = project.getIconPath();
                Bitmap icon;//图标
                if (!new File(icon_path).exists() || !new File(icon_path).isFile()) {
                    //icon路径不存在或者是目录
                    icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
                } else {
                    icon = BitmapFactory.decodeFile(icon_path);
                }
                String packagename = project.getXPackagename();//包名
                String versionname = project.getVersionName();
                String versioncode = project.getVersionCode();
                String main = project.getMainForm();//入口

                OneProjectData oneProjectData = new OneProjectData();
                oneProjectData.name = name;
                oneProjectData.icon = icon;
                oneProjectData.packagename = packagename;
                oneProjectData.versionname = versionname;
                oneProjectData.versioncode = versioncode;
                oneProjectData.propertiespath = propertiefilepath;
                oneProjectData.main = main;
                oneProjectData.createdate = project.getCreateDate();
                return oneProjectData;
            } catch (Exception e) {
                e.printStackTrace();
                Utils.log("load本地项目异常" + e.toString());
            }
        }
        return null;
    }
}
