package com.vpaliy.studioq.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import java.util.ArrayList;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.activities.utils.eventBus.ExitEvent;
import com.vpaliy.studioq.activities.utils.eventBus.Launcher;
import com.vpaliy.studioq.activities.utils.eventBus.Registrator;
import com.vpaliy.studioq.fragments.UtilSelectionFragment;
import com.vpaliy.studioq.media.MediaFile;
import com.vpaliy.studioq.fragments.MediaUtilReviewFragment;
import com.vpaliy.studioq.utils.ProjectUtils;
import com.squareup.otto.Subscribe;

public class MediaUtilCreatorScreen extends AppCompatActivity {

    private static final String  TAG=MediaUtilCreatorScreen.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_util_creator_layout);

        Bundle intentData=getIntent().getExtras();
        ArrayList<MediaFile> mediaFileList= intentData.getParcelableArrayList(ProjectUtils.MEDIA_DATA);
        final FragmentManager manager=getSupportFragmentManager();
        final FragmentTransaction transaction=manager.beginTransaction();
        transaction.replace(R.id.fragmentPlaceHolder,
                UtilSelectionFragment.newInstance(mediaFileList),
                ProjectUtils.SELECTION_FRAGMENT)
                .commit();

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
    public void onMediaSetReviewed(ExitEvent exitEvent) {
        setResult(RESULT_OK,exitEvent.intent);
        finish();
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
