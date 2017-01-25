package com.vpaliy.studioq.activities.main;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.activities.utils.eventBus.Launcher;
import com.vpaliy.studioq.activities.utils.eventBus.Registrator;
import com.vpaliy.studioq.adapters.FolderAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.BaseAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.MultiMode;
import com.vpaliy.studioq.media.MediaFile;
import com.vpaliy.studioq.media.MediaFolder;
import com.vpaliy.studioq.activities.GalleryActivity;
import com.vpaliy.studioq.activities.MediaUtilCreatorScreen;
import com.vpaliy.studioq.utils.FileUtils;
import com.vpaliy.studioq.utils.Permissions;
import com.vpaliy.studioq.utils.ProjectUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import android.support.annotation.NonNull;
import com.squareup.otto.Subscribe;


public class MainActivity extends AppCompatActivity {

    private final static String TAG=MainActivity.class.getSimpleName();

    private RecyclerView mContentGrid;
    private FloatingActionButton mFab;
    private FolderAdapter adapter;
    private Toolbar actionBar;
    private int currentMode;


    private MultiMode.Callback callback=new MultiMode.Callback() {
        @Override
        public boolean onMenuItemClick(BaseAdapter adapter, MenuItem item) {
            //TODO finish that
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        initUI(savedInstanceState);
    }

    private void initUI(Bundle savedInstanceState) {
        initActionBar();
        bindData(savedInstanceState);
        initNavigation(savedInstanceState);

        mFab=(FloatingActionButton)(findViewById(R.id.addFloatingActionButton));
    }

    private void initActionBar() {
        actionBar=(Toolbar)(findViewById(R.id.actionBar));

        if(getSupportActionBar()==null) {
            setSupportActionBar(actionBar);
        }

        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setShowHideAnimationEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        actionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void bindData(Bundle state) {
        if(!Permissions.requestIfNotAllowed(this,Manifest.permission.READ_EXTERNAL_STORAGE,
                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        ProjectUtils.ACCESS_TO_EXTERNAL_STORAGE)) {
            makeQuery(state);
        }

    }

    private void initNavigation(Bundle state) {
        if(state==null) {
            currentMode = R.id.allMedia;
        }else {
            currentMode = state.getInt(ProjectUtils.MODE, R.id.allMedia);
        }
        final DrawerLayout layout=(DrawerLayout)(findViewById(R.id.drawerLayout));
        NavigationView navigationView=(NavigationView)(findViewById(R.id.navigation));
        navigationView.setCheckedItem(currentMode);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.allMedia:
                        currentMode=R.id.allMedia;
                        adapter.setAdapterMode(FolderAdapter.Mode.ALL);
                        break;
                    case R.id.photos:
                        currentMode=R.id.photos;
                        adapter.setAdapterMode(FolderAdapter.Mode.IMAGE);
                        break;
                    case R.id.videos:
                        currentMode=R.id.videos;
                        adapter.setAdapterMode(FolderAdapter.Mode.VIDEO);
                        break;
                    case R.id.settings:
                        currentMode=R.id.settings; //TODO keep an eye on this
                        startSettings();
                        break;
                }
                layout.closeDrawers();
                return true;
            }
        });

    }

    private void makeQuery(final Bundle savedInstanceState) {
        new DataProvider(this) {
            @Override
            public void onPostExecute(ArrayList<MediaFolder> mediaFolders) {
                MultiMode mode=new MultiMode.Builder(actionBar,MainActivity.this)
                        .setMenu(R.menu.gallery_menu, callback)
                        .setBackgroundColor(Color.WHITE)
                        .build();
                if(savedInstanceState==null) {
                    adapter = new FolderAdapter(MainActivity.this, mode, mediaFolders);
                }else {
                    adapter = new FolderAdapter(MainActivity.this, mode, mediaFolders, savedInstanceState);
                }
                mContentGrid = (RecyclerView) (findViewById(R.id.mainContent));
                mContentGrid.setLayoutManager(new GridLayoutManager(MainActivity.this,
                        2,GridLayoutManager.VERTICAL, false));
                mContentGrid.setAdapter(adapter);
            }
        };
    }

    private void startSettings() {

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

    @Subscribe
    public void startGalleryActivity(Launcher<MediaFolder> launcher) {
        final Intent intent=new Intent(this,GalleryActivity.class);
        intent.putExtra(ProjectUtils.MEDIA_DATA,launcher.data);
        mFab.animate().scaleX(0f).scaleY(0f)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(100).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                    startActivityForResult(intent, ProjectUtils.LAUNCH_GALLERY,
                            ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                }else {
                    startActivityForResult(intent, ProjectUtils.LAUNCH_GALLERY);
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ProjectUtils.ACCESS_TO_EXTERNAL_STORAGE:
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                    makeQuery(null);
                }else {
                    Toast.makeText(this,"Give me the permission!",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(adapter!=null) {
            adapter.saveState(outState);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(adapter!=null) {
            adapter.onResume();
        }

        if(mFab!=null) {
            if(mFab.getScaleX()<1f) {
                mFab.animate().scaleX(1.f)
                        .scaleY(1.f).setListener(null)
                        .setDuration(200).start();
            }
        }

    }


    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ProjectUtils.CREATE_MEDIA_FOLDER:
                    new AsyncOnResultTask().execute(data);
                    break;
                case ProjectUtils.LAUNCH_GALLERY:
                    if(data.getBooleanExtra(ProjectUtils.DELETED,false))
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                makeQuery(null);
                            }
                        });
                    break;

            }
        }

    }

    @Override
    public void onBackPressed() {
        if(adapter!=null) {
            if (adapter.isMultiModeActivated()) {
                mContentGrid.setItemAnimator(null);
                adapter.unCheckAll(true);
                mContentGrid.post(new Runnable() {
                    @Override
                    public void run() {
                        mContentGrid.setItemAnimator(new DefaultItemAnimator());
                    }
                });

            }
        }else {
            super.onBackPressed();
        }
    }

    public void onClickFloatingButton(View view) {
        addMediaFolder();
    }


    //TODO provide all of the media files
    private void addMediaFolder() {
        Intent intent=new Intent(this,MediaUtilCreatorScreen.class);
        List<MediaFolder> folderList=adapter.geMediaFolderList();
        Set<MediaFile> fileSet=new LinkedHashSet<>();
        for(MediaFolder folder:folderList) {
            List<MediaFile> fileList=folder.getMediaFileList();
            if(fileList!=null) {
                for (MediaFile mediaFile : fileList) {
                    fileSet.add(mediaFile);
                }
            }
        }

        if(!fileSet.isEmpty()) {
            ArrayList<MediaFile> mediaFileList=new ArrayList<>(fileSet);
            intent.putParcelableArrayListExtra(ProjectUtils.MEDIA_DATA,mediaFileList);
            startActivityForResult(intent, ProjectUtils.CREATE_MEDIA_FOLDER);
        }else {
            //TODO language
            Toast.makeText(this,"There is no media files",Toast.LENGTH_LONG).show();
        }
    }


    private class AsyncOnResultTask extends AsyncTask<Intent,Void,Void> {

        @Override
        protected Void doInBackground(Intent... intents) {
            createFolderOnResult(intents[0]);
            return null;
        }

        private void createFolderOnResult(Intent data) {
            Bundle folderData=data.getExtras();
            String folderName=folderData.getString(ProjectUtils.MEDIA_TITLE);
            boolean moveTo=data.getBooleanExtra(ProjectUtils.MOVE_FILE_TO,false);
            List<MediaFile> contentList=folderData.getParcelableArrayList(ProjectUtils.MEDIA_DATA);
            if(folderName!=null) {
                String pathTo= Environment.getExternalStorageDirectory() + File.separator + folderName;
                File mediaFolder=new File(pathTo);
                if(!mediaFolder.mkdir()) {
                    if(!mediaFolder.exists())
                        Log.e(TAG,"Cannot create a folder here "+pathTo);
                    else
                        Log.e(TAG,"Folder exists "+pathTo);
                    return;
                }
                FileUtils.copyFileList(MainActivity.this,contentList,mediaFolder,moveTo);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            makeQuery(null);
        }
    }
}
