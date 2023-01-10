package com.example.runningtracker.service;

import android.location.Location;

public interface TrackerCallback {
    void runningTrackerLocationEvent(Location location);
}
