package com.wolf.net;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class Downloads {
    static URLConnection conn;
    static int downLoadFileSize;
    static int fileSize;
    ProgressDialog dialog;
    Handler handler;

    UP uup = null;

    class AnonymousClass_1 implements Runnable {
        final Downloads this$0;
        private final String val$Url;
        private final String val$path;

        AnonymousClass_1(Downloads r1_Downloads, String r2_String, String r3_String) {
            super();
            this$0 = r1_Downloads;
            val$Url = r2_String;
            val$path = r3_String;
        }

        public void run() {
            try {
                this$0.down_file(val$Url, val$path);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    class AnonymousClass_2 extends Handler {
        final /* synthetic */ Downloads this$0;
        private final /* synthetic */ Context val$context;
        private final /* synthetic */ String val$path;

        AnonymousClass_2(Downloads r1_Downloads, String r2_String, Context r3_Context) {
            super();
            this$0 = r1_Downloads;
            val$path = r2_String;
            val$context = r3_Context;
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                this$0.dialog.setMax(((Integer) msg.obj).intValue());
            } else if (msg.what == 1) {
                this$0.dialog.setProgress(downLoadFileSize);
            } else if (msg.what == 2) {
                this$0.dialog.dismiss();
                uup.ok(val$path);
            }
        }
    }


    static {
        fileSize = 0;
        downLoadFileSize = 0;
    }

    public Downloads(Context context, String Url, String path, UP up) {
        super();
        this.uup = up;
        handler = new Handler();
        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(1);
        dialog.setCancelable(false);
        dialog.setTitle("下载中...");
        dialog.show();
        new Thread(new AnonymousClass_1(this, Url, path)).start();
        handler = new AnonymousClass_2(this, path, context);
    }

    private void sendMsg(int flag) {
        Message msg = new Message();
        msg.what = flag;
        handler.sendMessage(msg);
    }

    public void down_file(String url, String path) throws IOException {
        conn = new URL(url).openConnection();
        conn.connect();
        InputStream is = conn.getInputStream();
        fileSize = conn.getContentLength();
        Message msg = new Message();
        msg.what = 0;
        msg.obj = Integer.valueOf(conn.getContentLength());
        handler.sendMessage(msg);
        if (fileSize <= 0) {
            throw new RuntimeException("\u65e0\u6cd5\u83b7\u77e5\u6587\u4ef6\u5927\u5c0f ");
        } else if (is == null) {
            throw new RuntimeException("stream is null");
        } else {
            FileOutputStream fos = new FileOutputStream(path);
            byte[] buf = new byte[1024];
            while (true) {
                int numread = is.read(buf);
                if (numread == -1) {
                    sendMsg(2);
                    try {
                        is.close();
                        return;
                    } catch (Exception e) {
                        Throwable ex = e;
                        Log.e("tag", new StringBuilder("error: ").append(ex.getMessage()).toString(), ex);
                        return;
                    }
                } else {
                    fos.write(buf, 0, numread);
                    downLoadFileSize += numread;
                    Message downloadsize = new Message();
                    downloadsize.what = 1;
                    downloadsize.obj = downloadsize;
                    handler.sendMessage(downloadsize);
                }
            }
        }
    }

    public interface UP {
        public void ok(String path);
    }
}
