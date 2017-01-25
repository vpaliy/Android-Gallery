package com.vpaliy.studioq.fragments;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.vpaliy.studioq.R;
import java.util.ArrayList;
import com.vpaliy.studioq.utils.ProjectUtils;


public abstract class BaseMediaFragment<T extends Parcelable> extends Fragment {


    protected ArrayList<T> mMediaDataList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null)
            savedInstanceState=getArguments();
        restoreMediaDataList(savedInstanceState);

    }

    public abstract void restoreMediaDataList(Bundle savedInstanceState);


    @Override
    public View onCreateView(LayoutInflater mInflater,
                             ViewGroup parentGroup, Bundle savedInstanceState) {
        return mInflater.inflate(R.layout.fragment_representation_layout,parentGroup,false);
    }

    @Override
    public abstract void onViewCreated(View root,Bundle savedInstanceState);


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(ProjectUtils.MEDIA_DATA,mMediaDataList);
        super.onSaveInstanceState(outState);

    }



}
