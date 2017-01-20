package com.vpaliy.studioq.adapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.MenuItem;
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
import com.vpaliy.studioq.media.MediaFile;
import com.vpaliy.studioq.MultiChoiceMode.MultiChoiceMode;
import com.vpaliy.studioq.slider.listeners.OnLaunchMediaSlider;


public class GalleryAdapter
        extends  BaseMediaAdapter<MediaFile> {

    private static final String TAG=GalleryAdapter.class.getSimpleName();

    private OnLaunchMediaSlider mediaSliderListener;
    private MediaFileControlListener mediaFileControlListener;
    private Context context;

    public GalleryAdapter(Context context, MultiChoiceMode multiChoiceModeListener, ArrayList<MediaFile> mDataModel) {
        super(context, multiChoiceModeListener,mDataModel);
        this.context=context;
        this.mediaSliderListener=(OnLaunchMediaSlider)(context);
        this.mediaFileControlListener=(MediaFileControlListener)(context);
    }



    public final class GalleryViewHolder extends SelectableViewHolder {

        private ImageView mImageView;
        private ImageView icon;

        public GalleryViewHolder(View itemView) {
            super(itemView);
            mImageView=(ImageView) (itemView.findViewById(R.id.mainImage));
            icon=(ImageView)(itemView.findViewById(R.id.icon));
        }


        public void onClick(View view) {
            if(!isActivatedMultipleChoiceMode()) {
                mediaSliderListener.onLaunchMediaSlider(getAdapterPosition(), mImageView);
            }
            super.onClick(view);
        }


        @Override
        protected void enterState() {
            itemView.animate()
                .scaleX(SCALE_ITEM)
                .scaleY(SCALE_ITEM)
                .setDuration(180).start();
        }

        @Override
        protected void exitState() {
            if (itemView.getScaleY() < 1.f) {
                itemView.animate().setDuration(180)
                        .scaleY(1.f).scaleX(1.f)
                        .start();
            }
        }

        @Override
        protected void animatedState() {
            itemView.setScaleX(SCALE_ITEM);
            itemView.setScaleY(SCALE_ITEM);
        }

        @Override
        protected void normalState() {
            if(itemView.getScaleX()<1f) {
                itemView.setScaleX(1.f);
                itemView.setScaleY(1.f);
            }
        }

        @Override
        public void onBindData(int position) {
            final MediaFile file=mDataModel.get(position);
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

            mImageView.clearColorFilter();


            //mImageView.setTag(ProjectUtils.TRANSITION_NAME(position));
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

    @Override
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

    }

    public void setMediaFileList(ArrayList<MediaFile> mediaFileList) {
        this.mDataModel=mediaFileList;
        notifyDataSetChanged();
    }



    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parentGroup, int viewType) {
        View root=mInflater.inflate(R.layout.item,parentGroup,false);
        return new GalleryViewHolder(root);
    }


    public interface MediaFileControlListener {
        void onDeleteMediaFile(GalleryAdapter adapter, ArrayList<MediaFile> fullMediaFileList, ArrayList<MediaFile> deleteMediaFileList);
        void onCopyTo(GalleryAdapter adapter, ArrayList<MediaFile> fullMediaFileList, ArrayList<MediaFile> copyMediaFileList, boolean moveTo);
    }

}
