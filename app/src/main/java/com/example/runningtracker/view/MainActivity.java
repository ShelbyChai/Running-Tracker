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

    // Adapter for the recycler view to show all runs
    private RunAdapter adapter;
    // View model fetches runs Live Data
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create viewModel and bind layout views to architecutre component
        ActivityMainBinding activityMainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        mainViewModel = new ViewModelProvider((ViewModelStoreOwner) this,
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.
                        getInstance(this.getApplication())).get(MainViewModel.class);

        activityMainBinding.setLifecycleOwner(this);
        setContentView(activityMainBinding.getRoot());
        activityMainBinding.setViewmodel(mainViewModel);


        // Setup spinner adapter for the Sort By/Filter of recycler view's display
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.run_sort, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activityMainBinding.spinnerRunFilter.setAdapter(arrayAdapter);
        activityMainBinding.spinnerRunFilter.setOnItemSelectedListener(this);


        // Setup recycler view adapter to display all Runs. When a run is clicked,
        // pass the primary key of the run (runID) via intent to RunRecordActivity
        // in order to display the single run's record/information.
        adapter = new RunAdapter(this, run -> {
            Intent intent = new Intent(MainActivity.this, RunRecordActivity.class);
            intent.putExtra(RunActivity.KEY_RUNID, run.getRunID());
            startActivity(intent);
        });
        activityMainBinding.recyclerView.setAdapter(adapter);
        activityMainBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /*
    * Onclick: Only ask/check for Location Permission and GPS when the user starts
    * to interact with the tracker service that requires the permission.
    * */
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

    // Start Statistic Activity (Overall Summary performance activity)
    public void onClickStartStatisticActivity(View view) {
        Log.d("comp3018", "MainActivity startStatistic Activity");
        Intent statisticActivity = new Intent(MainActivity.this, StatisticsActivity.class);
        startActivity(statisticActivity);
    }

    /*
     * Check and prompt for location permission.
     * 1. Precise location access granted.
     * 2. If user never click "Deny & don't ask me again" before, display an alert dialog to
     * explain why it is needed and re-prompt for location permission.
     * 3. If user clicked "Deny & don't ask me again" before, display a toast message containing
     * the location permission path if the user want to enable it.
     * */
    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {

                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION, false);

                        // 1
                        if (fineLocationGranted != null && fineLocationGranted) {
                            Log.d("comp3018", "fine location granted!");
                            checkLocationSettings();

                            // 2
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
                            Log.d("comp3018", "coarse location granted!");

                        } else {
                            // 3
                            Log.d("comp3018", "no location granted!");

                            Toast.makeText(this, R.string.set_permissions_in_settings,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );

    /*
     * Check if location settings is on. (GPS setting)
     * 1. Location settings (GPS) are satisfied, launch the tracker service.
     * 2. Prompt the user to change location settings (Turn on GPS)
     * Location settings are not satisfied, show a dialog by calling startResolutionForResult().
     * and check the result in onActivityResult().
     * */
    private void checkLocationSettings() {
        LocationRequest locationRequest = new
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        // 1
        task.addOnSuccessListener(this, locationSettingsResponse -> {
            Log.d("comp3018", "location setting is on!");

            startRunActivity();
        });

        // 2
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

        // Start the Run Activity if the user enabled the location settings (GPS settings)
        if (requestCode == RESULT_CODE_LOCATION_SETTINGS) {
            Log.d("comp3018", "result code: " + resultCode);
            if (resultCode != 0) {
                startRunActivity();
            }
        }
    }

    /*
    * When a spinner item is selected, change the data of the recycler view.
    * Filter type contains (Recent, Distance, Pace & Calories)
    * */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String selectedFilter = adapterView.getItemAtPosition(i).toString();
        Toast.makeText(getApplicationContext(), "Sort runs by " + selectedFilter, Toast.LENGTH_SHORT).show();

        switch(selectedFilter) {
            // Sort the runs by most recent
            case "Recent":
                mainViewModel.getAllRecentRuns().observe(this, adapter::setData);
                break;
            // Sort the runs by long distance
            case "Distance":
                mainViewModel.getAllDistanceRuns().observe(this, adapter::setData);
                break;
            // Sort the runs by low pace
            case "Pace":
                mainViewModel.getAllPaceRuns().observe(this, adapter::setData);
                break;
            // Sort the runs by high calories burned
            case "Calories":
                mainViewModel.getAllCaloriesRuns().observe(this, adapter::setData);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
