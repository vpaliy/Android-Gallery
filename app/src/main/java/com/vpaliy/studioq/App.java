package com.vpaliy.studioq;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.services.DataService;
import com.vpaliy.studioq.utils.ProjectUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class App extends Application {

    @Nullable
    private static App instance;

    @Nullable
    private DataService service;

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instance=null;

    }

    @Nullable
    public Set<File> provideStaleData() {
        if(service!=null) {
            return service.staleData();
        }
        return null;
    }

    @Nullable
    public Map<String, ArrayList<MediaFile>> provideFreshData() {
        if(service!=null) {
            return service.freshData();
        }
        return null;
    }

    public void delete(@NonNull ArrayList<MediaFile> mediaFileList) {
        if(service!=null) {
            service.deleteAction(mediaFileList);
        }else {
            Intent intent=new Intent(this,DataService.class);
            intent.putExtra(ProjectUtils.ACTION,DataService.ACTION_DELETE);
            intent.putExtra(ProjectUtils.MEDIA_DATA,mediaFileList);
            startDataService(intent);
        }
    }

    public void copy(@NonNull Map<String,ArrayList<MediaFile>> copyMap) {
        if(service!=null) {
            service.copyMoveAction(copyMap);
        }else {
            Intent intent=new Intent(this,DataService.class);
            intent.putExtra(ProjectUtils.ACTION,DataService.ACTION_COPY);
            intent.putExtra(ProjectUtils.MEDIA_DATA, DataService.DataWrapper.wrap(copyMap));
            startDataService(intent);
        }
    }

    public void move(@NonNull Map<String, ArrayList<MediaFile>> moveMap) {
        for(Map.Entry<String,ArrayList<MediaFile>> entry:moveMap.entrySet()) {
            delete(entry.getValue());
        }
        copy(moveMap);
    }

    private void startDataService(@NonNull Intent intent) {
        startService(intent);
        bindService(intent,connection,BIND_AUTO_CREATE);
    }


    public void unbindService() {
        unbindService(connection);
        service=null;
    }


    public static App appInstance() {
        return instance;
    }

    private final ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            DataService.DataServiceBinder binder=(DataService.DataServiceBinder)(iBinder);
            service=binder.provideService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service=null;
        }
    };

}
