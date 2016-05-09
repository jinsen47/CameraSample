package com.github.jinsen47.camerasample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.cam2.AbstractCameraActivity;
import com.commonsware.cwac.cam2.Facing;
import com.commonsware.cwac.cam2.FocusMode;
import com.commonsware.cwac.cam2.VideoRecorderActivity;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 100;
    private static final int REQUEST_TAKE_VIDEO = 101;

    private Button mButtonTakePhoto;
    private Button mButtonTakeVideo;
    private TextView mTextOutput;

    private File mTempFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonTakeVideo = ((Button) findViewById(R.id.btn_take_video));
        mButtonTakePhoto = ((Button) findViewById(R.id.btn_take_photo));
        mTextOutput = ((TextView) findViewById(R.id.text_output));

        mButtonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mTempFile = FileUtils.createTmpFile(MainActivity.this);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempFile));
                    startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mButtonTakeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mTempFile = FileUtils.createVideoFile(MainActivity.this);

                    VideoRecorderActivity.IntentBuilder builder = new VideoRecorderActivity.IntentBuilder(MainActivity.this);
                    builder.to(mTempFile);
                    builder.quality(AbstractCameraActivity.Quality.LOW);
                    builder.facing(Facing.BACK);
                    builder.facingExactMatch();
                    builder.debug();
                    builder.updateMediaStore();
                    builder.durationLimit(10 * 1000);
                    builder.focusMode(FocusMode.CONTINUOUS);
                    builder.debug();
                    Intent intent = builder.build();

                    startActivityForResult(intent, REQUEST_TAKE_VIDEO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied(ArrayList<String> arrayList) {
                Toast.makeText(MainActivity.this, "权限被禁用，无法使用部分功能", Toast.LENGTH_SHORT).show();
                MainActivity.this.finish();
            }
        };

        TedPermission ted = new TedPermission(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("请允许摄像拍照权限")
                .setPermissions(Manifest.permission.CAMERA);
        ted.check();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        if (requestCode == REQUEST_TAKE_PHOTO) {
            mTextOutput.append(mTempFile.getPath().toString() + "\n");
        } else if (requestCode == REQUEST_TAKE_VIDEO) {
            mTextOutput.append(mTempFile.getPath().toString() + "\n");
        }
    }
}