package com.vpaliy.studioq.activities.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.vpaliy.studioq.App;
import com.vpaliy.studioq.model.MediaFolder;
import com.vpaliy.studioq.services.DataService;
import com.vpaliy.studioq.utils.Permissions;
import com.vpaliy.studioq.utils.ProjectUtils;
import java.util.ArrayList;
import static com.vpaliy.studioq.utils.ProjectUtils.INIT;
import static com.vpaliy.studioq.utils.ProjectUtils.MEDIA_DATA;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!Permissions.requestIfNotAllowed(SplashActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE,
                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                ProjectUtils.ACCESS_TO_EXTERNAL_STORAGE)) {
            makeQuery();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ProjectUtils.ACCESS_TO_EXTERNAL_STORAGE:
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                    makeQuery();
                }else {
                    Toast.makeText(this,"Give me the permission!",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void makeQuery() {
        DataService service= App.appInstance().dataService();
        if(service!=null) {
            //fetch out the data of the service
        }else {
            new DataProvider(this) {
                @Override
                public void onPostExecute(ArrayList<MediaFolder> mediaFolders) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra(INIT, true);
                    intent.putExtra(MEDIA_DATA, mediaFolders);
                    startActivity(intent);
                }
            };
        }
    }
}
