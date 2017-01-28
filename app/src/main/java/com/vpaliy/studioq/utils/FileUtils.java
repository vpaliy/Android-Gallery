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
import java.util.List;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.model.VideoFile;

public final class FileUtils {

    private final static String TAG=FileUtils.class.getSimpleName();

    private FileUtils() {
        throw new UnsupportedOperationException();
    }

    public static   void makeFileCopy(File source, File dest) throws IOException {
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
            if(is!=null)
                is.close();
            if(os!=null)
                os.close();
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
            ContentValues values=new ContentValues();
            for (MediaFile mediaFile : contentList) {
                File file = new File(mediaFolder, mediaFile.mediaFile().getName());
                if (!file.exists()) {
                    try {
                        if (!file.createNewFile()) {
                            //showing only path here
                            Log.e(TAG, "Cannot create a file here " + mediaFile.mediaFile().getName());
                            //No need to copy the file any more
                            continue;
                        }

                        //copying file byte by byte
                        FileUtils.makeFileCopy(mediaFile.mediaFile().getAbsoluteFile(), file);

                        //if user's selected "move" option, you need to delete the file
                        if (moveTo) {
                            if (!mediaFile.mediaFile().delete()) {
                                Log.e(TAG, "Cannot delete file " + mediaFile.mediaFile().getAbsoluteFile());
                            }

                            //TODO it ain't working because MediaFile is neither ImageFile nor VideoFile
                            if(mediaFile instanceof VideoFile) {
                                context.getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                        MediaStore.MediaColumns.DATA + "=?", new String[]{mediaFile.mediaFile().getAbsolutePath()});
                            }else {
                                context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        MediaStore.MediaColumns.DATA + "=?", new String[]{mediaFile.mediaFile().getAbsolutePath()});
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        Log.e(TAG, ex.toString(), ex);
                        continue;
                    }
                    if (mediaFile instanceof VideoFile) {
                        values.put(MediaStore.Video.VideoColumns.DATA, file.getAbsolutePath());
                        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                    } else {
                        values.put(MediaStore.Images.ImageColumns.DATA, file.getAbsolutePath());
                        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    }
                }
            }
        }

    }


}
