package com.vpaliy.studioq.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.activities.utils.eventBus.EventBusProvider;
import com.vpaliy.studioq.activities.utils.eventBus.Launcher;
import com.vpaliy.studioq.activities.utils.eventBus.Registrator;
import com.vpaliy.studioq.activities.utils.eventBus.ReviewStateTrigger;
import com.vpaliy.studioq.adapters.multipleChoice.BaseAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.MultiMode;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.utils.ProjectUtils;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

import static butterknife.ButterKnife.findById;

//TODO check out removing in BaseAdapter manually

public class UtilSelectionFragment extends Fragment {


    public static UtilSelectionFragment newInstance(ArrayList<MediaFile> mediaFileList) {
        UtilSelectionFragment fragment=new UtilSelectionFragment();
        Bundle args=new Bundle();
        args.putParcelableArrayList(ProjectUtils.MEDIA_DATA,mediaFileList);
        fragment.setArguments(args);
        return fragment;
    }


    private List<MediaFile> mediaFileList;
    private MediaAdapter adapter;

    private MultiMode.Callback callback=new MultiMode.Callback() {
        @Override
        public boolean onMenuItemClick(BaseAdapter baseAdapter, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.proceed:
                    ArrayList<MediaFile> mediaFileList=adapter.getAllChecked();
                    if(mediaFileList==null) {
                        Toast.makeText(getContext(), "You did not select any item", Toast.LENGTH_SHORT).show();
                    }else {
                        Launcher<ArrayList<MediaFile>> launcher=new Launcher<>(mediaFileList,null);
                        EventBusProvider.defaultBus().post(launcher);
                    }
                    break;
                case R.id.checkAll:
                    baseAdapter.checkAll(true);
            }

            return true;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if(savedInstanceState==null)
            savedInstanceState=getArguments();
        mediaFileList=savedInstanceState.getParcelableArrayList(ProjectUtils.MEDIA_DATA);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.media_selection_layout,container,false);
    }

    @Override
    public void onViewCreated(View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        if(root!=null) {
            Toolbar actionBar=findById(root,R.id.actionBar);
            actionBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(adapter.getItemCount()==0) {
                        getActivity().finish();
                    }else {
                        adapter.unCheckAll(true);
                    }
                }
            });
            MultiMode mode = new MultiMode.Builder(actionBar, getActivity())
                    .setBackgroundColor(Color.WHITE)
                    .setMenu(R.menu.selection_util_menu, callback)
                    .setNavigationIcon(getResources().getDrawable(R.drawable.ic_clear_black_24dp))
                    .build();
            if(savedInstanceState==null) {
                adapter = new MediaAdapter(mediaFileList, mode);
            }else {
                adapter = new MediaAdapter(mediaFileList, mode, savedInstanceState);
            }

            adapter.turnOn();

            RecyclerView recyclerView=findById(root,R.id.mediaRecyclerView);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(),4,GridLayoutManager.VERTICAL,false));
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemAnimator(null);
            recyclerView.setAdapter(adapter);
        }
    }

    @Subscribe
    public void onRemoved(ReviewStateTrigger stateTrigger) {
        if(adapter!=null) {
            adapter.notifyAbout(stateTrigger.mediaFile);
        }
    }


    public class MediaAdapter extends BaseAdapter {

        private List<MediaFile> mediaFileList;
        private LayoutInflater inflater=LayoutInflater.from(getContext());

        public MediaAdapter(List<MediaFile> mediaFileList, MultiMode mode) {
            super(mode,true);
            this.mediaFileList=mediaFileList;
        }

        public MediaAdapter(List<MediaFile> mediaFileList, MultiMode mode, @NonNull Bundle state) {
            super(mode,true,state);
            this.mediaFileList=mediaFileList;
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MediaHolder(inflater.inflate(R.layout.gallery_item,parent,false));
        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {
            holder.onBindData();
        }

        public class MediaHolder extends BaseAdapter.BaseViewHolder {

            @BindView(R.id.galleryItem) ImageView thumbImage;

            public MediaHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this,itemView);
            }

            @Override
            public void updateBackground() {

            }

            @Override
            public void onBindData() {
                Glide.with(itemView.getContext())
                        .load(mediaFileList.get(getAdapterPosition()).mediaFile())
                        .asBitmap()
                        .centerCrop()
                        .into(thumbImage);
                determineState();
            }

            @Override
            public void enterState() {
                super.enterState();
                itemView.animate()
                        .scaleX(SCALE_F)
                        .scaleY(SCALE_F)
                        .setDuration(180).start();
            }

            @Override
            public void exitState() {
                super.exitState();
                if (itemView.getScaleY() < 1.f) {
                    itemView.animate().setDuration(180)
                            .scaleY(1.f).scaleX(1.f)
                            .start();
                }
            }

            @Override
            public void animatedState() {
                itemView.setScaleX(SCALE_F);
                itemView.setScaleY(SCALE_F);
            }

            @Override
            public void defaultState() {
                if(itemView.getScaleX()<1f) {
                    itemView.setScaleX(1.f);
                    itemView.setScaleY(1.f);
                }
            }
        }


        public void notifyAbout(MediaFile mediaFile) {
            if(mediaFile!=null) {
                int index=mediaFileList.indexOf(mediaFile);
                if(index>=0) {
                    removeAt(index,false);
                    notifyItemChanged(index);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mediaFileList.size();
        }


        public ArrayList<MediaFile> getAllChecked() {
            int[] checked=super.getAllChecked(false);
            if(checked!=null) {
                ArrayList<MediaFile> resultList = new ArrayList<>(checked.length);
                for (int index : checked) {
                    resultList.add(mediaFileList.get(index));
                }
                return resultList;
            }
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(adapter!=null) {
            adapter.onResume();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Registrator.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Registrator.unregister(this);
    }
}
