package com.vpaliy.studioq.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.adapters.multipleChoice.BaseAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.MultiMode;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.adapters.GalleryAdapter;
import com.vpaliy.studioq.utils.FragmentPageAdapter;
import com.vpaliy.studioq.utils.ProjectUtils;


public class GalleryFragment extends BaseMediaFragment<MediaFile> {

    private GalleryAdapter adapter;
    private RecyclerView recyclerView;

    private MultiMode.Callback callback=new MultiMode.Callback() {
        @Override
        public boolean onMenuItemClick(BaseAdapter adapter, MenuItem item) {
            return false;
        }

    };

    private View.OnClickListener onNavigationIconClick=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(adapter!=null) {
                if(adapter.isMultiModeActivated()) {
                    recyclerView.setItemAnimator(null);
                    adapter.unCheckAll(true);
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                        }
                    });
                    return;
                }
            }
            getActivity().finish();
        }
    };

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
    public void onResume() {
        super.onResume();
        if(adapter!=null) {
            adapter.onResume();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(adapter!=null) {
            adapter.saveState(outState);
        }
    }

    @Override
    public void restoreMediaDataList(Bundle savedInstanceState) {
        this.mMediaDataList=savedInstanceState.getParcelableArrayList(ProjectUtils.MEDIA_DATA);
    }

    @Override
    public View onCreateView(LayoutInflater mInflater, ViewGroup parentGroup, Bundle savedInstanceState) {
        return mInflater.inflate(R.layout.fragment_gallery_layout,parentGroup,false);
    }

    public GalleryAdapter getGalleryAdapter() {
        return adapter;
    }

    @Override
    public void onViewCreated(View root, Bundle savedInstanceState) {
        if(root!=null) {
            final Toolbar actionBar=(Toolbar)(root.findViewById(R.id.actionBar));
            actionBar.setNavigationIcon(R.drawable.ic_clear_black_24dp);
            recyclerView=(RecyclerView)(root.findViewById(R.id.mediaRecyclerView));
            actionBar.setNavigationOnClickListener(onNavigationIconClick);
            MultiMode mode = new MultiMode.Builder(actionBar, getActivity())
                    .setBackgroundColor(Color.WHITE)
                    .setNavigationIcon(getResources().getDrawable(R.drawable.ic_clear_black_24dp))
                    .setMenu(R.menu.gallery_menu, callback)
                    .build();
            if(savedInstanceState!=null) {
                adapter = new GalleryAdapter(getContext(), mode, mMediaDataList,savedInstanceState);
            }else {
                adapter = new GalleryAdapter(getContext(), mode, mMediaDataList);
            }
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false));
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);
        }
    }

    public void openCamera(View view) {
        startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));
    }
}
