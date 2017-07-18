package com.wolf.xscript.uidesigner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.devtools.simple.util.Files;
import com.wolf.xscript.R;

import java.io.File;
import java.io.IOException;

public class UIDesignerActivity extends AppCompatActivity {
    String xfilepath;//要编辑的.x文件路径


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        xfilepath = getIntent().getStringExtra("xfile");
        setContentView(R.layout.activity_uidesigner);
        try {
            UIParse parser = new UIParse(Files.read(new File(xfilepath), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void result(View v) {
        back();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        Intent intt = new Intent();
        intt.putExtra("xfile", xfilepath);
        setResult(0, intt);
        finish();
    }
}
