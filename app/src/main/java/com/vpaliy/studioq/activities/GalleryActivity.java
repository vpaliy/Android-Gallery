package com.vpaliy.studioq.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Transition;
import android.view.View;
import android.view.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.vpaliy.studioq.activities.utils.eventBus.Launcher;
import com.vpaliy.studioq.activities.utils.eventBus.Registrator;
import com.vpaliy.studioq.media.MediaFile;
import com.vpaliy.studioq.media.MediaFolder;
import com.vpaliy.studioq.adapters.GalleryAdapter;
import com.vpaliy.studioq.fragments.GalleryFragment;
import com.vpaliy.studioq.fragments.MediaFolderUtilSelectionFragment;
import com.vpaliy.studioq.slider.screens.MediaSliderActivity;
import com.vpaliy.studioq.utils.FileUtils;
import com.vpaliy.studioq.utils.Permissions;
import com.vpaliy.studioq.utils.ProjectUtils;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.utils.TransitionListenerAdapter;
import com.squareup.otto.Subscribe;


public class GalleryActivity extends AppCompatActivity
        implements MediaFolderUtilSelectionFragment.OnMediaContainerSelectedListener{


    private final static String TAG=GalleryActivity.class.getSimpleName();

    private MediaFolder mMediaFolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(Permissions.checkForVersion(Build.VERSION_CODES.LOLLIPOP)) {
            requestFeature();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_layout);
        if(savedInstanceState==null) {
            savedInstanceState = getIntent().getExtras();
        }
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

        mMediaFolder= args.getParcelable(ProjectUtils.MEDIA_DATA);
        if(mMediaFolder!=null) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.mediaFragmentPlaceHolder, GalleryFragment.PROVIDER.
                            createInstance((ArrayList<MediaFile>) (mMediaFolder.getMediaFileList())),
                    ProjectUtils.GALLERY_FRAGMENT);
            transaction.commit();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Subscribe
    public void startSliderActivity(Launcher<ArrayList<MediaFile>> launcher) {
        Intent intent=new Intent(this, MediaSliderActivity.class);
        intent.putExtra(ProjectUtils.MEDIA_DATA,launcher.data);
        intent.putExtra(ProjectUtils.POSITION,launcher.position);
        startActivityForResult(intent,ProjectUtils.LAUNCH_SLIDER);
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
                    final GalleryFragment fragment = (GalleryFragment)
                            (getSupportFragmentManager().findFragmentByTag(ProjectUtils.GALLERY_FRAGMENT));
                    fragment.getGalleryAdapter().setMediaFileList(mediaFileList);
                    //have to do this in order to prevent providing slider activity with wrong data
                    mMediaFolder.setMediaFileList(mediaFileList);
                    break;
                }
            }
        }
    }

    public void onDeleteMediaFile(final GalleryAdapter adapter, final ArrayList<MediaFile> fullMediaFileList, final ArrayList<MediaFile> deleteMediaFileList) {
        Snackbar.make(findViewById(R.id.rootView),
                //TODO support for languages here
                Integer.toString(deleteMediaFileList.size())+" moved to trash",7000)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        adapter.setMediaFileList(fullMediaFileList);
                    }
                })
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        switch (event) {
                            case DISMISS_EVENT_SWIPE:
                            case DISMISS_EVENT_TIMEOUT:
                                onDeleteMediaFileList(deleteMediaFileList,fullMediaFileList);
                        }
                    }
                })
                .show();
    }

    private void onDeleteMediaFileList(final List<MediaFile> deleteMediaFileList, final List<MediaFile> fullMediaFileList) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... aVoids) {
                FileUtils.deleteFileList(GalleryActivity.this,deleteMediaFileList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Intent data=new Intent();
                data.putExtra(ProjectUtils.DELETED,true);
                setResult(RESULT_OK,data);
                if(fullMediaFileList!=null) {
                    if (deleteMediaFileList.size() == fullMediaFileList.size())
                        finish();
                }
            }

        }.execute(null, null);

    }

    public void onCopyTo(GalleryAdapter adapter, ArrayList<MediaFile> fullMediaFileList, ArrayList<MediaFile> copyMediaFileList, boolean moveTo) {
        Bundle args=new Bundle();
        args.putBoolean(ProjectUtils.MOVE_FILE_TO,moveTo);
        args.putParcelableArrayList(ProjectUtils.ALL_MEDIA,getIntent().getParcelableArrayListExtra(ProjectUtils.ALL_MEDIA));
        args.putParcelableArrayList(ProjectUtils.MEDIA_DATA,copyMediaFileList);
        if(fullMediaFileList!=null) {
            args.putParcelableArrayList(ProjectUtils.ORIGINAL_MEDIA_LIST, fullMediaFileList);
        }
        FragmentManager manager=getSupportFragmentManager();
        Fragment fragment=manager.findFragmentByTag(ProjectUtils.GALLERY_FRAGMENT);
        manager.beginTransaction()
                .hide(fragment)
                .add(R.id.mediaFragmentPlaceHolder,MediaFolderUtilSelectionFragment.newInstance(args),ProjectUtils.SELECTION_FRAGMENT)
                .addToBackStack(ProjectUtils.GALLERY_FRAGMENT)
                .commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        FragmentManager manager=getSupportFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        Fragment fragment=manager.findFragmentByTag(ProjectUtils.SELECTION_FRAGMENT);
        if(fragment!=null) {
            transaction.detach(fragment);
            transaction.attach(fragment);
            transaction.commit();
        }else if((fragment=manager.findFragmentByTag(ProjectUtils.GALLERY_FRAGMENT))!=null) {
            transaction.detach(fragment);
            transaction.attach(fragment);
            transaction.commit();
        }
    }

    @Override
    public void onMediaContainerSelected(final Bundle data) {
        getSupportFragmentManager().popBackStack();

        final String pathTo=data.getString(ProjectUtils.MEDIA_FOLDER);
        final ArrayList<MediaFile> copyMediaFileList=data.getParcelableArrayList(ProjectUtils.MEDIA_DATA);
        final boolean moveTo=data.getBoolean(ProjectUtils.MOVE_FILE_TO,false);
        final ArrayList<MediaFile> originalMediaFileList=data.getParcelableArrayList(ProjectUtils.ORIGINAL_MEDIA_LIST);

        if(copyMediaFileList!=null && pathTo!=null) {
            Snackbar.make(findViewById(R.id.rootView),
                    //TODO support for languages here
                    Integer.toString(copyMediaFileList.size()) + " moved to "+pathTo, 7000)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(moveTo) {
                                final GalleryFragment fragment = (GalleryFragment)
                                        (getSupportFragmentManager().findFragmentByTag(ProjectUtils.GALLERY_FRAGMENT));
                                fragment.getGalleryAdapter().setMediaFileList(originalMediaFileList);
                            }
                        }
                    })
                    .setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            super.onDismissed(snackbar, event);
                            switch (event) {

                                case DISMISS_EVENT_SWIPE:
                                case DISMISS_EVENT_TIMEOUT:
                                    new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... aVoids) {
                                            FileUtils.copyFileList(GalleryActivity.this, copyMediaFileList, new File(pathTo), moveTo);
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Void aVoid) {
                                            super.onPostExecute(aVoid);
                                            //TODO update folders on return (updated, however, not sure about that yet)
                                            if(moveTo) {
                                                Intent data=new Intent();
                                                data.putExtra(ProjectUtils.DELETED,true);
                                                setResult(RESULT_OK,data);
                                                if(originalMediaFileList!=null) {
                                                    if (originalMediaFileList.size() == copyMediaFileList.size())
                                                        finish();
                                                }
                                            }

                                        }
                                    }.execute(null,null);
                                    break;
                            }
                        }
                    })
                    .show();
        }
    }

    @TargetApi(21)
    private void requestFeature() {
       getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
       getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
       Transition enterTransition=new Explode();
       enterTransition.excludeTarget(android.R.id.navigationBarBackground,true);
       enterTransition.excludeTarget(android.R.id.statusBarBackground,true);
       enterTransition.excludeTarget(R.id.cameraButton,true);
       enterTransition.setDuration(200);
       getWindow().setEnterTransition(enterTransition);
       getWindow().getEnterTransition().addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                super.onTransitionStart(transition);
                FloatingActionButton fab=(FloatingActionButton)
                        (findViewById(R.id.cameraButton));
                fab.setVisibility(View.VISIBLE);
                fab.animate().scaleX(1.f).scaleY(1.f)
                        .setDuration(200).start();
            }
        });

      getWindow().setReturnTransition(new Fade());
      getWindow().getReturnTransition().addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionStart(Transition transition) {
                super.onTransitionStart(transition);
                FloatingActionButton fab=(FloatingActionButton)
                        (findViewById(R.id.cameraButton));
                fab.setVisibility(View.VISIBLE);
                fab.animate().scaleX(0f).scaleY(0f)
                        .setDuration(200).start();
            }
        });

        getWindow().setSharedElementsUseOverlay(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
