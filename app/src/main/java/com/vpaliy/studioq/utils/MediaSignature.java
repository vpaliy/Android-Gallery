package com.vpaliy.studioq.utils;

import com.bumptech.glide.signature.StringSignature;
import com.vpaliy.studioq.model.MediaFile;

class MediaSignature extends StringSignature {

    private MediaSignature(String path, long lastModified) {
        super(path + lastModified);
    }

    public static MediaSignature sign(MediaFile mediaFile) {
        return new MediaSignature(mediaFile.pathToMediaFile(),
            mediaFile.mediaFile().lastModified());
    }
}
