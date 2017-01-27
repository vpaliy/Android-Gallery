package com.vpaliy.studioq.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.DrawableCrossFadeFactory;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.adapters.multipleChoice.BaseAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.MultiMode;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.model.MediaFolder;
import com.vpaliy.studioq.utils.OnLaunchGalleryActivity;

public class MediaFolderAdapter extends BaseAdapter {

    private final static String TAG=MediaFolderAdapter.class.getSimpleName();
    private static final float SCALE_F=0.85F;

    private ArrayList<MediaFolder> mediaFileList;
    private OnLaunchGalleryActivity mOnLaunchGallery;
    private ArrayList<MediaFolder> currentFolderList;
    private Mode adapterMode=Mode.ALL;
    private LayoutInflater inflater;
    private  final Bitmap paletteBitmap;
    private List<Palette.Swatch> swatchList;


    public MediaFolderAdapter(Context context, MultiMode mode, ArrayList<MediaFolder> mDataModel) {
        super(mode,true);
        this.mediaFileList=mDataModel;
        this.inflater=LayoutInflater.from(context);
        this.mOnLaunchGallery = (OnLaunchGalleryActivity) (context);
        this.currentFolderList=mDataModel;
        this.paletteBitmap= BitmapFactory.decodeResource(context.getResources(), R.drawable.bitmap);
        //initSwatchList();
    }

    public MediaFolderAdapter(Context context, MultiMode mode, ArrayList<MediaFolder> mDataModel, @NonNull Bundle state) {
        super(mode,true,state);
        this.mediaFileList=mDataModel;
        this.inflater=LayoutInflater.from(context);
        this.mOnLaunchGallery = (OnLaunchGalleryActivity) (context);
        this.currentFolderList=mDataModel;
        this.paletteBitmap= BitmapFactory.decodeResource(context.getResources(), R.drawable.bitmap);
        //initSwatchList();
    }



    private void initSwatchList() {
        if(paletteBitmap!=null) {
            if (!paletteBitmap.isRecycled()) {
                Palette.from(paletteBitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        swatchList=new ArrayList<>(palette.getSwatches());
                    }
                });
            }
        }
    }


    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBindData();
    }

    public class FolderViewHolder extends BaseAdapter.BaseViewHolder {

        private ImageView icon;
        private ImageView mMainImage;
        private TextView mFolderName;
        private TextView mImageCount;
        private RelativeLayout bodyLayout;

        public FolderViewHolder(View itemView) {
            super(itemView);
            this.icon=(ImageView)(itemView.findViewById(R.id.icon));
            this.mMainImage=(ImageView)(itemView.findViewById(R.id.mainImage));
            this.mFolderName=(TextView) (itemView.findViewById(R.id.folderName));
            this.mImageCount=(TextView)(itemView.findViewById(R.id.imageCount));
            this.bodyLayout=(RelativeLayout)(itemView.findViewById(R.id.cardBody));
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


        private void applySwatch() {
            Palette.Swatch currentSwatch=swatchList.get(getAdapterPosition() % 16);
            if(currentSwatch!=null) {
                bodyLayout.setBackgroundColor(currentSwatch.getRgb());
                mFolderName.setTextColor(currentSwatch.getTitleTextColor());
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
                mOnLaunchGallery.onLaunchGalleryActivity(currentFolderList,resultFolder,itemView);
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
            if(swatchList!=null) {
                applySwatch();
            }
            determineState();
        }

    }


    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup parentGroup, int viewType) {
        View root=inflater.inflate(R.layout.media_folder_adapter_item,parentGroup,false);
        return new FolderViewHolder(root);
    }


    public void setAdapterMode(Mode mode) {
        if(mode!=adapterMode) {
            this.adapterMode=mode;
            initCurrentList();
            notifyDataSetChanged();
        }
    }

    private void initCurrentList() {
        if(adapterMode==Mode.ALL) {
            this.currentFolderList=mediaFileList;
        }else if(adapterMode==Mode.IMAGE) {
            ArrayList<MediaFolder> imageFolderList=new ArrayList<>();
            for(MediaFolder folder:mediaFileList) {
                if(folder.getCoverForImage()!=null) {
                    imageFolderList.add(folder);
                }
            }
            currentFolderList=imageFolderList;
        }else {
            ArrayList<MediaFolder> videoFolderList=new ArrayList<>();
            for(MediaFolder folder:mediaFileList) {
                if(folder.getCoverForVideo()!=null) {
                    videoFolderList.add(folder);
                }
            }
            currentFolderList=videoFolderList;
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


    public ArrayList<MediaFolder> getMediaList() {
        return mediaFileList;
    }

    public void removeAt(int index) {
        mediaFileList.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return currentFolderList.size();
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
