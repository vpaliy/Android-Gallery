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

    private static final int NORMAL=0;
    private static final int ANIMATED=1;
    private static final int ENTER=2;
    private static final int EXIT=3;

    private int[] itemState;


    public SelectableAdapter(Context context, @NonNull MultiChoiceMode mMultipleChoiceMode, int size) {
        this.mAppCompatActivity = (AppCompatActivity) (context);
        this.mMultipleChoiceMode = mMultipleChoiceMode;
        mMultipleChoiceMode.setUpdateListener(this);
        mSelectedItemBackground=new ColorDrawable(Color.BLUE);
        mDefaultItemBackground=new ColorDrawable(Color.WHITE);
        this.itemState=new int[size];

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

        private void setBackground(int position) {
            switch (itemState[position]) {
                case ENTER:
                    enterState();
                    itemState[position] = ANIMATED;
                    break;
                case ANIMATED:
                    animatedState();
                    break;
                case EXIT:
                    exitState();
                    itemState[position] = NORMAL;
                    break;
                case NORMAL:
                    normalState();
                    break;
            }
        }

        public abstract void onBindData(int position);

        //TODO make it common for all of the objects
        @Override
        @CallSuper
        public void onClick(View view) {
            final int position=getAdapterPosition();
            if (mMultipleChoiceMode.isActivated()) {
                mMultipleChoiceMode.selectItem(position);
                if(itemState[position]==NORMAL||itemState[position]==EXIT) {
                    itemState[position] = ENTER;
                }else {
                    itemState[position] = EXIT;
                }
            }

            setBackground(position);
        }

        protected abstract void enterState();

        protected abstract void exitState();

        protected abstract void normalState();

        protected abstract void animatedState();

        @Override
        public boolean onLongClick(View view) {
            if(!mMultipleChoiceMode.isActivated()) {
                activateMultipleChoiceMode();
            }
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








