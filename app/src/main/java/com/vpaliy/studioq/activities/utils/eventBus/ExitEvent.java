package com.vpaliy.studioq.activities.utils.eventBus;

import android.content.Intent;
import android.support.annotation.Nullable;


public class ExitEvent{

    @Nullable
    public final Intent intent;

    public ExitEvent(@Nullable Intent intent) {
        this.intent = intent;
    }
}
