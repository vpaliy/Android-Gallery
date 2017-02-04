package com.vpaliy.studioq.services;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class DataService extends Service {


    private IBinder binder=new DataBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

    public class DataBinder extends Binder {
        public DataService dataService() {
            return DataService.this;
        }
    }
}
