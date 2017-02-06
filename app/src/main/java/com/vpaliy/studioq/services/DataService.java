package com.vpaliy.studioq.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.utils.FileUtils;
import com.vpaliy.studioq.utils.ProjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

public class DataService extends Service {

    private static final String TAG=DataService.class.getSimpleName();
    private static final int NOTIFICATION_ID=0;

    public static final int ACTION_DELETE=1;
    public static final int ACTION_COPY=2;

    @NonNull
    private final IBinder binder=new DataServiceBinder();

    private volatile Looper serviceLooper;
    private volatile Handler handler;

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;

    private ConcurrentLinkedQueue<MediaFile> deleteContainer=new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<String,ArrayList<MediaFile>> copyMoveContainer=new ConcurrentHashMap<>();

    private int lastId;

    public class DataServiceBinder extends Binder {
        public DataService provideService() {
            return DataService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initNotification();
        HandlerThread handlerThread=new HandlerThread("DataServiceThread");
        handlerThread.start();

        serviceLooper=handlerThread.getLooper();
        handler=new Handler(serviceLooper) {
            @Override
            public void handleMessage(@NonNull Message message) {
                switch (message.arg2) {
                    case ACTION_DELETE: {
                        if (message.obj != null) {
                            deleteContainer.addAll((Collection<? extends MediaFile>) message.obj);
                        }
                        delete();
                        break;
                    }
                    case ACTION_COPY: {
                        if (message.obj != null) {
                            copyMoveContainer.putAll((Map<String, ArrayList<MediaFile>>) message.obj);
                        }
                        copy();
                        break;
                    }
                }
                stopSelf(message.arg1);
            }
        };


    }

    private void initNotification() {
        notificationManager=(NotificationManager)(getSystemService(NOTIFICATION_SERVICE));
        notificationBuilder=new NotificationCompat.Builder(this)
                .setContentTitle("")
                .setContentText("")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true);
        startForeground(NOTIFICATION_ID,notificationBuilder.build());
    }

    private void updateNotification(String action, int maxProgress, int currentProgress) {
        if(notificationBuilder!=null) {
            notificationBuilder.setContentTitle(action);
            notificationBuilder.setProgress(maxProgress,currentProgress,maxProgress==currentProgress);
            notificationManager.notify(NOTIFICATION_ID,notificationBuilder.build());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @WorkerThread
    private void delete() {
        for(MediaFile mediaFile:deleteContainer) {
            FileUtils.deleteFile(this,mediaFile);
            deleteContainer.remove();
        }
    }


    @WorkerThread
    private void copy() {
        //TODO implement
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        lastId=startId;
        if(intent!=null) {
            determineAction(intent,startId);
        }
        return START_NOT_STICKY;
    }

    private void determineAction(@NonNull Intent intent, int startId) {
        Message message=new Message();
        message.arg1=startId;
        message.arg2=intent.getIntExtra(ProjectUtils.ACTION,ACTION_DELETE);
        message.obj=intent.getParcelableArrayListExtra(ProjectUtils.MEDIA_DATA);
        handler.sendMessage(message);
    }


    private void sendDummyMessage(@IntRange(from = ACTION_DELETE,to = ACTION_COPY)int action) {
        Message message=new Message();
        message.arg1=lastId;
        message.arg2=action;
        message.obj=null;
        handler.sendMessage(message);
    }

    public void deleteAction(@NonNull List<MediaFile> mediaFileList) {
        deleteContainer.addAll(mediaFileList);
        sendDummyMessage(ACTION_DELETE);
    }

    public void copyMoveAction(@NonNull Map<String,ArrayList<MediaFile>> map) {
        copyMoveContainer.putAll(map);
        sendDummyMessage(ACTION_COPY);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        serviceLooper.quit();
        stopForeground(true);
    }


    public static class DataWrapper implements Parcelable {

        @NonNull
        private Set<Map.Entry<String,ArrayList<MediaFile>>> data;

        private DataWrapper(@NonNull Set<Map.Entry<String,ArrayList<MediaFile>>> data) {
            this.data=data;
        }

        private DataWrapper(Parcel in) {
            int size=in.readInt();
            data=new LinkedHashSet<>(size);
            for(int index=0;index<size;index++) {
                ArrayList<MediaFile> temp=new ArrayList<>();
                in.readTypedList(temp,MediaFile.CREATOR);
                data.add(new Entry<>(in.readString(),temp));
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(data.size());
            for(Map.Entry<String,ArrayList<MediaFile>> entry:data) {
                out.writeTypedList(entry.getValue());
                out.writeString(entry.getKey());
            }
        }

        public final static Parcelable.Creator<DataWrapper> CREATOR=new Creator<DataWrapper>() {
            @Override
            public DataWrapper createFromParcel(Parcel source) {
                return new DataWrapper(source);
            }

            @Override
            public DataWrapper[] newArray(int size) {
                return new DataWrapper[size];
            }
        };

        private class Entry<K,V> implements Map.Entry<K,V> {

            private K key;
            private V value;

            Entry(K key, V value) {
                this.key=key;
                this.value=value;
            }

            @Override
            public K getKey() {
                return key;
            }

            @Override
            public V getValue() {
                return value;
            }

            @Override
            public V setValue(V value) {
                V last=this.value;
                this.value = value;
                return last;
            }
        }

        public static DataWrapper wrap(@NonNull Set<Map.Entry<String,ArrayList<MediaFile>>> data) {
            return new DataWrapper(data);
        }
    }
}
