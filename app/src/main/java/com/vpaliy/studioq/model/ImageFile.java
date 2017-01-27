package com.vpaliy.studioq.model;

import android.database.Cursor;
import android.os.Parcel;
import android.support.annotation.NonNull;

@org.parceler.Parcel(org.parceler.Parcel.Serialization.BEAN)
public class ImageFile extends MediaFile {

    public ImageFile(@NonNull Cursor cursor) {
        super(cursor, Type.IMAGE);
    }

    public ImageFile(@NonNull Cursor cursor, Type type) {
        super(cursor, type);
    }

    public final static Creator<MediaFile> CREATOR =new Creator<MediaFile>() {
        @Override
        public MediaFile createFromParcel(Parcel parcel) {
            return new MediaFile(parcel);
        }

        @Override
        public MediaFile[] newArray(int i) {
            return new MediaFile[0];
        }
    };

}
