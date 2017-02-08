package com.vpaliy.studioq.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import com.vpaliy.studioq.model.MediaFile;

public final class FileUtils {

    private final static String TAG=FileUtils.class.getSimpleName();

    private FileUtils() {
        throw new UnsupportedOperationException();
    }

    private static void makeFileCopy(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }finally {
            try {
                if (is != null)
                    is.close();
                if (os != null)
                    os.close();
            }catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void deleteFile(Context context, MediaFile mediaFile) {
        if(!mediaFile.mediaFile().delete()) {
            Log.e(TAG, "Cannot delete file "+ mediaFile.mediaFile().getAbsoluteFile());
        }
        String[] projection = { MediaStore.Images.Media._ID };

        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[] { mediaFile.mediaFile().getAbsolutePath() };

        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
        if(cursor!=null) {
            if (cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                contentResolver.delete(deleteUri, null, null);
            }
            cursor.close();
        }
    }

    public static void deleteFileList(Context context, List<? extends MediaFile> deleteMediaFileList) {
        for (MediaFile mediaFile : deleteMediaFileList) {
                deleteFile(context,mediaFile);
        }


    }

    public static void copyFileList(Context context, List<MediaFile> contentList, File mediaFolder, boolean moveTo) {
        if (contentList != null) {
            int size=0;
            int missed=0;
            ContentValues values=new ContentValues();
            for (int index=0;index<contentList.size();index++) {
                MediaFile mediaFile=contentList.get(index);
                File file = new File(mediaFolder, mediaFile.mediaFile().getName());
                boolean isVideo=mediaFile.getType()== MediaFile.Type.VIDEO;
                if (!file.exists()) {
                    try {
                        if (!file.createNewFile()) {
                            //showing only path here
                            Log.e(TAG, "Cannot create a file here " + mediaFile.mediaFile().getName());
                            //No need to copy the file any more
                            continue;
                        }

                        FileUtils.makeFileCopy(mediaFile.mediaFile().getAbsoluteFile(), file);


                    } catch (IOException ex) {
                        Log.d(TAG,"Exception on:"+mediaFile.mediaFile());
                        ex.printStackTrace();
                        Log.e(TAG, ex.toString(), ex);
                        continue;
                    }


                    if (isVideo) {
                        values.put(MediaStore.Video.VideoColumns.DATA, file.getAbsolutePath());
                        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                    } else {
                        values.put(MediaStore.Images.ImageColumns.DATA, file.getAbsolutePath());
                        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    }
                    size++;
                    Log.d(TAG,"Number of files:"+Integer.toString(size));
                }else {
                    missed++;
                }
            }
            Log.d(TAG,"Total number of files:"+Integer.toString(contentList.size()));
            Log.d(TAG,"Total number of copied files:"+Integer.toString(size));
            Log.d(TAG,"Total number of missed files:"+Integer.toString(missed));
        }
    }


    private static MediaFile convertToCopy(File to, MediaFile from) {
        return new MediaFile(from,to);
    }



}
