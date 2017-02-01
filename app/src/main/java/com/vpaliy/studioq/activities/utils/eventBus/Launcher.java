package com.vpaliy.studioq.activities.utils.eventBus;

import android.view.View;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Launcher<T> {

    @NonNull
    public final T data;

    public final int position;

    @Nullable
    public final View clickedView;

    public Launcher(@NonNull T data, @Nullable View clickedView) {
        this(data,clickedView,-1);
    }

    public Launcher(@NonNull T data, @Nullable View clickedView, int position) {
        this.data=data;
        this.clickedView=clickedView;
        this.position=position;
    }
}
