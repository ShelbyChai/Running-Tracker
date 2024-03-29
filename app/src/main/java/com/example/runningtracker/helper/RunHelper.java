package com.example.runningtracker.helper;

import android.annotation.SuppressLint;

/*
* This class contains static method to help with the formatting of
* duration and distance.
* */
public class RunHelper {

    @SuppressLint("DefaultLocale")
    public static String formatTime(int duration) {
        int seconds = (duration % 60);
        int minutes = (duration % 3600) / 60;
        int hours = (duration / 3600);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @SuppressLint("DefaultLocale")
    public static String formatDistance(int distance) {
        double kilometers = (double) distance / 1000;

        return String.format("%.3f", kilometers);
    }
}
