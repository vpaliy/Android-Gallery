package com.vpaliy.studioq.slider.tranformations;

import android.view.View;

public class ZoomIn extends AbstractTransformation {

    @Override
    public void transformThePage(View view, float position) {
        final int width=view.getWidth();
        final int height=view.getHeight();


        if(position<-1) {
            view.setAlpha(0.f);
        }else if(position<=1) {
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float verticalMargin = height * (1 - scaleFactor) / 2;
            float horizontalMargin = width * (1 - scaleFactor) / 2;
            if (position < 0) {
                view.setTranslationX(horizontalMargin - verticalMargin / 2);
            } else {
                view.setTranslationX(-horizontalMargin + verticalMargin/ 2);
            }

            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

            view.setAlpha(MIN_ALPHA +
                    (scaleFactor - MIN_SCALE) /
                            (1 - MIN_SCALE) * (1 - MIN_ALPHA));

        }else {
            view.setAlpha(0f);
        }
    }

    @Override
    public void backToNormal(View page) {
        page.setScaleX(1.f);
        page.setScaleX(1.f);
        page.setAlpha(1f);
    }
}
