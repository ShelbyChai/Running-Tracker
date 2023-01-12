package com.example.runningtracker.view;

import android.Manifest;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import com.example.runningtracker.service.TrackerService;
import com.example.runningtracker.viewmodel.RunViewModel;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class RunActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Intent Key name
    public final static String KEY_RUNID = "runID";

    // Key name to retrieve pause and resume buttons' visibility state for lifecycles
    private final String PAUSE_BUTTON_VISIBILITY = "Pause Visibility";
    private final String RESUME_BUTTON_VISIBILITY = "Resume Visibility";

    private RunViewModel runViewModel;
    private ActivityRunBinding activityRunBinding;

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


        // Set onClick Listener for pause, resume and stop buttons
        activityRunBinding.buttonPauseTracker.setOnClickListener(view -> {
            runViewModel.getTrackerBinder().pauseRunning();
            showResumeButton();
            Toast.makeText(this, "Run Activity paused", Toast.LENGTH_SHORT).show();
        });

        activityRunBinding.buttonResumeTracker.setOnClickListener(view -> {
            runViewModel.getTrackerBinder().startRunning();
            showPauseButton();
            Toast.makeText(this, "Run Activity resumed", Toast.LENGTH_SHORT).show();
        });

        activityRunBinding.buttonStopTracker.setOnClickListener(view -> {
            completeRun();

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

    // Enable the visibility of resume button
    public void showResumeButton() {
        activityRunBinding.buttonPauseTracker.setVisibility(View.GONE);
        activityRunBinding.buttonResumeTracker.setVisibility(View.VISIBLE);
    }

    // Enable the visibility of pause button
    public void showPauseButton() {
        activityRunBinding.buttonPauseTracker.setVisibility(View.VISIBLE);
        activityRunBinding.buttonResumeTracker.setVisibility(View.GONE);
    }

    // Finish this activity which brings the user back to main activity
    public void completeRun() {
        runViewModel.finishRun();
        finish();
    }

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String notificationIntent = intent.getAction();

            switch (notificationIntent) {
                case TrackerService.SERVICE_RUNNING:
                    Log.d("comp3018", "Notification resume pressed");
                    runViewModel.getTrackerBinder().startRunning();
                    showPauseButton();

                    break;
                case TrackerService.SERVICE_PAUSE:
                    Log.d("comp3018", "Notification pause pressed");
                    runViewModel.getTrackerBinder().pauseRunning();
                    showResumeButton();

                    break;
                case TrackerService.SERVICE_FINISH:
                    Log.d("comp3018", "Notification stop pressed");
                    completeRun();

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

    // Update content text of duration and distance of the notification
    private Notification updateNotificationContent() {
        runViewModel.getTrackerBinder().getNotificationBuilder().
                setContentTitle(getString(R.string.notification_title)).
                setContentText("Duration: " + runViewModel.formatTime(runViewModel.getTotalDuration().getValue())
                        + ", Distance: " + runViewModel.formatDistance(runViewModel.getTotalDistance().getValue()) + " km");

        return runViewModel.getTrackerBinder().getNotificationBuilder().build();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        runViewModel.setmMap(googleMap);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        runViewModel.getmMap().setMyLocationEnabled(true);
        runViewModel.getmMap().getUiSettings().setMyLocationButtonEnabled(true);
        runViewModel.getmMap().getUiSettings().setZoomGesturesEnabled(true);

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
            while (runViewModel.isRunning()) {
                runOnUiThread(() -> {
                    runViewModel.drawPolylineOnMap(latLngList);
                });
                SystemClock.sleep(1000);
            }
        }
    }

    /*
    * 1. Unbind the service
    * 2. Stop the foreground service, and unregister the notification Receiver
    * */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 1
        if (runViewModel.getTrackerBinder() != null) {
            unbindService(runViewModel.getServiceConnection());
            // 2
            if (isFinishing()) {
                runViewModel.setRunning(false);
                runViewModel.setServiceConnection(null);
                stopService(new Intent(RunActivity.this, TrackerService.class));
                runViewModel.setTrackerBinder(null);
                unregisterReceiver(notificationReceiver);
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

        outState.putInt(PAUSE_BUTTON_VISIBILITY, activityRunBinding.buttonPauseTracker.getVisibility());
        outState.putInt(RESUME_BUTTON_VISIBILITY, activityRunBinding.buttonResumeTracker.getVisibility());
    }

    // Restore the Pause & Resume buttons' visibility state
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        activityRunBinding.buttonPauseTracker.setVisibility(savedInstanceState.getInt(PAUSE_BUTTON_VISIBILITY));
        activityRunBinding.buttonResumeTracker.setVisibility(savedInstanceState.getInt(RESUME_BUTTON_VISIBILITY));
    }
}