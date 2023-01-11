package com.example.runningtracker.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import com.example.runningtracker.R;
import com.example.runningtracker.databinding.ActivityRunBinding;
import com.example.runningtracker.databinding.ActivityRunRecordBinding;
import com.example.runningtracker.model.RunRoomDatabase;
import com.example.runningtracker.model.entity.Run;
import com.example.runningtracker.viewmodel.RunRecordViewModel;
import com.example.runningtracker.viewmodel.RunViewModel;

public class RunRecordActivity extends AppCompatActivity {
    //TODO: If an EditText is being used, make sure to set its android:background to
    // @null so that TextInputLayout can set the proper background on it.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get viewModel and bind layout views to architecutre component
        ActivityRunRecordBinding activityRunRecordBinding = ActivityRunRecordBinding.inflate(LayoutInflater.from(this));
        RunRecordViewModel runRecordViewModel = new ViewModelProvider(this,
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.
                        getInstance(this.getApplication())).get(RunRecordViewModel.class);

        activityRunRecordBinding.setLifecycleOwner(this);

        setContentView(activityRunRecordBinding.getRoot());
        activityRunRecordBinding.setViewmodel(runRecordViewModel);

        // Get the run ID to display the information of the Run
        Intent intent = getIntent();
        runRecordViewModel.setRunID(intent.getStringExtra(RunActivity.KEY_RUNID));

        runRecordViewModel.getRun(runRecordViewModel.getRunID()).observe(this, run -> {
            if (run != null) {
                // Set the duration, distance, calories and pace
                activityRunRecordBinding.textViewShowRunDuration.setText(runRecordViewModel.formatTime(run.getDuration()));
                activityRunRecordBinding.textViewShowRunDistance.setText(runRecordViewModel.formatDistance(run.getDistance()));
                activityRunRecordBinding.textViewShowRunCalories.setText(String.valueOf(run.getCalories()));
                activityRunRecordBinding.textViewShowRunPace.setText(runRecordViewModel.formatPace(run.getPace()));
                activityRunRecordBinding.editTextRunName.setText(run.getName());
                activityRunRecordBinding.editTextRunNote.setText(run.getNote());
            }
        });
        // Set onClick Listener
    }
}