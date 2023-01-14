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
    private final LiveData<List<Run>> allRecentRuns;
    private final LiveData<List<Run>> allDistanceRuns;
    private final LiveData<List<Run>> allPaceRuns;
    private final LiveData<List<Run>> allCaloriesRuns;

    public MyRepository(Application application) {
        // Retrieve a reference to the database and the Dao
        RunRoomDatabase db = RunRoomDatabase.getDatabase(application);
        runDao = db.runDao();
        allRuns = runDao.getRuns();
        allRecentRuns = runDao.getRecentRuns();
        allDistanceRuns = runDao.getDistanceRuns();
        allPaceRuns = runDao.getPaceRuns();
        allCaloriesRuns = runDao.getCaloriesRuns();
    }

    public void insert(Run run) {
        RunRoomDatabase.databaseWriteExecutor.execute(() -> runDao.insert(run));
    }

    public void update(long runID, String runName, float runRating, String runNote, byte[] runPhoto) {
        RunRoomDatabase.databaseWriteExecutor.execute(() -> runDao.update(runID, runName, runRating, runNote, runPhoto));
    }

    public void delete(long runID) {
        RunRoomDatabase.databaseWriteExecutor.execute(() -> runDao.delete(runID));
    }

    public LiveData<Run> getRun(long runID) {
        return runDao.getRun(runID);
    }

    public LiveData<List<Run>> getRuns() {
        return allRuns;
    }

    public LiveData<List<Run>> getRecentRuns() {
        return allRecentRuns;
    }

    public LiveData<List<Run>> getDistanceRuns() {
        return allDistanceRuns;
    }

    public LiveData<List<Run>> getPaceRuns() {
        return allPaceRuns;
    }

    public LiveData<List<Run>> getCaloriesRuns() {
        return allCaloriesRuns;
    }
}
