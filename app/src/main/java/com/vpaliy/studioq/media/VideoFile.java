package com.vpaliy.studioq.media;

import android.database.Cursor;
import android.support.annotation.NonNull;
import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class VideoFile extends MediaFile {

    public VideoFile(@NonNull Cursor cursor) {
        super(cursor,Type.VIDEO);
    }

    public final static Creator<MediaFile> CREATOR =new Creator<MediaFile>() {
        @Override
        public MediaFile createFromParcel(android.os.Parcel parcel) {
            return new MediaFile(parcel);
        }

        @Override
        public MediaFile[] newArray(int i) {
            return new MediaFile[0];
        }
    };



}
