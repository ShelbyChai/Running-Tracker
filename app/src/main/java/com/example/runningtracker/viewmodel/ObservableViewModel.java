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
    public String formatTime(int totalSecs) {
        int seconds = (totalSecs % 60);
        int minutes = (totalSecs % 3600) / 60;
        int hours = (totalSecs / 3600);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
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