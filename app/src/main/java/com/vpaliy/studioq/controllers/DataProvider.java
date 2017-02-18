package com.vpaliy.studioq.controllers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import com.vpaliy.studioq.model.ImageFile;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.model.MediaFolder;
import com.vpaliy.studioq.model.VideoFile;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

abstract class DataProvider extends AsyncTask<Void,Void,Map<String,MediaFolder>> {

    private ContentResolver contentResolver;

    DataProvider(Context context) {
        this.contentResolver =context.getContentResolver();
        execute(null,null);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Map<String,MediaFolder> doInBackground(Void... voids) {
        Map<String,MediaFolder> folderMap=new LinkedHashMap<>();
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME
        };

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        Uri queryUri = MediaStore.Files.getContentUri("external");
        Cursor cursor=contentResolver.query(queryUri,projection,selection,null,
             MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
        if(cursor!=null) {
            try {
                if(cursor.moveToFirst()) {
                    do {
                        String pathTo=cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                        File file=new File(pathTo);

                        String folder=cursor.getString(cursor.
                            getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
                        String mimeType=cursor.getString(cursor.
                            getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE));

                        if(folder==null||mimeType==null)
                            continue;
                        //check if the folder already has been created
                        String pathToFolder=file.getParentFile().getAbsolutePath();
                        if(folderMap.get(pathToFolder)==null) {
                            folderMap.put(pathToFolder, new MediaFolder(folder));
                        }
                        if(mimeType.contains("video")) {
                            folderMap.get(pathToFolder).addVideoFile(new VideoFile(cursor));

                        }else if(!mimeType.contains("mp3")) {
                            MediaFile.Type type= MediaFile.Type.IMAGE;
                            if(mimeType.contains("gif")) {
                                type= MediaFile.Type.GIF;
                            }
                            folderMap.get(pathToFolder).addImageFile(new ImageFile(cursor,type));
                        }
                    }while(cursor.moveToNext());
                }
            }finally {
                cursor.close();
            }
        }
        return folderMap;
    }



    @Override
    public abstract void onPostExecute(Map<String,MediaFolder> result);
}
