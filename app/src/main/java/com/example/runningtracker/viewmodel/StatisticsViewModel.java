package com.example.runningtracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.runningtracker.model.entity.Run;
import com.example.runningtracker.model.repository.MyRepository;

import java.util.List;

public class StatisticsViewModel extends ObservableViewModel{
    LiveData<List<Run>> allRecentRuns;

    public StatisticsViewModel(@NonNull Application application) {
        super(application);

        MyRepository myRepository = new MyRepository(application);
        allRecentRuns = myRepository.getRecentRuns();
    }

    public LiveData<List<Run>> getAllRecentRuns() {
        return allRecentRuns;
    }
}
