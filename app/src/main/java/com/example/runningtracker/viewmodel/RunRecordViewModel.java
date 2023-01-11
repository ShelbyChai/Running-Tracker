package com.example.runningtracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.runningtracker.model.entity.Run;
import com.example.runningtracker.model.repository.MyRepository;

public class RunRecordViewModel extends ObservableViewModel {
    private Run runRecord;
    private String runID;

    /* Repository */
    private final MyRepository myRepository;

    public RunRecordViewModel(@NonNull Application application) {
        super(application);

        myRepository = new MyRepository(application);
    }

    /* Getter & Setter */

    public Run getRunRecord() {
        return runRecord;
    }

    public void setRunRecord(Run runRecord) {
        this.runRecord = runRecord;
    }

    public String getRunID() {
        return runID;
    }

    public void setRunID(String runID) {
        this.runID = runID;
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
