package com.vpaliy.studioq.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;

public class DummyFolder implements Parcelable {

    private MediaFile cover;
    private int size;
    private String name;


    DummyFolder(@NonNull MediaFile cover, String name,int size) {
        this.cover=cover;
        this.name=name;
        this.size=size;
    }

    private DummyFolder(Parcel in) {
        this.cover=in.readParcelable(MediaFolder.class.getClassLoader());
        this.size=in.readInt();
        this.name=in.readString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(cover,0);
        out.writeInt(size);
        out.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public MediaFile cover() {
        return cover;
    }

    public int size() {
        return size;
    }

    public String name() {
        return name;
    }

    public final static Parcelable.Creator<DummyFolder> CREATOR=new Creator<DummyFolder>() {
        @Override
        public DummyFolder createFromParcel(Parcel source) {
           return  new DummyFolder(source);
        }

        @Override
        public DummyFolder[] newArray(int size) {
            return new DummyFolder[size];
        }
    };
}
