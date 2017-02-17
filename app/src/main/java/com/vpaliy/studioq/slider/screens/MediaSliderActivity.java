package com.vpaliy.studioq.slider.screens;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.slider.adapters.ContentAdapter;
import com.vpaliy.studioq.slider.adapters.NavigationAdapter;
import com.vpaliy.studioq.slider.listeners.OnSliderEventListener;
import com.vpaliy.studioq.slider.screens.cases.DeleteCase;
import com.vpaliy.studioq.slider.screens.cases.EditCase;
import com.vpaliy.studioq.slider.screens.cases.NavigationCase;
import com.vpaliy.studioq.slider.tranformations.ZoomIn;
import com.vpaliy.studioq.slider.utils.PhotoSlider;
import com.vpaliy.studioq.utils.ProjectUtils;
import com.yalantis.ucrop.UCrop;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MediaSliderActivity extends AppCompatActivity
        implements OnSliderEventListener {

    @BindView(R.id.mediaNavigator)
    protected RecyclerView mediaNavigator;

    @BindView(R.id.mediaSlider)
    protected PhotoSlider mediaSlider;

    private ContentAdapter contentAdapter;
    private NavigationAdapter navigationAdapter;

    private boolean hasDeletionOccurred;
    private int startPosition;

    private NavigationCase navigationCase;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);
        ButterKnife.bind(this);
        if(savedInstanceState==null) {
            savedInstanceState = getIntent().getExtras();
        }
        initUI(savedInstanceState);
    }

    private void initActionBar() {
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        Toolbar actionBar=ButterKnife.findById(this,R.id.actionBar);
        actionBar.setVisibility(View.INVISIBLE);
        actionBar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        actionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if(getSupportActionBar()==null) {
            actionBar.setTitle(null);
            setSupportActionBar(actionBar);
        }

    }

    private void initUI(Bundle args) {
        //the data for adapters
        ArrayList<MediaFile> mediaData=args.getParcelableArrayList(ProjectUtils.MEDIA_DATA);

        navigationCase=NavigationCase.start(this,getWindow().getDecorView());
        initMediaSlider(args, mediaData);
        initTransformation();
        initNavigator(mediaData);
        initActionBar();

    }

    private void initNavigator(ArrayList<MediaFile> data) {
        mediaNavigator.setAdapter((navigationAdapter = new NavigationAdapter(MediaSliderActivity.this, data, this)));
        mediaNavigator.setLayoutManager(new LinearLayoutManager(MediaSliderActivity.this, LinearLayoutManager.HORIZONTAL, false));
        mediaNavigator.setItemAnimator(new DefaultItemAnimator());
        mediaNavigator.setVisibility(View.INVISIBLE);
        mediaNavigator.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                navigationCase.carryOn();
            }
        });
        onClick(startPosition);
    }

    private void initMediaSlider(Bundle args, ArrayList<MediaFile> data) {
        startPosition = args.getInt(ProjectUtils.POSITION);
        hasDeletionOccurred = args.getBoolean(ProjectUtils.DELETED);
        mediaSlider.setAdapter((contentAdapter = new ContentAdapter(this, data, this)));
        mediaSlider.post(new Runnable() {
            @Override
            public void run() {
                mediaSlider.setCurrentItem(startPosition);
            }
        });
    }

    private void initTransformation() {
        mediaSlider.setPageTransformer(false,new ZoomIn());
    }

    @Override
    public void onClick(int position) {
        mediaSlider.setCurrentItem(position);
        mediaNavigator.scrollToPosition(position);
        navigationCase.makeAction();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.slider_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MediaFile mediaFile=contentAdapter.dataAt(mediaSlider.getCurrentItem());

        switch (item.getItemId()) {
            case R.id.home:
                onBackPressed();
                return true;
            case R.id.deleteItem:
                DeleteCase.startWith(this,contentAdapter.getData())
                        .subscribeForChange(contentAdapter)
                        .subscribeForChange(navigationAdapter)
                        .blockNavigation(navigationCase)
                        .startUIChain();
                return true;
            case R.id.shareItem:
                return true;
            case R.id.showInfo:
                break;
            case R.id.filter:
                EditCase.start(this,mediaFile)
                        .execute();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ProjectUtils.POSITION,startPosition);
        outState.putBoolean(ProjectUtils.DELETED,hasDeletionOccurred);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK) {
            switch (requestCode) {
                case UCrop.REQUEST_CROP:
                    if(data!=null) {
                        EditCase.handleResult(data);
                    }
            }

        }
    }

    @Override
    public boolean isNavigationActivated() {
        return navigationCase.isActivated();
    }

    @Override
    public void onBackPressed() {
        if(hasDeletionOccurred) {
            Intent data=new Intent();
            data.putParcelableArrayListExtra(ProjectUtils.MEDIA_DATA,
                contentAdapter.getData());
            setResult(RESULT_OK,data);
        }
        super.onBackPressed();
    }
}