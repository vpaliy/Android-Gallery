package com.vpaliy.studioq.slider.utils.detailsProvider;

import android.graphics.BitmapFactory;
import android.media.MediaDataSource;
import android.util.Log;
import android.view.animation.TranslateAnimation;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.xmp.XmpDirectory;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

class MetaData {

    private static Set<Class<?>> usefulDirectories = new HashSet<Class<?>>();

    private static final String TAG=MetaData.class.getSimpleName();


    static {
        usefulDirectories.add(ExifIFD0Directory.class);
        usefulDirectories.add(ExifSubIFDDirectory.class);
        usefulDirectories.add(GpsDirectory.class);
        usefulDirectories.add(XmpDirectory.class);

    }

    private int width = -1;
    private int height = -1;
    private String make = null;
    private String model = null;
    private String fNumber = null;
    private String iso = null;
    private String exposureTime = null;
    private Date dateOriginal = null;
    private GeoLocation location = null;

    public  MetaData(File file) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        setWidth(options.outWidth);
        setHeight(options.outHeight);

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            for(Directory directory : metadata.getDirectories()) {
                if (usefulDirectories.contains(directory.getClass())) {
                    if (directory.getClass().equals(ExifSubIFDDirectory.class) || directory.getClass().equals(ExifIFD0Directory.class)) {
                        ExifDirectoryBase d = (ExifDirectoryBase) directory;

                        if (d.containsTag(ExifDirectoryBase.TAG_MAKE))
                            setMake(d.getString(ExifDirectoryBase.TAG_MAKE));
                        if (d.containsTag(ExifDirectoryBase.TAG_MODEL))
                            setModel(d.getString(ExifDirectoryBase.TAG_MODEL));

                        if (d.containsTag(ExifDirectoryBase.TAG_ISO_EQUIVALENT))
                            setIso(d.getString(ExifDirectoryBase.TAG_ISO_EQUIVALENT));
                        if (d.containsTag(ExifDirectoryBase.TAG_EXPOSURE_TIME) && d.getRational(ExifDirectoryBase.TAG_EXPOSURE_TIME) != null)
                            setExposureTime(new DecimalFormat("0.000").format(d.getRational(ExifDirectoryBase.TAG_EXPOSURE_TIME)));
                        if (d.containsTag(ExifDirectoryBase.TAG_FNUMBER))
                            setfNumber(d.getString(ExifDirectoryBase.TAG_FNUMBER));

                        if (d.containsTag(ExifDirectoryBase.TAG_DATETIME_ORIGINAL))
                            setDateOriginal(d.getDate(ExifDirectoryBase.TAG_DATETIME_ORIGINAL));

                    } else if (directory.getClass().equals(ExifSubIFDDirectory.class)) {
                        setDateOriginal(((ExifSubIFDDirectory) directory).getDateOriginal(TimeZone.getDefault()));
                    } else if (directory.getClass().equals(XmpDirectory.class)) {
                        XmpDirectory d = (XmpDirectory) directory;

                        if (d.containsTag(XmpDirectory.TAG_DATETIME_ORIGINAL))
                            setDateOriginal(d.getDate(XmpDirectory.TAG_DATETIME_ORIGINAL));

                        if (d.containsTag(XmpDirectory.TAG_MAKE))
                            setMake(d.getString(XmpDirectory.TAG_MAKE));
                        if (d.containsTag(XmpDirectory.TAG_MODEL))
                            setModel(d.getString(XmpDirectory.TAG_MODEL));

                        if (d.containsTag(XmpDirectory.TAG_F_NUMBER))
                            setfNumber(d.getString(XmpDirectory.TAG_F_NUMBER));
                    }
                    else if (directory.getClass().equals(GpsDirectory.class)) {
                        GpsDirectory d = (GpsDirectory) directory;
                        setLocation(d.getGeoLocation());
                    }
                }
            }

        } catch (Exception e) {
            Log.wtf(TAG,e.toString());
        }
    }

    Date getDateOriginal() {
        return dateOriginal;
    }

    private void setDateOriginal(Date dateOriginal) {
        this.dateOriginal = dateOriginal;
    }
    public GeoLocation getLocation() {
        return location;
    }

    public void setLocation(GeoLocation location) {
        this.location = location;
    }

    private int getWidth() {
        return width;
    }

    private void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public String getResolution() {
        if(width !=-1 && height != -1)
            return String.format("%dx%d", getWidth(), getHeight());
        return null;
    }

    String getCameraInfo() {

        if (make != null && model != null) {
            if (model.contains(make)) return model;
            return String.format("%s %s", make, model);
        }
        return null;
    }

    String getExifInfo() {
        StringBuilder result = new StringBuilder();
        String asd;
        if((asd = getfNumber()) != null) result.append(asd).append(" ");
        if((asd = getExposureTime()) != null) result.append(asd).append(" ");
        if((asd = getIso()) != null) result.append(asd).append(" ");
        return result.length() == 0 ? null : result.toString();
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    private void setModel(String model) {
        this.model = model;
    }

    private String getfNumber() {
        if(fNumber != null)
            return String.format("f/%s", fNumber);
        return null;
    }

    private void setfNumber(String fNumber) {
        this.fNumber = fNumber;
    }

    private String getIso() {
        if(iso != null)
            return String.format("ISO-%s", iso);
        return null;
    }

    private void setIso(String iso) {
        this.iso = iso;
    }

    private String getExposureTime() {
        if(exposureTime != null)
            return String.format("%ss", exposureTime);
        return null;
    }

    private void setExposureTime(String exposureTime) {
        this.exposureTime = exposureTime;
    }
}
