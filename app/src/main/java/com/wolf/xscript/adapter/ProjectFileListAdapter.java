package com.wolf.xscript.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.devtools.simple.util.Files;
import com.wolf.util.Utils;
import com.wolf.xscript.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by GreyWolf on 2016/5/19.
 */
public class ProjectFileListAdapter extends BaseAdapter {
    private List<String> filelist;
    private Context context;
    private String currpath;
    private String rootpath;

    public ProjectFileListAdapter(Context c, String projectpath) {
        this.context = c;
        this.currpath = this.rootpath = projectpath;//项目根目录
        filelist = new ArrayList<String>();
        refresh();
    }

    /**
     * 刷新子目录
     */
    private void refresh() {
        filelist.clear();
        filelist.add("/root");
        filelist.add("/new");
        filelist.add("/up");
        File rootfile = new File(currpath);
        if (!rootfile.exists() || !rootfile.isDirectory()) return;
        List<String> folders = new ArrayList<String>();
        List<String> files = new ArrayList<String>();
        for (File f : rootfile.listFiles()) {
            if (f.isDirectory())
                folders.add(f.getName());
            else if (f.isFile())
                files.add(f.getName());
        }
        Comparator<String> com = new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        };
        Collections.sort(folders, com);
        Collections.sort(files, com);
        filelist.addAll(folders);
        filelist.addAll(files);
//        Utils.log("目录个数=" + filelist.size());
        this.notifyDataSetChanged();
    }

    /**
     * 返回上级目录
     */
    private void gotoParent() {
        File fcurr = new File(currpath);
        File root = new File(rootpath);
        if (fcurr.getAbsolutePath().equals(root.getAbsolutePath())) return;
        currpath = fcurr.getParent();
        refresh();
    }

    /**
     * 进入子目录
     *
     * @param position
     */
    private void gotoSub(int position) {
        if (position >= 0 && position < filelist.size()) {
            String name = filelist.get(position);
            if (name.equals("") || name == null) return;
            if (new File(currpath + File.separator + name).isFile()) return;
            currpath += File.separator + name;
            refresh();
        }
    }

    /**
     * 进入项目根目录
     */
    private void gotoRoot() {
        currpath = rootpath;
        refresh();
    }

    /**
     * 获取当前文件绝对路径
     *
     * @param position
     * @return
     */
    public String getCurrentFilePath(int position) {
        return currpath + File.separator + filelist.get(position);
    }

    /**
     * 列表项被点击
     *
     * @param position
     */
    public void itemclick(int position) {
        if (position == 0)
            gotoRoot();
        else if (position == 1) {
            createNew();
        } else if (position == 2) {
            gotoParent();
        } else {
            gotoSub(position);
        }
    }

    /**
     * 列表项长按
     *
     * @param positon
     */
    public void itemLongClick(final int positon) {
        if (positon > 2) {
            SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("删除" + filelist.get(positon) + "?")
                    .setContentText("删除后不能恢复！")
                    .setConfirmText("删除")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            // reuse previous dialog instance
                            String pa = currpath + File.separator + filelist.get(positon);
                            Utils.deleteFile(new File(pa));
                            sDialog.setTitleText("已删除")
                                    .setConfirmText("OK")
                                    .setContentText("")
                                    .showCancelButton(false)
                                    .setConfirmClickListener(null)
                                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            refresh();
                        }
                    })
                    .setCancelText("取消")
                    .setCancelClickListener(null);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        }
    }


    /**
     * 新建文件或目录
     */
    private void createNew() {
        final String[] type_new = {
                "新建文件夹",
                "新建.x文件",
                "新建文件"
        };
        new AlertDialog.Builder(context).setIcon(R.mipmap.ic_launcher)
                .setTitle("新建")
                .setItems(type_new, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                newFile(type_new[which], true, "");
                                break;
                            case 1:
                                newFile(type_new[which], false, ".x");
                                break;
                            case 2:
                                newFile(type_new[which], false, "");
                                break;
                        }
                    }
                }).create().show();
    }

    private void newFile(final String title, final boolean isDir, final String houzhui) {
        final EditText editText = new EditText(context);
        new AlertDialog.Builder(context)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(title)
                .setView(editText)
                .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editText.getText().toString().equals("")) {
                            Toast.makeText(context, "请输入文件名", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (isDir) {
                            if (new File(currpath + File.separator + editText.getText().toString().trim()).mkdirs())
                                Toast.makeText(context, context.getResources().getString(R.string.createSuccess), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(context, context.getResources().getString(R.string.createFailed), Toast.LENGTH_SHORT).show();
                        } else {
                            if (houzhui.equals("")) {
                                try {
                                    if (new File(currpath + File.separator + editText.getText().toString().trim()).createNewFile())
                                        Toast.makeText(context, context.getResources().getString(R.string.createSuccess), Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(context, context.getResources().getString(R.string.createFailed), Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    Toast.makeText(context, context.getResources().getString(R.string.createException), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            } else {
                                String s = editText.getText().toString().trim();
                                if (!s.endsWith(houzhui))
                                    s += houzhui;
                                try {
                                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                                    if (new File(currpath + File.separator + s).createNewFile()) {
                                        if (houzhui.endsWith(".x"))
                                            Files.write(("'Created on " + sf.format(new Date())).getBytes("UTF-8"), new File(currpath + File.separator + s));
                                        Toast.makeText(context, context.getResources().getString(R.string.createSuccess), Toast.LENGTH_SHORT).show();
                                    } else
                                        Toast.makeText(context, context.getResources().getString(R.string.createFailed), Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    Toast.makeText(context, context.getResources().getString(R.string.createException), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }

                            }
                        }
                        refresh();
                    }
                }).setNegativeButton("取消", null).create().show();
    }

    @Override
    public int getCount() {
        return filelist.size();
    }

    @Override
    public Object getItem(int position) {
        return filelist.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.project_file_item, null);
            holder.file_icon = (ImageView) convertView.findViewById(R.id.img_file_type);
            holder.file_name = (TextView) convertView.findViewById(R.id.tv_project_fie_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String f = filelist.get(position);
        holder.file_name.setText(f);
        File file = new File(currpath + File.separator + f);
        int type;
        if (file.isDirectory()) {
            type = R.mipmap.filedialog_folder;
        } else if (f.equals("/root")) {
            holder.file_name.setText(currpath + File.separator);
            type = R.mipmap.filedialog_root;
        } else if (f.equals("/up")) {
            holder.file_name.setText("返回上一级");
            type = R.mipmap.filedialog_folder_up;
        } else if (f.equals("/new")) {
            type = R.mipmap.file_new;
            holder.file_name.setText("新建");
        } else if (f.endsWith(".x")) {
            type = R.mipmap.file_type_java;
        } else if (f.endsWith(".properties")) {
            type = R.mipmap.file_modfieid;
        } else if (f.endsWith(".xml")) {
            type = R.mipmap.file_type_xml;
        } else if (f.endsWith(".apk")) {
            type = R.mipmap.ic_launcher;
        } else {
            type = R.mipmap.file_type_unknown;
        }
        holder.file_icon.setImageResource(type);
        return convertView;
    }

    class ViewHolder {
        public ImageView file_icon;
        public TextView file_name;
    }
}
