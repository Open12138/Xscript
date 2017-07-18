package com.wolf.xscript.activity;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.danielnilsson9.colorpickerview.dialog.ColorPickerDialogFragment;
import com.wolf.view.filedialog.OpenFileDialog;
import com.wolf.xscript.R;
import com.wolf.xscript.XCompile;

import net.youmi.android.AdManager;
import net.youmi.android.spot.SpotManager;


public class MainActivity extends AppCompatActivity implements ColorPickerDialogFragment.ColorPickerDialogListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAD();
        ((Button) findViewById(R.id.compile)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                XCompile.compile(MainActivity.this, Environment.getExternalStorageDirectory().getPath() + "/XProject/Tetris");
            }
        });

        startActivity(new Intent(this, ProjectListActivity.class));
        finish();
    }

    private void initAD() {
        AdManager.getInstance(this).init("f5a99b3390bbdcd6", "2dcf287e4bdbf363", false);
        SpotManager.getInstance(this).loadSpotAds();
        SpotManager.getInstance(this).setSpotOrientation(SpotManager.ORIENTATION_PORTRAIT);
        SpotManager.getInstance(this).setAnimationType(SpotManager.ANIM_ADVANCE);

//        SpotManager.getInstance(this).showSpotAds(this);
    }

    public void color(View v) {

        ColorPickerDialogFragment f = ColorPickerDialogFragment
                .newInstance(1, null, null, Color.GREEN, true);

        f.setStyle(DialogFragment.STYLE_NORMAL, R.style.LightPickerDialogTheme);
        f.show(getFragmentManager(), "d");
    }

    public void selectfile(View v) {
        OpenFileDialog.show(this, "选择文件", new OpenFileDialog.CallbackBundle() {
            @Override
            public void callback(Bundle bundle) {
                OpenFileDialog.hide();
                final String filepath = bundle.getString("path");
                Toast.makeText(MainActivity.this, filepath, Toast.LENGTH_SHORT).show();
            }
        }, ".apk");
       /* try {
            UnzipAssets.ua(getApplicationContext(), "Tetris.zip", XInit.ProjectDir, true);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "复制异常", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }*/
    }

    public void dialog(View v) {
        startActivity(new Intent(this, TestActivity.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        OpenFileDialog.reset();
    }

    @Override
    public void onColorSelected(int dialogId, int color) {

    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }
}
