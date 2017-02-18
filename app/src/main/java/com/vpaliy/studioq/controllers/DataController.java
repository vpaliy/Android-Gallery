package com.vpaliy.studioq.controllers;


import com.vpaliy.studioq.App;
import com.vpaliy.studioq.common.dataUtils.Subscriber;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.model.MediaFolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

//Hello, CRUD
public class DataController {

    private static DataController instance;

    private List<Subscriber> subscriberList;
    private Map<String,MediaFolder> dataModel;

    private volatile boolean finished=true;


    private DataController() {
        subscriberList=new LinkedList<>();
    }

    public void subscribe(@NonNull Subscriber subscriber) {
        subscriberList.add(subscriber);
    }

    public void unSubscribe(@NonNull Subscriber subscriber) {
        subscriberList.remove(subscriber);
    }

    public void unSubscribeAll() {
        subscriberList.clear();
    }

    public void notifyAboutChange() {
        for(Subscriber subscriber:subscriberList) {
            subscriber.notifyAboutChange();
        }
    }

    public static DataController controllerInstance() {
        //supposed to be initialized only once
        if (instance == null) {
            synchronized (DataController.class) {
                if (instance == null) {
                    instance = new DataController();
                }
            }
        }
        return instance;
    }

    public ArrayList<MediaFolder> getFolders() {
        return new ArrayList<>(dataModel.values());
    }

    //create
    public void makeQuery() {
        makeQuery(null);
    }

    //create
    public void makeQuery(final @Nullable Callback callback) {
        if (queryBefore()) {
            new DataProvider(App.appInstance()) {
                @Override
                public void onPostExecute(Map<String,MediaFolder> result) {
                    dataModel = result;
                    if(callback!=null) {
                        callback.onFinished();
                    }
                    notifyAboutChange();
                    queryPostCondition();
                }
            };
        }
    }

    public void makeQueryIfEmpty() {
        if(dataModel==null) {
            makeQuery();
        }else {
            notifyAboutChange();
        }
    }

    private boolean queryBefore() {
        if(finished) {
            finished=false;
            return true;
        }
        return false;
    }

    private void queryPostCondition() {
        finished=true;
    }

    //delete
    public void sensitiveDelete(@NonNull String folderName, List<MediaFile> data) {
        delete(folderName,data,true);
    }

    public void sensitiveDelete(List<MediaFile> data) {
        if(!data.isEmpty()) {
            delete(data.get(0).parentPath(),data,true);
        }
    }

    public void justDelete(@NonNull String folderName, List<MediaFile> data) {
        delete(folderName,data,false);
    }


    public void justDelete(@NonNull List<MediaFile> data) {
        if(!data.isEmpty()) {
            delete(data.get(0).parentPath(),data,false);
        }
    }

    private void delete(@NonNull String folderName, List<MediaFile> data, boolean notify) {
        MediaFolder target=dataModel.get(folderName);
        if(target!=null) {
            if(target.removeAll(data)) {
                dataModel.remove(folderName);
            }
            //notify everybody about a great event!
            if(notify) {
                notifyAboutChange();
            }
        }
    }

    //add
    public void sensitiveAdd(@NonNull MediaFolder folder) {
        add(folder,true);
    }

    public void justAdd(@NonNull MediaFolder folder) {
        add(folder,false);
    }

    private void add(@NonNull MediaFolder folder, boolean notify) {
        dataModel.put(folder.toPath(),folder);
        if(notify) {
            notifyAboutChange();
        }
    }

    //update
    public void sensitiveUpdate(@NonNull String folder, ArrayList<MediaFile> data) {
        update(folder,data,true);
    }

    public void justUpdate(@NonNull String folder, ArrayList<MediaFile> data) {
        update(folder,data,false);
    }

    private void update(@NonNull String folder, ArrayList<MediaFile> data, boolean notify) {
        MediaFolder target=dataModel.get(folder);
        if(target!=null) {
            target.addAll(data);
            //tell your friends about this great event!
            if(notify) {
                notifyAboutChange();
            }
        }
    }

    public ArrayList<MediaFile> filterDuplicates() {
        Collection<MediaFolder> collection=dataModel.values();
        Set<MediaFile> set=new LinkedHashSet<>();
        //let's go
        for(MediaFolder folder:collection) {
            set.addAll(folder.list());
        }

        //you're welcome
        return new ArrayList<>(set);
    }

    public boolean containsFolder(@NonNull String pathTo) {
        return dataModel.containsKey(pathTo);
    }

    public interface Callback {
        void onFinished();
    }

}
