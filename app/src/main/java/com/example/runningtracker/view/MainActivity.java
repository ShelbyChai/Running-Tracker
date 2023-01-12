package com.example.runningtracker.view;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.runningtracker.R;
import com.example.runningtracker.adapters.RunAdapter;
import com.example.runningtracker.databinding.ActivityMainBinding;
import com.example.runningtracker.viewmodel.MainViewModel;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    public static final int RESULT_CODE_LOCATION_SETTINGS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create viewModel and bind layout views to architecutre component
        ActivityMainBinding activityMainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        MainViewModel mainViewModel = new ViewModelProvider((ViewModelStoreOwner) this,
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.
                        getInstance(this.getApplication())).get(MainViewModel.class);

        activityMainBinding.setLifecycleOwner(this);

        setContentView(activityMainBinding.getRoot());
        activityMainBinding.setViewmodel(mainViewModel);


        // Setup spinner adapter for Run filter functionality
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.run_filter, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activityMainBinding.spinnerRunFilter.setAdapter(arrayAdapter);
        activityMainBinding.spinnerRunFilter.setOnItemSelectedListener(this);


        // Setup recycler view adapter to display Run
        final RunAdapter adapter = new RunAdapter(this, run -> {
            Intent intent = new Intent(MainActivity.this, RunRecordActivity.class);
            intent.putExtra(RunActivity.KEY_RUNID, run.getRunID());
            startActivity(intent);
        });
        activityMainBinding.recyclerView.setAdapter(adapter);
        activityMainBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Observe
        mainViewModel.getAllRuns().observe(this, adapter::setData);
    }

    public void onClickStartRunActivity(View view) {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET
        });
    }

    // Start Run Activity
    public void startRunActivity() {
        Log.d("comp3018", "MainActivity startRunActivity");
        Intent runActivity = new Intent(MainActivity.this, RunActivity.class);

        startActivity(runActivity);
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

            startRunActivity();
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
            // Start the Run Activity if the user enabled the location settings (GPS settings)
            case RESULT_CODE_LOCATION_SETTINGS:
                Log.d("comp3018", "result code: " + resultCode);
                if (resultCode != 0) {
                    startRunActivity();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String selectedFilter = adapterView.getItemAtPosition(i).toString();

        Toast.makeText(getApplicationContext(), selectedFilter, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
