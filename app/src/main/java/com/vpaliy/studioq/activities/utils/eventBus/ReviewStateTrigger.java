package com.vpaliy.studioq.activities.utils.eventBus;

import com.vpaliy.studioq.model.MediaFile;

public class ReviewStateTrigger {

    public final MediaFile mediaFile;

    public ReviewStateTrigger(MediaFile mediaFile) {
        this.mediaFile=mediaFile;
    }

}
