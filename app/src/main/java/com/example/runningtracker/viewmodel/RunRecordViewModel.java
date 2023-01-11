package com.example.runningtracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.lifecycle.LiveData;

import com.example.runningtracker.model.entity.Run;
import com.example.runningtracker.model.repository.MyRepository;

public class RunRecordViewModel extends ObservableViewModel {
    private String runID;
    private LiveData<Run> currentRun;


    /* Repository */
    private final MyRepository myRepository;

    public RunRecordViewModel(@NonNull Application application) {
        super(application);

        myRepository = new MyRepository(application);
    }

    /* Getter & Setter */

    public String getRunID() {
        return runID;
    }

    public void setRunID(String runID) {
        this.runID = runID;
    }

    @Bindable
    public LiveData<Run> getCurrentRun() {
        return currentRun;
    }

    public void setCurrentRun(LiveData<Run> currentRun) {
        this.currentRun = currentRun;
    }

    /* Getter & Setter (Repository) */

    public void update(String runID, String runName, float runRating, String runNote) {
        myRepository.update(runID, runName, runRating, runNote);
    }

    public void delete(String runID) {
        myRepository.delete(runID);
    }

    public LiveData<Run> getRun(String runID) {
        return myRepository.getRun(runID);
    }
}
