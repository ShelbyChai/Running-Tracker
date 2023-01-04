package com.example.runningtracker.view;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.runningtracker.R;
import com.example.runningtracker.service.TrackerCallback;
import com.example.runningtracker.service.TrackerService;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    public static final int RESULT_CODE_LOCATION_SETTINGS = 2;

    private TrackerService.MyBinder trackerBinder = null;
    TrackerCallback trackerCallback;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trackerCallback = new TrackerCallback() {
            @Override
            public void runningTrackerLocationEvent(Location location) {
                //                    if (prevLocation != null) {
//                        distance = prevLocation.distanceTo(location);
//                        Log.d("comp3018", "Travel distance from last location: " + distance);
//                        Log.d("comp3018", "Travel speed: " + location.getSpeed());
//
//                    }
//                    prevLocation = location;
                Log.d("comp3018", "location " + location.toString());
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(serviceConnection);
        serviceConnection = null;
        stopService(new Intent(MainActivity.this, TrackerService.class));
    }

    public void onClickStartRunning(View view) {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    // Start the TrackerService
    public void startTrackerService() {
        Intent startTrackerService = new Intent(MainActivity.this, TrackerService.class);
        bindService(startTrackerService, serviceConnection, Context.BIND_AUTO_CREATE);
        getApplicationContext().startForegroundService(startTrackerService);
    }

    /*
     * Check if required permission is enabled.
     * */
    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {

                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION, false);

                        if (fineLocationGranted != null && fineLocationGranted) {
                            // Precise location access granted.
                            Log.d("comp3018", "fine location granted!");
                            checkLocationSettings();

                            // Display a dialog to re-prompt the location permission
                        } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION) &&
                                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                            Log.d("comp3018", "explanation required");

                            // AlertDialog building
                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                            alertDialog.setTitle("The GPS permission is off");
                            alertDialog.setMessage("Location permission is required for this app to track your runs");
                            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", (dialog, which) -> {
                            });
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) ->
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                                    Manifest.permission.ACCESS_FINE_LOCATION},
                                            REQUEST_CODE_LOCATION_PERMISSION
                                    ));
                            alertDialog.show();

                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            // Only approximate location access granted.
                            Log.d("comp3018", "coarse location granted!");

                        } else {
                            // No location access granted.
                            Log.d("comp3018", "no location granted!");

                            Toast.makeText(this, R.string.set_permissions_in_settings,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );

    /*
     * Check if location settings is on. (GPS setting)
     * */
    private void checkLocationSettings() {
        LocationRequest locationRequest = new
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        // Location settings are satisfied, launch the tracker service
        task.addOnSuccessListener(this, locationSettingsResponse -> {
            Log.d("comp3018", "location setting is on!");

            startTrackerService();
        });

        /*
         * Prompt the user to change location settings (Turn on GPS)
         * Location settings are not satisfied, show a dialog by calling startResolutionForResult().
         * and check the result in onActivityResult().
         * */
        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {

                try {
                    Log.d("comp3018", "location setting is off");
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MainActivity.this,
                            RESULT_CODE_LOCATION_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            // Launch the TrackerService if the user enabled the location settings (GPS settings)
            case RESULT_CODE_LOCATION_SETTINGS:
                Log.d("comp3018", "result code: " + resultCode);
                if (resultCode != 0) {
                    startTrackerService();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}

// When a feature in your app needs location access, wait until the user interacts with the feature before making the permission request. (Based on WorkManagerDemo, MartinBroadcast)
// TODO: ROBUST -> Request upgraded permission for best experience
// TODO: Callback return location information (Specific information like lat lon?)