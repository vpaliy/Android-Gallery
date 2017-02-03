package com.vpaliy.studioq.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.vpaliy.studioq.fragments.GalleryFragment;
import com.vpaliy.studioq.model.DummyFolder;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FolderUtilAdapter extends RecyclerView.Adapter<FolderUtilAdapter.FolderViewHolder> {

    private static final String TAG=FolderUtilAdapter.class.getSimpleName();
    private static final String DATA="util:adapter:data";
    private static final String MOVE="util:adapter:move";

    private ArrayList<DummyFolder> data;
    private LayoutInflater inflater;
    private int[] checked;
    private boolean move;

    public FolderUtilAdapter(Context context, ArrayList<DummyFolder> data, int checked[], boolean move) {
        this.data=data;
        this.checked=checked;
        this.inflater=LayoutInflater.from(context);
        this.move=move;
    }

    public FolderUtilAdapter(Context context, ArrayList<DummyFolder> data, @NonNull Bundle state) {
        this.inflater=LayoutInflater.from(context);
        this.data=data;
        this.move=state.getBoolean(MOVE,false);
        this.checked=state.getIntArray(DATA);
    }


    class FolderViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @BindView(R.id.mainImage) ImageView coverImage;
        @BindView(R.id.folderName) TextView folderName;
        @BindView(R.id.imageCount) TextView mediaSize;

        FolderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position=getAdapterPosition();
            String folderName=data.get(position).name();
            EventBusProvider.defaultBus().post(new GalleryFragment.MoveEvent(folderName,checked,move));
        }


        void onBindData() {
            int position=getAdapterPosition();
            Glide.with(itemView.getContext())
                    .load(data.get(position).cover().mediaFile())
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
                    .into(coverImage);

            folderName.setText(data.get(position).name());
            mediaSize.setText(String.format(Locale.US,"%d",data.get(position).size()));
        }

    }

    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FolderViewHolder(inflater.inflate(R.layout.dummy_item,parent,false));
    }

    public void saveState(Bundle outState) {
        outState.putIntArray(DATA,checked);
        outState.putBoolean(MOVE,move);
    }

    @Override
    public void onBindViewHolder(FolderViewHolder holder, int position) {
        holder.onBindData();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
