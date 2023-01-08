package com.example.runningtracker.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.example.runningtracker.R;
import com.example.runningtracker.databinding.ActivityMainBinding;
import com.example.runningtracker.databinding.ActivityRunBinding;
import com.example.runningtracker.service.TrackerService;
import com.example.runningtracker.viewmodel.MainViewModel;
import com.example.runningtracker.viewmodel.RunViewModel;

public class RunActivity extends AppCompatActivity {
    private RunViewModel runViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        // Create viewModel and bind layout views to architecutre component
        ActivityRunBinding activityRunBinding = ActivityRunBinding.inflate(LayoutInflater.from(this));
        runViewModel = new ViewModelProvider((ViewModelStoreOwner) this,
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.
                        getInstance(this.getApplication())).get(RunViewModel.class);

        setContentView(activityRunBinding.getRoot());
        activityRunBinding.setViewmodel(runViewModel);

        startTrackerService();
    }

    private void startTrackerService() {
        Intent trackerService = new Intent(RunActivity.this, TrackerService.class);
        bindService(trackerService, runViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
        startForegroundService(trackerService);
    }

    public void onClickResumeRunning(View view) {

    }

    public void onClickPauseRunning(View view) {
        if (runViewModel.getTrackerBinder() != null) runViewModel.getTrackerBinder().pauseRunning();
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

        unbindService(runViewModel.getServiceConnection());
        runViewModel.setServiceConnection(null);
        stopService(new Intent(RunActivity.this, TrackerService.class));
    }
}