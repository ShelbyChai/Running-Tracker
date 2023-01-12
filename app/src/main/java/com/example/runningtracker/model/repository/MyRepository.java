package com.example.runningtracker.model.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.runningtracker.model.daos.RunDao;
import com.example.runningtracker.model.entity.Run;
import com.example.runningtracker.model.RunRoomDatabase;

import java.util.List;

public class MyRepository {
    private final RunDao runDao;
    private final LiveData<List<Run>> allRuns;

    public MyRepository(Application application) {
        // Retrieve a reference to the database and the Dao
        RunRoomDatabase db = RunRoomDatabase.getDatabase(application);
        runDao = db.runDao();
        allRuns = runDao.getRuns();
    }

    public void insert(Run run) {
        RunRoomDatabase.databaseWriteExecutor.execute(() -> {
            runDao.insert(run);
        });
    }

    public void update(String runID, String runName, float runRating, String runNote, byte[] runPhoto) {
        RunRoomDatabase.databaseWriteExecutor.execute(() -> {
            runDao.update(runID, runName, runRating, runNote, runPhoto);
        });
    }

    public void delete(String runID) {
        RunRoomDatabase.databaseWriteExecutor.execute(() -> {
            runDao.delete(runID);
        });
    }

    public LiveData<List<Run>> getRuns() {
        return allRuns;
    }

    public LiveData<Run> getRun(String runID) {
        return runDao.getRun(runID);
    }
}
