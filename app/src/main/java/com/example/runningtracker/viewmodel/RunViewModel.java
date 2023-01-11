package com.example.runningtracker.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Color;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class RunViewModel extends ObservableViewModel {
    /* Binder */
    private TrackerService.MyBinder trackerBinder = null;
    private TrackerCallback trackerCallback;

    /* Instantiate required variables */
    private GoogleMap mMap;
    private LatLng latLng;
    private boolean running;
    private String uniqueRunID ;

    /* Bindable Object */
    private final MutableLiveData<Integer> totalDuration = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalDistance = new MutableLiveData<>(0);
    private final MutableLiveData<Double> totalPace = new MutableLiveData<>((double) 0);
    private final MutableLiveData<Integer> totalCalories = new MutableLiveData<>(0);

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

    /*
    * 1. Set the latitude and longitude of the current location for map usage.
    * 2. Update the current distance vairable.
    * 3. Increment the duration variable.
    * 4. Calculate the current pace using the distance / duration.
    * 5. Calculate the total calories (60 cal per hour).
    * 6. Set the total distance, duration, calories and pace and notify observer for changes.
    * */
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
                    // 1
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // 2
                    if (prevLocation != null)
                        distance += Math.round(prevLocation.distanceTo(location));

                    // 3
                    duration += 1;

                    // 4
                    double kilometers = ((double) distance / 1000);
                    double minutes = ((double) duration / 60);

                    if (kilometers != 0f) {
                        pace = minutes / kilometers;
                    }

                    // 5
                    calories = (int) (kilometers * 60);

                    // 6
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

    /*
    * 1. Set thread running to false and stop the service.
    * 2. Insert the new run data into the database with current date and time as UID.
    * */
    public void finishRun() {
        if (trackerBinder != null) {
            // 1
            running = false;
            trackerBinder.stopRunning();

            // 2
            uniqueRunID = String.valueOf(Calendar.getInstance().getTime());
            insert(new Run(uniqueRunID,
                    "Activity",
                    uniqueRunID,
                    totalDuration.getValue(),
                    totalDistance.getValue(),
                    totalPace.getValue(),
                    totalCalories.getValue()));
        }
    }

    /*
    * 1. Draw polyline route on the map if service is running.
    * 2. Clear the LatLng List and not draw on the map if service is pause.
    * */
    public void drawPolylineOnMap(List<LatLng> latLngList) {

        if (latLng != null && trackerBinder.getServiceStatus() != null) {
            String statusService = trackerBinder.getServiceStatus();
            latLngList.add(latLng);

            // 1
            if (Objects.equals(statusService, TrackerService.SERVICE_RUNNING)) {
                Log.d("comp3018", "Map running");

                PolylineOptions options = new PolylineOptions().color(Color.RED).width(10).addAll(latLngList);
                mMap.addPolyline(options);
                mMap.addMarker(new MarkerOptions().position(latLngList.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                // 2
            } else if (Objects.equals(statusService, TrackerService.SERVICE_PAUSE)) {
                Log.d("comp3018", "Map paused");

                latLngList.clear();
            }
        }
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

    public String getUniqueRunID() {
        return uniqueRunID;
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
