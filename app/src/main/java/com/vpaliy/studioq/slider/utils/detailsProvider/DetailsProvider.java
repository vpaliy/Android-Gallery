package com.vpaliy.studioq.slider.utils.detailsProvider;


import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.vpaliy.studioq.model.MediaFile;

import java.util.LinkedHashMap;
import java.util.Map;

public class DetailsProvider {

    private static final String TAG=DetailsProvider.class.getSimpleName();

    private MediaFile mediaFile;
    private Context context;


    private DetailsProvider(Context context, MediaFile mediaFile) {
        this.mediaFile=mediaFile;
        this.context=context;
    }

    private Map<String,String> initData() {
        Map<String,String> detailsMap=new LinkedHashMap<>();
        String[] projection=new String[] {
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.CONTENT_TYPE,
                MediaStore.Images.Media.MIME_TYPE};
        StringBuilder builder=new StringBuilder();
        builder.append(MediaStore.Images.Media.DATA+" ?=");
        Log.d(TAG,mediaFile.mediaFile().getAbsolutePath());
        Cursor cursor=context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,builder.toString(),new String[]{mediaFile.mediaFile().getAbsolutePath()},null);
        if(cursor!=null) {
            Log.d(TAG,"Cursor installed");
            if(cursor.moveToFirst()) {
                Log.d(TAG,"It's moved");
            }
            cursor.close();
        }
        return detailsMap;
    }

    public static Map<String,String> provideFor(@NonNull Context context,@NonNull  MediaFile mediaFile) {
        return new DetailsProvider(context,mediaFile).initData();
    }

}


