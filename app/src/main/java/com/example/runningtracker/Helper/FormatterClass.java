package com.example.runningtracker.Helper;

import android.annotation.SuppressLint;

public class FormatterClass {

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
