package com.vpaliy.studioq.activities.main;

import com.vpaliy.studioq.model.MediaFolder;

import java.util.LinkedHashMap;
import java.util.Map;

abstract class DataLoader extends Thread {

    protected Map<String,MediaFolder> dataModel= new LinkedHashMap<>();

    @Override
    public abstract void run();

    public abstract Map<String,MediaFolder> retrieveData();


}
