package com.vpaliy.studioq.slider.tranformations;


import android.support.annotation.AnyThread;
import android.support.v4.view.ViewPager;
import android.view.View;

public abstract class AbstractTransformation
     implements ViewPager.PageTransformer{


    private volatile boolean lockLeft;
    private volatile boolean lockRight;

    static final float MIN_SCALE=0.85f;
    static final float MIN_ALPHA=0.5f;

    AbstractTransformation() {
        lockLeft=lockRight=false;
    }

    @Override
    public void transformPage(View page, float position) {
        if(position<0) {
            if(!lockLeft) {
                transformThePage(page,position);
            }
        }else if(position>0) {
            if(!lockRight) {
                transformThePage(page,position);
            }
        }else {
            unLock();
            transformThePage(page,position);
        }

    }

    public abstract void transformThePage(View thePage, float position);

    @AnyThread
    public void lockLeft() {
        lockLeft=true;
    }

    @AnyThread
    public void lockRight() {
        lockRight=true;
    }

    @AnyThread
    public void unLock() {
        lockLeft=lockRight=false;
    }

}
