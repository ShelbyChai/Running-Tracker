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
            "note = :runNote," +
            "photo = :photo " +
            "WHERE runID = :runID")
    void update(String runID, String runName, float runRating, String runNote, byte[] photo);

    @Query("DELETE FROM run_table WHERE runID = :runID")
    void delete(String runID);

    // Query the specific run record
    @Query("SELECT * FROM run_table WHERE runID = :runID")
    LiveData<Run> getRun(String runID);

    // General query of all the run records
    @Query("SELECT * FROM run_table")
    LiveData<List<Run>> getRuns();

    // Query run records by recent time
    @Query("SELECT * FROM run_table ORDER BY datetime(endDateTime) DESC")
    LiveData<List<Run>> getRecentRuns();

    // Query run records by longest distance
    @Query("SELECT * FROM run_table ORDER BY distance DESC")
    LiveData<List<Run>> getDistanceRuns();

    @Query("SELECT * FROM run_table ORDER BY pace ASC")
    LiveData<List<Run>> getPaceRuns();

    @Query("SELECT * FROM run_table ORDER BY calories DESC")
    LiveData<List<Run>> getCaloriesRuns();
}
