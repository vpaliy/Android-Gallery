package com.vpaliy.studioq;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.vpaliy.studioq.services.DataService;

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
        unbindService(connection);
    }

    public static App appInstance() {
        return instance;
    }

    private final ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            DataService.DataBinder binder=(DataService.DataBinder)(iBinder);
            service=binder.dataService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service=null;
        }
    };

    @Nullable
    public DataService dataService() {
        return service;
    }
}
