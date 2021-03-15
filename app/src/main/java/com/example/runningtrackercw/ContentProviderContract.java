package com.example.runningtrackercw;

import android.net.Uri;


public class ContentProviderContract {

    public static final String AUTHORITY = "com.example.runningtrackercw.AppContentProvider";

    public static final Uri EXERCISES_URI = Uri.parse("content://"+AUTHORITY+"/exercises");
    public static final Uri ALL_URI = Uri.parse("content://"+AUTHORITY+"/");

    public static final String _ID = "_id";

    public static final String TIME = "time";
    public static final String DISTANCE = "distance";
    public static final String TAG = "tag";
    public static final String IMAGE = "image";
    public static final String IMAGE_INFO = "image_info";
    public static final String NOTES = "notes";
    public static final String DATE = "date";

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/AppContentProvider.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/AppContentProvider.data.text";
}