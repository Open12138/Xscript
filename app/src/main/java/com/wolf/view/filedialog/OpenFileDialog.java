// filename: OpenFileDialog.java
package com.wolf.view.filedialog;

import android.content.Context;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.wolf.xscript.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenFileDialog {
    public static String tag = "OpenFileDialog";
    static final public String sRoot = "/";
    static final public String sParent = "..";
    static final public String sFolder = ".";
    static final public String sEmpty = "";
    static final private String sOnErrorMsg = "No rights to access!";

    // 参数说明
    // context:上下文
    // dialogid:对话框ID
    // title:对话框标题
    // callback:一个传递Bundle参数的回调接口
    // suffix:需要选择的文件后缀，比如需要选择wav、mp3文件的时候设置为".wav;.mp3;"，注意最后需要一个分号(;)
    // images:用来根据后缀显示的图标资源ID。
    //	根目录图标的索引为sRoot;
    //	父目录的索引为sParent;
    //	文件夹的索引为sFolder;
    //	默认图标的索引为sEmpty;
    //	其他的直接根据后缀进行索引，比如.wav文件图标的索引为"wav"
    public static AlertDialog dialog = null;

    public static void show(Context context, String title, CallbackBundle callback, String suffix) {
        if (dialog == null) {
            dialog = createDialog(context, title, callback, suffix);
        }
        dialog.show();
    }

    public static void hide() {
        if (dialog != null) {
            dialog.cancel();
        }
    }

    public static void reset() {
        dialog = null;
    }

    public static AlertDialog createDialog(Context context, String title, CallbackBundle callback, String suffix) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        Map<String, Integer> images = new HashMap<String, Integer>();
        // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
        images.put(OpenFileDialog.sRoot, R.mipmap.filedialog_root);    // 根目录图标
        images.put(OpenFileDialog.sParent, R.mipmap.filedialog_folder_up);    //返回上一层的图标
        images.put(OpenFileDialog.sFolder, R.mipmap.filedialog_folder);    //文件夹图标
        images.put(OpenFileDialog.sEmpty, R.mipmap.filedialog_root);
        images.put("apk", R.mipmap.ic_launcher);
        images.put("c", R.mipmap.file_type_c);
        images.put("cpp", R.mipmap.file_type_cpp);
        images.put("css", R.mipmap.file_type_css);
        images.put("h", R.mipmap.file_type_h);
        images.put("java", R.mipmap.file_type_java);
        images.put("js", R.mipmap.file_type_js);
        images.put("txt", R.mipmap.file_type_txt);
        images.put("", R.mipmap.file_type_unknown);
        images.put("xml", R.mipmap.file_type_xml);
        images.put("zip", R.mipmap.format_zip);
        images.put("rar", R.mipmap.format_zip);
        images.put("jar", R.mipmap.format_zip);
        builder.setView(new FileSelectView(context, callback, suffix, images));
        AlertDialog dialog = builder.create();
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setTitle(title + " *" + suffix);
        dialog.setIcon(R.mipmap.ic_launcher);

        return dialog;
    }

    static class FileSelectView extends ListView implements OnItemClickListener {


        private CallbackBundle callback = null;
        private String path = sRoot;
        private List<Map<String, Object>> list = null;

        private String suffix = null;

        private Map<String, Integer> imagemap = null;

        public FileSelectView(Context context, CallbackBundle callback, String suffix, Map<String, Integer> images) {
            super(context);
            this.imagemap = images;
            this.suffix = suffix.toLowerCase();
            this.callback = callback;
            this.setFastScrollEnabled(true);
            this.setOnItemClickListener(this);
            refreshFileList();
        }

        private int getImageId(String s) {
            if (imagemap == null) {
                return 0;
            } else if (imagemap.containsKey(s)) {
                return imagemap.get(s);
            } else if (imagemap.containsKey(sEmpty)) {
                return imagemap.get(sEmpty);
            } else {
                return 0;
            }
        }

        private int refreshFileList() {
            // 刷新文件列表
            File[] files = null;
            try {
                files = new File(path).listFiles();
            } catch (Exception e) {
                files = null;
            }
            if (files == null) {
                // 访问出错
                Toast.makeText(getContext(), sOnErrorMsg, Toast.LENGTH_SHORT).show();
                return -1;
            }
            if (list != null) {
                list.clear();
            } else {
                list = new ArrayList<Map<String, Object>>(files.length);
            }

            // 用来先保存文件夹和文件夹的两个列表
            ArrayList<Map<String, Object>> lfolders = new ArrayList<Map<String, Object>>();
            ArrayList<Map<String, Object>> lfiles = new ArrayList<Map<String, Object>>();

            List<String> strfolder = new ArrayList<String>();
            List<String> strfile = new ArrayList<String>();
            for (File file : files) {
                if (file.isDirectory() && file.listFiles() != null) {
                    // 添加文件夹
                    strfolder.add(file.getName());
                }
            }
            for (File file : files) {
                if (file.isFile()) {
                    // 添加文件
                    strfile.add(file.getName());
                }
            }
            Collections.sort(strfolder);
            Collections.sort(strfile);
            for (String fd : strfolder) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("name", fd);
                for (File f : files) {
                    if (f.getName().equals(fd)) {
                        map.put("path", f.getPath());
                    }
                }
                map.put("img", getImageId(sFolder));
                lfolders.add(map);
            }
            strfolder.clear();
            strfolder = null;
            for (String f1 : strfile) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("name", f1);
                for (File f : files) {
                    if (f.getName().equals(f1)) {
                        map.put("path", f.getPath());
                    }
                }
                map.put("img", getImageId(getSuffix(f1.toLowerCase())));
                lfiles.add(map);
            }
            strfile.clear();
            strfile = null;
            if (!this.path.equals(sRoot)) {
                // 添加根目录 和 上一层目录
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("name", sRoot);
                map.put("path", sRoot);
                map.put("img", getImageId(sRoot));
                list.add(map);

                map = new HashMap<String, Object>();
                map.put("name", sParent);
                map.put("path", path);
                map.put("img", getImageId(sParent));
                list.add(map);
            }
            list.addAll(lfolders); // 先添加文件夹，确保文件夹显示在上面
            list.addAll(lfiles);    //再添加文件


            SimpleAdapter adapter = new SimpleAdapter(getContext(), list, R.layout.filedialogitem, new String[]{"img", "name", "path"}, new int[]{R.id.filedialogitem_img, R.id.filedialogitem_name, R.id.filedialogitem_path});
            this.setAdapter(adapter);
            return files.length;
        }

        private String getSuffix(String filename) {
            int dix = filename.lastIndexOf('.');
            if (dix < 0) {
                return "";
            } else {
                return filename.substring(dix + 1);
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            // 条目选择
            String pt = (String) list.get(position).get("path");
            String fn = (String) list.get(position).get("name");
            if (fn.equals(sRoot) || fn.equals(sParent)) {
                // 如果是更目录或者上一层
                File fl = new File(pt);
                String ppt = fl.getParent();
                if (ppt != null) {
                    // 返回上一层
                    path = ppt;
                } else {
                    // 返回更目录
                    path = sRoot;
                }
            } else {
                File fl = new File(pt);
                if (fl.isFile() && fl.getName().endsWith(suffix)) {
                    // 设置回调的返回值
                    Bundle bundle = new Bundle();
                    bundle.putString("path", pt);
                    bundle.putString("name", fn);
                    // 调用事先设置的回调函数
                    this.callback.callback(bundle);
                    return;
                } else if (fl.isFile() && !fl.getName().endsWith(suffix)) {
                    return;
                } else if (fl.isDirectory()) {
                    // 如果是文件夹
                    // 那么进入选中的文件夹
                    path = pt;
                }
            }
            this.refreshFileList();
        }
    }

    public interface CallbackBundle {
        abstract void callback(Bundle bundle);
    }
}
