package com.example.runningtracker.view;

import android.Manifest;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.runningtracker.R;
import com.example.runningtracker.databinding.ActivityRunBinding;
import com.example.runningtracker.model.entity.Run;
import com.example.runningtracker.service.TrackerService;
import com.example.runningtracker.viewmodel.RunViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RunActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Intent Key name
    public final static String KEY_RUNID = "runID";

    // Key name to retrieve pause and resume buttons' visibility state for lifecycles
    private final String PAUSE_BUTTON_VISIBILITY = "Pause Visibility";
    private final String RESUME_BUTTON_VISIBILITY = "Resume Visibility";

    private GoogleMap mMap;
    private RunViewModel runViewModel;
    private ActivityRunBinding activityRunBinding;
    private boolean running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get viewModel and bind layout views to architecutre component
        activityRunBinding = ActivityRunBinding.inflate(LayoutInflater.from(this));
        runViewModel = new ViewModelProvider(this,
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.
                        getInstance(this.getApplication())).get(RunViewModel.class);

        activityRunBinding.setLifecycleOwner(this);

        setContentView(activityRunBinding.getRoot());
        activityRunBinding.setViewmodel(runViewModel);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        // Buttons set onClick Listener
        activityRunBinding.pauseTracker.setOnClickListener(view -> {
            pauseRun();
            Toast.makeText(this, "Run Activity paused", Toast.LENGTH_SHORT).show();
        });
        activityRunBinding.resumeTracker.setOnClickListener(view -> {
            resumeRun();
            Toast.makeText(this, "Run Activity resumed", Toast.LENGTH_SHORT).show();
        });
        activityRunBinding.stopTracker.setOnClickListener(view -> {
            finishRun();
            Toast.makeText(this, "Run Activity finished", Toast.LENGTH_SHORT).show();
        });


        // Register Broadcast Receiver for notification buttons and content update
        IntentFilter notificationFilter = new IntentFilter();
        notificationFilter.addAction(TrackerService.SERVICE_RUNNING);
        notificationFilter.addAction(TrackerService.SERVICE_PAUSE);
        notificationFilter.addAction(TrackerService.SERVICE_FINISH);
        notificationFilter.addAction(TrackerService.NOTIFICATION_CONTENT_UPDATE);
        registerReceiver(notificationReceiver, notificationFilter);


        // Start the Tracker Service
        Intent trackerService = new Intent(this, TrackerService.class);
        bindService(trackerService, runViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
        startForegroundService(trackerService);
    }

    public void pauseRun() {
        if (runViewModel.getTrackerBinder() != null) {
            Log.d("comp3018", "Run Activity pause pressed");
            runViewModel.getTrackerBinder().pauseRunning();

            // Enable the visibility of resume button
            activityRunBinding.pauseTracker.setVisibility(View.GONE);
            activityRunBinding.resumeTracker.setVisibility(View.VISIBLE);
        }
    }

    public void resumeRun() {
        Log.d("comp3018", "Run Activity resume pressed");
        if (runViewModel.getTrackerBinder() != null) {
            runViewModel.getTrackerBinder().startRunning();

            // Enable the visibility of pause button
            activityRunBinding.pauseTracker.setVisibility(View.VISIBLE);
            activityRunBinding.resumeTracker.setVisibility(View.GONE);
        }
    }

    public void finishRun() {
        if (runViewModel.getTrackerBinder() != null) {
            running = false;
            runViewModel.getTrackerBinder().stopRunning();

            String uniqueRunID = UUID.randomUUID().toString();

            runViewModel.insert(new Run(uniqueRunID,
                    String.valueOf(Calendar.getInstance().getTime()),
                    runViewModel.getTotalDuration().getValue(),
                    runViewModel.getTotalDistance().getValue(),
                    runViewModel.getTotalPace().getValue(),
                    runViewModel.getTotalCalories().getValue()));

            Log.d("comp3018", "RunID in RunActivity: " + uniqueRunID);

            Intent runRecordActivity = new Intent(this, RunRecordActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(KEY_RUNID, uniqueRunID);
            runRecordActivity.putExtras(bundle);

            startActivity(runRecordActivity);
            finish();
        }
    }

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String notificationIntent = intent.getAction();

            switch (notificationIntent) {
                case TrackerService.SERVICE_RUNNING:
                    Log.d("comp3018", "Notification resume pressed");
                    resumeRun();

                    break;
                case TrackerService.SERVICE_PAUSE:
                    Log.d("comp3018", "Notification pause pressed");
                    pauseRun();

                    break;
                case TrackerService.SERVICE_FINISH:
                    Log.d("comp3018", "Notification stop pressed");
                    finishRun();

                    break;
                case TrackerService.NOTIFICATION_CONTENT_UPDATE:
                    Log.d("comp3018", "Notification content text updated");
                    runViewModel.getTrackerBinder().getNotificationManager().notify(TrackerService.NOTIFICATION_ID,
                            updateNotificationContent());

                    break;
                default:
                    break;
            }
        }
    };

    // Update duration and distance on content text
    private Notification updateNotificationContent() {
        runViewModel.getTrackerBinder().getNotificationBuilder().
                setContentTitle(getString(R.string.notification_title)).
                setContentText("Duration: " + runViewModel.formatTime(runViewModel.getTotalDuration().getValue())
                        + ", Distance: " + runViewModel.formatDistance(runViewModel.getTotalDistance().getValue()) + " km");

        return runViewModel.getTrackerBinder().getNotificationBuilder().build();
    }

    // TODO: Create a thread for this location update
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        // Start thread to update Map
        new MapUpdate().start();
    }

    /*
     * Thread class to update and draw on Map.
     * */
    private class MapUpdate extends Thread implements Runnable {
        // LatLng List for drawing a polyline
        List<LatLng> latLngList = new ArrayList<LatLng>();

        @Override
        public void run() {
            while (running) {
                runOnUiThread(() -> {
                    LatLng latLng = runViewModel.getLatLng();

                    if (latLng != null && runViewModel.getTrackerBinder().getServiceStatus() != null) {
                        String statusService = runViewModel.getTrackerBinder().getServiceStatus();
                        latLngList.add(latLng);

                        // Draw polyline route on the map if service is running
                        if (Objects.equals(statusService, TrackerService.SERVICE_RUNNING)) {
                            Log.d("comp3018", "Map running");

                            PolylineOptions options = new PolylineOptions().color(Color.RED).width(10).addAll(latLngList);
                            mMap.addPolyline(options);
                            mMap.addMarker(new MarkerOptions().position(latLngList.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                            // Clear the LatLng List and not draw on the map if service is pause
                        } else if (Objects.equals(statusService, TrackerService.SERVICE_PAUSE)) {
                            Log.d("comp3018", "Map paused");

                            latLngList.clear();
                        }
                    }
                });
                SystemClock.sleep(1000);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (runViewModel.getTrackerBinder() != null) {
            unbindService(runViewModel.getServiceConnection());
            if (isFinishing()) {
                running = false;
                runViewModel.setServiceConnection(null);
                stopService(new Intent(RunActivity.this, TrackerService.class));
            }
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        Log.d("comp3018", "RunActivity onBackPressed");
    }

    // Store the Pause & Resume buttons' visibility state
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(PAUSE_BUTTON_VISIBILITY, activityRunBinding.pauseTracker.getVisibility());
        outState.putInt(RESUME_BUTTON_VISIBILITY, activityRunBinding.resumeTracker.getVisibility());
    }

    // Restore the Pause & Resume buttons' visibility state
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        activityRunBinding.pauseTracker.setVisibility(savedInstanceState.getInt(PAUSE_BUTTON_VISIBILITY));
        activityRunBinding.resumeTracker.setVisibility(savedInstanceState.getInt(RESUME_BUTTON_VISIBILITY));
    }
}