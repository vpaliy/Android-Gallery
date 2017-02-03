package com.vpaliy.studioq.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import com.squareup.otto.Subscribe;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.activities.utils.eventBus.EventBusProvider;
import com.vpaliy.studioq.activities.utils.eventBus.ExitEvent;
import com.vpaliy.studioq.activities.utils.eventBus.Registrator;
import com.vpaliy.studioq.adapters.FolderUtilAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.BaseAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.MultiMode;
import com.vpaliy.studioq.model.DummyFolder;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.adapters.GalleryAdapter;
import com.vpaliy.studioq.model.MediaFolder;
import com.vpaliy.studioq.utils.FileUtils;
import com.vpaliy.studioq.utils.ProjectUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import static butterknife.ButterKnife.findById;

//TODO when snack is enabled and the activity finishes

public class GalleryFragment extends Fragment {

    private static final String TAG=GalleryFragment.class.getSimpleName();

    private static final String KEY=TAG+"util:enabled";

    public static GalleryFragment newInstance(@NonNull Bundle args) {
        GalleryFragment fragment=new GalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.mediaRecyclerView)
    protected RecyclerView recyclerView;

    @BindView(R.id.cameraButton)
    protected FloatingActionButton actionButton;

    private GalleryAdapter adapter;
    private MediaFolder mediaFolder;

    private FolderUtilAdapter utilAdapter;
    private ArrayList<DummyFolder> dummyFolders;

    private Unbinder unbinder;

    private MultiMode.Callback callback=new MultiMode.Callback() {

        private boolean showButton;

        @Override
        public boolean onMenuItemClick(BaseAdapter baseAdapter, MenuItem item) {
            showButton=true;
            switch (item.getItemId()) {
                case R.id.deleteItem:
                    showButton=false;
                    delete();
                    break;
                case R.id.shareItem:
                    share();
                    break;
                case R.id.checkAll:
                    adapter.checkAll(true);
                    break;
                case R.id.copyItem:
                    showButton=false;
                    change(false);
                    break;
                case R.id.moveItem:
                    showButton=false;
                    change(true);
                    break;

            }
            return true;
        }

        @Override
        public void onModeActivated() {
            super.onModeActivated();
            hideActionButton();
        }

        @Override
        public void onModeDisabled() {
            super.onModeDisabled();
            if(showButton) {
                showActionButton();
            }

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null) {
            savedInstanceState=getArguments();
        }

        setRetainInstance(true);
        restoreMediaDataList(savedInstanceState);
    }

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


    @Override
    public void onResume() {
        super.onResume();
        boolean isModeActivated=false;
        if(adapter!=null) {
            adapter.onResume();
            isModeActivated=adapter.isMultiModeActivated();
        }

        if(!isModeActivated && utilAdapter==null) {
            if(actionButton!=null) {
                actionButton.setVisibility(View.VISIBLE);
                if (actionButton.getScaleX() < 1f) {
                    actionButton.animate().scaleX(1.f).scaleY(1.f)
                            .setListener(null).setDuration(200)
                            .start();
                }
            }
        }
    }


    private void delete() {
        View root=getView();
        if (root!= null) {
            if (adapter.isMultiModeActivated()) {
                final ArrayList<MediaFile> deleteFolderList = adapter.getAllChecked();
                final ArrayList<MediaFile> originalList=new ArrayList<>(adapter.getData());
                recyclerView.setItemAnimator(null);
                final int[] checked=adapter.getAllCheckedForDeletion();
                //remove the items from the list
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        for(int index:checked) {
                            adapter.removeAt(index);
                        }
                    }
                });

                Snackbar.make(root,
                        //TODO support for languages here
                        Integer.toString(deleteFolderList.size()) + " have been moved to trash", 7000)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showActionButton();
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

                                        showActionButton();
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

    private void deleteInBackground(final List<MediaFile> mediaFileList, final boolean finish) {
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                FileUtils.deleteFileList(getContext(),mediaFileList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(finish) {
                    EventBusProvider.defaultBus().post(new ExitEvent(new Intent()));
                }
            }
        }.execute();
    }

    private void share() {
        List<MediaFile> data=adapter.getAllChecked();
        Intent shareIntent=new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        ArrayList<Uri> sharedData=new ArrayList<>(data.size());
        for(MediaFile mediaFile:data) {
            sharedData.add(Uri.fromFile(mediaFile.mediaFile()));
        }
        onNavigationIconClick.onClick(null);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,sharedData);
        shareIntent.setType("image/*");
        getContext().startActivity(Intent.createChooser(shareIntent,"Share via"));

    }

    public void showActionButton() {
        if(actionButton!=null) {
            if(!actionButton.isShown()) {
                actionButton.show();
            }
        }
    }

    public void hideActionButton() {
        if(actionButton!=null) {
            if (actionButton.isShown()) {
                actionButton.hide();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Registrator.register(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(adapter!=null) {
            adapter.saveState(outState);
        }
        outState.putBoolean(KEY,false);
        outState.putParcelable(ProjectUtils.MEDIA_FOLDER,mediaFolder);
        outState.putParcelableArrayList(ProjectUtils.ALL_MEDIA,dummyFolders);
        if(utilAdapter!=null) {
            utilAdapter.saveState(outState);
            outState.putBoolean(KEY, true);
        }
        super.onSaveInstanceState(outState);
    }

    private void restoreMediaDataList(Bundle savedInstanceState) {
        this.dummyFolders=savedInstanceState.getParcelableArrayList(ProjectUtils.ALL_MEDIA);
        this.mediaFolder=savedInstanceState.getParcelable(ProjectUtils.MEDIA_FOLDER);
    }

    @Override
    public View onCreateView(LayoutInflater mInflater, ViewGroup parentGroup, Bundle savedInstanceState) {
        View root= mInflater.inflate(R.layout.fragment_gallery,parentGroup,false);
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
            actionBar.setTitle(mediaFolder.getFolderName());
            actionBar.setNavigationOnClickListener(onNavigationIconClick);
            actionBar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
            MultiMode mode = new MultiMode.Builder(actionBar, getActivity())
                    .setBackgroundColor(Color.WHITE)
                    .setNavigationIcon(getResources().getDrawable(R.drawable.ic_cancel_black_24dp))
                    .setMenu(R.menu.gallery_menu, callback)
                    .build();
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false));

            //determine and set the adapter
            RecyclerView.Adapter tempAdapter;
            if(savedInstanceState!=null) {
                if(!savedInstanceState.getBoolean(KEY,false)) {
                    tempAdapter=adapter = new GalleryAdapter(getContext(), mode, mediaFolder.getMediaFileList(), savedInstanceState);
                }else {
                    adapter=new GalleryAdapter(getContext(),mode,mediaFolder.getMediaFileList());
                    tempAdapter=utilAdapter = new FolderUtilAdapter(getContext(),dummyFolders, savedInstanceState);
                }
            }else {
                tempAdapter=adapter = new GalleryAdapter(getContext(), mode, mediaFolder.getMediaFileList());
            }
            recyclerView.setAdapter(tempAdapter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(actionButton!=null) {
            actionButton.hide();
        }
    }


    public boolean onBackPressed() {
        if(adapter.isMultiModeActivated()) {
            onNavigationIconClick.onClick(null);
            return true;
        }
        return false;
    }

    @Subscribe
    public void onChange(@NonNull final MoveEvent event) {
        Log.d(TAG,"onChange");
        utilAdapter=null;
        recyclerView.setAdapter(adapter);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                View root = getView();
                if (root!= null) {
                    final ArrayList<MediaFile> delete=new ArrayList<>(event.checked.length);
                    final ArrayList<MediaFile> original=new ArrayList<>(mediaFolder.getMediaFileList());
                    if(event.move) {
                        for(int index=0, itemShift=0;index<event.checked.length;index++,itemShift++) {
                            int jIndex=event.checked[index]-itemShift;
                            delete.add(adapter.getData().get(jIndex));
                            adapter.removeAt(jIndex);
                        }
                    }
                    hideActionButton();
                    Snackbar.make(root,
                            //TODO support for languages here
                            Integer.toString(event.checked.length) + " have been moved to "+event.folderName, 7000)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    showActionButton();
                                    if(event.move) {
                                        mediaFolder.setMediaFileList(original);
                                        adapter.setData(original);
                                    }
                                }
                            })
                            .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                @Override
                                public void onDismissed(Snackbar transientBottomBar, int eve) {
                                    super.onDismissed(transientBottomBar, eve);
                                    switch (eve) {
                                        case DISMISS_EVENT_SWIPE:
                                        case DISMISS_EVENT_TIMEOUT:
                                            //if size == full --> finish activity
                                            showActionButton();
                                            //copy/move the data
                                            //                           FileUtils.copyFileList(getContext(),delete,new File(event.folderName),event.move);
                                            break;
                                    }
                                }
                            })
                            .show();
                }
            }
        });
    }


    private void change(boolean move) {
        int[] checked=adapter.getAllChecked(true);
        utilAdapter=new FolderUtilAdapter(getContext(),dummyFolders,checked,move);
        hideActionButton();
        recyclerView.setAdapter(utilAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        Registrator.unregister(this);
    }

    public void openCamera(View view) {
        startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));
    }

    public static class MoveEvent{
        @NonNull
        int[] checked;

        @NonNull
        String folderName;

        boolean move;

        public MoveEvent(@NonNull String folderName,@NonNull int[] checked,boolean move) {
            this.folderName=folderName;
            this.checked=checked;
            this.move=move;
        }
    }

}
