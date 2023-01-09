package com.example.runningtracker.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.lifecycle.MutableLiveData;

import com.example.runningtracker.BR;
import com.example.runningtracker.service.TrackerCallback;
import com.example.runningtracker.service.TrackerService;

public class RunViewModel extends ObservableViewModel {
    private TrackerService.MyBinder trackerBinder = null;
    private TrackerCallback trackerCallback;

    /* Bindable Object */
    private final MutableLiveData<Integer> totalTime = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalDistance = new MutableLiveData<>(0);
    private final MutableLiveData<Float> pace = new MutableLiveData<Float>((float) 0);

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("comp3018", "MainActivity onServiceConnected");
            trackerBinder = (TrackerService.MyBinder) service;
            trackerBinder.registerCallback(trackerCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("comp3018", "MainActivity onServiceDisconnected");
            trackerBinder.unregisterCallback(trackerCallback);
            trackerBinder = null;
        }
    };

    public RunViewModel(@NonNull Application application) {
        super(application);

        updateRunData();
    }

    private void updateRunData() {
        trackerCallback = new TrackerCallback() {
            private Location prevLocation = null;
            private int duration = 0;
            private int distance = 0;

            @Override
            public void runningTrackerLocationEvent(Location location, int serviceStatus) {
                // Reset previous location to null for Service Paused behaviour
                if (serviceStatus == TrackerService.SERVICE_PAUSED) {
                    prevLocation = null;
                }

                if (serviceStatus == TrackerService.SERVICE_RUNNING && prevLocation != null) {
                    // Increment duration
                    duration += 1;

                    // Update totalDistance
                    distance += Math.round(prevLocation.distanceTo(location));

                    // Calculate pace
//                    float kilometers = distance / 1000;
//                    float minutes = duration / 60;
//
//                    if (kilometers != 0) {
//                        pace.setValue(minutes / kilometers);
//                    }

                    totalTime.setValue(duration);
                    totalDistance.setValue(distance);

                    notifyPropertyChanged(BR.totalTime);
                    notifyPropertyChanged(BR.totalDistance);
//                    notifyPropertyChanged(BR.pace);
                }
                prevLocation = location;
            }
        };
    }

    /* Getter & Setter */
    public TrackerService.MyBinder getTrackerBinder() {
        return trackerBinder;
    }

    public void setTrackerBinder(TrackerService.MyBinder trackerBinder) {
        this.trackerBinder = trackerBinder;
    }

    @Bindable
    public MutableLiveData<Integer> getTotalDistance() {
        return totalDistance;
    }

    @Bindable
    public MutableLiveData<Integer> getTotalTime() {
        return totalTime;
    }

    @Bindable
    public MutableLiveData<Float> getPace() {
        return pace;
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public void setServiceConnection(ServiceConnection serviceConnection) {
        this.serviceConnection = serviceConnection;
    }
}
