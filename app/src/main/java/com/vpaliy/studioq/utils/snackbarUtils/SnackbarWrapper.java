package com.vpaliy.studioq.utils.snackbarUtils;


import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.view.View;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import com.drew.lang.annotations.NotNull;

//This class is for my particular needs

public class SnackbarWrapper {


    @NonNull
    private final Snackbar snackbar;

    @Nullable
    private ActionCallback actionCallback;

    private SnackbarWrapper(@NonNull View root, int duration) {
        this(root,"",duration);
    }

    private SnackbarWrapper(@NonNull View root, @StringRes int resId, int duration) {
        this.snackbar=Snackbar.make(root,resId,duration);
    }

    private SnackbarWrapper(@NonNull View root, String text, int duration) {
        this.snackbar=Snackbar.make(root,text,duration);
    }

    public static SnackbarWrapper start(@NonNull View root, int duration) {
        return new SnackbarWrapper(root,duration);
    }

    public static SnackbarWrapper start(@NonNull View root, @NonNull String text, int duration) {
        return new SnackbarWrapper(root,text,duration);
    }

    public static SnackbarWrapper start(@NonNull View root, @StringRes int resId, int duration) {
        return new SnackbarWrapper(root,resId,duration);
    }

    public SnackbarWrapper duration(int duration) {
        this.snackbar.setDuration(duration);
        return this;
    }

    public SnackbarWrapper text(@NotNull String text) {
        this.snackbar.setText(text);
        return this;
    }

    public SnackbarWrapper text(@StringRes int resId) {
        this.snackbar.setText(resId);
        return this;
    }

    public SnackbarWrapper callback(@Nullable final ActionCallback callback) {
        this.actionCallback=callback;
        return this;
    }

    public void show() {
        if(actionCallback!=null) {
            snackbar.setAction(actionCallback.message, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionCallback.onCancel();
                }
            });
            snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    switch (event) {
                        case DISMISS_EVENT_TIMEOUT:
                        case DISMISS_EVENT_SWIPE:
                            actionCallback.onPerform();
                            break;
                    }
                }
            });
        }
        snackbar.show();
    }

}
