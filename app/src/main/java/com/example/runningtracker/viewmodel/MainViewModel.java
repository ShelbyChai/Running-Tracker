package com.example.runningtracker.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.lifecycle.MutableLiveData;

import com.example.runningtracker.service.TrackerCallback;
import com.example.runningtracker.service.TrackerService;
import com.example.runningtracker.BR;

public class MainViewModel extends ObservableViewModel {
    private TrackerService.MyBinder trackerBinder = null;
    private TrackerCallback trackerCallback;

    /* Bindable Object */
    private final MutableLiveData<Float> distanceTravelled = new MutableLiveData<>((float) 0);

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("comp3018", "MainActivity onServiceConnected");
            trackerBinder = (TrackerService.MyBinder) service;
            trackerBinder.registerCallback(trackerCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("comp3018", "MainActivity onServiceDisconnected");
            trackerBinder.unregisterCallback(trackerCallback);
            trackerBinder = null;
        }
    };

    public MainViewModel(@NonNull Application application) {
        super(application);

        calculateTotalDistance();
    }

    private void calculateTotalDistance() {
        trackerCallback = new TrackerCallback() {
            private Location prevLocation = null;

            @Override
            public void runningTrackerLocationEvent(Location location) {
                Log.d("comp3018", "location " + location.toString());

                if (prevLocation != null) {
                    float distance = prevLocation.distanceTo(location);
                    distanceTravelled.setValue(distanceTravelled.getValue() + distance);
                    notifyPropertyChanged(BR.distanceTravelled);

                    Log.d("comp3018", "Distance Travelled: " + distanceTravelled.getValue());
                }
                prevLocation = location;
            }
        };
    }
    /* Getter & Setter */
    @Bindable
    public MutableLiveData<Float> getDistanceTravelled() {
        return distanceTravelled;
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public void setServiceConnection(ServiceConnection serviceConnection) {
        this.serviceConnection = serviceConnection;
    }
}
