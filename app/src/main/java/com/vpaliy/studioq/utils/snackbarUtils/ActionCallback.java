package com.vpaliy.studioq.utils.snackbarUtils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

public abstract class ActionCallback {

    @NonNull
    String message;

    protected ActionCallback(@NonNull String message) {
        this.message = message;
    }

    protected ActionCallback(@NonNull Context context, @StringRes int resourceId) {
        this.message=context.getResources().getString(resourceId);
    }

    public abstract void onCancel();
    public abstract void onPerform();

    public void onDismiss() {}

}
