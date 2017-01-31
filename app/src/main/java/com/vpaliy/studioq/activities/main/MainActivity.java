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
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.model.MediaFolder;
import com.vpaliy.studioq.activities.GalleryActivity;
import com.vpaliy.studioq.activities.MediaUtilCreatorScreen;
import com.vpaliy.studioq.utils.FileUtils;
import com.vpaliy.studioq.utils.Permissions;
import com.vpaliy.studioq.utils.ProjectUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import android.support.annotation.NonNull;
import com.squareup.otto.Subscribe;
import butterknife.BindView;
import butterknife.ButterKnife;
import static butterknife.ButterKnife.findById;


public class MainActivity extends AppCompatActivity {

    private final static String TAG=MainActivity.class.getSimpleName();

    @BindView(R.id.mainContent)
    protected RecyclerView contentGrid;

    @BindView(R.id.addFloatingActionButton)
    protected FloatingActionButton actionButton;

    @BindView(R.id.actionBar)
    protected Toolbar actionBar;

    private FolderAdapter adapter;
    private int currentMode;


    private final MultiMode.Callback callback=new MultiMode.Callback() {

        private boolean isDeleteAction;

        @Override
        public boolean onMenuItemClick(BaseAdapter baseAdapter, MenuItem item) {
            switch(item.getItemId()) {
                case R.id.deleteAction:
                    isDeleteAction=true;
                    deleteFolder();
                    break;
            }
            return true;
        }

        @Override
        public void onModeActivated() {
            super.onModeActivated();
            isDeleteAction=false;
            hideButton();
        }

        @Override
        public void onModeDisabled() {
            super.onModeDisabled();
            if(!isDeleteAction) {
                showButton();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        ButterKnife.bind(this);
        initUI(savedInstanceState);
    }

    private void initUI(Bundle savedInstanceState) {
        initActionBar();
        bindData(savedInstanceState);
        initNavigation(savedInstanceState);
    }

    private void initActionBar() {

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

        final DrawerLayout layout=findById(this,R.id.drawerLayout);
        final NavigationView navigationView=findById(this,R.id.navigation);

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
                        //  currentMode=R.id.settings;
                        startSettings();
                        break;
                }
                layout.closeDrawers();
                return true;
            }
        });

    }


    private void hideButton() {
        if(actionButton.isShown()) {
            actionButton.hide();
        }
    }

    private void showButton() {
        if(!actionButton.isShown()) {
            actionButton.show();
        }
    }

    private void makeQuery(final Bundle savedInstanceState) {
        adapter=null;
        final MultiMode mode=new MultiMode.Builder(actionBar,MainActivity.this)
                .setMenu(R.menu.main_menu, callback)
                .setBackgroundColor(Color.WHITE)
                .build();
        contentGrid.setLayoutManager(new GridLayoutManager(MainActivity.this,
                2, GridLayoutManager.VERTICAL, false));
        if(savedInstanceState!=null) {
            adapter=new FolderAdapter(this,mode,savedInstanceState);
            contentGrid.setAdapter(adapter);
        }

        if(adapter==null||adapter.getItemCount()==0) {
            new DataProvider(this) {
                @Override
                public void onPostExecute(ArrayList<MediaFolder> mediaFolders) {
                    adapter = new FolderAdapter(MainActivity.this, mode, mediaFolders);
                    contentGrid.setAdapter(adapter);
                }
            };
        }
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
        actionButton.animate().scaleX(0f).scaleY(0f)
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

        if(actionButton!=null) {
            if(actionButton.getScaleX()<1f) {
                actionButton.animate().scaleX(1.f).scaleY(1.f)
                        .setListener(null).setDuration(200)
                        .start();
            }
        }

    }


    private void deleteFolder() {
        if (adapter != null) {
            if (adapter.isMultiModeActivated()) {
                final ArrayList<MediaFolder> deleteFolderList = adapter.getAllChecked();
                final List<MediaFolder> originalList=new ArrayList<>(adapter.getData());
                contentGrid.setItemAnimator(null);
                final int[] checked=adapter.getAllCheckedForDeletion();
                contentGrid.post(new Runnable() {
                    @Override
                    public void run() {
                        contentGrid.setItemAnimator(new DefaultItemAnimator());
                        for(int index:checked) {
                            adapter.removeAt(index);
                        }
                    }
                });
                Snackbar.make(findViewById(R.id.rootView),
                        //TODO support for languages here
                        Integer.toString(deleteFolderList.size()) + " have been moved to trash", 7000)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                adapter.setData(originalList);
                                showButton();
                            }
                        })
                        .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);
                                showButton();
                                switch (event) {
                                    case DISMISS_EVENT_SWIPE:
                                    case DISMISS_EVENT_TIMEOUT:
                                        deleteInBackground(deleteFolderList, adapter.getAdapterMode());
                                        break;
                                }
                            }
                        })
                        .show();
            }
        }
    }

    private void deleteInBackground(final ArrayList<MediaFolder> deleteFolderList, final FolderAdapter.Mode mode) {
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if(deleteFolderList!=null) {
                    for(MediaFolder folder:deleteFolderList) {
                        List<? extends MediaFile> result;
                        if(mode== FolderAdapter.Mode.IMAGE) {
                            result = folder.getImageFileList();
                        }else if(mode== FolderAdapter.Mode.VIDEO) {
                            result=folder.getVideoFileList();
                        }else {
                            result = folder.getMediaFileList();
                        }
                        FileUtils.deleteFileList(MainActivity.this,result);
                    }
                }
                return null;
            }
        }.execute(null,null);
    }



    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ProjectUtils.CREATE_MEDIA_FOLDER: {
                    MediaFolder folder = data.getParcelableExtra(ProjectUtils.MEDIA_FOLDER);
                    adapter.addFolder(folder);
                    break;
                }
                case ProjectUtils.LAUNCH_GALLERY: {
                    if (data.getBooleanExtra(ProjectUtils.DELETED, false)) {
                        MediaFolder folder = data.getParcelableExtra(ProjectUtils.MEDIA_FOLDER);
                        adapter.removeWith(folder);
                    }
                    break;
                }
            }
        }

    }

    @Override
    public void onBackPressed() {
        if(adapter!=null) {
            if (adapter.isMultiModeActivated()) {
                contentGrid.setItemAnimator(null);
                adapter.unCheckAll(true);
                contentGrid.post(new Runnable() {
                    @Override
                    public void run() {
                        contentGrid.setItemAnimator(new DefaultItemAnimator());
                    }
                });
                return;
            }
        }
        super.onBackPressed();
    }

    public void onClickFloatingButton(View view) {
        addMediaFolder();
    }


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
            Toast.makeText(this,"You don't have any photos",Toast.LENGTH_LONG).show();
        }
    }
}
