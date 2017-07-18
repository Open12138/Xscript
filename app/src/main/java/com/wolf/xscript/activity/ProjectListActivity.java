package com.wolf.xscript.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.devtools.simple.compiler.XInit;
import com.google.devtools.simple.util.Files;
import com.wolf.net.Downloads;
import com.wolf.util.UnzipAssets;
import com.wolf.util.Utils;
import com.wolf.xscript.R;
import com.wolf.xscript.adapter.ProjectListAdapter;
import com.wolf.xscript.data.OneProjectData;

import net.youmi.android.AdManager;
import net.youmi.android.update.AppUpdateInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProjectListActivity extends BaseActivity {

    private ProjectListAdapter projectListAdapter;
    public static TextView emptytv;
    public static ListView project_listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setTitle("Xscript" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + "测试版");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        super.setContentView(R.layout.activity_project_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                newProject();
            }
        });
        initView();
        try {
            //更新
            new UpdateHelper(this).execute();
        } catch (Exception e) {
            Toast.makeText(ProjectListActivity.this, "检查更新异常", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        emptytv = (TextView) findViewById(R.id.emptytv);
        project_listview = (ListView) findViewById(R.id.project_listview);
        projectListAdapter = new ProjectListAdapter(this);
        project_listview.setAdapter(projectListAdapter);
        project_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intt = new Intent(ProjectListActivity.this, ProjectActivity.class);
                intt.putExtra("path", ((OneProjectData) projectListAdapter.getItem(position)).propertiespath);
                startActivity(intt);
                finish();
            }
        });
    }

    private void newProject() {
        new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher)
                .setTitle("新建工程")
                .setItems(new String[]{
                                "新建项目",
                                "俄罗斯方块"
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        createNewProject();
                                        break;
                                    case 1:
                                        loadTetrisDemo();
                                        break;
                                }
                            }
                        }).create().show();
    }

    private void createNewProject() {
        View v = LayoutInflater.from(this).inflate(R.layout.newprojectdialog, null);
        final EditText name = (EditText) v.findViewById(R.id.appname);
        final EditText packagename = (EditText) v.findViewById(R.id.apppackagename);
        new AlertDialog.Builder(this).setTitle("新建项目")
                .setIcon(R.mipmap.ic_launcher)
                .setView(v)
                .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String appName = name.getText().toString().trim();
                        String appPn = packagename.getText().toString().trim();
                        if (appName.equals("") || appPn.equals("")) {
                            Toast.makeText(ProjectListActivity.this, "信息输入不完整", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (appPn.startsWith(".") || appPn.endsWith(".")) {
                            Toast.makeText(ProjectListActivity.this, "包名输入有误", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String[] tt = {" ", "#", "*", "(", ")", "$", "&", "@"};
                        for (String t : tt)
                            appPn = appPn.replace(t, "");
                        createnew(appName, appPn);
                    }
                }).setNegativeButton("取消", null).create().show();
    }

    /**
     * 创建一个新的项目
     *
     * @param name 项目名字
     * @param pn   项目包名
     */
    private void createnew(String name, String pn) {
        File rootDir = Files.createDirectory(new File(XInit.ProjectDir), name);//项目根目录
        File src = Files.createDirectory(rootDir, "src");//src路径
        File assets = Files.createDirectory(rootDir, "assets");
        File res = Files.createDirectory(rootDir, "res");//res路径
        File res_drawable = Files.createDirectory(res, "drawable");//res/drawable
        File icon = new File(res_drawable.getAbsolutePath() + File.separator + "icon.png");
        File xscript = Files.createDirectory(rootDir, "xscript");
        File properties = new File(xscript.getAbsolutePath() + File.separator + "project.properties");

        //包路径
        File com_xxx = new File(src + File.separator + pn.replace(".", "/"));
        com_xxx.mkdirs();

        File mainForm = new File(com_xxx.getAbsolutePath() + File.separator + "MainForm.x");//入口
        try {
            //写入入口文件
            Files.write(getMainForm("MainForm").getBytes("UTF-8"), mainForm);
            //写入project.properties
            Files.write(getProperties(name, pn).getBytes("UTF-8"), properties);

            InputStream in = this.getAssets().open("ic_launcher.png");
            OutputStream out = new FileOutputStream(icon);
            byte[] b = new byte[1024];
            int n;
            while ((n = in.read(b)) != -1)
                out.write(b, 0, n);
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        projectListAdapter.refreshData();
    }

    private String getProperties(String name, String pn) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        String s = "#配置文件 *为必填\n" +
                "#入口*\n" +
                "main=" + pn + ".MainForm\n" +
                "\n" +
                "#包名  默认为入口所在的包\n" +
                "packagename=" + pn + "\n" +
                "\n" +
                "#软件名字*\n" +
                "name=" + name + "\n" +
                "\n" +
                "#assets路径*\n" +
                "assets=../assets\n" +
                "\n" +
                "#源码路径*\n" +
                "source=../src\n" +
                "\n" +
                "#生成目录*\n" +
                "build=../build\n" +
                "\n" +
                "#版本名  默认为1.0\n" +
                "versionname=1.0\n" +
                "\n" +
                "#版本号 必须是整形数字  默认为1\n" +
                "versioncode=1\n" +
                "\n" +
                "#图标路径*\n" +
                "icon=../res/drawable/icon.png\n" +
                "createdate=" + sf.format(new Date());
        return s;
    }

    private String getMainForm(String mainname) {
        String s = "'此行是注释\n\n" +
                "Event " + mainname + ".Keyboard(keycode As Integer)\n" +
                "   If keycode = Component.KEYCODE_BACK Then Finish()\n" +
                "End Event\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "$Properties\n" +
                "$Source $Form\n" +
                "$Define " + mainname + " $As Form\n" +
                "Layout = 3\n" +
                "BackgroundColor = &HFF146E6E\n" +
                "\n" +
                "$Define HelloWorld $As Label\n" +
                "BackgroundColor = &HFF146E6E\n" +
                "FontSize = 16.0\n" +
                "FontTypeface = 3\n" +
                "Text = \"HelloWorld Xscript!\"\n" +
                "TextColor = &HFFFFFFFF\n" +
                "Width = 105\n" +
                "$End $Define\n" +
                "\n" +
                "\n" +
                "$End $Define\n" +
                "$End $Properties\n";
        return s;
    }

    /**
     * 加载俄罗斯方块Demo
     */
    private void loadTetrisDemo() {
        try {
            UnzipAssets.ua(getApplicationContext(), "Tetris.zip", XInit.ProjectDir, true);
            projectListAdapter.refreshData();
        } catch (IOException e) {
            Toast.makeText(this, "复制异常", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.projectlist, menu);
        //true 显示   false  不显示
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                projectListAdapter.refreshData();
                break;
            case R.id.menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkforUpDate() {

    }

    Downloads.UP up = new Downloads.UP() {
        @Override
        public void ok(String path) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
            ProjectListActivity.this.startActivity(intent);
            ProjectListActivity.this.finish();
        }
    };

    class UpdateHelper extends AsyncTask<Void, Void, AppUpdateInfo> {
        private Context mContext;

        public UpdateHelper(Context context) {
            mContext = context;
        }

        @Override
        protected AppUpdateInfo doInBackground(Void... params) {
            try {
                // 在 doInBackground 中调用 AdManager 的 checkAppUpdate 即可从有米服务器获得应用更新信息。
                return AdManager.getInstance(mContext).syncCheckAppUpdate();
                // 此方法务必在非 UI 线程调用，否则有可能不成功。
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final AppUpdateInfo result) {
            super.onPostExecute(result);
            try {
                if (result == null || result.getUrl() == null) {
                    // 如果 AppUpdateInfo 为 null 或它的 url 属性为 null，则可以判断为没有新版本。
//                    Toast.makeText(mContext, "当前版本已经是最新版", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 这里简单示例使用一个对话框来显示更新信息
                new AlertDialog.Builder(mContext)
                        .setTitle("发现新版本")
                        .setMessage(result.getUpdateTips().replace("\\n", "\n")) // 这里是版本更新信息
                        .setPositiveButton("马上升级",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                      /*  Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(result.getUrl()));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        mContext.startActivity(intent);*/
                                        // ps：这里示例点击“马上升级”按钮之后简单地调用系统浏览器进行新版本的下载，
                                        // 但强烈建议开发者实现自己的下载管理流程，这样可以获得更好的用户体验。
                                        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "XScriptDownload";
                                        if (!new File(dir).exists()) new File(dir).mkdirs();
                                        String file = dir + File.separator + new Date().getTime() + ".apk";
                                        new Downloads(mContext, result.getUrl(), file, up);
                                    }
                                })
                        .setNegativeButton("下次再说",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }).create().show();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
