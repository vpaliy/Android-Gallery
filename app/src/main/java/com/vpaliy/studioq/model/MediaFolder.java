package com.vpaliy.studioq.model;

import android.os.Parcel;
import android.os.Parcelable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import android.support.annotation.NonNull;
import org.parceler.ParcelConstructor;
import org.parceler.ParcelPropertyConverter;

@org.parceler.Parcel(org.parceler.Parcel.Serialization.BEAN)
public final class MediaFolder implements Parcelable,
    Comparable<MediaFolder>{

    private String folderName;

    @ParcelPropertyConverter(MediaFile.MediaListConverter.class)
    private List<MediaFile> mediaFileList;
    private List<MediaFile> imageFileList;
    private List<MediaFile> videoFileList;

    public MediaFolder(String folderName) {
        this.folderName = folderName;
        this.mediaFileList= new ArrayList<>();
        init();
    }

    private MediaFolder(Parcel in) {
        this.folderName=in.readString();
        mediaFileList=new ArrayList<>();
        in.readTypedList(mediaFileList, MediaFile.CREATOR);
        init();
    }

    @ParcelConstructor
    public MediaFolder(String folderName, List<MediaFile> mediaFileList) {
        this.folderName=folderName;
        this.mediaFileList=mediaFileList;
        init();
    }

    public MediaFolder(String folderName, Collection<? extends MediaFile> collection) {
        this(folderName);
        this.mediaFileList=new ArrayList<>(collection);
    }

    public MediaFolder add(MediaFile mediaFile) {
        mediaFileList.add(mediaFile);
        if(mediaFile.getType()== MediaFile.Type.VIDEO) {
            videoFileList.add(mediaFile);
        }else {
            imageFileList.add(mediaFile);
        }
        return this;
    }

    public void updateWith(@NonNull  MediaFolder folder) {
        List<MediaFile> data=folder.list();
        if(data!=null) {
            for (MediaFile mediaFile : data) {
                if(!mediaFileList.contains(mediaFile)) {
                    add(mediaFile);
                }
            }
        }
    }

    public  MediaFolder addAll(Collection<? extends MediaFile> collection) {
        if(collection!=null) {
            for (MediaFile mediaFile : collection) {
                mediaFileList.add(mediaFile);
                boolean isVideo=mediaFile.getType()== MediaFile.Type.VIDEO;
                if(isVideo) {
                    videoFileList.add(mediaFile);
                }else {
                    imageFileList.add(mediaFile);
                }
            }
        }
        return this;
    }

    public void replaceWith(Collection<? extends MediaFile> collection) {
        mediaFileList.clear();
        videoFileList.clear();
        imageFileList.clear();
        if(collection!=null) {
            for (MediaFile mediaFile : collection) {
                mediaFileList.add(mediaFile);
                boolean isVideo=mediaFile.getType()== MediaFile.Type.VIDEO;
                if(isVideo) {
                    videoFileList.add(mediaFile);
                }else {
                    imageFileList.add(mediaFile);
                }
            }
        }
    }


    public boolean isEmpty() {
        return mediaFileList == null || mediaFileList.isEmpty();
    }

    public void addVideoFile(VideoFile videoFile) {
        mediaFileList.add(videoFile);
        videoFileList.add(videoFile);
    }

    public void addImageFile(ImageFile imageFile) {
        mediaFileList.add(imageFile);
        imageFileList.add(imageFile);
    }

    //returns true if folder is empty now
    public boolean removeAll(MediaFolder mediaFolder) {
        if(mediaFolder!=null) {
            mediaFileList.removeAll(mediaFolder.list());
            imageFileList.removeAll(mediaFolder.imageList());
            videoFileList.removeAll(mediaFolder.videoList());
            return mediaFileList.isEmpty();
        }
        return false;
    }

    public String getAbsolutePathToFolder() {
        if(mediaFileList!=null) {
            if (mediaFileList.get(0) != null)
                return mediaFileList.get(0).mediaFile().getParentFile().getAbsolutePath();
        }

        return folderName;
    }

    public void setMediaFileList(List<MediaFile> mediaFileList) {
        this.mediaFileList=mediaFileList;
    }

    private void init(){
        if(mediaFileList!=null) {
            imageFileList=new ArrayList<>();
            videoFileList=new ArrayList<>();
            for (MediaFile mediaFile : mediaFileList) {
                boolean isVideo=mediaFile.getType()== MediaFile.Type.VIDEO;
                if(isVideo) {
                    videoFileList.add(mediaFile);
                }else {
                    imageFileList.add(mediaFile);
                }
            }
        }
    }

    @Override
    public int compareTo(MediaFolder o) {
        return BY_NAME.compare(this,o);
    }

    //Covers
    public MediaFile cover() {
        return mediaFileList.get(0);
    }

    public MediaFile videoCover() {
        if (videoFileList.isEmpty()) {
            return null;
        }
        return videoFileList.get(0);
    }

    public MediaFile imageCover() {
        if (imageFileList.isEmpty())
            return null;
        return imageFileList.get(0);
    }


    //Data providers
    public List<MediaFile> imageList() {
        return imageFileList;
    }

    public List<MediaFile> videoList() {
        return videoFileList;
    }

    public List<MediaFile> list() {
        return mediaFileList;
    }



    public int size() {
        return mediaFileList.size();
    }

    public String name() {
        return folderName;
    }



    @Override
    public String toString() {
        return name();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof MediaFolder)) {
            return false;
        }
        MediaFolder folder=(MediaFolder) (obj);
        return new EqualsBuilder()
                .append(folder.folderName,folderName)
                .isEquals();
    }

    public MediaFolder createImageSubfolder() {
        MediaFolder imageFolder=new MediaFolder(folderName);
        imageFolder.addAll(imageFileList);
        return imageFolder;
    }

    public MediaFolder createVideoSubfolder() {
        MediaFolder videoFolder=new MediaFolder(folderName);
        videoFolder.addAll(videoFileList);
        return videoFolder;
    }

    //returns true if empty
    public boolean removeAll(@NonNull List<MediaFile> data) {
        mediaFileList.removeAll(data);
        if(!mediaFileList.isEmpty()) {
            imageFileList.removeAll(data);
            videoFileList.removeAll(data);
            return false;
        }
        imageFileList.clear();
        videoFileList.clear();
        return true;
    }

    public String toPath() {
       return  mediaFileList.get(0).parentPath();
    }

    //Parcel stuff goes here
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(folderName);
        out.writeTypedList(mediaFileList);
    }

    public final static Creator<MediaFolder> CREATOR
            =new Creator<MediaFolder>() {
        @Override
        public MediaFolder createFromParcel(Parcel parcel) {
            return new MediaFolder(parcel);
        }

        @Override
        public MediaFolder[] newArray(int size) {
            return new MediaFolder[size];
        }
    };


    public static DummyFolder createDummy(@NonNull MediaFolder folder) {
        int size=folder.mediaFileList!=null?folder.mediaFileList.size():0;
        return new DummyFolder(folder.cover(),folder.folderName,size);
    }


    public static final Comparator<MediaFolder> BY_SIZE=new Comparator<MediaFolder>() {
        @Override
        public int compare(MediaFolder o1, MediaFolder o2) {
            if(o1.mediaFileList.size()>o2.mediaFileList.size()) {
                return 1;
            }else if(o1.mediaFileList.size()<o2.mediaFileList.size()) {
                return -1;
            }
            return 0;
        }
    };

    public static final  Comparator<MediaFolder> BY_DATE=new Comparator<MediaFolder>() {
        @Override
        public int compare(MediaFolder o1, MediaFolder o2) {

            return 0;
        }
    };


    public static final  Comparator<MediaFolder> BY_NAME=new Comparator<MediaFolder>() {
        @Override
        public int compare(MediaFolder o1, MediaFolder o2) {
            return o1.folderName.compareTo(o2.folderName);
        }
    };

}
