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
import androidx.lifecycle.SavedStateHandle;

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
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class RunViewModel extends ObservableViewModel {
    // Declare saveStateHandle to persist the data
    private final SavedStateHandle savedStateHandle;

    /* Binder */
    private TrackerService.MyBinder trackerBinder = null;
    private TrackerCallback trackerCallback;

    /*
    * Declare Google Map associated variables
    * latLng (pass to google map for update)
    * isRunning (stop the google map update if set to false)
    * prevLocation (stores the previous callback's location)
    * */
    private GoogleMap mMap;
    private LatLng latLng;
    private boolean isMapRunning;
    private Location prevLocation;

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
    public RunViewModel(@NonNull Application application, SavedStateHandle savedStateHandle) {
        super(application);
        this.savedStateHandle = savedStateHandle;

        myRepository = new MyRepository(application);
        isMapRunning = true;

        if (this.savedStateHandle.contains("latLng")) {
            latLng = this.savedStateHandle.get("latLng");
        }

        if (this.savedStateHandle.contains("isRunning")) {
            isMapRunning = Boolean.TRUE.equals(this.savedStateHandle.get("isRunning"));
        }

        updateRunData();
    }

    /*
     * 1. Set the latitude and longitude of the current location for map usage.
     * 2. Update the current distance vairable.
     * 3. Assigned the duration based on the timer from the tracker service.
     * 4. Calculate the current pace using the distance / duration.
     * 5. Calculate the total calories (60 cal per hour).
     * 6. Set the total distance, duration, calories and pace and notify observer for changes.
     *
     * The timer will only increments when the GPS is turned on else the distance variable
     * aren't updating which will affect the statistic of the run.
     * */
    private void updateRunData() {
        trackerCallback = new TrackerCallback() {
            private int distance = 0;
            private double pace = 0;

            @Override
            public void runningTrackerLocationEvent(Location location, int timer) {
                if (trackerBinder != null) {
                    if (trackerBinder.getServiceStatus() != null) {
                        if (Objects.equals(trackerBinder.getServiceStatus(), TrackerService.SERVICE_PAUSE)) {
                            prevLocation = null;
                        }

                        if (Objects.equals(trackerBinder.getServiceStatus(), TrackerService.SERVICE_RUNNING)) {
                            // 1
                            setLatLng(new LatLng(location.getLatitude(), location.getLongitude()));

                            // 2
                            if (prevLocation != null) {
                                distance += Math.round(prevLocation.distanceTo(location));
                            }

                            // 3

                            // 4
                            double kilometers = ((double) distance / 1000);
                            double minutes = ((double) timer / 60);

                            if (kilometers != 0f) {
                                pace = minutes / kilometers;
                            }

                            // 5
                            int calories = (int) (kilometers * 60);

                            // 6
                            runDuration.postValue(timer);
                            runDistance.postValue(distance);
                            runPace.postValue(pace);
                            runCalories.postValue(calories);

                            notifyPropertyChanged(BR.runDuration);
                            notifyPropertyChanged(BR.runDistance);
                            notifyPropertyChanged(BR.runPace);
                            notifyPropertyChanged(BR.runCalories);
                        }

                        prevLocation = location;
                    }
                }
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
            isMapRunning = false;
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
            insert(new Run("Run Activity",
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
        if (trackerBinder != null) {
            if (latLng != null && trackerBinder.getServiceStatus() != null) {
                String statusService = trackerBinder.getServiceStatus();
                latLngList.add(latLng);

                // 1
                if (Objects.equals(statusService, TrackerService.SERVICE_RUNNING)) {
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

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
        this.savedStateHandle.set("latLng", latLng);
    }

    public boolean isMapRunning() {
        return isMapRunning;
    }

    public void setMapRunning(boolean mapRunning) {
        this.isMapRunning = mapRunning;
        this.savedStateHandle.set("isRunning", isMapRunning);
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
