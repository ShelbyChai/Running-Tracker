package com.example.runningtracker.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.runningtracker.R;
import com.example.runningtracker.databinding.ActivityRunBinding;
import com.example.runningtracker.model.entity.Run;
import com.example.runningtracker.service.TrackerService;
import com.example.runningtracker.viewmodel.RunViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;

public class RunActivity extends AppCompatActivity implements OnMapReadyCallback{
    private RunViewModel runViewModel;
    private GoogleMap mMap;

    private MaterialButton mPauseButton;
    private MaterialButton mResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get viewModel and bind layout views to architecutre component
        runViewModel = new ViewModelProvider(this).get(RunViewModel.class);
        ActivityRunBinding activityRunBinding = ActivityRunBinding.inflate(LayoutInflater.from(this));
        setContentView(activityRunBinding.getRoot());
        activityRunBinding.setViewmodel(runViewModel);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Get Views
        mPauseButton = findViewById(R.id.pauseTracker);
        mResume = findViewById(R.id.resumeTracker);

        // Start the Tracker Service
        startTrackerService();
    }

    private void startTrackerService() {
        Intent trackerService = new Intent(RunActivity.this, TrackerService.class);
        bindService(trackerService, runViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
        startForegroundService(trackerService);
    }

    public void onClickPauseRunning(View view) {
        if (runViewModel.getTrackerBinder() != null) {
            Log.d("comp3018", "Run Activity pause pressed");
            runViewModel.getTrackerBinder().pauseRunning();

            mPauseButton.setVisibility(View.GONE);
            mResume.setVisibility(View.VISIBLE);
        }
    }

    public void onClickResumeRunning(View view) {
        Log.d("comp3018", "Run Activity resume pressed");
        runViewModel.getTrackerBinder().startRunning();

        mPauseButton.setVisibility(View.VISIBLE);
        mResume.setVisibility(View.GONE);
    }

    public void onClickStopRunning(View view) {
        if (runViewModel.getTrackerBinder() != null) {
            runViewModel.getTrackerBinder().stopRunning();

            runViewModel.insert(new Run(String.valueOf(Calendar.getInstance().getTime()),
                    runViewModel.getTotalDuration().getValue(),
                    runViewModel.getTotalDistance().getValue(),
                    runViewModel.getTotalPace().getValue(),
                    runViewModel.getTotalCalories().getValue()));

            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(runViewModel.getServiceConnection());
        runViewModel.setServiceConnection(null);
        stopService(new Intent(RunActivity.this, TrackerService.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

//        unbindService(runViewModel.getServiceConnection());
//        runViewModel.setServiceConnection(null);
//        stopService(new Intent(RunActivity.this, TrackerService.class));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
    }
}