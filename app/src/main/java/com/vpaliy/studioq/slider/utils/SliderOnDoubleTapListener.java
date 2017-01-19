package com.vpaliy.studioq.slider.utils;

import android.view.GestureDetector;
import android.view.MotionEvent;

import uk.co.senab.photoview.PhotoViewAttacher;

public class SliderOnDoubleTapListener implements GestureDetector.OnDoubleTapListener {

    private PhotoViewAttacher photoViewAttacher;

    public SliderOnDoubleTapListener(PhotoViewAttacher photoViewAttacher) {
        this.photoViewAttacher=photoViewAttacher;
    }



    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        /*if (this.photoViewAttacher == null)
            return false;

           //TODO take care of this code

        ImageView imageView = photoViewAttacher.getImageView();

        if (null != photoViewAttacher.getOnPhotoTapListener()) {
            final RectF displayRect = photoViewAttacher.getDisplayRect();

            if (null != displayRect) {
                final float x = e.getX(), y = e.getY();

                // Check to see if the user tapped on the photo
                if (displayRect.contains(x, y)) {

                    float xResult = (x - displayRect.left)
                            / displayRect.width();
                    float yResult = (y - displayRect.top)
                            / displayRect.height();

                    photoViewAttacher.getOnPhotoTapListener().onPhotoTap(imageView, xResult, yResult);
                    return true;
                }else{
                    photoViewAttacher.getOnPhotoTapListener().onOutsidePhotoTap();
                }
            }
        }
        if (null != photoViewAttacher.getOnViewTapListener()) {
            photoViewAttacher.getOnViewTapListener().onViewTap(imageView, e.getX(), e.getY());
        }
        */
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent ev) {
        if (photoViewAttacher == null)
            return false;

        try {
            float scale = photoViewAttacher.getScale();
            float x = ev.getX();
            float y = ev.getY();

            if (scale >= photoViewAttacher.getMediumScale()) {
                photoViewAttacher.setScale((photoViewAttacher.getMinimumScale()), x, y, true);
            } else {
                photoViewAttacher.setScale(photoViewAttacher.getMediumScale(), x, y, true);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Can sometimes happen when getX() and getY() is called
        }

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // Wait for the confirmed onDoubleTap() instead
        return false;
    }



}
