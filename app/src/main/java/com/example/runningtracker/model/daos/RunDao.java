package com.example.runningtracker.model.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.runningtracker.model.entity.Run;

import java.util.List;

@Dao
public interface RunDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Run run);

    @Query("UPDATE run_table SET " +
            "name = :runName," +
            "rating = :runRating," +
            "note = :runNote " +
            "WHERE runID = :runID")
    void update(String runID, String runName, float runRating, String runNote);

    @Query("DELETE FROM run_table WHERE runID = :runID")
    void delete(String runID);

    @Query("SELECT * FROM run_table WHERE runID = :runID")
    LiveData<Run> getRun(String runID);

    @Query("SELECT * FROM run_table")
    LiveData<List<Run>> getRuns();
}
