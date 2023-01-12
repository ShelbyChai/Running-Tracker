package com.example.runningtracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.runningtracker.model.entity.Run;
import com.example.runningtracker.model.repository.MyRepository;

import java.util.List;

public class MainViewModel extends ObservableViewModel {
    private final MyRepository myRepository;
    private final LiveData<List<Run>> allRecentRuns;
    private final LiveData<List<Run>> allDistanceRuns;
    private final LiveData<List<Run>> allPaceRuns;
    private final LiveData<List<Run>> allCaloriesRuns;

    public MainViewModel(@NonNull Application application) {
        super(application);

        myRepository = new MyRepository(application);
        allRecentRuns = myRepository.getRecentRuns();
        allDistanceRuns = myRepository.getDistanceRuns();
        allPaceRuns = myRepository.getPaceRuns();
        allCaloriesRuns = myRepository.getCaloriesRuns();
    }

    public LiveData<List<Run>> getAllRecentRuns() {
        return allRecentRuns;
    }

    public LiveData<List<Run>> getAllDistanceRuns() {
        return allDistanceRuns;
    }

    public LiveData<List<Run>> getAllPaceRuns() {
        return allPaceRuns;
    }

    public LiveData<List<Run>> getAllCaloriesRuns() {
        return allCaloriesRuns;
    }
}
