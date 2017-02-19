package com.vpaliy.studioq.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.vpaliy.studioq.App;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.common.dataUtils.FileUtils;
import com.vpaliy.studioq.common.eventBus.ExitEvent;
import com.vpaliy.studioq.common.eventBus.Launcher;
import com.vpaliy.studioq.common.eventBus.Registrator;
import com.vpaliy.studioq.controllers.DataController;
import com.vpaliy.studioq.fragments.UtilSelectionFragment;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.fragments.MediaUtilReviewFragment;
import com.vpaliy.studioq.model.MediaFolder;
import com.vpaliy.studioq.common.utils.ProjectUtils;
import butterknife.ButterKnife;
import com.squareup.otto.Subscribe;

import static com.vpaliy.studioq.common.utils.ProjectUtils.SELECTION_FRAGMENT;
import static com.vpaliy.studioq.common.utils.ProjectUtils.REVIEW_FRAGMENT;
import static com.vpaliy.studioq.common.utils.ProjectUtils.MEDIA_TITLE;
import static com.vpaliy.studioq.common.utils.ProjectUtils.MEDIA_DATA;
import static com.vpaliy.studioq.common.utils.ProjectUtils.MOVE_FILE_TO;

public class MediaUtilCreatorScreen extends AppCompatActivity {

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
                    UtilSelectionFragment.newInstance(mediaFileList),SELECTION_FRAGMENT)
                    .commit();
        }

    }

    @Subscribe
    public void onMediaSetCreated(Launcher<ArrayList<MediaFile>> launcher) {
        final FragmentManager manager=getSupportFragmentManager();
        manager.beginTransaction()
                .hide(manager.findFragmentByTag(SELECTION_FRAGMENT))
                .add(R.id.fragmentPlaceHolder,
                 MediaUtilReviewFragment.newInstance(launcher.data),REVIEW_FRAGMENT)
                .addToBackStack(SELECTION_FRAGMENT)
                .commit();
    }


    @Subscribe
    public void onMediaSetReviewed(final ExitEvent exitEvent) {
        if(exitEvent.intent!=null) {
            String folderName = exitEvent.intent.getStringExtra(MEDIA_TITLE);
            boolean moveTo = exitEvent.intent.getBooleanExtra(MOVE_FILE_TO, false);
            ArrayList<MediaFile> result = exitEvent.intent.getParcelableArrayListExtra(MEDIA_DATA);
            if (folderName != null) {
                File mediaFolder= FileUtils.createFolderInExternalStorage(this,folderName);
                if(mediaFolder==null) {
                    finish();
                    return;
                }
                Map<String, ArrayList<MediaFile>> map = new HashMap<>();
                map.put(mediaFolder.getAbsolutePath(),result);
                DataController.controllerInstance()
                        .justAdd(mediaFolder.getAbsolutePath(), new MediaFolder(folderName,MediaFile.
                                createReferenceList(mediaFolder.getAbsolutePath(),result)));
                if(moveTo) {
                    DataController.controllerInstance()
                        .justDelete(result);
                    App.appInstance().move(map);
                }else {
                    App.appInstance().copy(map);
                }
            }
            setResult(RESULT_OK, null);
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
