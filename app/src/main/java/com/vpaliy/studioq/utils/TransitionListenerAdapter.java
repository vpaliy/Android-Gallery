package com.vpaliy.studioq.utils;

import android.annotation.TargetApi;
import android.transition.Transition;

@TargetApi(21)
public abstract class TransitionListenerAdapter
        implements Transition.TransitionListener {

    @Override
    public void onTransitionStart(Transition transition) {

    }

    @Override
    public void onTransitionEnd(Transition transition) {
    }

    @Override
    public void onTransitionCancel(Transition transition) {

    }

    @Override
    public void onTransitionPause(Transition transition) {

    }

    @Override
    public void onTransitionResume(Transition transition) {

    }
}
