package com.vpaliy.studioq.activities.main;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

abstract class DataProvider extends AsyncTask<Void,Void,ArrayList<MediaFolder>> {

    private final static String  TAG=DataProvider.class.getSimpleName();

    private Context context;
    private ContentResolver contentResolver;



    DataProvider(Context context) {
        this.context=context;
        this.contentResolver =context.getContentResolver();
        execute(null,null);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<MediaFolder> doInBackground(Void... voids) {
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
                        String folder=cursor.getString(cursor.
                            getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
                        String mimeType=cursor.getString(cursor.
                            getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE));

                        File file=new File(cursor.getString(cursor.getColumnIndex
                            (MediaStore.Files.FileColumns.DATA)));
                        //check if the folder already has been created
                        String pathToFolder=file.getParentFile().getAbsolutePath();
                        if(folderMap.get(pathToFolder)==null) {
                            folderMap.put(pathToFolder, new MediaFolder(folder));
                        }
                        if(mimeType.contains("video")) {
                            folderMap.get(pathToFolder).addVideoFile(new VideoFile(cursor));

                        }else {
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
        return new ArrayList<>(folderMap.values());
    }

    @Override
    public abstract void onPostExecute(ArrayList<MediaFolder> mediaFolders);
}
