package com.vpaliy.studioq.cases;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.vpaliy.studioq.App;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.adapters.MediaAdapter;
import com.vpaliy.studioq.common.snackbarUtils.ActionCallback;
import com.vpaliy.studioq.common.snackbarUtils.SnackbarWrapper;
import com.vpaliy.studioq.model.MediaFile;

import java.util.ArrayList;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings("WeakerAccess")
public class DeleteCase<T> extends Case {

    @BindView(R.id.actionButton)
    protected FloatingActionButton actionButton;

    @BindView(R.id.mediaRecyclerView)
    protected RecyclerView recyclerView;

    @BindView(R.id.rootView)
    protected View root;

    @NonNull
    private MediaAdapter<T> adapter;

    @NonNull
    private Filter<T> filter;

    @Nullable
    private Callback callback;

    @NonNull
    private String message;

    private DeleteCase(@NonNull Activity activity, @NonNull MediaAdapter<T> adapter) {
        ButterKnife.bind(this,activity);
        this.adapter=adapter;
        message=activity.getResources().getString(R.string.move);
    }

    private DeleteCase(@NonNull View root, @NonNull MediaAdapter<T> adapter) {
        ButterKnife.bind(this,root);
        this.adapter=adapter;
        message=root.getResources().getString(R.string.move);
    }

    public DeleteCase<T> message(@NonNull String message) {
        this.message=message;
        return this;
    }

    public DeleteCase<T> message(@StringRes int res) {
        this.message=root.getResources().getString(res);
        return this;
    }

    public DeleteCase<T> filter(Filter<T> filter) {
        this.filter=filter;
        return this;
    }

    public DeleteCase<T> callback(@Nullable Callback callback) {
        this.callback=callback;
        return this;
    }
    @Override
    public void execute() {
        if (adapter.isMultiModeActivated()) {
            hideButton();
            final List<T> data= adapter.getAllChecked();
            final ArrayList<T> originalData=new ArrayList<>(adapter.getData());
            recyclerView.setItemAnimator(null);
            final int[] checked=adapter.getAllCheckedForDeletion();
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    for(int index:checked) {
                        adapter.removeAt(index);
                    }
                }
            });
            showUI(data,originalData);
        }
    }

    private void showButton() {
        if(actionButton!=null) {
            actionButton.show();
        }
    }

    private void hideButton() {
        if(actionButton!=null) {
            actionButton.hide();
        }
    }

    private void showUI(final List<T> staleData, final ArrayList<T> original) {
        SnackbarWrapper.start(root, Integer.toString(staleData.size()) + message, R.integer.snackbarLength)
                .callback(new ActionCallback("UNDO") {
                    @Override
                    public void onCancel() {
                        showButton();
                        adapter.setData(original);
                    }

                    @Override
                    public void onPerform() {
                        showButton();
                        delete(filter.filterData(staleData));


                    }
                }).show();
    }

    private void delete(ArrayList<MediaFile> data) {
        if(callback!=null) {
            callback.onExecute(data);
        }
        App.appInstance().delete(data);
    }

    public static<T> DeleteCase<T> startWith(@NonNull Activity activity, @NonNull MediaAdapter<T> adapter) {
        return new DeleteCase<>(activity,adapter);
    }

    public static<T> DeleteCase<T> startWith(@NonNull View root,@NonNull MediaAdapter<T> adapter) {
        return new DeleteCase<>(root,adapter);
    }

    public interface Filter<T> {
        ArrayList<MediaFile> filterData(List<T> input);
    }

    public interface Callback {
        void onExecute(ArrayList<MediaFile> result);
    }
}
