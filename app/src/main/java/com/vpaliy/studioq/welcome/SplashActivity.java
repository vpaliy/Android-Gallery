package com.vpaliy.studioq.welcome;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.vpaliy.studioq.activities.MainActivity;
import com.vpaliy.studioq.common.dataUtils.Subscriber;
import com.vpaliy.studioq.controllers.DataController;
import com.vpaliy.studioq.common.utils.Permissions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.vpaliy.studioq.common.utils.ProjectUtils.INIT;
import static com.vpaliy.studioq.common.utils.ProjectUtils.ACCESS_TO_EXTERNAL_STORAGE;

public class SplashActivity extends AppCompatActivity
        implements Subscriber {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataController.controllerInstance().subscribe(this);
        if(!Permissions.requestIfNotAllowed(SplashActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE,
                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE},ACCESS_TO_EXTERNAL_STORAGE)) {
           DataController.controllerInstance().makeQueryIfEmpty();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==ACCESS_TO_EXTERNAL_STORAGE) {
            switch (grantResults[0]) {
                case PackageManager.PERMISSION_GRANTED:
                    DataController.controllerInstance().makeQuery();
                    break;
                case PackageManager.PERMISSION_DENIED:
                    Toast.makeText(this,"Give me the permission!",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }


    @Override
    public void notifyAboutChange() {
        DataController.controllerInstance()
            .unSubscribe(this);
        //start the main activity
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(INIT, true);
        startActivity(intent);
    }


}
