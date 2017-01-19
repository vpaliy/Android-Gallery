package com.vpaliy.studioq.screens.main;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;
import com.vpaliy.studioq.MultiChoiceMode.MultiChoiceMode;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.adapters.MediaFolderAdapter;
import com.vpaliy.studioq.media.MediaFile;
import com.vpaliy.studioq.media.MediaFolder;
import com.vpaliy.studioq.screens.GalleryActivity;
import com.vpaliy.studioq.screens.MediaUtilCreatorScreen;
import com.vpaliy.studioq.utils.FileUtils;
import com.vpaliy.studioq.utils.OnLaunchGalleryActivity;
import com.vpaliy.studioq.utils.Permissions;
import com.vpaliy.studioq.utils.ProjectUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity
        implements OnLaunchGalleryActivity {

    private final static String TAG=MainActivity.class.getSimpleName();

    private RecyclerView mContentGrid;
    private FloatingActionButton mFab;
    private MediaFolderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        init();
        initActionBar();
        initNavigation();
    }

    private void initActionBar() {
        if(getSupportActionBar()==null) {
            setSupportActionBar((Toolbar) (findViewById(R.id.actionBar)));
        }
    }

    private void init() {
        mFab=(FloatingActionButton)(findViewById(R.id.addFloatingActionButton));
        if(!Permissions.requestIfNotAllowed(this,Manifest.permission.READ_EXTERNAL_STORAGE,
            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE},ProjectUtils.ACCESS_TO_EXTERNAL_STORAGE)) {
             makeQuery();
        }

    }

    private void initNavigation() {
        final DrawerLayout layout=(DrawerLayout)(findViewById(R.id.drawerLayout));
        NavigationView navigationView=(NavigationView)(findViewById(R.id.navigation));
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.allMedia:
                        adapter.setAdapterMode(MediaFolderAdapter.Mode.ALL);
                        break;
                    case R.id.photos:
                        adapter.setAdapterMode(MediaFolderAdapter.Mode.IMAGE);
                        break;
                    case R.id.videos:
                        adapter.setAdapterMode(MediaFolderAdapter.Mode.VIDEO);
                        break;
                    case R.id.settings:
                        startSettings();
                        break;
                }
                layout.closeDrawers();
                return true;
            }
        });

    }

    private void makeQuery() {
        new DataProvider(this) {
            @Override
            public void onPostExecute(ArrayList<MediaFolder> mediaFolders) {
                adapter= new MediaFolderAdapter(MainActivity.this,
                        new MultiChoiceMode(R.menu.gallery_menu) {},mediaFolders);
                mContentGrid=(RecyclerView)(findViewById(R.id.mainContent));
                mContentGrid.setLayoutManager(new GridLayoutManager(MainActivity.this,
                    2,GridLayoutManager.VERTICAL, false));
                mContentGrid.setAdapter(adapter);
            }
        };
    }

    private void startSettings() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ProjectUtils.ACCESS_TO_EXTERNAL_STORAGE:
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                    makeQuery();
                }
                break;
        }
    }


    @Override
    @TargetApi(21)
    public void onLaunchGalleryActivity(ArrayList<MediaFolder> allMediaFolders,MediaFolder mediaFolder,View imageView) {
        final Intent intent=new Intent(this, GalleryActivity.class);

      /* ArrayList<MediaFolder> copyList=new ArrayList<>(allMediaFolders);
        if(mMainFolder!=mediaFolder)
            copyList.remove(mediaFolder);
        copyList.remove(mMainFolder);
        intent.putExtra(ProjectUtils.ALL_MEDIA,copyList); */

        intent.putExtra(ProjectUtils.MEDIA_DATA,mediaFolder);
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
    protected void onResume() {
        super.onResume();
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
                                makeQuery();
                            }
                        });
                    break;

            }
        }

    }

    public void onClickFloatingButton(View view) {
        addMediaFolder();
    }


    //TODO provide all of the media files
    private void addMediaFolder() {
        Intent intent=new Intent(this,MediaUtilCreatorScreen.class);
        ArrayList<MediaFolder> folderList=adapter.getMediaList();
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
                /*if (contentList != null) {
                    ContentValues values=new ContentValues();
                    for (MediaFile mediaFile : contentList) {
                        File file = new File(mediaFolder, mediaFile.getMediaFile().getName());
                        if (!file.exists()) {
                            try {
                                if (!file.createNewFile()) {
                                    //showing only path here
                                    Log.e(TAG, "Cannot create a file here " + mediaFile.getMediaFile().getName());
                                    //No need to copy the file any more
                                    continue;
                                }

                                //copying file byte by byte
                                FileUtils.makeFileCopy(mediaFile.getMediaFile().getAbsoluteFile(), file);

                                //if user's selected "move" option, you need to delete the file
                                if (moveTo) {
                                    if (!mediaFile.getMediaFile().delete()) {
                                        Log.e(TAG, "Cannot delete file " + mediaFile.getMediaFile().getAbsoluteFile());
                                    }

                                    //TODO it ain't working because MediaFile is neither ImageFile nor VideoFile
                                    if(mediaFile instanceof VideoFile) {
                                        getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                                MediaStore.MediaColumns.DATA + "=?", new String[]{mediaFile.getMediaFile().getAbsolutePath()});
                                    }else {
                                        getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                MediaStore.MediaColumns.DATA + "=?", new String[]{mediaFile.getMediaFile().getAbsolutePath()});
                                    }
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                Log.e(TAG, ex.toString(), ex);
                                continue;
                            }
                            if (mediaFile instanceof VideoFile) {
                                values.put(MediaStore.Video.VideoColumns.DATA, file.getAbsolutePath());
                                getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                            } else {
                                values.put(MediaStore.Images.ImageColumns.DATA, file.getAbsolutePath());
                                getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                            }
                        }
                    }
                }*/
                //afterwards, you need to start thread for retrieving media data
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            makeQuery();
        }
    }
}
