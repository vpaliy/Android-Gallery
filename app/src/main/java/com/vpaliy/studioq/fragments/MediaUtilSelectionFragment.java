package com.vpaliy.studioq.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.squareup.otto.Subscribe;
import com.vpaliy.studioq.media.DynamicImageView;
import com.vpaliy.studioq.media.MediaFile;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.screens.utils.eventBus.Registrator;
import com.vpaliy.studioq.screens.utils.eventBus.ReviewStateTrigger;
import com.vpaliy.studioq.utils.ProjectUtils;

public class MediaUtilSelectionFragment extends Fragment
        implements View.OnClickListener{

    private static final String TAG=MediaUtilSelectionFragment.class.getSimpleName();

    private List<MediaFile> mediaFileList;
    private MediaAdapter adapter;
    private OnMediaSetCreatedListener mediaSetCreatedListener;

    public static MediaUtilSelectionFragment newInstance(ArrayList<MediaFile> mediaFileList) {
        MediaUtilSelectionFragment fragment=new MediaUtilSelectionFragment();
        Bundle args=new Bundle();
        args.putParcelableArrayList(ProjectUtils.MEDIA_DATA,mediaFileList);
        fragment.setArguments(args);
        return fragment;
    }

    @Subscribe
    public void onRemoved(ReviewStateTrigger stateTrigger) {
        if(adapter!=null) {
            adapter.notifyAbout(stateTrigger.mediaFile);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mediaSetCreatedListener=(OnMediaSetCreatedListener)(context);
    }

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
        if(root!=null) {
            TextView textView=(TextView)(root.findViewById(R.id.selectedImageNumber));
            RecyclerView mediaRecyclerView=(RecyclerView)(root.findViewById(R.id.mediaRecyclerView));
            mediaRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),4, GridLayoutManager.VERTICAL,false));
            mediaRecyclerView.setAdapter(adapter=new MediaAdapter(getContext(),(ArrayList<MediaFile>)mediaFileList,textView));
            mediaRecyclerView.setItemAnimator(new DefaultItemAnimator());
            final ImageButton mCancelButton=(ImageButton)(root.findViewById(R.id.cancel));
            final ImageButton mProceedButton=(ImageButton)(root.findViewById(R.id.proceed));
            mCancelButton.setOnClickListener(this);
            mProceedButton.setOnClickListener(this);

        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.cancel:
                adapter.clear();
                break;
            case R.id.proceed:

                ArrayList<MediaFile> mediaFileList=adapter.getSelectedMediaFiles();
                //TODO quit out of this option, just return to main layout
                if(mediaFileList==null) {
                    Toast.makeText(getContext(), "Nothing has been selected", Toast.LENGTH_LONG).show();
                    return;
                }
                mediaSetCreatedListener.onMediaSetCreated(mediaFileList);
                break;

        }
    }

    public static class MediaAdapter
        extends RecyclerView.Adapter<MediaAdapter.AbstractMediaItem> {

        private List<MediaFile> mediaFileList;
        private LayoutInflater inflater;
        private TextView mItemSelectedCount;
        private int selectedItemCount;
        private final Glide glideInstance;
        private int[] checked;


        private static final int NORMAL=0;
        private static final int ANIMATED=1;
        private static final int ENTER=2;
        private static final int EXIT=3;

        private static final float SCALE_F=0.8f;

        public MediaAdapter(Context context, ArrayList<MediaFile> mDataModel, TextView textView) {
            this.inflater=LayoutInflater.from(context);
            this.mItemSelectedCount=textView;
            this.mediaFileList=mDataModel;
            this.checked=new int[mDataModel.size()];
            this.glideInstance=Glide.get(context);
            glideInstance.setMemoryCategory(MemoryCategory.HIGH);
        }


        public abstract class AbstractMediaItem extends RecyclerView.ViewHolder {

            private DynamicImageView image;

            public AbstractMediaItem(View itemView) {
                super(itemView);
                this.image=(DynamicImageView) (itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int index=getAdapterPosition();
                        Log.d(TAG,Integer.toString(checked[index]));
                        if(checked[index]==NORMAL||checked[index]==EXIT) {
                            checked[index] = ENTER;
                            selectedItemCount++;
                        }else {
                            checked[index] = EXIT;
                            selectedItemCount--;
                        }
                        mItemSelectedCount.setText(String.format("%s %s",
                            Integer.toString(selectedItemCount)," selected"));
                        setBackground(index);
                    }
                });
            }

            public void setBackground(int position) {

                switch (checked[position]) {
                    case EXIT:
                        if (itemView.getScaleY() < 1.f) {
                            itemView.animate().setDuration(180)
                                .scaleY(1.f).scaleX(1.f)
                                .start();
                            checked[position]=NORMAL;
                        }
                    case NORMAL:
                        if(itemView.getScaleX()<1.f) {
                            itemView.setScaleX(1.f);
                            itemView.setScaleY(1.f);
                        }
                        break;
                    case ENTER:
                        itemView.animate().setDuration(180)
                                .scaleX(SCALE_F)
                                .scaleY(SCALE_F)
                                .start();
                        checked[position] = ANIMATED;
                        break;
                    case ANIMATED:
                        itemView.setScaleX(SCALE_F);
                        itemView.setScaleY(SCALE_F);
                        break;

                }
            }

            public void onBindData(int position) {
                setBackground(position);
                Glide.with(itemView.getContext())
                        .load(mediaFileList.get(position).mediaFile())
                        .asBitmap()
                        .centerCrop()
                        .into(image);
            }

        }

        @Override
        public int getItemCount() {
            return mediaFileList.size();
        }



        @Override
        public AbstractMediaItem onCreateViewHolder(ViewGroup parentGroup, int viewType) {
            View root=inflater.inflate(R.layout.gallery_item,parentGroup,false);
            return new AbstractMediaItem(root) {
                @Override
                public void onBindData(int position) {
                    super.onBindData(position);
                }
            };
        }

        public void notifyAbout(MediaFile mediaFile) {
            boolean changed=false;
            if(mediaFile!=null) {
                for(int index=0;index<mediaFileList.size();index++) {
                    if (mediaFileList.get(index) == mediaFile) {
                        checked[index]=NORMAL;
                        notifyItemChanged(index);
                        changed=true;
                        Log.d(TAG,Integer.toString(index));
                        break;
                    }
                }

                if(!changed) {
                    for (int index = 0; index < mediaFileList.size(); index++) {
                        if (mediaFileList.get(index).equals(mediaFile)) {
                            checked[index]=NORMAL;
                            notifyItemChanged(index);
                        }
                    }
                }
            }
        }

        @Override
        public void onBindViewHolder(AbstractMediaItem holder, int position) {
            holder.onBindData(position);
        }

        public void clear() {
            for(int index=0;index<checked.length;index++) {
                if(checked[index]==ANIMATED) {
                    checked[index] = EXIT;
                }else {
                    checked[index] = NORMAL;
                }
            }
            selectedItemCount=0;
            notifyDataSetChanged();
        }

        public ArrayList<MediaFile> getSelectedMediaFiles() {
            List<MediaFile> tempList=new LinkedList<>();
            for(int index=0;index<checked.length;index++) {
                if (checked[index]==ANIMATED||checked[index]==ENTER)
                    tempList.add(mediaFileList.get(index));
            }
            return new ArrayList<>(tempList);
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

    public interface OnMediaSetCreatedListener {
        void onMediaSetCreated(ArrayList<MediaFile> mediaMap);
    }

}
