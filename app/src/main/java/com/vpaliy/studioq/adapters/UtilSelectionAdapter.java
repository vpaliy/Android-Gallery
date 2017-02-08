package com.vpaliy.studioq.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.adapters.multipleChoice.BaseAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.MultiMode;
import com.vpaliy.studioq.model.MediaFile;
import java.util.ArrayList;
import java.util.List;
import butterknife.ButterKnife;

import android.support.annotation.NonNull;
import butterknife.BindView;

public class UtilSelectionAdapter extends BaseAdapter {

    private List<MediaFile> mediaFileList;
    private LayoutInflater inflater;

    public UtilSelectionAdapter(@NonNull Context context, List<MediaFile> mediaFileList, MultiMode mode) {
        super(mode,true);
        this.inflater=LayoutInflater.from(context);
        this.mediaFileList=mediaFileList;
    }

    public UtilSelectionAdapter(@NonNull Context context, List<MediaFile> mediaFileList, MultiMode mode, @NonNull Bundle state) {
        super(mode,true,state);
        this.inflater=LayoutInflater.from(context);
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

        @BindView(R.id.galleryItem)
        ImageView thumbImage;

         MediaHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void updateBackground() {}

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
