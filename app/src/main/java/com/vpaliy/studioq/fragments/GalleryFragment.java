package com.vpaliy.studioq.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.vpaliy.studioq.App;
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
import com.vpaliy.studioq.utils.ProjectUtils;
import com.vpaliy.studioq.utils.snackbarUtils.ActionCallback;
import com.vpaliy.studioq.utils.snackbarUtils.SnackbarWrapper;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import static butterknife.ButterKnife.findById;
import android.widget.Toast;

import butterknife.OnClick;
import android.support.annotation.NonNull;
import com.squareup.otto.Subscribe;
import butterknife.BindView;

//TODO when snack is enabled and the activity finishes

public class GalleryFragment extends Fragment {

    private static final String TAG=GalleryFragment.class.getSimpleName();

    private static final String KEY=TAG+"util:enabled";
    private static final String UPDATED=TAG+"data:updated";

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

    private ArrayList<MediaFolder> updatedData;
    private FolderUtilAdapter utilAdapter;
    private ArrayList<DummyFolder> dummyFolders;
    private boolean changed=false;

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

                SnackbarWrapper.start(root,Integer.toString(deleteFolderList.size()) +
                        " have been moved to trash", R.integer.snackbarLength)
                        .callback(new ActionCallback("UNDO") {
                            @Override
                            public void onCancel() {
                                showActionButton();
                                adapter.setData(originalList);
                            }

                            @Override
                            public void onPerform() {
                                showActionButton();
                                deleteInBackground(deleteFolderList,
                                        deleteFolderList.size() == originalList.size());
                            }
                        }).show();
            }
        }
    }

    private void deleteInBackground(final ArrayList<MediaFile> mediaFileList, final boolean finish) {
        changed=true;
        App.appInstance().delete(mediaFileList);
        if(finish) {
            finish();
        }
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

    private void finish() {
        if(changed || updatedData!=null) {
            Intent data = new Intent();
            if(updatedData!=null) {
                data.putExtra(ProjectUtils.MEDIA_DATA,updatedData);
            }
            if(changed) {
                data.putExtra(ProjectUtils.MEDIA_FOLDER, mediaFolder);
            }
            EventBusProvider.defaultBus().post(new ExitEvent(data));
        }else {
            EventBusProvider.defaultBus().post(new ExitEvent(null));
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
        outState.putBoolean(ProjectUtils.DELETED,changed);
        outState.putBoolean(KEY,false);
        outState.putParcelable(ProjectUtils.MEDIA_FOLDER,mediaFolder);
        outState.putParcelableArrayList(ProjectUtils.ALL_MEDIA,dummyFolders);
        if(updatedData!=null) {
            outState.putParcelableArrayList(UPDATED, updatedData);
        }
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
                changed=savedInstanceState.getBoolean(ProjectUtils.DELETED);
                updatedData=savedInstanceState.getParcelableArrayList(UPDATED);
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


    public void onBackPressed() {
        if(adapter.isMultiModeActivated()) {
            onNavigationIconClick.onClick(null);
        }else {
            finish();
        }
    }

    @Subscribe
    public void onChange(@NonNull final MoveEvent event) {
        utilAdapter=null;
        recyclerView.setAdapter(adapter);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                moveData(event);
            }
        });
    }


    private void moveData(@NonNull final MoveEvent event) {
        View root = getView();
        if (root!= null) {
            final ArrayList<MediaFile> delete = new ArrayList<>(event.checked.length);
            final ArrayList<MediaFile> original = new ArrayList<>(mediaFolder.getMediaFileList());

            for (int index = 0, itemShift = 0; index < event.checked.length; index++) {
                int jIndex = event.checked[index] - itemShift;
                delete.add(adapter.getData().get(jIndex));
                if(event.move) {
                    adapter.removeAt(jIndex);
                    itemShift++;
                }
            }

            final ArrayList<MediaFile> temp=new ArrayList<>(delete);
            updateWith(event.moveFolder.getName(),delete);
            if(!delete.isEmpty()) {
                hideActionButton();
               actionSnackbarWith(event,root,original,delete);
            }else {
                if(event.move) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                    builder.setMessage(R.string.copyDuplicate)
                            .setPositiveButton(R.string.Okay,new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mediaFolder.setMediaFileList(original);
                                    adapter.setData(original);
                                }
                            })
                            .setNegativeButton(R.string.Cancel,new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int index) {
                                    App.appInstance().delete(temp);
                                    dialogInterface.cancel();
                                }
                            })
                            .create().show();
                }else {
                    Toast.makeText(getContext(), "You have already copied the data", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }



    private void actionSnackbarWith(@NonNull final MoveEvent event, @NonNull View root,
                            final ArrayList<MediaFile> original, final ArrayList<MediaFile> delete) {
        SnackbarWrapper.start(root,
                Integer.toString(event.checked.length) +
                        " have been moved to " + event.moveFolder.getName(), R.integer.snackbarLength)
                .callback(new ActionCallback("UNDO") {
                    @Override
                    public void onCancel() {
                        showActionButton();
                        if (event.move) {
                            mediaFolder.setMediaFileList(original);
                            adapter.setData(original);
                        }
                    }

                    @Override
                    public void onPerform() {
                        changed = event.move || changed;

                        //make the operation here
                        Map<String, ArrayList<MediaFile>> mapData = new HashMap<>();
                        mapData.put(event.moveFolder.getAbsolutePath(), delete);
                        App.appInstance().copy(mapData);

                        //cancel if needed
                        if (event.move) {
                            if (delete.size() == original.size()) {
                                finish();
                                return;
                            }
                        }
                        showActionButton();
                    }
                }).show();
    }


    //update the date that will be sent back to the Activity
    private void updateWith(String folder,ArrayList<MediaFile> data) {
        if(updatedData==null) {
            updatedData=new ArrayList<>();
        }

        MediaFolder mediaFolder=new MediaFolder(folder,data);
        int index=updatedData.indexOf(mediaFolder);
        if(index<0) {
            updatedData.add(mediaFolder);
        }else {
            MediaFolder current=updatedData.get(index);

            //remove the data that has been copied more than once
            data.removeAll(current.getMediaFileList());

            //update
            current.updateWith(mediaFolder);
        }
    }


    //this method changes the adapter in order to provide users with the UI for selecting a folder
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

    @OnClick(R.id.cameraButton)
    public void openCamera(View view) {
        startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));
    }

    /**
     * This nested class is used as a wrapper
     * When a user has selected a folder where to copy/move selected items
     */
    public static class MoveEvent{
        @NonNull
        int[] checked;

        @NonNull
        File moveFolder;

        boolean move;

        public MoveEvent(@NonNull File moveFolder,@NonNull int[] checked,boolean move) {
            this.moveFolder=moveFolder;
            this.checked=checked;
            this.move=move;
        }
    }

}
