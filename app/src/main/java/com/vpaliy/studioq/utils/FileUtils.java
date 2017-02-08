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
import java.nio.channels.FileChannel;
import java.util.List;
import com.vpaliy.studioq.model.MediaFile;

public final class FileUtils {

    private final static String TAG=FileUtils.class.getSimpleName();

    private FileUtils() {
        throw new UnsupportedOperationException();
    }


    private static void makeFileCopy(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            try {
                if (inputChannel != null)
                    inputChannel.close();
                if (outputChannel != null)
                    outputChannel.close();
            }catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void deleteFile(Context context, MediaFile mediaFile) {
        if(!mediaFile.mediaFile().delete()) {
            Log.e(TAG, "Cannot delete file "+ mediaFile.mediaFile().getAbsoluteFile());
            return;
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

    public static void copyFileList(Context context, List<MediaFile> contentList, File mediaFolder) {
        if (contentList != null) {
            ContentValues values=new ContentValues();
            int size=0;
            for (int index=0;index<contentList.size();index++) {
                MediaFile mediaFile=contentList.get(index);
                File file = new File(mediaFolder, mediaFile.mediaFile().getName());
                boolean isVideo=mediaFile.getType()== MediaFile.Type.VIDEO;
                if (!file.exists()) {
                    try {
                        FileUtils.makeFileCopy(mediaFile.mediaFile().getAbsoluteFile(), file);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        continue;
                    }


                    values.clear();
                    size++;
                }
                Log.d(TAG,"Copied files:"+ Integer.toString(size));
            }
        }
    }


    private static MediaFile convertToCopy(File to, MediaFile from) {
        return new MediaFile(from,to);
    }



}
