package com.vpaliy.studioq.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Transition;
import android.view.Window;
import java.util.ArrayList;
import com.vpaliy.studioq.activities.utils.eventBus.ExitEvent;
import com.vpaliy.studioq.activities.utils.eventBus.Launcher;
import com.vpaliy.studioq.activities.utils.eventBus.Registrator;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.model.MediaFolder;
import com.vpaliy.studioq.fragments.GalleryFragment;
import com.vpaliy.studioq.slider.screens.MediaSliderActivity;
import com.vpaliy.studioq.utils.Permissions;
import com.vpaliy.studioq.utils.ProjectUtils;
import com.vpaliy.studioq.R;
import com.squareup.otto.Subscribe;
import butterknife.ButterKnife;


public class GalleryActivity extends AppCompatActivity {

    private final static String TAG=GalleryActivity.class.getSimpleName();

    private MediaFolder mediaFolder;
    private GalleryFragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Permissions.checkForVersion(Build.VERSION_CODES.LOLLIPOP)) {
            requestFeature();
        }
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);
        initUI(savedInstanceState);
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

    private void initUI(Bundle args) {
        if (args == null) {
            args = getIntent().getExtras();
            fragment = GalleryFragment.newInstance(args);
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            if (Permissions.checkForVersion(Build.VERSION_CODES.LOLLIPOP)) {
                startPostponedEnterTransition();
            } else {
                transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
            }
            transaction.replace(R.id.mediaFragmentPlaceHolder, fragment,
                    ProjectUtils.GALLERY_FRAGMENT);
            transaction.commit();
            manager.executePendingTransactions();
        }
    }


    @Override
    public void onBackPressed() {
        if(fragment!=null) {
            if(fragment.onBackPressed()) {
                super.onBackPressed();
            }
        }
    }

    @Subscribe
    public void startSliderActivity(Launcher<ArrayList<MediaFile>> launcher) {
        Intent intent=new Intent(this, MediaSliderActivity.class);
        intent.putExtra(ProjectUtils.MEDIA_DATA,launcher.data);
        intent.putExtra(ProjectUtils.POSITION,launcher.position);
        startActivityForResult(intent,ProjectUtils.LAUNCH_SLIDER);
    }

    @Subscribe
    public void onExit(@NonNull ExitEvent exitEvent) {
        if(exitEvent.intent!=null) {
            exitEvent.intent.putExtra(ProjectUtils.DELETED, true);
            exitEvent.intent.putExtra(ProjectUtils.MEDIA_FOLDER, mediaFolder);
            setResult(RESULT_OK, exitEvent.intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ProjectUtils.LAUNCH_SLIDER: {
                    ArrayList<MediaFile> mediaFileList=data.getParcelableArrayListExtra(ProjectUtils.MEDIA_DATA);
                    Intent resultData = new Intent();
                    resultData.putExtra(ProjectUtils.DELETED, true);
                    setResult(RESULT_OK, resultData);
                    if(mediaFileList==null||mediaFileList.isEmpty()) {
                        finish();
                    }
                    mediaFolder.setMediaFileList(mediaFileList);
                    break;
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ProjectUtils.MEDIA_DATA,mediaFolder);
    }

    @TargetApi(21)
    private void requestFeature() {

        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        Transition enterTransition=new Explode();
        enterTransition.excludeTarget(android.R.id.navigationBarBackground,true);
        enterTransition.excludeTarget(android.R.id.statusBarBackground,true);
        enterTransition.excludeTarget(R.id.cameraButton,true);
        enterTransition.setDuration(200);
        getWindow().setEnterTransition(enterTransition);
        getWindow().setReturnTransition(new Fade());
        getWindow().setSharedElementsUseOverlay(true);
        postponeEnterTransition();
    }
}
