package com.vpaliy.studioq.utils;

import android.view.View;

import java.util.ArrayList;

import com.vpaliy.studioq.model.MediaFolder;

public interface OnLaunchGalleryActivity {
    void onLaunchGalleryActivity(ArrayList<MediaFolder> allMediaFolders, MediaFolder mediaFolder, View imageView);
}
