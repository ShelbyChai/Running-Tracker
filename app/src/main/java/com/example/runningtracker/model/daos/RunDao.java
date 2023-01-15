package com.example.runningtracker.model.daos;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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
            "WHERE run_ID = :runID")
    void update(long runID, String runName, float runRating, String runNote, byte[] photo);

    @Query("DELETE FROM run_table WHERE run_ID = :runID")
    void delete(long runID);

    // Query the specific run record
    @Query("SELECT * FROM run_table WHERE run_ID = :runID")
    LiveData<Run> getRun(long runID);

    // General query of all the run records
    @Query("SELECT * FROM run_table")
    LiveData<List<Run>> getRuns();

    // Query run records by recent time
    @Query("SELECT * FROM run_table ORDER BY datetime(endDateTime) DESC")
    LiveData<List<Run>> getRecentRuns();

    // Query run records by longest distance first
    @Query("SELECT * FROM run_table ORDER BY distance DESC")
    LiveData<List<Run>> getDistanceRuns();

    // Query run records by lowest pace first
    @Query("SELECT * FROM run_table ORDER BY pace ASC")
    LiveData<List<Run>> getPaceRuns();

    // Query run records by highest calories burned
    @Query("SELECT * FROM run_table ORDER BY calories DESC")
    LiveData<List<Run>> getCaloriesRuns();

    /* Cursor query for content provider */

    // Query for single record
    @Query("SELECT * FROM run_table WHERE run_ID = :run_ID")
    Cursor getCursorSelectedRun(long run_ID);

    // Query for entire table
    @Query("SELECT * FROM run_table")
    Cursor getCursorRuns();

    // Insert one Run record
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertCPRun(Run run);

    // Delete one Run record
    @Query("DELETE FROM run_table WHERE run_ID = :runID")
    int deleteCPRun(int runID);

    // Delete all Run record
    @Query("DELETE FROM run_table")
    int deleteCPAllRuns();

    // Update one Run record
    @Update(entity = Run.class)
    int updateCPRun(Run run);
}
