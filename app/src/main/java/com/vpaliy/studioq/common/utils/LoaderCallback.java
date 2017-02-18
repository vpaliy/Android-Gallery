package com.vpaliy.studioq.common.utils;

import android.widget.ImageView;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public  class LoaderCallback<T,M>
        implements RequestListener<T,M> {

    private ImageView sharedImage;
    private Callback callback;

    public LoaderCallback(ImageView sharedImage, Callback callback) {
        this.sharedImage=sharedImage;
        this.callback=callback;
    }

    @Override
    public boolean onException(Exception e, T model, Target<M> target, boolean isFirstResource) {
        return false;
    }

    @Override
    public boolean onResourceReady(M resource, T model, Target<M> target, boolean isFromMemoryCache, boolean isFirstResource) {
        if(isFirstResource) {
            callback.onReady(sharedImage);
        }
        return false;
    }

    public interface Callback {
        void onReady(ImageView image);
    }
}