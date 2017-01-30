package com.vpaliy.studioq.activities.utils.eventBus;

import android.content.Intent;


public class ExitEvent{

    public final Intent intent;

    public ExitEvent(Intent intent) {
        this.intent = intent;
    }
}
