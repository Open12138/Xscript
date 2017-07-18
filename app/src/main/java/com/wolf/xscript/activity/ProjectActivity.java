package com.wolf.xscript.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.devtools.simple.util.Files;
import com.wolf.util.Utils;
import com.wolf.util.tint.StatusBarUtil;
import com.wolf.xscript.R;
import com.wolf.xscript.XCompile;
import com.wolf.xscript.adapter.ProjectFileListAdapter;
import com.wolf.xscript.data.OneProjectData;
import com.wolf.xscript.project.LoadProject;
import com.wolf.xscript.uidesigner.UIDesignerActivity;
import com.wolf.xscript.uidesigner.UIParse;

import java.io.File;
import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProjectActivity extends BaseActivity {

    private String propertiespath = "";//传递过来的properties文件路径
    private ImageView project_icon;//项目图标
    private TextView project_name;//项目名字
    private TextView project_packagename;//项目包名
    private OneProjectData thisProjectData;//项目数据
    private ProjectFileListAdapter fileadapter = null;
    private EditText codeEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        propertiespath = getIntent().getStringExtra("path");
        super.setContentView(R.layout.activity_project);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                saveSource();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        init();
        initXCodeEditor();
    }

    /*****
     * 代码编辑框
     ******/

    private String currentEditFilePath = "";


    /**
     * 初始化
     */
    private void initXCodeEditor() {
        codeEditor = (EditText) findViewById(R.id.mycodeedittext);
        codeEditor.setMinEms(15);
        setSource(propertiespath);
    }

    private void closeEditor() {
        currentEditFilePath = "";
        setSource("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setSource(data.getStringExtra("xfile"));
    }

    private void setSource(String path) {
        try {
            if (new File(path).exists()) {
                codeEditor.setText(Files.read(new File(path), "UTF-8"));
                currentEditFilePath = path;
            } else {
                codeEditor.setText("");
            }
        } catch (IOException e) {
            e.printStackTrace();
            codeEditor.setText("");
        }
        for (int i = 0; i < 30; i++) {
            codeEditor.append("\n");
        }
        codeEditor.setSelection(0);
    }

    private void saveSource() {
        if (!new File(currentEditFilePath).exists()) {
            Toast.makeText(ProjectActivity.this, "当前没有打开文件", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuffer sb = new StringBuffer(codeEditor.getText().toString());
        int i = sb.length() - 1;
        while (sb.charAt(i) == '\n') {
            sb.deleteCharAt(i--);
        }
        try {
            Files.write(sb.toString().getBytes("UTF-8"), new File(currentEditFilePath));
            Toast.makeText(ProjectActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*****代码编辑框******/

    /**
     * 初始化控件
     */
    private void initView() {
        project_icon = (ImageView) findViewById(R.id.img_project_icon);
        project_name = (TextView) findViewById(R.id.tv_project_name);
        project_packagename = (TextView) findViewById(R.id.tv_project_packagename);
        ListView filelistview = (ListView) findViewById(R.id.project_file_list);
        if (fileadapter == null)
            fileadapter = new ProjectFileListAdapter(this, new File(propertiespath).getParentFile().getParent());
        filelistview.setAdapter(fileadapter);
        filelistview.setOnItemClickListener(filelistviewitemClick);
        filelistview.setOnItemLongClickListener(filelistlongclick);
        filelistview.setFastScrollEnabled(true);
    }

    private AdapterView.OnItemClickListener filelistviewitemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String currpath = fileadapter.getCurrentFilePath(position);
            if (currpath.endsWith(".x") || currpath.endsWith(".properties")) {
                setSource(currpath);
                closeDrawer();
            }
            fileadapter.itemclick(position);
        }
    };
    private AdapterView.OnItemLongClickListener filelistlongclick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            fileadapter.itemLongClick(position);
            return true;
        }
    };

    /**
     * 初始化
     */
    private void init() {
        initView();//找到控件
        thisProjectData = LoadProject.load(this, propertiespath);
        project_icon.setImageBitmap(thisProjectData.icon);
        project_name.setText(thisProjectData.name);
        project_packagename.setText(thisProjectData.packagename);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("确定退出?")
                    .setContentText("确保已经保存所有文件!")
                    .setConfirmText("退出")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            startActivity(new Intent(ProjectActivity.this, ProjectListActivity.class));
                            finish();
                        }
                    })
                    .setCancelText("取消")
                    .setCancelClickListener(null);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.project, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ((item.getItemId())) {
            case R.id.compile:
                XCompile.compile(this, new File(propertiespath).getParentFile().getParent());
                break;
            case R.id.close:
                closeEditor();
                break;
            case R.id.closeproject:
                startActivity(new Intent(ProjectActivity.this, ProjectListActivity.class));
                finish();
                break;
            case R.id.deleteproject:
                //删除当前工程
                SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("删除当前工程?")
                        .setContentText("删除后不能恢复！")
                        .setConfirmText("删除")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                Utils.deleteFile(new File((new File(propertiespath).getParentFile().getParent())));
                                sDialog.dismissWithAnimation();
                                startActivity(new Intent(ProjectActivity.this, ProjectListActivity.class));
                                finish();
                            }
                        })
                        .setCancelText("取消")
                        .setCancelClickListener(null);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                break;
            case R.id.uidesigner:
                if (currentEditFilePath.endsWith(".x")) {
                    Intent intt = new Intent(this, UIDesignerActivity.class);
                    intt.putExtra("xfile", currentEditFilePath);
                    startActivityForResult(intt, 0);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 关闭左侧侧滑
     */
    private void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    /**
     * 打开左侧侧滑
     */
    private void openDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }
    /*
    *  DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);*/

    /**
     * 沉浸状态栏
     */
    @Override
    protected void setStatusBar() {
        int mAlpha = StatusBarUtil.DEFAULT_STATUS_BAR_ALPHA;
        int mStatusBarColor = getResources().getColor(R.color.colorPrimary);
        StatusBarUtil.setColorForDrawerLayout(this, (DrawerLayout) findViewById(R.id.drawer_layout), mStatusBarColor, mAlpha);
    }
}
