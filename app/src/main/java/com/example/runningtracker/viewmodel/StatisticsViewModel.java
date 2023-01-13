package com.example.runningtracker.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.lifecycle.LiveData;

import com.example.runningtracker.BR;
import com.example.runningtracker.model.entity.Run;
import com.example.runningtracker.model.repository.MyRepository;

import java.util.List;

public class StatisticsViewModel extends ObservableViewModel{
    private int runsCount;
    private int totalDistance;
    private double averagePace;
    private int totalCalories;
    private int totalDuration;

    private final LiveData<List<Run>> allRecentRuns;

    public StatisticsViewModel(@NonNull Application application) {
        super(application);

        runsCount = 0;
        totalDistance = 0;
        averagePace = 0;
        totalCalories = 0;
        totalDuration = 0;

        MyRepository myRepository = new MyRepository(application);
        allRecentRuns = myRepository.getRecentRuns();
    }

    // Calculate the total distance, pace, duration, activity and average pace for statistics display.
    public void calculateRunsAverages() {
        if (runsCount != 0) {
            List<Run> runList = allRecentRuns.getValue();

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

    /* Getter and Setter */

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

    public LiveData<List<Run>> getAllRecentRuns() {
        return allRecentRuns;
    }
}
