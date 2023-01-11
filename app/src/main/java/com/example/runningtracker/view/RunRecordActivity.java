package com.example.runningtracker.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.runningtracker.R;
import com.example.runningtracker.databinding.ActivityRunRecordBinding;
import com.example.runningtracker.viewmodel.RunRecordViewModel;

import java.util.Objects;

public class RunRecordActivity extends AppCompatActivity{
    private RunRecordViewModel runRecordViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get viewModel and bind layout views to architecutre component
        ActivityRunRecordBinding activityRunRecordBinding = ActivityRunRecordBinding.inflate(LayoutInflater.from(this));

        runRecordViewModel = new ViewModelProvider(this,
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.
                        getInstance(this.getApplication())).get(RunRecordViewModel.class);

        activityRunRecordBinding.setLifecycleOwner(this);
        setContentView(activityRunRecordBinding.getRoot());
        activityRunRecordBinding.setViewmodel(runRecordViewModel);


        // Get run ID from intent to display the information of the Run
        Intent intent = getIntent();
        runRecordViewModel.setRunID(intent.getStringExtra(RunActivity.KEY_RUNID));
        // Assign the runData and display via DataBinding
        runRecordViewModel.setCurrentRun(runRecordViewModel.getRun(runRecordViewModel.getRunID()));

        // Button Save onClickListener
        activityRunRecordBinding.buttonSaveRunRecord.setOnClickListener(view -> {
            String runID = Objects.requireNonNull(runRecordViewModel.getCurrentRun().getValue()).getRunID();
            String runName = Objects.requireNonNull(activityRunRecordBinding.editTextRunName.getText()).toString();
            float runRating = activityRunRecordBinding.ratingBarRun.getRating();
            String runNote = Objects.requireNonNull(activityRunRecordBinding.editTextRunNote.getText()).toString();

            runRecordViewModel.update(runID, runName, runRating, runNote);
        });

        // Set onClickListener for top app bar
        setSupportActionBar(activityRunRecordBinding.topAppBar);
        activityRunRecordBinding.topAppBar.setNavigationOnClickListener(view -> {
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.top_app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.deleteRunActivity) {
            Toast.makeText(this, "Delete", Toast.LENGTH_SHORT).show();

            runRecordViewModel.delete(Objects.requireNonNull(runRecordViewModel.getCurrentRun().getValue()).getRunID());
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}