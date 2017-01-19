package com.vpaliy.studioq.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

public final class Permissions {

    private Permissions() {
        throw new UnsupportedOperationException();
    }

    public static boolean requestIfNotAllowed(Activity activity, String permission, String[] requestPermission, int requestCode) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if(activity.checkSelfPermission(permission)== PackageManager.PERMISSION_DENIED) {
                activity.requestPermissions(requestPermission, requestCode);
                return true;
            }
            return false;
        }
        return false;
    }

    public static boolean checkForVersion(int version) {
        return Build.VERSION.SDK_INT>=version;
    }
}
