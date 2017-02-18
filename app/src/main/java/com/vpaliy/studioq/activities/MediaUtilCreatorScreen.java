package com.vpaliy.studioq.activities;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.vpaliy.studioq.App;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.common.eventBus.ExitEvent;
import com.vpaliy.studioq.common.eventBus.Launcher;
import com.vpaliy.studioq.common.eventBus.Registrator;
import com.vpaliy.studioq.fragments.UtilSelectionFragment;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.fragments.MediaUtilReviewFragment;
import com.vpaliy.studioq.model.MediaFolder;
import com.vpaliy.studioq.common.utils.ProjectUtils;
import butterknife.ButterKnife;

import com.squareup.otto.Subscribe;

public class MediaUtilCreatorScreen extends AppCompatActivity {

    private static final String  TAG=MediaUtilCreatorScreen.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_util);
        ButterKnife.bind(this,MediaUtilCreatorScreen.this);
        initUI(savedInstanceState);
    }

    private void initUI(Bundle outState) {
        if(outState==null) {
            Bundle intentData = getIntent().getExtras();
            ArrayList<MediaFile> mediaFileList = intentData.getParcelableArrayList(ProjectUtils.MEDIA_DATA);
            final FragmentManager manager = getSupportFragmentManager();
            final FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.fragmentPlaceHolder,
                    UtilSelectionFragment.newInstance(mediaFileList),
                    ProjectUtils.SELECTION_FRAGMENT)
                    .commit();
        }

    }

    @Subscribe
    public void onMediaSetCreated(Launcher<ArrayList<MediaFile>> launcher) {
        final FragmentManager manager=getSupportFragmentManager();
        manager.beginTransaction()
                .hide(manager.findFragmentByTag(ProjectUtils.SELECTION_FRAGMENT))
                .add(R.id.fragmentPlaceHolder,
                        MediaUtilReviewFragment.newInstance(launcher.data),
                         ProjectUtils.REVIEW_FRAGMENT)
                .addToBackStack(ProjectUtils.SELECTION_FRAGMENT)
                .commit();
    }


    @Subscribe
    public void onMediaSetReviewed(final ExitEvent exitEvent) {
        if(exitEvent.intent!=null) {
            String folderName = exitEvent.intent.getStringExtra(ProjectUtils.MEDIA_TITLE);
            boolean moveTo = exitEvent.intent.getBooleanExtra(ProjectUtils.MOVE_FILE_TO, false);
            ArrayList<MediaFile> result = exitEvent.intent.getParcelableArrayListExtra(ProjectUtils.MEDIA_DATA);

            if (folderName != null) {

                String pathTo = Environment.getExternalStorageDirectory() + File.separator + folderName+File.separator;
                File mediaFolder = new File(pathTo);
                if (!mediaFolder.mkdir()) {
                    Toast.makeText(MediaUtilCreatorScreen.this, "Failed to create the folder", Toast.LENGTH_SHORT).show();
                    finish();
                }

                Map<String, ArrayList<MediaFile>> map = new HashMap<>();
                map.put(mediaFolder.getAbsolutePath(), result);
                if(moveTo) {
                    App.appInstance().move(map);
                }else {
                    App.appInstance().copy(map);
                }

                exitEvent.intent.putExtra(ProjectUtils.MEDIA_FOLDER, new MediaFolder(folderName, result));

            }

            setResult(RESULT_OK, exitEvent.intent);
            finish();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Registrator.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Registrator.unregister(this);
    }
}
