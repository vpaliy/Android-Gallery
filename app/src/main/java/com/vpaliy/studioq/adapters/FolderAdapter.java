package com.vpaliy.studioq.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.DrawableCrossFadeFactory;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.activities.utils.eventBus.EventBusProvider;
import com.vpaliy.studioq.activities.utils.eventBus.Launcher;
import com.vpaliy.studioq.adapters.multipleChoice.BaseAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.MultiMode;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.model.MediaFolder;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FolderAdapter extends BaseAdapter {

    private final String KEY="adapter:mode";

    private final static float SCALE_F=0.85f;
    private LayoutInflater inflater;
    private List<MediaFolder> mediaFolderList;

    private List<MediaFolder> currentFolderList;
    private Mode adapterMode=Mode.ALL;


    public FolderAdapter(Context context, MultiMode mode, List<MediaFolder> mediaFolderList) {
        super(mode,true);
        this.inflater=LayoutInflater.from(context);
        this.mediaFolderList=mediaFolderList;
        this.currentFolderList=mediaFolderList;
    }

    public FolderAdapter(Context context, @NonNull  MultiMode mode,
            List<MediaFolder> mediaFolderList, @NonNull Bundle state) {
        super(mode,true,state);
        this.mediaFolderList=mediaFolderList;
        this.currentFolderList=mediaFolderList;
        this.inflater=LayoutInflater.from(context);
        adapterMode=Mode.valueOf(state.getString(KEY,Mode.ALL.name()));
        initCurrentList();
    }

    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup parentGroup, int viewType) {
        View root=inflater.inflate(R.layout.media_folder_adapter_item,parentGroup,false);
        return new FolderViewHolder(root);
    }


    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBindData();
    }

    @Override
    public int getItemCount() {
        return currentFolderList.size();
    }

    public class FolderViewHolder extends BaseAdapter.BaseViewHolder {

        @BindView(R.id.icon) ImageView icon;
        @BindView(R.id.mainImage) ImageView mMainImage;
        @BindView(R.id.folderName) TextView mFolderName;
        @BindView(R.id.imageCount) TextView mImageCount;
        @BindView(R.id.cardBody) RelativeLayout bodyLayout;

        public FolderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
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



        @Override
        public void onClick(View view) {
            if(!isMultiModeActivated()) {
                MediaFolder resultFolder=currentFolderList.get(getAdapterPosition());
                if(adapterMode==Mode.IMAGE) {
                    resultFolder = resultFolder.createImageSubfolder();
                }else if(adapterMode==Mode.VIDEO) {
                    resultFolder = resultFolder.createVideoSubfolder();
                }
                EventBusProvider.defaultBus().post(new Launcher<>(resultFolder,view));
            }
            super.onClick(view);
        }


        @Override
        public void updateBackground() {

        }

        @Override
        public void onBindData() {
            int position=getAdapterPosition();
            MediaFile mediaFile=loaderCover(position);
            Glide.with(itemView.getContext())
                    .load(mediaFile.mediaFile())
                    .listener(new RequestListener<File, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, File model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, File model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            if (isFirstResource) {
                                return new DrawableCrossFadeFactory<>()
                                        .build(false, false)
                                        .animate(resource, (GlideAnimation.ViewAdapter) target);
                            }
                            return false;
                        }
                    })
                    .centerCrop()
                    .priority(Priority.HIGH)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .thumbnail(0.5f)
                    .placeholder(R.drawable.placeholder)
                    .animate(R.anim.fade_in)
                    .into(mMainImage);

            icon.setVisibility(mediaFile.getType()== MediaFile.Type.VIDEO?View.VISIBLE:View.INVISIBLE);

            mFolderName.setText(currentFolderList.get(position).getFolderName());
            mImageCount.setText(String.format(Locale.US,"%d",currentFolderList.get(position).getFileCount()));

            determineState();
        }

    }



    public void setAdapterMode(Mode mode) {
        if(mode!=adapterMode) {
            this.adapterMode=mode;
            initCurrentList();
            notifyDataSetChanged();
        }
    }

    private MediaFile loaderCover(int position) {
        if(adapterMode==Mode.ALL) {
            return currentFolderList.get(position).getCoverForAll();
        }else if(adapterMode==Mode.IMAGE) {
            return currentFolderList.get(position).getCoverForImage();
        }
        return currentFolderList.get(position).getCoverForVideo();
    }


    private void initCurrentList() {
        if(adapterMode==Mode.ALL) {
            this.currentFolderList=mediaFolderList;
        }else if(adapterMode==Mode.IMAGE) {
            ArrayList<MediaFolder> imageFolderList=new ArrayList<>();
            for(MediaFolder folder:mediaFolderList) {
                if(folder.getCoverForImage()!=null) {
                    imageFolderList.add(folder);
                }
            }
            currentFolderList=imageFolderList;
        }else {
            ArrayList<MediaFolder> videoFolderList=new ArrayList<>();
            for(MediaFolder folder:mediaFolderList) {
                if(folder.getCoverForVideo()!=null) {
                    videoFolderList.add(folder);
                }
            }
            currentFolderList=videoFolderList;
        }
    }

    public List<MediaFolder> geMediaFolderList() {
        return mediaFolderList;
    }

    public ArrayList<MediaFolder> getAllChecked() {
        int[] checked=super.getAllChecked(false);
        if(checked!=null) {
            ArrayList<MediaFolder> resultList = new ArrayList<>(checked.length);
            for (int index : checked) {
                resultList.add(mediaFolderList.get(index));
            }
            return resultList;
        }
        return null;
    }

    public void removeAt(int index) {
        super.removeAt(index,false);
        MediaFolder folder=currentFolderList.get(index);
        currentFolderList.remove(index);
        if(currentFolderList!=mediaFolderList) {
            mediaFolderList.remove(folder);
        }
        notifyItemRemoved(index);
    }

    public void setData(List<MediaFolder> folderList) {
        if(folderList!=null) {
            currentFolderList=folderList;
            if(adapterMode!=Mode.ALL) {
                mediaFolderList.addAll(folderList);
            }else {
                mediaFolderList = folderList;
            }
            notifyDataSetChanged();
        }
    }

    public  List<MediaFolder> getData() {
        return currentFolderList;
    }

    public Mode getAdapterMode() {
        return adapterMode;
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putString(KEY,adapterMode.name());
    }

    public enum Mode {

        ALL (1),

        VIDEO(2),

        IMAGE (0);

        Mode(int ni){
            nativeInt = ni;
        }

        final int nativeInt;

    }

}
