package com.example.runningtracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.runningtracker.model.entity.Run;
import com.example.runningtracker.model.repository.MyRepository;

import java.util.List;

public class MainViewModel extends ObservableViewModel {
    private final MyRepository myRepository;
    private final LiveData<List<Run>> allRuns;

    public MainViewModel(@NonNull Application application) {
        super(application);

        myRepository = new MyRepository(application);
        allRuns = myRepository.getRuns();

    }

    public LiveData<List<Run>> getAllRuns() {
        return allRuns;
    }

    public void insert(Run run) {
        myRepository.insert(run);
    }
}
