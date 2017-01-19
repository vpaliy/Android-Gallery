package com.vpaliy.studioq.screens;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.vpaliy.studioq.media.MediaFile;
import com.vpaliy.studioq.media.MediaFolder;
import com.vpaliy.studioq.adapters.GalleryAdapter;
import com.vpaliy.studioq.fragments.GalleryFragment;
import com.vpaliy.studioq.fragments.MediaFolderUtilSelectionFragment;
import com.vpaliy.studioq.slider.listeners.OnLaunchMediaSlider;
import com.vpaliy.studioq.slider.screens.MediaSliderActivity;
import com.vpaliy.studioq.utils.FileUtils;
import com.vpaliy.studioq.utils.Permissions;
import com.vpaliy.studioq.utils.ProjectUtils;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.utils.TransitionListenerAdapter;

//TODO consider screen rotation when context action bar is launched, or snack_bar is showed
public class GalleryActivity extends AppCompatActivity
        implements OnLaunchMediaSlider, GalleryAdapter.MediaFileControlListener,
        MediaFolderUtilSelectionFragment.OnMediaContainerSelectedListener{


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



    private void initUI(Bundle args) {

        if (getSupportActionBar() == null) {
            Toolbar actionBar = (Toolbar) (findViewById(R.id.actionBar));
            setSupportActionBar(actionBar);
            if(getSupportActionBar()!=null)
                getSupportActionBar().setDisplayShowTitleEnabled(false);
           // actionBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        }

        if (Permissions.checkForVersion(Build.VERSION_CODES.KITKAT)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }



        mMediaFolder= args.getParcelable(ProjectUtils.MEDIA_DATA);

        if(mMediaFolder!=null) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.mediaFragmentPlaceHolder, GalleryFragment.PROVIDER.
                            createInstance((ArrayList<MediaFile>) (mMediaFolder.getMediaFileList())),
                    ProjectUtils.GALLERY_FRAGMENT);
            transaction.commit();
        }

        FloatingActionButton cameraButton=(FloatingActionButton)(findViewById(R.id.cameraButton));
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });


    }

    //TODO take care of hiding system bars
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
      /*  View decorView = getWindow().getDecorView();
        if (!hasFocus) {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;

            if (Build.VERSION.SDK_INT >= 19) {
                flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }

            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        } else {
            decorView.setSystemUiVisibility(0);
        }*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void openCamera() {
        startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));
    }

    @Override
    @TargetApi(21)
    public void onLaunchMediaSlider(int position, ImageView sharedView) {

       /*if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            Intent intent=new Intent(this,MediaSliderActivity.class);
            intent.putExtra(ProjectUtils.MEDIA_DATA,(ArrayList<MediaFile>)mMediaFolder.getMediaFileList());
            intent.putExtra(ProjectUtils.POSITION,position);
            TransitionStarter.with(this).from(sharedView).
                startForResult(intent, ProjectUtils.LAUNCH_SLIDER,position);
        }else {
            Intent intent=new Intent(this, MediaSliderActivity.class);
            intent.putExtra(ProjectUtils.MEDIA_DATA,(ArrayList<MediaFile>)mMediaFolder.getMediaFileList());
            intent.putExtra(ProjectUtils.POSITION,position);
            ActivityOptionsCompat options= ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this,sharedView,sharedView.getTransitionName());
            startActivityForResult(intent,ProjectUtils.LAUNCH_SLIDER,options.toBundle());
        } */

       /* TransitionSet shared=new TransitionSet();
        shared.addTransition(new ChangeBounds());
        shared.addTransition(new ChangeTransform());
        shared.addTransition(new ChangeImageTransform());
        shared.addTarget(sharedView.getTransitionName());

        Fade fade=new Fade();
        fade.excludeTarget(sharedView.getTransitionName(),true);
        TransitionSet set=new TransitionSet();
        set.addTransition(shared);
        set.addTransition(fade);

        Scene scene=Scene.getSceneForLayout((ViewGroup)findViewById(R.id.rootView),
            R.id.mediaFragmentPlaceHolder,this);    */

        Intent intent=new Intent(this, MediaSliderActivity.class);
        intent.putExtra(ProjectUtils.MEDIA_DATA,(ArrayList<MediaFile>)(mMediaFolder.getMediaFileList()));
        intent.putExtra(ProjectUtils.POSITION,position);
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

    @Override
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
                //TODO after testing delete this code below
                /*
                for (MediaFile mediaFile : deleteMediaFileList) {
                    if(!mediaFile.getMediaFile().delete()) {
                        Log.e(TAG, "Cannot delete file "+ mediaFile.getMediaFile().getAbsoluteFile());
                    }
                    String[] projection = { MediaStore.Images.Media._ID };

                    String selection = MediaStore.Images.Media.DATA + " = ?";
                    String[] selectionArgs = new String[] { mediaFile.getMediaFile().getAbsolutePath() };

                    Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    ContentResolver contentResolver = getContentResolver();
                    Cursor cursor = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
                    if(cursor!=null) {
                        if (cursor.moveToFirst()) {
                            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                            Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                            contentResolver.delete(deleteUri, null, null);
                        }
                        cursor.close();
                    }
                }
                */
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

    @Override
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
