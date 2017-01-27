package com.vpaliy.studioq.slider.screens;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.squareup.otto.Subscribe;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.activities.utils.eventBus.ChangeFilter;
import com.vpaliy.studioq.activities.utils.eventBus.Registrator;
import com.vpaliy.studioq.slider.adapters.ThumbnailAdapter;
import com.vpaliy.studioq.utils.ProjectUtils;
import com.zomato.photofilters.SampleFilters;

import java.util.Arrays;
import java.util.List;


public class FilterActivity extends AppCompatActivity {

    private MediaFile mediaFile;
    private ImageView mainImage;
    private Bitmap imageBitmap;

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Registrator.register(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        if(savedInstanceState==null) {
            savedInstanceState = getIntent().getExtras();
        }

        mediaFile=savedInstanceState.getParcelable(ProjectUtils.MEDIA_DATA);
        imageBitmap=savedInstanceState.getParcelable(ProjectUtils.BITMAP);

        initUI();
    }


    private void initUI() {
        this.mainImage=(ImageView)(findViewById(R.id.filteredImage));
        if(imageBitmap==null) {
            Glide.with(this)
                    .load(mediaFile.mediaFile())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            imageBitmap=resource;
                            mainImage.setImageBitmap(imageBitmap);
                            RecyclerView thumbnailList=(RecyclerView)(findViewById(R.id.thumbnailList));
                            thumbnailList.setLayoutManager(new LinearLayoutManager(FilterActivity.this,LinearLayoutManager.HORIZONTAL,false));
                            List<ThumbnailAdapter.ThumbnailItem> list= Arrays.asList(new ThumbnailAdapter.ThumbnailItem(SampleFilters.getBlueMessFilter(),imageBitmap),
                                    new ThumbnailAdapter.ThumbnailItem(SampleFilters.getLimeStutterFilter(),imageBitmap),
                                    new ThumbnailAdapter.ThumbnailItem(SampleFilters.getNightWhisperFilter(),imageBitmap),
                                    new ThumbnailAdapter.ThumbnailItem(SampleFilters.getStarLitFilter(),imageBitmap),
                                    new ThumbnailAdapter.ThumbnailItem(SampleFilters.getAweStruckVibeFilter(),imageBitmap));

                            thumbnailList.setAdapter(new ThumbnailAdapter(FilterActivity.this,list));
                        }
                    });
        }else {
            mainImage.setImageBitmap(imageBitmap);
        }


    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ProjectUtils.MEDIA_DATA,mediaFile);
        outState.putParcelable(ProjectUtils.BITMAP,imageBitmap);
    }

    @Subscribe
    public void onFilterClicked(ChangeFilter changeFilter){
        mainImage.setImageBitmap(changeFilter.filter.processFilter(imageBitmap));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Registrator.unregister(this);
    }
}
