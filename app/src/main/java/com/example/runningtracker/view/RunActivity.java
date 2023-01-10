package com.example.runningtracker.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.runningtracker.R;
import com.example.runningtracker.databinding.ActivityRunBinding;
import com.example.runningtracker.service.TrackerService;
import com.example.runningtracker.viewmodel.RunViewModel;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

public class RunActivity extends AppCompatActivity implements OnMapReadyCallback{
    private RunViewModel runViewModel;

    private MaterialButton mPauseButton;
    private MaterialButton mResume;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        // Get viewModel and bind layout views to architecutre component
        ActivityRunBinding activityRunBinding = ActivityRunBinding.inflate(LayoutInflater.from(this));
        runViewModel = new ViewModelProvider((ViewModelStoreOwner) this,
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.
                        getInstance(this.getApplication())).get(RunViewModel.class);

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
        if (runViewModel.getTrackerBinder() != null) runViewModel.getTrackerBinder().stopRunning();
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}