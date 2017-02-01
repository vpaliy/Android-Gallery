package com.vpaliy.studioq.utils;

public final class ProjectUtils {

    private ProjectUtils() {
        throw new UnsupportedOperationException();
    }

    public static final int IMAGE_INSTANCE=0x000;

    public static final int VIDEO_INSTANCE=0x001;

    public static final int ACCESS_TO_EXTERNAL_STORAGE=0x003;

    public static final int LAUNCH_GALLERY=0x004;

    public static final int LAUNCH_SLIDER=0x005;

    public static final int CREATE_MEDIA_FOLDER=0x0010;

    public static final String INIT="init";

    public static final String MEDIA_DATA="mediaData";

    public static final String BITMAP="bitmap";

    public static final String GALLERY_FRAGMENT="galleryFragment";

    public static final String SLIDER_FRAGMENT="sliderFragment";

    public static final String POSITION="position";

    public static final String MODE="MODE";

    public static final  String SELECTION_FRAGMENT="selectionFragment";

    public static final  String REVIEW_FRAGMENT="reviewFragment";

    public static final  String MEDIA_TITLE="mediaTitle";

    public static final  String MOVE_FILE_TO="moveFileTo";

    public static final String DELETED="deleted";

    public static final String ALL_MEDIA="allMedia";

    public static final String MEDIA_FOLDER="mediaFolder";

    public static final String TRANSITION_NAME="shared";

    public static final String ORIGINAL_MEDIA_LIST="ORIGINAL_MEDIA_LIST";

    public static String TRANSITION_NAME(int position) {
        return "data:" + Integer.toString(position);
    }






}
