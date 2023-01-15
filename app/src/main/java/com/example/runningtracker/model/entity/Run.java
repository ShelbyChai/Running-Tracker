package com.example.runningtracker.model.entity;

import android.content.ContentValues;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.runningtracker.contentprovider.RunProviderContract;

@Entity(tableName = "run_table")
public class Run {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "run_ID")
    private long run_ID;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "dateTimeFormatted")
    private final String dateTimeFormatted;

    @ColumnInfo(name = "endDateTime")
    private final String endDateTime;

    @ColumnInfo(name = "duration")
    private int duration;

    @ColumnInfo(name = "distance")
    private int distance;

    @ColumnInfo(name = "calories")
    private int calories;

    @ColumnInfo(name = "pace")
    private double pace;

    @ColumnInfo(name = "rating")
    private float rating;

    @ColumnInfo(name = "note")
    private String note;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] photo;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] mapSnapshot;


    public Run(String name, String endDateTime, String dateTimeFormatted, int duration, int distance, double pace, int calories, byte[] mapSnapshot) {
        this.name = name;
        this.endDateTime = endDateTime;
        this.dateTimeFormatted = dateTimeFormatted;
        this.duration = duration;
        this.distance = distance;
        this.pace = pace;
        this.calories = calories;
        this.mapSnapshot = mapSnapshot;
    }

    /* Getters & Setters */

    public long getRun_ID() {
        return run_ID;
    }

    public void setRun_ID(long run_ID) {
        this.run_ID = run_ID;
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

    public String getDateTimeFormatted() {
        return dateTimeFormatted;
    }


    // Insert / Update the run with the content provider's value
    // runID not required as it is auto generated
    public static Run fromContentValues(@Nullable ContentValues contentValues) {
        // Initialise the required variable as empty
        String name = "";
        String dateTimeFormatted = "";
        String endDateTime = "";
        int duration = 0;
        int distance = 0;
        double pace = (double) 0;
        int calories = 0;
        byte[] mapSnapshot = new byte[0];

        if (contentValues != null) {
            if (contentValues.containsKey(RunProviderContract.NAME)){
                name = contentValues.getAsString(RunProviderContract.NAME);
            }
            if (contentValues.containsKey(RunProviderContract.DATE_TIME_FORMATTED)){
                dateTimeFormatted = contentValues.getAsString(RunProviderContract.DATE_TIME_FORMATTED);
            }
            if (contentValues.containsKey(RunProviderContract.END_DATE_TIME)){
                endDateTime = contentValues.getAsString(RunProviderContract.END_DATE_TIME);
            }
            if (contentValues.containsKey(RunProviderContract.DURATION)){
                duration = contentValues.getAsInteger(RunProviderContract.DURATION);
            }
            if (contentValues.containsKey(RunProviderContract.DISTANCE)){
                distance = contentValues.getAsInteger(RunProviderContract.DISTANCE);
            }
            if (contentValues.containsKey(RunProviderContract.PACE)){
                pace = contentValues.getAsDouble(RunProviderContract.PACE);
            }
            if (contentValues.containsKey(RunProviderContract.CALORIES)){
                calories = contentValues.getAsInteger(RunProviderContract.CALORIES);
            }
            return new Run(name, dateTimeFormatted, endDateTime, duration, distance, pace, calories, mapSnapshot);
        }

        return null;
    }
}
