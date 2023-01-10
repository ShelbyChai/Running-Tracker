package com.example.runningtracker.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "run_table")
public class Run {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "duration")
    private int duration;

    @ColumnInfo(name = "distance")
    private int distance;

    @ColumnInfo(name = "pace")
    private double pace;

    @ColumnInfo(name = "calories")
    private int calories;

//    @ColumnInfo(name = "note")
//    private double notes;
//
//    @ColumnInfo(name = "rating")
//    private double rating;
//
//    @ColumnInfo(name = "weather")
//    private double weather;


    public Run(@NonNull String name, int duration, int distance, double pace, int calories) {
        this.name = name;
        this.duration = duration;
        this.distance = distance;
        this.pace = pace;
        this.calories = calories;
    }

    /* Getters & Setters */
    @NonNull
    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public int getDistance() {
        return distance;
    }

    public double getPace() {
        return pace;
    }

    public int getCalories() {
        return calories;
    }

}
