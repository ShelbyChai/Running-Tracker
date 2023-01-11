package com.example.runningtracker.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "run_table")
public class Run {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "runID")
    private String runID;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] mapSnapshot;

    @ColumnInfo(name = "duration")
    private int duration;

    @ColumnInfo(name = "distance")
    private int distance;

    @ColumnInfo(name = "calories")
    private int calories;

    @ColumnInfo(name = "pace")
    private double pace;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "rating")
    private float rating;

    @ColumnInfo(name = "note")
    private String note;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] photo;

    @ColumnInfo(name = "endDateTime")
    private String endDateTime;


    public Run(@NonNull String runID, String name, String endDateTime, int duration, int distance, double pace, int calories) {
        this.runID = runID;
        this.name = name;
        this.endDateTime = endDateTime;
        this.duration = duration;
        this.distance = distance;
        this.pace = pace;
        this.calories = calories;
    }

    /* Getters & Setters */

    @NonNull
    public String getRunID() {
        return runID;
    }

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

    public byte[] getMapSnapshot() {
        return mapSnapshot;
    }

    public float getRating() {
        return rating;
    }

    public String getNote() {
        return note;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setRunId(@NonNull String runID) {
        this.runID = runID;
    }

    public void setMapSnapshot(byte[] mapSnapshot) {
        this.mapSnapshot = mapSnapshot;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public void setPace(double pace) {
        this.pace = pace;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

}
