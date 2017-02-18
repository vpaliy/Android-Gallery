package com.vpaliy.studioq.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.parceler.ParcelConstructor;
import org.parceler.ParcelPropertyConverter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@org.parceler.Parcel(org.parceler.Parcel.Serialization.BEAN)
public final class MediaFolder implements Parcelable {

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
        List<MediaFile> data=folder.getMediaFileList();
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
            mediaFileList.removeAll(mediaFolder.getMediaFileList());
            imageFileList.removeAll(mediaFolder.getImageFileList());
            videoFileList.removeAll(mediaFolder.getVideoFileList());
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

    //Covers
    public MediaFile getCoverForAll() {
        return mediaFileList.get(0);
    }

    public MediaFile  getCoverForVideo() {
        if (videoFileList.isEmpty()) {
            return null;
        }
        return videoFileList.get(0);
    }

    public MediaFile  getCoverForImage() {
        if (imageFileList.isEmpty())
            return null;
        return imageFileList.get(0);
    }

    //Data providers
    public List<MediaFile> getImageFileList() {
        return imageFileList;
    }

    public List<MediaFile> getVideoFileList() {
        return videoFileList;
    }

    public List<MediaFile> getMediaFileList() {
        return mediaFileList;
    }



    public int getFileCount() {
        return mediaFileList.size();
    }

    public String getFolderName() {
        return folderName;
    }

    @Override
    public String toString() {
        return getFolderName();
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
        return new DummyFolder(folder.getCoverForAll(),folder.folderName,size);
    }

}
