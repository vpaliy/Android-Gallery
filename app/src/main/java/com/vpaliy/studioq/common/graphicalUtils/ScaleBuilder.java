package com.vpaliy.studioq.common.graphicalUtils;

import android.animation.Animator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/** Just a util class for an animation **/

public class ScaleBuilder {

    View target;

    @Nullable
    Interpolator interpolator;

    @Nullable
    Animator.AnimatorListener listener;

    int duration=180;
    int startDelay=0;

    float scaleX;
    float scaleY;

    boolean hardwareAcceleration;
    private boolean applyCondition;


    private ScaleBuilder(@NonNull View target) {
        this.target=target;
    }

    private ScaleBuilder(@NonNull View target, float scaleXY) {
        this(target);
        this.scaleX=scaleXY;
        this.scaleY=scaleXY;
    }

    private ScaleBuilder(@NonNull View target, float scaleX, float scaleY) {
        this(target);
        this.scaleX=scaleX;
        this.scaleY=scaleY;
    }

    public ScaleBuilder interpolator(@NonNull Interpolator interpolator) {
        this.interpolator=interpolator;
        return this;
    }

    public ScaleBuilder duration(int duration) {
        this.duration=duration;
        return this;
    }

    public ScaleBuilder scaleX(float scaleX) {
        this.scaleX=scaleX;
        return this;
    }

    public ScaleBuilder scaleY(float scaleY) {
        this.scaleY=scaleY;
        return this;
    }

    public ScaleBuilder scaleXY(@FloatRange(from=0f,to=1f) float scaleXY) {
        scaleX(scaleXY);
        return scaleY(scaleXY);
    }

    public ScaleBuilder listener(@Nullable Animator.AnimatorListener listener) {
        this.listener=listener;
        return this;
    }

    public ScaleBuilder delay(int startDelay) {
        if(startDelay>=0) {
            this.startDelay = startDelay;
        }
        return this;
    }

    public ScaleBuilder applyCondition() {
        this.applyCondition=true;
        return this;
    }

    private boolean checkCondition() {
        if(target==null)
            return false;
        if(target.getVisibility()!=View.VISIBLE) {
            target.setVisibility(View.VISIBLE);
        }
        return (!applyCondition||(target.getScaleX()<scaleX && target.getScaleY()<scaleY));
    }

    public ScaleBuilder accelerate() {
        this.hardwareAcceleration=true;
        return this;
    }

    public static ScaleBuilder start(@NonNull View target, float scaleXY) {
        return new ScaleBuilder(target,scaleXY);
    }

    public static ScaleBuilder start(@NonNull View target, float scaleX, float scaleY) {
        return new ScaleBuilder(target,scaleX,scaleY);
    }

    public void execute() {
        if(interpolator==null) {
            interpolator=new DecelerateInterpolator();
        }

        if(checkCondition()) {
            AnimationUtils.scaleTargetTo(this);
        }
    }

}