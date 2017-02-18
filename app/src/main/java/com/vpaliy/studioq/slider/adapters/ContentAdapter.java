package com.vpaliy.studioq.slider.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.slider.listeners.OnSliderEventListener;
import com.vpaliy.studioq.slider.screens.PlayerActivity;
import com.vpaliy.studioq.slider.utils.RecyclingPagerAdapter;
import com.vpaliy.studioq.slider.utils.SliderImageView;
import com.vpaliy.studioq.slider.utils.SliderOnDoubleTapListener;
import com.vpaliy.studioq.common.utils.ProjectUtils;
import com.vpaliy.studioq.views.MediaView;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoViewAttacher;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ContentAdapter extends RecyclingPagerAdapter
        implements ChangeableAdapter<ArrayList<MediaFile>> {

    private static final String TAG=ContentAdapter.class.getSimpleName();

    private static final int TYPE_VIDEO=0;
    private static final int TYPE_IMAGE=1;
    private static final int TYPE_GIF=2;

    private LayoutInflater inflater;
    private Bitmap currentBitmap=null;
    private ArrayList<MediaFile> mediaFileList;
    private OnSliderEventListener sliderEventListener;


    public ContentAdapter(Context context, @NonNull ArrayList<MediaFile> mediaFileList,
                          @NonNull OnSliderEventListener listener) {
        this.inflater=LayoutInflater.from(context);
        this.mediaFileList=mediaFileList;
        this.sliderEventListener=listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup container) {
        switch (getItemViewType(position)) {
            case TYPE_VIDEO:
                return createVideo(convertView,position);
            case TYPE_GIF:
                return createGif(convertView,position);
            default:
                return createImage(convertView,position);
        }
    }

    private View createGif(@Nullable View view, int position) {
        ImageView gifImage;
        if(view==null||!(view instanceof ImageView)) {
            gifImage=new ImageView(inflater.getContext());
            gifImage.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }else {
            gifImage=ImageView.class.cast(view);
        }
        Glide.with(gifImage.getContext())
                .load(mediaFileList.get(position).mediaFile())
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .fitCenter()
                .into(gifImage);
        return gifImage;
    }

    private View createVideo(@Nullable View view, final int position) {
        final MediaView videoView;
        if(view==null||!(view instanceof MediaView)) {
            videoView=new MediaView(inflater.getContext());
            videoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            videoView.setDescriptionGravity(Gravity.CENTER);
            videoView.setDescriptionIcon(R.drawable.ic_play_circle_outline_white_48dp);

            videoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    GestureDetector gestureDetector=new GestureDetector(inflater.getContext(),
                            new GestureDetector.SimpleOnGestureListener() {

                                @Override
                                public boolean onDown(MotionEvent e) {
                                    return true;
                                }
                                @Override
                                public boolean onSingleTapConfirmed(MotionEvent e) {
                                    sliderEventListener.onClick(position);
                                    return true;
                                }

                                @Override
                                public boolean onDoubleTapEvent(MotionEvent e) {
                                    Context context = inflater.getContext();
                                    Intent intent = new Intent(context, PlayerActivity.class);
                                    intent.putExtra(ProjectUtils.MEDIA_DATA, mediaFileList.get(position).mediaFile().getAbsolutePath());
                                    context.startActivity(intent);
                                    return true;
                                }
                            });
                    return gestureDetector.onTouchEvent(event);
                }
            });

            videoView.setOnIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = inflater.getContext();
                    Intent intent = new Intent(context, PlayerActivity.class);
                    intent.putExtra(ProjectUtils.MEDIA_DATA, mediaFileList.get(position).mediaFile().getAbsolutePath());
                    context.startActivity(intent);
                }
            });
        }else {
            videoView = MediaView.class.cast(view);
        }

        Glide.with(inflater.getContext())
                .load(mediaFileList.get(position).mediaFile())
                .asBitmap()
                .centerCrop()
                .into(new ImageViewTarget<Bitmap>(videoView.getMainContent()) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        videoView.setMainContent(resource);
                    }
                });


        return videoView;
    }

    private View createImage(@Nullable View view, final int position) {
        final SliderImageView image;
        if(view==null||!(view instanceof SliderImageView)) {
            image = new SliderImageView(inflater.getContext());
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setAdjustViewBounds(true);
            image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }else {
            image = SliderImageView.class.cast(view);
        }

        image.post(new Runnable() {
            @Override
            public void run() {
                image.setMaximumScale(7.f);
                image.setMediumScale(3.f);
                PhotoViewAttacher attacher=PhotoViewAttacher.class.cast(image.getIPhotoViewImplementation());
                image.setOnDoubleTapListener(new SliderOnDoubleTapListener(attacher) {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        sliderEventListener.onClick(position);
                        return super.onSingleTapConfirmed(e);
                    }
                });
            }
        });

        Glide.with(inflater.getContext())
                .load(mediaFileList.get(position).mediaFile())
                .asBitmap()
                .fitCenter()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(new ImageViewTarget<Bitmap>(image) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        image.setImageBitmap(resource);
                    }
                });
        return image;
    }

    public Bitmap getCurrentBitmap() {
        return currentBitmap;
    }

    @Override
    public void setData(ArrayList<MediaFile> data) {
        this.mediaFileList=data;
        notifyDataSetChanged();
    }

    public ArrayList<MediaFile> getData() {
        return mediaFileList;
    }

    public MediaFile dataAt(int index) {
        return mediaFileList.get(index);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        //it may happen sometimes
        if(position>=mediaFileList.size())
            position=mediaFileList.size()-1;
        MediaFile mediaFile=mediaFileList.get(position);
        if(mediaFile.getType()== MediaFile.Type.VIDEO)
            return TYPE_VIDEO;
        else if(mediaFile.getType()== MediaFile.Type.GIF)
            return TYPE_GIF;
        return TYPE_IMAGE;
    }

    @Override
    public int getCount() {
        return mediaFileList.size();
    }
}