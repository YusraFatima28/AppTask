package com.example.capturevideo;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
//import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.LinearLayout;
import com.example.capturevideo.async.FramesExtractorTask;
import com.example.capturevideo.utils.FileUtils;
//import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int VIDEO_PICK_INTENT = 1;
    private static final int FRAME_PREVIEW_INTENT = 2;
    private String path;
    private LinearLayout framesBar;
    private final OnFrameClickListener listener = new OnFrameClickListener() {
        @Override
        public void onFrameClicked(int time) {
            showVideoFrame(time);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        framesBar = findViewById(R.id.framesBar);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem selectItem = menu.findItem(R.id.action_select);
        selectItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                chooseFile();
                return true;
            }
        });
        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        settingsItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                toSettings();
                return true;
            }
        });
        return true;
    }
    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        if (arg0 == VIDEO_PICK_INTENT && arg1 == RESULT_OK) {
            Uri uri = arg2.getData();
            System.out.println(uri.getPath());
            if (uri != null) {
                path = FileUtils.getRealPath(MainActivity.this, uri);
                System.out.println(path);
                clearVideoFrames();
                loadVideoFrames(path);
            }
        }
    }
    private void chooseFile() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("video/*");
        startActivityForResult(i, VIDEO_PICK_INTENT);
    }
    //@TargetApi(Build.VERSION_CODES.CUPCAKE)
    private void loadVideoFrames(String path) {
        FramesExtractorTask task = new FramesExtractorTask(framesBar,listener);
        task.execute(path);
    }
    private void clearVideoFrames() {
        framesBar.removeAllViews();
    }
    private void showVideoFrame(int time) {
        Intent i = new Intent(MainActivity.this, FrameActivity.class);
        i.putExtra(FrameActivity.PATH_ARG, path);
        i.putExtra(FrameActivity.TIME_ARG, time);
        startActivityForResult(i, FRAME_PREVIEW_INTENT);
    }
    private void toSettings() {
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(i);
    }

}
