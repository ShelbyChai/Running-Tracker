package com.example.runningtracker.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class RunViewModel extends ObservableViewModel {
    /* Binder */
    private TrackerService.MyBinder trackerBinder = null;
    private TrackerCallback trackerCallback;

    /* Declare required variables */
    private GoogleMap mMap;
    private LatLng latLng;
    private boolean running;

    /* Bindable Object */
    // Declare the Mutable Live Data required for displaying on the Run Activity
    private final MutableLiveData<Integer> runDuration = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> runDistance = new MutableLiveData<>(0);
    private final MutableLiveData<Double> runPace = new MutableLiveData<>((double) 0);
    private final MutableLiveData<Integer> runCalories = new MutableLiveData<>(0);

    private final MyRepository myRepository;

    /*
     * Connection to get the service instance, callback management and handle disconnecting
     * */
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

    // Constructor links repository and start updating the UI via the tracker callback
    // after the viewModel is initialised
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
                    runDuration.setValue(duration);
                    runDistance.setValue(distance);
                    runPace.setValue(pace);
                    runCalories.setValue(calories);

                    notifyPropertyChanged(BR.runDuration);
                    notifyPropertyChanged(BR.runDistance);
                    notifyPropertyChanged(BR.runPace);
                    notifyPropertyChanged(BR.runCalories);
                }

                prevLocation = location;
            }
        };
    }

    /*
     * 1. Set thread running to false and stop the service.
     * 2. Save a snapshot of the Map to upload in the database and
     * Insert the new run data into the database with current date and time as UID.
     * */
    public void finishRun() {
        if (trackerBinder != null) {
            // 1
            running = false;
            trackerBinder.stopRunning();

            // 2
            mMap.snapshot(callback);
        }
    }

    GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
        // Set date format for end date time and display date time
        final SimpleDateFormat formatterName = new SimpleDateFormat("dd MMMM yyyy h:mm aa");
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final Date curDate = new Date(System.currentTimeMillis());
        Bitmap bitmap;
        byte[] mapByteArray;

        @Override
        public void onSnapshotReady(Bitmap snapshot) {
            bitmap = snapshot;
            String uniqueRunID = String.valueOf(Calendar.getInstance().getTime());

            String endTime = formatter.format(curDate);
            String dateTimeFormatted = formatterName.format(curDate);

            // Convert the Map snapshot into byte array for storing
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
                mapByteArray = stream.toByteArray();
                Log.d("comp3018", mapByteArray.length + "Length");

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Insert the new run record data
            insert(new Run(uniqueRunID,
                    "Run Activity",
                    endTime,
                    dateTimeFormatted,
                    runDuration.getValue(),
                    runDistance.getValue(),
                    runPace.getValue(),
                    runCalories.getValue(),
                    mapByteArray));
        }
    };

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

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Bindable
    public MutableLiveData<Integer> getRunDistance() {
        return runDistance;
    }

    @Bindable
    public MutableLiveData<Integer> getRunDuration() {
        return runDuration;
    }

    @Bindable
    public MutableLiveData<Double> getRunPace() {
        return runPace;
    }

    @Bindable
    public MutableLiveData<Integer> getRunCalories() {
        return runCalories;
    }

    /* Getter & Setter (Repository) */

    public void insert(Run run) {
        myRepository.insert(run);
    }
}
