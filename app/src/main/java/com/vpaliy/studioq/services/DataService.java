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

import com.vpaliy.studioq.App;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.utils.FileUtils;
import com.vpaliy.studioq.utils.ProjectUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import android.util.Log;

public class DataService extends Service {

    private static final String TAG=DataService.class.getSimpleName();
    private static final int NOTIFICATION_ID=1;

    public static final int ACTION_DELETE=1;
    public static final int ACTION_COPY=2;

    @NonNull
    private final IBinder binder=new DataServiceBinder();

    private volatile Looper serviceLooper;
    private volatile Handler handler;
    private volatile int request=0;

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;

    private ConcurrentLinkedQueue<MediaFile> deleteContainer=new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<String,ArrayList<MediaFile>> copyContainer=new ConcurrentHashMap<>();

    private int lastId;


    public class DataServiceBinder extends Binder {
        public DataService provideService() {
            return DataService.this;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
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
                            deleteContainer.addAll(Collection.class.cast(message.obj));
                        }
                        delete();
                        break;
                    }
                    case ACTION_COPY: {
                        if (message.obj != null) {
                            copyContainer.putAll(DataWrapper.convertToMap(message.obj));
                        }
                        copy();
                        break;
                    }
                }
                request--;
                if(request==0) {
                    synchronized(DataService.this) {
                        App.appInstance().unbindService();
                        stopSelf();
                    }
                }
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
        updateNotification("Deleting data",0,0);
        while(!deleteContainer.isEmpty()){
            FileUtils.deleteFile(this,deleteContainer.peek());
            deleteContainer.poll();
        }
    }


    @WorkerThread
    private void copy() {
        updateNotification("Copying data",0,0);
        for(String key:copyContainer.keySet()) {
            FileUtils.copyFileList(this,copyContainer.get(key),new File(key),false);
        }
    }

    public  Set<File> staleData() {
        if(deleteContainer==null) {
            return null;
        }
        Set<File> resultSet=new LinkedHashSet<>(deleteContainer.size());
        for(MediaFile file:deleteContainer) {
            resultSet.add(file.mediaFile());
        }
       return resultSet;
    }


    public Map<String, ArrayList<MediaFile>> freshData() {
        if(copyContainer==null) {
            return null;
        }
        return new HashMap<>(copyContainer);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        lastId=startId;
        if(intent!=null) {
            determineAction(intent,startId);
        }
        return START_REDELIVER_INTENT;
    }

    private void determineAction(@NonNull Intent intent, int startId) {
        request++;
        Message message=new Message();
        message.arg1=startId;
        message.arg2=intent.getIntExtra(ProjectUtils.ACTION,ACTION_DELETE);
        supplyWithData(message,intent);
        handler.sendMessage(message);
    }

    private void supplyWithData(@NonNull Message message, @NonNull Intent intent) {
        switch (message.arg2) {
            case ACTION_DELETE:
                message.obj=intent.getParcelableArrayListExtra(ProjectUtils.MEDIA_DATA);
                break;
            case ACTION_COPY:
                message.obj=intent.getParcelableExtra(ProjectUtils.MEDIA_DATA);
                break;
        }
    }

    private void sendDummyMessage(@IntRange(from = ACTION_DELETE,to = ACTION_COPY)int action) {
        request++;
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
        copyContainer.putAll(map);
        sendDummyMessage(ACTION_COPY);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceLooper.quit();
        stopForeground(true);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        serviceLooper.quit();
        stopForeground(true);
    }

    //This class helps to pass a map as a Parcelable
    public static class DataWrapper implements Parcelable {

        @NonNull
        private Map<String,ArrayList<MediaFile>> dataMap;

        private DataWrapper(@NonNull Map<String,ArrayList<MediaFile>> dataMap) {
            this.dataMap=dataMap;
        }

        private DataWrapper(Parcel in) {
            int size=in.readInt();
            dataMap=new LinkedHashMap<>();
            for(int index=0;index<size;index++) {
                ArrayList<MediaFile> temp=new ArrayList<>();
                in.readTypedList(temp,MediaFile.CREATOR);
                dataMap.put(in.readString(),temp);
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(dataMap.size());
            for(Map.Entry<String,ArrayList<MediaFile>> entry:dataMap.entrySet()) {
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


        public static DataWrapper wrap(@NonNull Map<String,ArrayList<MediaFile>> dataMap) {
            return new DataWrapper(dataMap);
        }

        //helpful method
        private static Map<String, ArrayList<MediaFile>> convertToMap(@NonNull Object obj) {
            if(!(obj instanceof DataWrapper))
                return null;
           return DataWrapper.class.cast(obj).dataMap;
        }
    }
}
