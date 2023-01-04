package com.example.runningtracker.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.runningtracker.R;
import com.example.runningtracker.view.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class TrackerService extends Service {
    private final String CHANNEL_ID = "100";

    private final RemoteCallbackList<MyBinder> remoteCallbackList = new RemoteCallbackList<MyBinder>();

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    public class MyBinder extends Binder implements IInterface {
        private TrackerCallback trackerCallback;

        @Override
        public IBinder asBinder() {
            return this;
        }

        public void registerCallback(TrackerCallback callback) {
            this.trackerCallback = callback;
            remoteCallbackList.register(MyBinder.this);
        }

        public void unregisterCallback(TrackerCallback callback) {
            remoteCallbackList.unregister(MyBinder.this);
            this.trackerCallback = null;
        }
    }

    /*
     * Callback method to broadcast the location
     * */
    private void doCallbacks(Location location) {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i = 0; i < n; i++) {
            remoteCallbackList.getBroadcastItem(i).trackerCallback.runningTrackerLocationEvent(location);
        }
        remoteCallbackList.finishBroadcast();
    }

    @Override
    public void onCreate() {
        Log.d("comp3018", "TrackerService onCreate");
        super.onCreate();

        buildNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("comp3018", "TrackerService onStartCommand");

        startLocationUpdates();
        buildNotification();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("comp3018", "TrackerService onBind");
        return new MyBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    // Finish tracker
    // Pause tracker
    // Start Tracker

    private void buildNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Running Tracker";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void buildNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).
                setPriority(NotificationCompat.PRIORITY_HIGH).
                setContentTitle("Running Tracker is running").
                setSmallIcon(R.drawable.ic_launcher_background).
                setContentIntent(pendingIntent).build();

        startForeground(1, notification);
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    doCallbacks(location);
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        } catch (SecurityException e) {
            // lacking permission to access location
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
