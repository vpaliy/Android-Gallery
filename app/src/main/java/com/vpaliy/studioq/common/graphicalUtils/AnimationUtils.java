package com.vpaliy.studioq.common.graphicalUtils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewPropertyAnimator;

import android.support.annotation.NonNull;

import com.vpaliy.studioq.common.utils.Permissions;

public class AnimationUtils {

    public static void scaleTargetTo(@NonNull final ScaleBuilder animationBuilder) {
        if(animationBuilder.hardwareAcceleration) {
            if(!Permissions.checkForVersion(16)) {
                animationBuilder.target.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                Animator.AnimatorListener temp=new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        animationBuilder.target.post(new Runnable() {
                            @Override
                            public void run() {
                                animationBuilder.target.setLayerType(View.LAYER_TYPE_NONE,null);
                            }
                        });
                    }
                };
                if(animationBuilder.listener!=null) {
                    animationBuilder.listener=composition(animationBuilder.listener,temp);
                }else {
                    animationBuilder.listener=temp;
                }
            }
        }
        ViewPropertyAnimator animator=animationBuilder.target.animate();
        if(animationBuilder.hardwareAcceleration) {
            if (Permissions.checkForVersion(16)) {
                animator.withLayer();
            }
        }
        animator.scaleX(animationBuilder.scaleX)
                .scaleY(animationBuilder.scaleY)
                .setDuration(animationBuilder.duration)
                .setListener(animationBuilder.listener)
                .setStartDelay(animationBuilder.startDelay)
                .setInterpolator(animationBuilder.interpolator)
                .start();
    }


    private static Animator.AnimatorListener composition(final Animator.AnimatorListener ... listener) {
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if(listener!=null) {
                    for(Animator.AnimatorListener iListener:listener) {
                        iListener.onAnimationStart(animation);
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(listener!=null) {
                    for(Animator.AnimatorListener iListener:listener) {
                        iListener.onAnimationEnd(animation);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if(listener!=null) {
                    for(Animator.AnimatorListener iListener:listener) {
                        iListener.onAnimationCancel(animation);
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if(listener!=null) {
                    for(Animator.AnimatorListener iListener:listener) {
                        iListener.onAnimationRepeat(animation);
                    }
                }
            }
        };
    }

}
