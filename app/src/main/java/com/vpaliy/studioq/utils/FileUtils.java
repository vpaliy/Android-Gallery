package com.vpaliy.studioq.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Random;

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
        String pathTo=mediaFile.mediaFile().getAbsolutePath();

        if(!mediaFile.mediaFile().delete()) {
            Log.e(TAG, "Cannot delete file "+ mediaFile.mediaFile().getAbsoluteFile());
            File anotherTry=new File(Environment.getExternalStorageDirectory()+mediaFile.mediaFile().getAbsolutePath());
            if(!anotherTry.delete()) {
                Log.d(TAG,"Second Try didn't work either");
                return;
            }
            pathTo=anotherTry.getAbsolutePath();
        }

        if(mediaFile.getType()== MediaFile.Type.VIDEO) {
            deleteVideo(context, pathTo);
        }else {
            deleteImage(context, pathTo);
        }
    }

    private static void deleteVideo(@NonNull Context context, @NonNull String pathTo) {
        String[] projection = { MediaStore.Video.Media._ID };

        String selection = MediaStore.Video.Media.DATA + " = ?";
        String[] selectionArgs = new String[] { pathTo };

        Uri queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
        if(cursor!=null) {
            if (cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                Uri deleteUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                contentResolver.delete(deleteUri, null, null);
            }
            cursor.close();
        }
    }

    private static void deleteImage(@NonNull Context context, @NonNull String pathTo) {
        String[] projection = { MediaStore.Images.Media._ID };

        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[] { pathTo };

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

            for (int index=0;index<contentList.size();index++) {
                MediaFile mediaFile=contentList.get(index);
                String fileName=mediaFile.mediaFile().getName();

                boolean isVideo=mediaFile.getType()== MediaFile.Type.VIDEO;
                File file = new File(mediaFolder, fileName);
                //let a user to decide whether to create a copy of already existing files
                if(!file.exists()) {
                    file=new File(mediaFolder,uniqueNameFor(fileName));
                }

                if(!file.exists()) {
                    try {
                        FileUtils.makeFileCopy(mediaFile.mediaFile().getAbsoluteFile(), file);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        continue;
                    }

                    if (isVideo) {
                        values.put(MediaStore.Video.VideoColumns.DATA, file.getAbsolutePath());
                        values.put(MediaStore.Video.VideoColumns.MIME_TYPE,mediaFile.getMimeType());
                        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                    } else {
                        values.put(MediaStore.Images.ImageColumns.DATA, file.getAbsolutePath());
                        values.put(MediaStore.Images.ImageColumns.MIME_TYPE,mediaFile.getMimeType());
                        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    }

                }
                values.clear();
            }
        }
    }

    private static String uniqueNameFor(@NonNull String fileName) {
        Random random=new Random();
        fileName+=Integer.toString(random.nextInt(100));
        return fileName;
    }

    private static MediaFile convertToCopy(File to, MediaFile from) {
        return new MediaFile(from,to);
    }

}
