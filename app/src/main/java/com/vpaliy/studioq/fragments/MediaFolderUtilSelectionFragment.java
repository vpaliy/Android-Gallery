package com.vpaliy.studioq.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.vpaliy.studioq.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.vpaliy.studioq.model.MediaFolder;
import com.vpaliy.studioq.utils.ProjectUtils;

public class MediaFolderUtilSelectionFragment extends Fragment {


    private static final String TAG=MediaFolderUtilSelectionFragment.class.getSimpleName();

    private MediaFolderUtilAdapter adapter;
    private Bundle args;
    private OnMediaContainerSelectedListener mediaContainerSelectedListener;

    public static MediaFolderUtilSelectionFragment newInstance(Bundle args) {
        MediaFolderUtilSelectionFragment fragment=new MediaFolderUtilSelectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mediaContainerSelectedListener=(OnMediaContainerSelectedListener)(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if(savedInstanceState==null)
            savedInstanceState=getArguments();
        ArrayList<MediaFolder> mediaFolderList=savedInstanceState.getParcelableArrayList(ProjectUtils.ALL_MEDIA);
        adapter=new MediaFolderUtilAdapter(getContext(),mediaFolderList);
        args=savedInstanceState;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_representation_layout,container,false);
    }


    @Override
    public void onViewCreated(View root, @Nullable Bundle savedInstanceState) {
        if(root!=null) {
            RecyclerView mMediaRecyclerView=(RecyclerView)(root.findViewById(R.id.mediaRecyclerView));
            mMediaRecyclerView.setAdapter(adapter);
            //TODO change to xml representation
            if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE) {
                mMediaRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4, GridLayoutManager.VERTICAL, false));
            }else {
                mMediaRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
            }
            mMediaRecyclerView.setItemAnimator(new DefaultItemAnimator());
        }
    }


    private class MediaFolderUtilAdapter extends RecyclerView.Adapter<MediaFolderUtilAdapter.ItemHolder> {

        private List<MediaFolder> mediaFolderList;
        private LayoutInflater inflater;

        public MediaFolderUtilAdapter(Context context, List<MediaFolder> mediaFolderList) {
            this.inflater = LayoutInflater.from(context);
            this.mediaFolderList = mediaFolderList;
        }

        public class ItemHolder extends RecyclerView.ViewHolder {

            private ImageView headerImage;
            private TextView folderName;
            private TextView itemCount;

            public ItemHolder(View itemView) {
                super(itemView);
                this.headerImage=(ImageView)(itemView.findViewById(R.id.mainImage));
                this.folderName=(TextView)(itemView.findViewById(R.id.folderName));
                this.itemCount=(TextView)(itemView.findViewById(R.id.imageCount));

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        args.putString(ProjectUtils.MEDIA_FOLDER,
                            mediaFolderList.get(getAdapterPosition()).getAbsolutePathToFolder());
                        mediaContainerSelectedListener.
                            onMediaContainerSelected(args);
                        //folder has been selected, now launch executor of this task here :)
                    }
                });
            }

            public void onBindData(int position) {
                Glide.with(itemView.getContext()).load(mediaFolderList.get(position).getCoverForImage())
                        .asBitmap()
                        .centerCrop()
                        .into(headerImage);
                folderName.setText(mediaFolderList.get(position).getFolderName());
                itemCount.setText(String.format(Locale.US,"%d",mediaFolderList.get(position).getFileCount()));
            }

        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parentGroup, int viewType) {
            View root=inflater.inflate(R.layout.media_folder_adapter_item,parentGroup,false);
            return new ItemHolder(root);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            holder.onBindData(position);
        }

        @Override
        public int getItemCount() {
            return mediaFolderList.size();
        }
    }


    public interface OnMediaContainerSelectedListener {
        void onMediaContainerSelected(Bundle data);
    }
}
