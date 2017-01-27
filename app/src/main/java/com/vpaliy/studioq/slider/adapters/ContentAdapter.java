package com.vpaliy.studioq.slider.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.slider.listeners.OnSliderEventListener;
import com.vpaliy.studioq.slider.screens.PlayerActivity;
import com.vpaliy.studioq.utils.ProjectUtils;
import java.util.List;

public class ContentAdapter extends PagerAdapter {

    private static final String TAG=ContentAdapter.class.getSimpleName();

    private LayoutInflater inflater;
    private Bitmap currentBitmap=null;
    private List<MediaFile> mediaFileList;
    private int startPosition;
    // private LoaderCallback.Callback callback;
    private OnSliderEventListener sliderEventListener;
    private volatile boolean hasAnimated=false;

    /*public ContentAdapter(@NonNull List<MediaFile> mediaFileList, @NonNull LoaderCallback.Callback callback,
                    int startPosition) {
        this.mediaFileList=mediaFileList;
        this.callback=callback;
        this.startPosition=startPosition;
    }*/

    public ContentAdapter(Context context,@NonNull List<MediaFile> mediaFileList, int startPosition,
                          @NonNull OnSliderEventListener listener) {
        this.inflater=LayoutInflater.from(context);
        this.mediaFileList=mediaFileList;
        this.startPosition=startPosition;
        this.sliderEventListener=listener;

    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public View instantiateItem(ViewGroup container, final int position) {
        MediaFile.Type fileType=mediaFileList.get(position).getType();
        if(fileType== MediaFile.Type.VIDEO) {
            return createVideo(position, container);
        }else if(fileType== MediaFile.Type.GIF) {
            return createGif(position, container);
        }
        final SubsamplingScaleImageView image=new SubsamplingScaleImageView(container.getContext());
        container.addView(image);
        image.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
        image.setDoubleTapZoomScale(3.f);
        image.setMaxScale(5.f);
        if(fileType== MediaFile.Type.IMAGE) {
            Glide.with(image.getContext())
                    .load(mediaFileList.get(position).mediaFile()).
                    asBitmap().fitCenter().diskCacheStrategy(DiskCacheStrategy.RESULT).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    image.setImage(ImageSource.bitmap(resource));
                    currentBitmap=resource;
                }
            });
        }

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sliderEventListener.onClick(position);
            }
        });


        return image;
    }

    private View createGif(final int position, final ViewGroup container) {
        ImageView gifImage=new ImageView(container.getContext());
        container.addView(gifImage,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Glide.with(gifImage.getContext())
            .load(mediaFileList.get(position).mediaFile())
            .asGif()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .fitCenter()
            .into(gifImage);

        return gifImage;
    }

    private View createVideo(final int position, final ViewGroup container) {
        View root=inflater.inflate(R.layout.video_item,container,false);
        container.addView(root);
        ImageView videoFrame=(ImageView)(root.findViewById(R.id.videoItem));
        ImageView icon=(ImageView)(root.findViewById(R.id.icon));

        Log.d(TAG,Integer.toString(position));
        Glide.with(container.getContext())
                .load(mediaFileList.get(position).mediaFile())
                .asBitmap()
                .fitCenter().into(videoFrame);

        videoFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sliderEventListener.onClick(position);
            }
        });
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = container.getContext();
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra(ProjectUtils.MEDIA_DATA, mediaFileList.get(position).mediaFile().getAbsolutePath());
                context.startActivity(intent);
            }
        });
        return root;
    }

    private boolean checkForTransition(int position) {
        if(position==startPosition) {
            if (!hasAnimated) {
                return (hasAnimated = true);
            }
        }
        return false;
    }

    public Bitmap getCurrentBitmap() {
        return currentBitmap;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mediaFileList.size();
    }

}
