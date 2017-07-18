package com.wolf.xscript.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.devtools.simple.compiler.Project;
import com.google.devtools.simple.compiler.XInit;
import com.wolf.util.Utils;
import com.wolf.xscript.R;
import com.wolf.xscript.XCompile;
import com.wolf.xscript.activity.ProjectListActivity;
import com.wolf.xscript.data.OneProjectData;
import com.wolf.xscript.project.LoadProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by GreyWolf on 2016/5/17.
 */
public class ProjectListAdapter extends BaseAdapter {

    private List<OneProjectData> allProject = null;
    private Context context;

    public ProjectListAdapter(Context c) {
        this.context = c;
        refreshData();
    }

    /**
     * 重新加载数据
     */
    public void refreshData() {
        if (allProject == null) allProject = new ArrayList<OneProjectData>();
        loadAllProject();
        this.notifyDataSetChanged();
        if (getCount() == 0) {
            ProjectListActivity.emptytv.setVisibility(View.VISIBLE);
            ProjectListActivity.project_listview.setVisibility(View.GONE);
        } else {
            ProjectListActivity.emptytv.setVisibility(View.GONE);
            ProjectListActivity.project_listview.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getCount() {
        return allProject.size();
    }

    @Override
    public Object getItem(int position) {
        return allProject.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.projectlistitem, null);
            holder.icon = (ImageView) convertView.findViewById(R.id.project_icon);
            holder.name = (TextView) convertView.findViewById(R.id.project_name);
            holder.describe = (TextView) convertView.findViewById(R.id.project_describe);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        OneProjectData data = allProject.get(position);
        holder.icon.setImageBitmap(data.icon);
        holder.name.setText(data.name);
        holder.describe.setText("创建时间：" + data.createdate + "\n" +
                "版本：" + data.versionname + "\n" +
                "入口：" + data.main);
        return convertView;
    }

    class ViewHolder {
        public ImageView icon;
        public TextView name;
        public TextView describe;
    }

    private void loadAllProject() {
        allProject.clear();
        File dir = new File(XInit.ProjectDir);
        if (!dir.exists()) return;
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                String propertiefilepath = f.getAbsolutePath() + File.separator + XCompile.propertiesDir
                        + File.separator + "project.properties";
                if (new File(propertiefilepath).exists() && new File(propertiefilepath).isFile()) {
                    try {
                        OneProjectData oneProjectData = LoadProject.load(context, propertiefilepath);
                        if (oneProjectData != null)
                            allProject.add(oneProjectData);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.log("load本地项目异常" + e.toString());
                    }
                }
            }
        }
    }
}
