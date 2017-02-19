package com.vpaliy.studioq.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.cases.DeleteCase;
import com.vpaliy.studioq.common.graphicalUtils.ScaleBuilder;
import com.vpaliy.studioq.common.eventBus.Launcher;
import com.vpaliy.studioq.common.eventBus.Registrator;
import com.vpaliy.studioq.adapters.FolderAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.BaseAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.MultiMode;
import com.vpaliy.studioq.common.utils.Permissions;
import com.vpaliy.studioq.controllers.DataController;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.model.MediaFolder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import butterknife.ButterKnife;

import android.support.annotation.NonNull;
import com.squareup.otto.Subscribe;
import butterknife.BindView;
import butterknife.OnClick;

import static com.vpaliy.studioq.common.utils.ProjectUtils.LAUNCH_GALLERY;
import static com.vpaliy.studioq.common.utils.ProjectUtils.INIT;
import static com.vpaliy.studioq.common.utils.ProjectUtils.CREATE_MEDIA_FOLDER;
import static com.vpaliy.studioq.common.utils.ProjectUtils.MEDIA_DATA;
import static com.vpaliy.studioq.common.utils.ProjectUtils.MODE;
import static butterknife.ButterKnife.findById;


public class MainActivity extends AppCompatActivity {


    @BindView(R.id.mediaRecyclerView)
    protected RecyclerView contentGrid;

    @BindView(R.id.actionButton)
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
        setContentView(R.layout.activity_main);
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
        if(state==null) {
            state = getIntent().getExtras();
            if (state == null) {
                makeQuery();
                return;
            }
        }

        final MultiMode mode=new MultiMode.Builder(actionBar,MainActivity.this)
                .setMenu(R.menu.main_menu, callback)
                .setBackgroundColor(Color.WHITE)
                .build();
        contentGrid.setLayoutManager(new GridLayoutManager(MainActivity.this,
                2, GridLayoutManager.VERTICAL, false));
        if(state.getBoolean(INIT,false)) {
            ArrayList<MediaFolder> data= DataController.controllerInstance().getFolders();
            adapter=new FolderAdapter(this,mode,data);
        }else {
            adapter=new FolderAdapter(this,mode,state);
        }

        contentGrid.setLayoutManager(new GridLayoutManager(MainActivity.this,
                2, GridLayoutManager.VERTICAL, false));
        contentGrid.setAdapter(adapter);
    }

    private void initNavigation(Bundle state) {
        if(state==null) {
            currentMode = R.id.allMedia;
        }else {
            currentMode = state.getInt(MODE, R.id.allMedia);
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

    private void makeQuery() {
        adapter=null;
        DataController.controllerInstance()
                .makeQuery(new DataController.Callback() {
                    @Override
                    public void onFinished() {
                        final MultiMode mode=new MultiMode.Builder(actionBar,MainActivity.this)
                                .setMenu(R.menu.main_menu, callback)
                                .setBackgroundColor(Color.WHITE)
                                .build();
                        contentGrid.setLayoutManager(new GridLayoutManager(MainActivity.this,
                                2, GridLayoutManager.VERTICAL, false));
                        adapter = new FolderAdapter(MainActivity.this, mode,
                                DataController.controllerInstance().getFolders());
                        contentGrid.setAdapter(adapter);
                    }
                });
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
    public void startGalleryActivity(Launcher<Bundle> launcher) {
        final Intent intent=new Intent(this,GalleryActivity.class);
        intent.putExtras(launcher.data);

        ScaleBuilder.start(actionButton,0f)
            .duration(100)
            .accelerate()
            .listener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if(Permissions.checkForVersion(Build.VERSION_CODES.LOLLIPOP)) {
                        startActivityForResult(intent, LAUNCH_GALLERY,
                                ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                    }else {
                        startActivityForResult(intent, LAUNCH_GALLERY);
                    }
                }
            }).execute();
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

        ScaleBuilder.start(actionButton,1.f)
                .duration(300)
                .accelerate()
                .execute();

    }


    private void deleteFolder() {
        DeleteCase.startWith(this,adapter)
            .filter(new DeleteCase.Filter<MediaFolder>() {
                @Override
                public ArrayList<MediaFile> filterData(List<MediaFolder> input) {
                    List<MediaFile> tempList = new LinkedList<>();
                    FolderAdapter.Mode mode=adapter.getAdapterMode();
                    for(MediaFolder folder:input) {
                        if(mode== FolderAdapter.Mode.IMAGE) {
                            tempList.addAll(folder.imageList());
                        }else if(mode== FolderAdapter.Mode.VIDEO) {
                            tempList.addAll(folder.videoList());
                        }else {
                            tempList.addAll(folder.list());
                        }
                    }
                    return new ArrayList<>(tempList);
                }
            })
            .execute();
    }


    @Override
    public void onActivityResult(int requestCode,int resultCode, @NonNull Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case LAUNCH_GALLERY:
                case CREATE_MEDIA_FOLDER:
                    adapter.notifyAboutChange();
                    break;
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

    @OnClick(R.id.actionButton)
    public void onClickFloatingButton(View view) {
        addMediaFolder();
    }


    private void addMediaFolder() {
        ArrayList<MediaFile> mediaFileList=DataController.
            controllerInstance().filterDuplicates();
        if(!mediaFileList.isEmpty()){
            Intent intent=new Intent(this,MediaUtilCreatorScreen.class);
            intent.putParcelableArrayListExtra(MEDIA_DATA,mediaFileList);
            startActivityForResult(intent, CREATE_MEDIA_FOLDER);
        }else {
            //TODO propose some options | my creativity goes down at this point :(
            Toast.makeText(this,"You don't have any photos",Toast.LENGTH_LONG).show();
        }
    }
}
