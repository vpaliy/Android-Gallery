package com.vpaliy.studioq.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import java.util.ArrayList;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.media.MediaFile;
import com.vpaliy.studioq.adapters.GalleryAdapter;
import com.vpaliy.studioq.utils.FragmentPageAdapter;
import com.vpaliy.studioq.utils.ProjectUtils;


public class GalleryFragment extends BaseMediaFragment<MediaFile> {

    private GalleryAdapter adapter;

    public final static FragmentPageAdapter.FragmentInstanceProvider<MediaFile> PROVIDER=new FragmentPageAdapter.FragmentInstanceProvider<MediaFile>() {
        @Override
        public Fragment createInstance(ArrayList<MediaFile> mDataModel) {
            GalleryFragment galleryFragment=new GalleryFragment();
            Bundle args=new Bundle();
            args.putParcelableArrayList(ProjectUtils.MEDIA_DATA,mDataModel);
            galleryFragment.setArguments(args);
            return galleryFragment;
        }
    };


    @Override
    public void restoreMediaDataList(Bundle savedInstanceState) {
        this.mMediaDataList=savedInstanceState.getParcelableArrayList(ProjectUtils.MEDIA_DATA);
        this.adapter=new GalleryAdapter(getContext(),getMultiChoiceModeInstance(R.menu.gallery_menu),mMediaDataList);
    }

    public GalleryAdapter getGalleryAdapter() {
        return adapter;
    }

    //TODO fix grid size
    @Override
    public void onViewCreated(View root, Bundle savedInstanceState) {
        if(root!=null) {
            RecyclerView mMediaRecyclerView=(RecyclerView)(root.findViewById(R.id.mediaRecyclerView));
            if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT) {
                mMediaRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false));
            }
            else {
                mMediaRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4, GridLayoutManager.VERTICAL, false));
            }
            mMediaRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mMediaRecyclerView.setAdapter(adapter);
            mMediaRecyclerView.setHasFixedSize(true);
        }
    }
}
