package com.vpaliy.studioq.activities.utils.eventBus;

import android.view.View;

public class Launcher<T> {

    public final T data;
    public final int position;
    public final View clickedView;

    public Launcher(T data, View clickedView) {
        this(data,clickedView,-1);
    }

    public Launcher(T data, View clickedView, int position) {
        this.data=data;
        this.clickedView=clickedView;
        this.position=position;
    }
}
