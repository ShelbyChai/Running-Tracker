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
import com.example.runningtracker.model.entity.Run;
import com.example.runningtracker.model.repository.MyRepository;
import com.example.runningtracker.service.TrackerCallback;
import com.example.runningtracker.service.TrackerService;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

public class RunViewModel extends ObservableViewModel {
    private TrackerService.MyBinder trackerBinder = null;
    private TrackerCallback trackerCallback;

    private GoogleMap mMap;
    private LatLng latLng;
    private boolean running;

    /* Bindable Object */
    private final MutableLiveData<Integer> totalDuration = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalDistance = new MutableLiveData<>(0);
    private final MutableLiveData<Double> totalPace = new MutableLiveData<>((double) 0);
    private final MutableLiveData<Integer> totalCalories = new MutableLiveData<>(0);

    /* Repository */
    private final MyRepository myRepository;

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

        myRepository = new MyRepository(application);
        running = true;

        updateRunData();
    }

    private void updateRunData() {
        trackerCallback = new TrackerCallback() {
            private Location prevLocation = null;
            private int duration = 0;
            private int distance = 0;
            private double pace = 0;
            private int calories = 0;

            @Override
            public void runningTrackerLocationEvent(Location location) {
                if (Objects.equals(trackerBinder.getServiceStatus(), TrackerService.SERVICE_PAUSE)) {
                    prevLocation = null;
                }

                if (Objects.equals(trackerBinder.getServiceStatus(), TrackerService.SERVICE_RUNNING)) {
                    // Set LatLng
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // Update distance (km)
                    if (prevLocation != null)
                        distance += Math.round(prevLocation.distanceTo(location));


                    // Increment duration (seconds)
                    duration += 1;

                    // Calculate pace (min/km)
                    double kilometers = ((double) distance / 1000);
                    double minutes = ((double) duration / 60);

                    if (kilometers != 0f) {
                        pace = minutes / kilometers;
                    }

                    // Calculate calories burned 60 cal per km
                    calories = (int) (kilometers * 60);

                    // Set the Observable value and notify for changes
                    totalDuration.setValue(duration);
                    totalDistance.setValue(distance);
                    totalPace.setValue(pace);
                    totalCalories.setValue(calories);

                    notifyPropertyChanged(BR.totalDuration);
                    notifyPropertyChanged(BR.totalDistance);
                    notifyPropertyChanged(BR.totalPace);
                    notifyPropertyChanged(BR.totalCalories);
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

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public void setServiceConnection(ServiceConnection serviceConnection) {
        this.serviceConnection = serviceConnection;
    }

    public GoogleMap getmMap() {
        return mMap;
    }

    public void setmMap(GoogleMap mMap) {
        this.mMap = mMap;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Bindable
    public MutableLiveData<Integer> getTotalDistance() {
        return totalDistance;
    }

    @Bindable
    public MutableLiveData<Integer> getTotalDuration() {
        return totalDuration;
    }

    @Bindable
    public MutableLiveData<Double> getTotalPace() {
        return totalPace;
    }

    @Bindable
    public MutableLiveData<Integer> getTotalCalories() {
        return totalCalories;
    }

    /* Getter & Setter (Repository) */

    public void insert(Run run) {
        myRepository.insert(run);
    }
}
