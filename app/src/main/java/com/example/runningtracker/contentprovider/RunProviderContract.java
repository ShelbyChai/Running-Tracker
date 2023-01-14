package com.example.runningtracker.contentprovider;

import android.net.Uri;

public class RunProviderContract {
    public static final String AUTHORITY = "com.example.runningtracker.contentprovider.RunProvider";

    public static final Uri RUN_URI = Uri.parse("content://"+AUTHORITY+"/run");
    public static final Uri ALL_URI = Uri.parse("content://"+AUTHORITY+"/");

    public static final String RUN_ID = "runID";
    public static final String NAME = "name";
    public static final String DATE_TIME_FORMATTED = "dateTimeFormatted";
    public static final String END_DATE_TIME = "endDateTime";
    public static final String DURATION = "duration";
    public static final String DISTANCE = "distance";
    public static final String CALORIES = "calories";
    public static final String PACE = "pace";
    public static final String RATING = "rating";
    public static final String NOTE = "note";
    public static final String PHOTO = "photo";
    public static final String MAP_SNAPSHOT = "mapSnapshot";

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/RunProvider.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/RunProvider.data.text";
}
