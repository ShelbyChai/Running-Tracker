package com.example.runningtracker.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import com.example.runningtracker.databinding.ActivityStatisticsBinding;
import com.example.runningtracker.model.RunRoomDatabase;
import com.example.runningtracker.viewmodel.StatisticsViewModel;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create viewModel and bind layout views to architecutre component
        ActivityStatisticsBinding activityStatisticsBinding = ActivityStatisticsBinding.inflate(LayoutInflater.from(this));
        StatisticsViewModel statisticsViewModel = new ViewModelProvider((ViewModelStoreOwner) this,
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.
                        getInstance(this.getApplication())).get(StatisticsViewModel.class);
        activityStatisticsBinding.setLifecycleOwner(this);
        setContentView(activityStatisticsBinding.getRoot());
        activityStatisticsBinding.setViewmodel(statisticsViewModel);


        // Set onClickListener for top app bar
        setSupportActionBar(activityStatisticsBinding.topAppBar);
        activityStatisticsBinding.topAppBar.setNavigationOnClickListener(view -> {
            finish();
        });


        // Observe runs LiveData to get all run objects
        statisticsViewModel.getAllRecentRuns().observe(this, runs -> {
            if (runs != null) {
                // Set the number of run activities
                statisticsViewModel.setRunsCount(runs.size());
                statisticsViewModel.calculateRunsAverages();
            }
        });
    }
}