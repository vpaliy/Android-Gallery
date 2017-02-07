package com.vpaliy.studioq.activities;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.activities.utils.eventBus.ExitEvent;
import com.vpaliy.studioq.activities.utils.eventBus.Launcher;
import com.vpaliy.studioq.activities.utils.eventBus.Registrator;
import com.vpaliy.studioq.fragments.UtilSelectionFragment;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.fragments.MediaUtilReviewFragment;
import com.vpaliy.studioq.model.MediaFolder;
import com.vpaliy.studioq.utils.FileUtils;
import com.vpaliy.studioq.utils.ProjectUtils;
import com.squareup.otto.Subscribe;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MediaUtilCreatorScreen extends AppCompatActivity {

    private static final String  TAG=MediaUtilCreatorScreen.class.getSimpleName();

    @BindView(R.id.progress)
    protected ProgressBar progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_util);
        ButterKnife.bind(this,MediaUtilCreatorScreen.this);

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
    public void onMediaSetReviewed(final ExitEvent exitEvent) {
        if(exitEvent.intent.getParcelableArrayListExtra(ProjectUtils.MEDIA_DATA)!=null) {
            new AsyncTask<Void, Void, Boolean>() {

                FileUtils.UpdateCallback callback=new FileUtils.UpdateCallback() {
                    @Override
                    public void onUpdate(int index, int max) {
                        progress.setProgress(index);
                    }
                };

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected Boolean doInBackground(Void... params) {
                    String folderName = exitEvent.intent.getStringExtra(ProjectUtils.MEDIA_TITLE);
                    boolean moveTo = exitEvent.intent.getBooleanExtra(ProjectUtils.MOVE_FILE_TO, false);
                    List<MediaFile> contentList = exitEvent.intent.getParcelableArrayListExtra(ProjectUtils.MEDIA_DATA);
                    if (folderName != null) {
                        String pathTo = Environment.getExternalStorageDirectory() + File.separator + folderName;
                        File mediaFolder = new File(pathTo);
                        if (!mediaFolder.mkdir()) {
                            Toast.makeText(MediaUtilCreatorScreen.this, "Failed to create a folder", Toast.LENGTH_SHORT).show();
                            return Boolean.FALSE;
                        }
                        ArrayList<MediaFile> result = new ArrayList<>(FileUtils.copyFileList(MediaUtilCreatorScreen.this,
                                 contentList, mediaFolder, moveTo));
                        exitEvent.intent.putExtra(ProjectUtils.MEDIA_FOLDER, new MediaFolder(folderName, result));
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    if (result == Boolean.TRUE) {
                        setResult(RESULT_OK, exitEvent.intent);
                    }
                    finish();
                }

            }.execute();
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

    @Override
    public void onBackPressed() {
        if(progress.getVisibility()!=View.VISIBLE) {
            super.onBackPressed();
        }
    }
}
