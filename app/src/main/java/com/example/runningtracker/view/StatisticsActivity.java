package com.example.runningtracker.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.runningtracker.R;
import com.example.runningtracker.databinding.ActivityStatisticsBinding;
import com.example.runningtracker.viewmodel.StatisticsViewModel;

public class StatisticsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private ActivityStatisticsBinding activityStatisticsBinding;
    private StatisticsViewModel statisticsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create viewModel and bind layout views to architecutre component
        activityStatisticsBinding = ActivityStatisticsBinding.inflate(LayoutInflater.from(this));
        statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        activityStatisticsBinding.setLifecycleOwner(this);
        setContentView(activityStatisticsBinding.getRoot());
        activityStatisticsBinding.setViewmodel(statisticsViewModel);


        // Set onClickListener for top app bar's navigation button, destroy this activity if clicked
        setSupportActionBar(activityStatisticsBinding.topAppBar);
        activityStatisticsBinding.topAppBar.setNavigationOnClickListener(view -> finish());


        // Observe runs LiveData to get all run objects
        statisticsViewModel.getAllRuns().observe(this, runs -> {
            if (runs != null) {
                // Set the number of run activities
                statisticsViewModel.setRunsCount(runs.size());
                statisticsViewModel.calculateRunsAverages();
            }
        });


        // Setup spinner adapter for Run filter functionality
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.graph_sort, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activityStatisticsBinding.spinnerGraph.setAdapter(arrayAdapter);
        activityStatisticsBinding.spinnerGraph.setOnItemSelectedListener(this);


        // Observe spinnerText value and change the summary graph view base on the selected choice
        statisticsViewModel.getSelectedSpinnerText().observe(this, string -> {
            activityStatisticsBinding.graphViewRuns.removeAllSeries();
            activityStatisticsBinding.graphViewRuns.addSeries(statisticsViewModel.plotOverallRunsGraph());
            activityStatisticsBinding.graphViewRuns.setTitle(string + " - Across all runs (Oldest to Recent)");
            activityStatisticsBinding.graphViewRuns.getGridLabelRenderer().setVerticalAxisTitle(string);
            activityStatisticsBinding.graphViewRuns.getGridLabelRenderer().setHorizontalAxisTitle("Runs");
            activityStatisticsBinding.graphViewRuns.getGridLabelRenderer().setNumHorizontalLabels(statisticsViewModel.getRunsCount());
            activityStatisticsBinding.graphViewRuns.getGridLabelRenderer().setHorizontalLabelsVisible(true);
        });
    }

    /*
     * When a spinner item is selected, change the Graph view based on the value of the filter.
     * Filter type contains (Distance, Duratioin, Pace & Calories)
     * */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String selectedFilter = adapterView.getItemAtPosition(i).toString();

        Toast.makeText(getApplicationContext(), "Show graph by " + selectedFilter, Toast.LENGTH_SHORT).show();
        // Set the currentSpinnerText
        statisticsViewModel.getSelectedSpinnerText().setValue(selectedFilter);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityStatisticsBinding.unbind();
    }
}