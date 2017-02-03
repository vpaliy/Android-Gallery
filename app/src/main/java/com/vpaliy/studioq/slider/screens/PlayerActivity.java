package com.vpaliy.studioq.slider.screens;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.VideoView;

import com.vpaliy.studioq.R;
import com.vpaliy.studioq.utils.ProjectUtils;

public class PlayerActivity extends AppCompatActivity {

    private String pathTo;
    private VideoView video;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        if(savedInstanceState==null)
            savedInstanceState=getIntent().getExtras();

        pathTo=savedInstanceState.getString(ProjectUtils.MEDIA_DATA,null);
        int startPosition=savedInstanceState.getInt(ProjectUtils.POSITION,0);
        if(pathTo!=null) {
            video=(VideoView)(findViewById(R.id.videoPlayer));
            video.setVideoPath(pathTo);
            video.setMediaController(new MediaController(this));
            video.seekTo(startPosition);
            video.start();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(pathTo!=null) {
            outState.putString(ProjectUtils.MEDIA_DATA, pathTo);
        }
        outState.putInt(ProjectUtils.POSITION,video.getCurrentPosition());
    }


}
