package com.example.runningtracker.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.example.runningtracker.BR;
import com.example.runningtracker.model.entity.Run;
import com.example.runningtracker.model.repository.MyRepository;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;
import java.util.Objects;

public class StatisticsViewModel extends ObservableViewModel {

    private int runsCount;
    private int totalDistance ;
    private double averagePace;
    private int totalCalories;
    private int totalDuration;

    private final MutableLiveData<String> selectedSpinnerText = new MutableLiveData<>();
    private final LiveData<List<Run>> allRuns;

    // Constructor links repository
    public StatisticsViewModel(@NonNull Application application) {
        super(application);

        runsCount = 0;

        MyRepository myRepository = new MyRepository(application);
        allRuns = myRepository.getRuns();
    }

    // Calculate the total distance, pace, duration, activity and average pace for display.
    public void calculateRunsAverages() {
        totalDistance = 0;
        averagePace = 0;
        totalCalories = 0;
        totalDuration = 0;

        if (runsCount != 0) {
            List<Run> runList = allRuns.getValue();

            assert runList != null;
            for (Run run: runList) {
                totalDistance += run.getDistance();
                averagePace += run.getPace();
                totalCalories += run.getCalories();
                totalDuration += run.getDuration();
            }

            averagePace = averagePace / runsCount;

            // Notify for changes
            notifyPropertyChanged(BR.totalDistance);
            notifyPropertyChanged(BR.averagePace);
            notifyPropertyChanged(BR.totalCalories);
            notifyPropertyChanged(BR.totalDuration);
            notifyPropertyChanged(BR.runsCount);
        }
    }

    /*
    * Plot the graph based on spinner's value (Duration, Distance, Pace & Calories)
    * */
    public LineGraphSeries<DataPoint> plotOverallRunsGraph() {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        List<Run> runList = allRuns.getValue();

        if (runsCount != 0) {
            for (int i=0; i< runsCount; i++) {
                assert runList != null;
                Run run = runList.get(i);

                switch(Objects.requireNonNull(selectedSpinnerText.getValue())) {
                    // Show the summary of runs based on Duration
                    case "Duration":
                        series.appendData(new DataPoint(i, run.getDuration()), true, runsCount);
                        break;
                    // Show the summary of runs based on Distance
                    case "Distance":
                        series.appendData(new DataPoint(i, run.getDistance()), true, runsCount);
                        break;
                    // Show the summary of runs based on Pace
                    case "Pace":
                        series.appendData(new DataPoint(i, run.getPace()), true, runsCount);
                        break;
                    // Show the summary of runs based on Calories
                    case "Calories":
                        series.appendData(new DataPoint(i, run.getCalories()), true, runsCount);
                        break;
                }
            }
        }

        return series;
    }

    /* Getter and Setter */

    public MutableLiveData<String> getSelectedSpinnerText() {
        return selectedSpinnerText;
    }

    @Bindable
    public int getRunsCount() {
        return runsCount;
    }

    public void setRunsCount(int runsCount) {
        this.runsCount = runsCount;
    }

    @Bindable
    public int getTotalDistance() {
        return totalDistance;
    }

    @Bindable
    public double getAveragePace() {
        return averagePace;
    }

    @Bindable
    public int getTotalCalories() {
        return totalCalories;
    }

    @Bindable
    public int getTotalDuration() {
        return totalDuration;
    }

    /* Getter and Setter (Repository) */

    public LiveData<List<Run>> getAllRuns() {
        return allRuns;
    }
}
