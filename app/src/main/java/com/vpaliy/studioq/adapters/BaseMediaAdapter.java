package com.vpaliy.studioq.adapters;


import android.content.Context;
import android.support.annotation.CallSuper;
import android.view.LayoutInflater;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;

import java.util.ArrayList;

import com.vpaliy.studioq.MultiChoiceMode.MultiChoiceMode;
import com.vpaliy.studioq.MultiChoiceMode.SelectableAdapter;

public abstract class BaseMediaAdapter<T> extends SelectableAdapter {

    protected ArrayList<T> mDataModel;
    protected LayoutInflater mInflater;

    protected static final float SCALE_ITEM=0.85f;

    public BaseMediaAdapter(Context context, MultiChoiceMode multiChoiceModeListener, ArrayList<T> mDataModel) {
        super(context, multiChoiceModeListener);
        this.mInflater = LayoutInflater.from(context);
        this.mDataModel = mDataModel;
    }


    @Override
    public int getItemCount() {
        return mDataModel.size();
    }

    @Override
    @CallSuper
    public void onUpdateAll() {
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(SelectableViewHolder selectableViewHolder, int position) {
        super.onBindViewHolder(selectableViewHolder,position);
        selectableViewHolder.onBindData(position);
    }



}
