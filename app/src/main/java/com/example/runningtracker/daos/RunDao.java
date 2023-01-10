package com.example.runningtracker.daos;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.runningtracker.entity.Run;

import java.util.List;

@Dao
public interface RunDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Run run);

    @Query("SELECT * FROM run_table")
    Cursor getAllRun();

    @Query("SELECT * FROM run_table")
    LiveData<List<Run>> getRuns();

    @Query("DELETE FROM run_table")
    void deleteAll();
}
