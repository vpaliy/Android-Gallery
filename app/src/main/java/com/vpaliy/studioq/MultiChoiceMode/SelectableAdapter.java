package com.vpaliy.studioq.MultiChoiceMode;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract  class SelectableAdapter extends RecyclerView.Adapter<SelectableAdapter.SelectableViewHolder>
        implements MultiChoiceMode.OnControlItemStateListener{

    private AppCompatActivity mAppCompatActivity;
    private MultiChoiceMode mMultipleChoiceMode;
    protected Drawable mSelectedItemBackground;
    protected Drawable mDefaultItemBackground;


    public SelectableAdapter(Context context, @NonNull MultiChoiceMode mMultipleChoiceMode) {
        this.mAppCompatActivity = (AppCompatActivity) (context);
        this.mMultipleChoiceMode = mMultipleChoiceMode;
        mMultipleChoiceMode.setUpdateListener(this);
        mSelectedItemBackground=new ColorDrawable(Color.BLUE);
        mDefaultItemBackground=new ColorDrawable(Color.WHITE);
    }

    @CallSuper
    public void activateMultipleChoiceMode() {
        mAppCompatActivity.startSupportActionMode(mMultipleChoiceMode);
    }

    public abstract class SelectableViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener,View.OnLongClickListener {


        public SelectableViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public abstract void setBackground(int position);

        public abstract void onBindData(int position);

        //TODO make it common for all of the objects
        @Override
        @CallSuper
        public void onClick(View view) {
            if (mMultipleChoiceMode.isActivated()) {
                mMultipleChoiceMode.selectItem(getPosition());
                setBackground(getPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if(!mMultipleChoiceMode.isActivated())
                activateMultipleChoiceMode();
            onClick(view);
            return true;
        }

    }



    @Override
    public void onCheckAll() {
        for (int index = 0; index < getItemCount(); index++) {
            if (!isSelected(index)) {
                mMultipleChoiceMode.selectItem(index);
                notifyItemChanged(index);
            }
        }

    }

    @Override
    public void onUnCheckAll() {
        for(int index=0;index<getItemCount();index++) {
            if (isSelected(index)) {
                mMultipleChoiceMode.selectItem(index);
                notifyItemChanged(index);
            }
        }
    }


    @Override
    @CallSuper
    public void onBindViewHolder(SelectableAdapter.SelectableViewHolder selectableViewHolderInstance, int position) {
        selectableViewHolderInstance.setBackground(position);
        selectableViewHolderInstance.onBindData(position);
    }

    public void setSelectedBackgroundDrawable(Drawable selectedItemBackgroundDrawable) {
        this.mSelectedItemBackground = selectedItemBackgroundDrawable;
    }

    public void setDefaultBackgroundDrawable(Drawable defaultBackgroundDrawable) {
        this.mDefaultItemBackground = defaultBackgroundDrawable;
    }

    public boolean isSelected(int position) {
        return mMultipleChoiceMode.isSelected(position);
    }

    public boolean isActivatedMultipleChoiceMode() {
        return mMultipleChoiceMode.isActivated();
    }

}








