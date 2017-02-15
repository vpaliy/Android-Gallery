package com.vpaliy.studioq.slider.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.slider.listeners.OnSliderEventListener;
import java.util.ArrayList;
import java.util.List;
import butterknife.ButterKnife;

import android.support.annotation.NonNull;
import butterknife.BindView;


public class NavigationAdapter
    extends RecyclerView.Adapter<NavigationAdapter.NavigationItem>
        implements ChangeableAdapter<ArrayList<MediaFile>> {

    private List<MediaFile> mediaFileList;
    private LayoutInflater inflater;

    private OnSliderEventListener sliderEventListener;

    public NavigationAdapter(Context context, @NonNull List<MediaFile> mediaFileList,
                @NonNull OnSliderEventListener listener) {
        this.inflater=LayoutInflater.from(context);
        this.mediaFileList=mediaFileList;
        this.sliderEventListener=listener;
    }

    @Override
    public int getItemCount() {
        return mediaFileList.size();
    }

    public class NavigationItem extends RecyclerView.ViewHolder
        implements View.OnClickListener {

        @BindView(R.id.navigationImage) ImageView image;

        public NavigationItem(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            image.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            sliderEventListener.onClick(getAdapterPosition());
        }

        public void onBindData() {
            Glide.with(image.getContext())
                .load(mediaFileList.get(getAdapterPosition()).mediaFile())
                .asBitmap().centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .thumbnail(0.2f).into(image);
        }

    }

    @Override
    public void setData(ArrayList<MediaFile> data) {
        this.mediaFileList=data;
        notifyDataSetChanged();
    }

    @Override
    public NavigationItem onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NavigationItem(inflater.inflate(R.layout.adapter_navigation_item,parent,false));
    }

    @Override
    public void onBindViewHolder(NavigationItem holder, int position) {
        holder.onBindData();
    }
}
