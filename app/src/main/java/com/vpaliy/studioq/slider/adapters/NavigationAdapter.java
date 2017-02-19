package com.vpaliy.studioq.slider.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.slider.listeners.OnSliderEventListener;
import com.vpaliy.studioq.views.MediaView;

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

    private int width;
    private int height;

    private OnSliderEventListener sliderEventListener;

    public NavigationAdapter(Context context, @NonNull List<MediaFile> mediaFileList,
                @NonNull OnSliderEventListener listener) {
        this.inflater=LayoutInflater.from(context);
        this.mediaFileList=mediaFileList;
        this.sliderEventListener=listener;

        this.height=(int) (context.getResources().getDimension(R.dimen.navigationHeight)
             * context.getResources().getDisplayMetrics().density);
        this.width=(int) (context.getResources().getDimension(R.dimen.navigationWidth)
             * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return mediaFileList.size();
    }

    @SuppressWarnings("WeakerAccess")
    public class NavigationItem extends RecyclerView.ViewHolder
        implements View.OnClickListener {

        @BindView(R.id.navigationImage)
        MediaView image;

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
            determineDescription(mediaFileList.get(getAdapterPosition()).getType());
            Glide.with(image.getContext())
                .load(mediaFileList.get(getAdapterPosition()).mediaFile())
                .asBitmap().centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .override(width,height)
                .dontAnimate()
                .into(new ImageViewTarget<Bitmap>(image.getMainContent()) {
                @Override
                protected void setResource(Bitmap resource) {
                    image.setMainContent(resource);
                }
            });
        }

        private void determineDescription(MediaFile.Type type) {
            if(type== MediaFile.Type.GIF) {
                image.setDescriptionIcon(R.drawable.ic_gif_white_24dp);
            }else if(type== MediaFile.Type.VIDEO) {
                image.setDescriptionIcon(R.drawable.ic_play_circle_outline_white_48dp);
            }else {
                image.setDescriptionIcon(null);
            }
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
