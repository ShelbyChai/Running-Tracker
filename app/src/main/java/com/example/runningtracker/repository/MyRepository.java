package com.example.runningtracker.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.runningtracker.daos.RunDao;
import com.example.runningtracker.entity.Run;
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

    public LiveData<List<Run>> getRuns() {
        return allRuns;
    }

    public void insert(Run run) {
        RunRoomDatabase.databaseWriteExecutor.execute(() -> {
            runDao.insert(run);
        });
    }
}
