package com.vpaliy.studioq.adapters;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.ArrayList;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.DrawableCrossFadeFactory;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.activities.utils.eventBus.EventBusProvider;
import com.vpaliy.studioq.activities.utils.eventBus.Launcher;
import com.vpaliy.studioq.adapters.multipleChoice.BaseAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.MultiMode;
import com.vpaliy.studioq.media.MediaFile;


public class GalleryAdapter extends BaseAdapter {

    private static final String TAG=GalleryAdapter.class.getSimpleName();
    private final static float SCALE_F=0.85f;

    private ArrayList<MediaFile> mediaFileList;
    private LayoutInflater inflater;

    public GalleryAdapter(Context context, MultiMode mode, ArrayList<MediaFile> mDataModel) {
        super(mode,true);
        this.inflater=LayoutInflater.from(context);
        this.mediaFileList=mDataModel;
    }

    public GalleryAdapter(Context context, MultiMode mode,
            @NonNull ArrayList<MediaFile> mDataModel,@NonNull Bundle savedInstanceState) {
        super(mode,true,savedInstanceState);
        this.inflater=LayoutInflater.from(context);
        this.mediaFileList=mDataModel;
    }



    public final class GalleryViewHolder extends BaseAdapter.BaseViewHolder {

        private ImageView mImageView;
        private ImageView icon;

        public GalleryViewHolder(View itemView) {
            super(itemView);
            mImageView=(ImageView) (itemView.findViewById(R.id.mainImage));
            icon=(ImageView)(itemView.findViewById(R.id.icon));
        }

        @Override
        public void onClick(View view) {
            if(!isMultiModeActivated()) {
                EventBusProvider.defaultBus().post(new Launcher<>(mediaFileList,null,getAdapterPosition()));
            }
            super.onClick(view);
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
        public void onBindData() {
            final MediaFile file=mediaFileList.get(getAdapterPosition());
            Glide.with(itemView.getContext())
                    .load(file.mediaFile())
                    .listener(new RequestListener<File, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, File model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, File model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            if (isFirstResource) {
                                icon.setVisibility(file.getType()== MediaFile.Type.VIDEO?View.VISIBLE:View.INVISIBLE);
                                return new DrawableCrossFadeFactory<>()
                                        .build(false, false)
                                        .animate(resource, (GlideAnimation.ViewAdapter) target);
                            }
                            return false;
                        }
                    })
                    // .signature(MediaSignature.sign(file))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .thumbnail(0.5f)
                    .placeholder(R.drawable.placeholder)
                    .animate(R.anim.fade_in)
                    .into(mImageView);
            determineState();
        }

        @Override
        public void updateBackground() {

        }
    }

    private static class Target extends GlideDrawableImageViewTarget {

        public Target(ImageView image) {
            super(image);
        }

        @Override
        public void onLoadStarted(Drawable placeholder) {
            if (placeholder != null) {
                super.onLoadStarted(placeholder);
            }
        }

        @Override
        public void onLoadCleared(Drawable placeholder) {
            if (placeholder != null) {
                super.onLoadCleared(placeholder);
            }
        }
    }

  /*  @Override
    public void onActionItemClicked(MenuItem item,int[] mCheckedIndices) {

        ArrayList<MediaFile> changeMediaFileList = new ArrayList<>(mCheckedIndices.length);
        switch (item.getItemId()) {

            case R.id.deleteItem: {
                ArrayList<MediaFile> tempMediaList = new ArrayList<>(mDataModel);
                for (int index : mCheckedIndices) {
                    changeMediaFileList.add(mDataModel.get(index));
                    mDataModel.remove(index);
                    notifyItemRemoved(index);
                }

                mediaFileControlListener.onDeleteMediaFile(this, tempMediaList, changeMediaFileList);
                break;
            }

            case R.id.moveItem:
                ArrayList<MediaFile> tempMediaList = new ArrayList<>(mDataModel);
                for (int index : mCheckedIndices) {
                    changeMediaFileList.add(mDataModel.get(index));
                    mDataModel.remove(index);
                    notifyItemRemoved(index);
                }

                mediaFileControlListener.onCopyTo(this,tempMediaList,changeMediaFileList,true);
                break;

            case R.id.copyItem:
                for (int index : mCheckedIndices) {
                    changeMediaFileList.add(mDataModel.get(index));
                }
                notifyDataSetChanged();
                mediaFileControlListener.onCopyTo(this,null,changeMediaFileList,false);
                break;

            case R.id.shareItem:
                Intent shareIntent=new Intent();
                shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                ArrayList<Uri> sharedData=new ArrayList<>(mCheckedIndices.length);
                for(int index:mCheckedIndices) {
                    sharedData.add(Uri.fromFile(mDataModel.get(index).mediaFile()));
                }
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,sharedData);
                shareIntent.setType("image/*");
                context.startActivity(Intent.createChooser(shareIntent,"Share via"));

        }

    }*/

    public void setMediaFileList(ArrayList<MediaFile> mediaFileList) {
        this.mediaFileList=mediaFileList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBindData();
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parentGroup, int viewType) {
        View root=inflater.inflate(R.layout.item,parentGroup,false);
        return new GalleryViewHolder(root);
    }

    public void removeAt(int index) {
        super.removeAt(index,true);
        mediaFileList.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mediaFileList.size();
    }


}
