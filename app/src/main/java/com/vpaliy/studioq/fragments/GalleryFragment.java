package com.vpaliy.studioq.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.vpaliy.studioq.cases.DeleteCase;
import com.vpaliy.studioq.cases.SortCase;
import com.vpaliy.studioq.common.graphicalUtils.ScaleBuilder;
import com.vpaliy.studioq.common.eventBus.EventBusProvider;
import com.vpaliy.studioq.common.eventBus.ExitEvent;
import com.vpaliy.studioq.common.eventBus.Registrator;
import com.vpaliy.studioq.adapters.FolderUtilAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.BaseAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.MultiMode;
import com.vpaliy.studioq.controllers.DataController;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.adapters.GalleryAdapter;
import com.vpaliy.studioq.model.MediaFolder;
import com.vpaliy.studioq.common.utils.ProjectUtils;
import com.vpaliy.studioq.common.snackbarUtils.ActionCallback;
import com.vpaliy.studioq.common.snackbarUtils.SnackbarWrapper;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import static butterknife.ButterKnife.findById;

import butterknife.OnClick;
import android.support.annotation.NonNull;
import com.squareup.otto.Subscribe;
import butterknife.BindView;
import android.support.annotation.Nullable;


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

    @BindView(R.id.actionButton)
    protected FloatingActionButton actionButton;

    @BindView(R.id.actionBar)
    protected Toolbar actionBar;

    private ActionBarCallback actionBarCallback;

    private MediaFolder mediaFolder;

    private GalleryAdapter adapter;
    private FolderUtilAdapter utilAdapter;

    private boolean changed=false;

    private Unbinder unbinder;

    private MultiMode.Callback callback=new MultiMode.Callback() {

        private boolean showButton;
        private boolean showMenu;

        @Override
        public boolean onMenuItemClick(BaseAdapter baseAdapter, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.deleteItem:
                    showButton=false;
                    delete();
                    return  true;
                case R.id.shareItem:
                    share();
                    return  true;
                case R.id.checkAll:
                    adapter.checkAll(true);
                    return  true;
                case R.id.copyItem:
                    showMenu=false;
                    showButton=false;
                    change(false);
                    return  true;
                case R.id.moveItem:
                    showMenu=false;
                    showButton=false;
                    change(true);
                    return  true;

            }
            return onOptionsItemSelected(item);
        }

        @Override
        public void onModeActivated() {
            super.onModeActivated();
            showButton=showMenu=true;
            hideActionButton();
            setHasOptionsMenu(false);
        }

        @Override
        public void onModeDisabled() {
            super.onModeDisabled();
            if(showButton) {
                showActionButton();
            }

            if(showMenu) {
                setHasOptionsMenu(true);
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.actionBarCallback=ActionBarCallback.class.cast(getContext());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null) {
            savedInstanceState=getArguments();
        }

        setHasOptionsMenu(true);
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
            ScaleBuilder.start(actionButton,1.f)
                    .duration(300)
                    .applyCondition()
                    .accelerate()
                    .execute();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.gallery_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getGroupId()==R.id.sortingChoice) {
            SortCase<MediaFile> sortCase=SortCase.startWith(adapter.getData());
            switch (item.getItemId()) {
                case R.id.byDate:
                    sortCase.comparator(MediaFile.BY_DATE);
                    break;
                case R.id.byName:
                    sortCase.comparator(MediaFile.BY_NAME);
                    break;
                case R.id.bySize:
                    sortCase.comparator(MediaFile.BY_SIZE);
                    break;
            }
            sortCase.callback(new SortCase.Callback<MediaFile>() {
                @Override
                public void onFinished(List<MediaFile> model) {
                    adapter.notifyDataSetChanged();
                    changed=true;
                    DataController.controllerInstance()
                        .justUpdateOrder(mediaFolder.getAbsolutePathToFolder(),
                            model);
                }
            }).execute();
            return true;
        }else {
            switch (item.getItemId()) {
                case R.id.sort:
                    //expand the choices
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private void delete() {
        View root=getView();
        if(root!=null) {
            DeleteCase.startWith(root, adapter)
                    .filter(new DeleteCase.Filter<MediaFile>() {
                        @Override
                        public ArrayList<MediaFile> filterData(List<MediaFile> input) {
                            return new ArrayList<>(input);
                        }
                    }).callback(new DeleteCase.Callback() {
                        @Override
                        public void onExecute(ArrayList<MediaFile> result) {
                            changed=true;
                            DataController.controllerInstance().
                                    sensitiveDelete(result);
                            if(adapter.isEmpty()) {
                                finish();
                            }
                        }
                    }).execute();
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
        if(changed) {
            Log.d(TAG,"Changed");
            Intent dummyData = new Intent();
            EventBusProvider.defaultBus().post(new ExitEvent(dummyData));
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
        if(utilAdapter!=null) {
            utilAdapter.saveState(outState);
            outState.putBoolean(KEY, true);
        }
        super.onSaveInstanceState(outState);
    }

    private void restoreMediaDataList(Bundle savedInstanceState) {
        this.mediaFolder=savedInstanceState.getParcelable(ProjectUtils.MEDIA_FOLDER);
    }

    @Override
    public View onCreateView(LayoutInflater mInflater, ViewGroup parentGroup, Bundle savedInstanceState) {
        View root= mInflater.inflate(R.layout.fragment_gallery,parentGroup,false);
        unbinder= ButterKnife.bind(this,root);
        actionBar.setTitle(mediaFolder.name());
        actionBar.setNavigationOnClickListener(onNavigationIconClick);
        actionBar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        actionBarCallback.hookUp(actionBar);
        return root;
    }


    @Override
    public void onViewCreated(View root, Bundle savedInstanceState) {
        if(root!=null) {
            MultiMode mode = new MultiMode.Builder(actionBar, getActivity())
                    .setBackgroundColor(Color.WHITE)
                    .setNavigationIcon(getResources().getDrawable(R.drawable.ic_cancel_black_24dp))
                    .setMenu(R.menu.gallery_choice_menu, callback)
                    .build();
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false));
            //determine and set the adapter
            RecyclerView.Adapter tempAdapter;
            if(savedInstanceState!=null) {
                changed=savedInstanceState.getBoolean(ProjectUtils.DELETED);
                if(!savedInstanceState.getBoolean(KEY,false)) {
                    tempAdapter=adapter = new GalleryAdapter(getContext(), mode, mediaFolder.list(), savedInstanceState);
                }else {
                    adapter=new GalleryAdapter(getContext(),mode,mediaFolder.list());
                    tempAdapter=utilAdapter = new FolderUtilAdapter(getContext(), savedInstanceState);
                }
            }else {
                tempAdapter=adapter = new GalleryAdapter(getContext(), mode, mediaFolder.list());
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
        if(utilAdapter!=null) {
            //change the adapter
            setHasOptionsMenu(true);
            onChange(null);

        } else if(adapter.isMultiModeActivated()) {
            onNavigationIconClick.onClick(null);
        }else {
            finish();
        }
    }

    @Subscribe
    public void onChange(@Nullable final MoveEvent event) {
        utilAdapter=null;
        recyclerView.setAdapter(adapter);
        if(event!=null) {
            setHasOptionsMenu(true);
        }
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                if(event!=null) {
                    moveData(event);
                }
            }
        });
    }


    private void moveData(@NonNull final MoveEvent event) {
        View root = getView();
        if (root!= null) {
            final ArrayList<MediaFile> delete = new ArrayList<>(event.checked.length);
            final ArrayList<MediaFile> original = new ArrayList<>(mediaFolder.list());

            for (int index = 0, itemShift = 0; index < event.checked.length; index++) {
                int jIndex = event.checked[index] - itemShift;
                delete.add(adapter.getData().get(jIndex));
                if(event.move) {
                    adapter.removeAt(jIndex);
                    itemShift++;
                }
            }

            hideActionButton();
            actionSnackbarWith(event,root,original,delete);
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
                        changed = true;

                        //make the operation here
                        ArrayList<MediaFile> result=MediaFile.
                            createReferenceList(event.moveFolder.getAbsolutePath(),delete);
                        Map<String, ArrayList<MediaFile>> mapData = new HashMap<>();
                        mapData.put(event.moveFolder.getAbsolutePath(), delete);

                        DataController.controllerInstance().
                            sensitiveUpdate(event.moveFolder.getAbsolutePath(),result);

                        if(event.move) {
                            App.appInstance().move(mapData);
                            DataController.controllerInstance()
                                .sensitiveDelete(delete);
                            if (delete.size() == original.size()) {
                                finish();
                                return;
                            }
                        }else {
                            App.appInstance().copy(mapData);
                        }

                        showActionButton();
                    }
                }).show();
    }

    //this method changes the adapter in order to provide users with the UI for selecting a folder
    private void change(boolean move) {
        int[] checked=adapter.getAllChecked(true);
        utilAdapter=new FolderUtilAdapter(getContext(),checked,move);
        hideActionButton();
        recyclerView.setAdapter(utilAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        Registrator.unregister(this);
    }

    @OnClick(R.id.actionButton)
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

    public interface ActionBarCallback {
        void hookUp(@NonNull Toolbar toolbar);
        void invalidateOptionsMenu();
    }
}
