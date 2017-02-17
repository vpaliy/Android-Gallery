package com.vpaliy.studioq.slider.screens.cases;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import com.vpaliy.studioq.model.MediaFile;
import com.yalantis.ucrop.UCrop;
import java.io.File;

import com.vpaliy.studioq.App;
import android.support.annotation.NonNull;

public class EditCase {

    @NonNull
    private MediaFile target;

    @NonNull
    private String destinationName;

    @NonNull
    private Activity activity;

    @SuppressWarnings("All")
    private  static EditCase instance;


    private EditCase(@NonNull MediaFile target, @NonNull Activity activity) {
        this.activity=activity;
        this.target=target;
        this.destinationName=target.mediaFile().getName()+"-edited";
    }

    public EditCase resultName(@NonNull String result) {
        this.destinationName=result;
        return this;
    }

    private UCrop.Options getUcropOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionQuality(90);
        options.setFreeStyleCropEnabled(true);

        return options;
    }

    public void execute() {
        UCrop.of(target.uri(), Uri.fromFile(new File(activity.getCacheDir(),destinationName)))
            .start(activity);
    }

    public static EditCase start(@NonNull Activity activity,@NonNull MediaFile target) {
        synchronized (EditCase.class) {
            if(instance==null) {
                instance=new EditCase(target,activity);
            }else {
                instance.activity=activity;
                instance.target=target;
            }
        }
        return instance;
    }

    public static void handleResult(@NonNull Intent data) {
        Uri image=UCrop.getOutput(data);
        if(image!=null) {
            App.appInstance().copy(instance.target.parentPath(),
                MediaFile.createFrom(image,instance.target));
        }
        instance=null;
    }

}
