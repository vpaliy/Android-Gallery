package com.vpaliy.studioq.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import java.util.List;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.adapters.multipleChoice.BaseAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.MultiMode;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.adapters.GalleryAdapter;
import com.vpaliy.studioq.utils.FragmentPageAdapter;
import com.vpaliy.studioq.utils.ProjectUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import static butterknife.ButterKnife.findById;


public class GalleryFragment extends BaseMediaFragment<MediaFile> {

    @BindView(R.id.mediaRecyclerView)
    protected RecyclerView recyclerView;

    @BindView(R.id.cameraButton)
    protected FloatingActionButton actionButton;

    private GalleryAdapter adapter;

    private Unbinder unbinder;

    private MultiMode.Callback callback=new MultiMode.Callback() {
        @Override
        public boolean onMenuItemClick(BaseAdapter baseAdapter, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.deleteItem:
                    delete();
                    break;
            }
            return true;
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


    private void delete() {
        View root=getView();
        if (root!= null) {
            if (adapter.isMultiModeActivated()) {
                final ArrayList<MediaFile> deleteFolderList = adapter.getAllChecked();
                final ArrayList<MediaFile> originalList=new ArrayList<>(adapter.getData());
                int[] checked=adapter.getAllCheckedForDeletion();
                for(int index:checked) {
                    adapter.removeAt(index);
                }
                Snackbar.make(root,
                        //TODO support for languages here
                        Integer.toString(deleteFolderList.size()) + " have been moved to trash", 7000)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                adapter.setData(originalList);
                            }
                        })
                        .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);
                                switch (event) {
                                    case DISMISS_EVENT_SWIPE:
                                    case DISMISS_EVENT_TIMEOUT:
                                        deleteInBackground(deleteFolderList,
                                             deleteFolderList.size()==originalList.size());
                                        break;
                                }
                            }
                        })
                        .show();
            }
        }
    }

    private void deleteInBackground(List<MediaFile> mediaFileList, boolean finish) {

    }


    //TODO may be you don't need that
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
        View root= mInflater.inflate(R.layout.fragment_gallery_layout,parentGroup,false);
        unbinder= ButterKnife.bind(this,root);
        return root;
    }

    public GalleryAdapter getGalleryAdapter() {
        return adapter;
    }

    @Override
    public void onViewCreated(View root, Bundle savedInstanceState) {
        if(root!=null) {
            final Toolbar actionBar=findById(root,R.id.actionBar);
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
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    public void openCamera(View view) {
        startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));
    }
}
