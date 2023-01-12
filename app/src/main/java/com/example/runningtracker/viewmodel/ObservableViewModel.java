package com.example.runningtracker.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.AndroidViewModel;

public class ObservableViewModel extends AndroidViewModel implements Observable {

    PropertyChangeRegistry callbacks = new PropertyChangeRegistry();
    public ObservableViewModel(@NonNull Application application) {
        super(application);
    }

    /* Helper formatter functions */

    @SuppressLint("DefaultLocale")
    public String formatTime(int duration) {
        int seconds = (duration % 60);
        int minutes = (duration % 3600) / 60;
        int hours = (duration / 3600);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @SuppressLint("DefaultLocale")
    public String formatDistance(int distance) {
        double kilometers = (double) distance / 1000;

        return String.format("%.3f", kilometers);
    }

    @SuppressLint("DefaultLocale")
    public String formatPace(double pace) {

        return String.format("%.2f", pace);
    }

    /* Override */

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    public void notifyChange() {
        callbacks.notifyCallbacks(this, 0, null);
    }

    public void notifyPropertyChanged(int fieldId){
        callbacks.notifyCallbacks(this, fieldId, null);
    }
}