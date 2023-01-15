package com.example.runningtracker.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.runningtracker.R;
import com.example.runningtracker.view.RunActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Objects;

public class TrackerService extends Service {
    /* Global Tag */
    public final static String SERVICE_RUNNING = "Resume";
    public final static String SERVICE_PAUSE = "Pause";
    public final static String SERVICE_FINISH = "Finish";
    public final static String NOTIFICATION_CONTENT_UPDATE = "Notification content update";
    public final static int NOTIFICATION_ID = 1;

    private final RemoteCallbackList<MyBinder> remoteCallbackList = new RemoteCallbackList<MyBinder>();
    // Contain the current service status, whether its pause or running
    private String serviceStatus;

    /* Declare timer thread, timer and current location variables*/
    private TimerThread timerThread;
    private int timer;
    private boolean isTimerThreadRunning;
    private Location currentLocation;

    /* Declare location variable */
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    /* Notification objects and variables */
    private final String NOTIFICATION_CHANNEL_ID = "100";
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;


    /* Binder */

    /*
     * An interface like class for the service.
     * It wrap up and contains the methods that the activity can
     * call on the service. The MyBinder object reference into
     * the service and call methods.
     * */
    public class MyBinder extends Binder implements IInterface {
        private TrackerCallback trackerCallback;

        @Override
        public IBinder asBinder() {
            return this;
        }

        public void startRunning() {
            Log.d("comp3018", "Service Start Running");
            TrackerService.this.startLocationUpdate();
        }

        public void pauseRunning() {
            Log.d("comp3018", "Service Pause Running");
            TrackerService.this.pauseLocationUpdate();
        }

        public void stopRunning() {
            Log.d("comp3018", "Service Stop Running");
            TrackerService.this.finishLocationUpdate();
        }

        public NotificationManager getNotificationManager() {
            return TrackerService.this.notificationManager;
        }

        public NotificationCompat.Builder getNotificationBuilder() {
            return TrackerService.this.notificationBuilder;
        }

        public String getServiceStatus() {
            return TrackerService.this.serviceStatus;
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
    private void doCallbacks(Location location, int timer) {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i = 0; i < n; i++) {
            remoteCallbackList.getBroadcastItem(i).trackerCallback.runningTrackerLocationEvent(location, timer);
        }
        remoteCallbackList.finishBroadcast();
    }

    /*
     * A timer thread to do callback of the current time, current location and broadcast intent
     * to update notification content text while the service is running */
    private class TimerThread extends Thread implements Runnable {
        @Override
        public void run() {
            while (isTimerThreadRunning) {
                Log.d("comp3018", "TimerThread: " + timer);
                SystemClock.sleep(1000);

                if (serviceStatus != null) {
                    if (!Objects.equals(serviceStatus, SERVICE_PAUSE)) {
                        timer += 1;

                        doCallbacks(currentLocation, timer);

                        // Broadcast intent to update notification Content text in Run Activity
                        Intent updateNotificationText = new Intent(NOTIFICATION_CONTENT_UPDATE);
                        sendBroadcast(updateNotificationText);
                    }
                }
            }
        }
    }

    /* Service Lifecycle */

    @Override
    public void onCreate() {
        Log.d("comp3018", "TrackerService onCreate");
        super.onCreate();

        serviceStatus = SERVICE_PAUSE;
        isTimerThreadRunning = true;
        timer = 0;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location update for every 1 second
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(1000).build();

        // Set the current location from the location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Log.d("comp3018", "location " + location.toString());
                    currentLocation = location;
                }
            }
        };

        // Start the notification and location update
        buildNotificationChannel();
        buildNotification();
        startLocationUpdate();


        // Start the timer thread
        timerThread = new TimerThread();
        timerThread.start();
    }

    /* Location update buttons */

    // If the service status is not running, start the location update and set the status to running
    private void startLocationUpdate() {
        if (!Objects.equals(serviceStatus, SERVICE_RUNNING)) {
            serviceStatus = SERVICE_RUNNING;
            try {
                fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback,
                        Looper.getMainLooper());
            } catch (SecurityException e) {
                // lacking permission to access location
            }
        }
    }

    // If the service status is not pause, remove the location updates and set the status to pause
    private void pauseLocationUpdate() {
        if (!Objects.equals(serviceStatus, SERVICE_PAUSE) && fusedLocationClient != null) {
            serviceStatus = SERVICE_PAUSE;
            // Pass null location to reset previous location to null
            doCallbacks(null, timer);
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    // Remove the thread, location update, foreground service and notification
    private void finishLocationUpdate() {
        // Interrupt the timer thread and set the its running variable to false
        if (timerThread != null) {
            timerThread.interrupt();
            isTimerThreadRunning = false;
            timerThread = null;
        }

        if (fusedLocationClient != null) {
            serviceStatus = SERVICE_FINISH;
            fusedLocationClient.removeLocationUpdates(locationCallback);
            fusedLocationClient = null;
        }

        // Stop all foreground services and cancel all the notification
        stopForeground(true);
        notificationManager.cancelAll();
    }

    /* Notification */

    private void buildNotificationChannel() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        CharSequence channelName = "Running Tracker";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
        notificationManager.createNotificationChannel(channel);
    }

    // Return Pending intent notification button action
    public PendingIntent notificationButtons(String name) {
        Intent notificationIntent = new Intent(name);

        return PendingIntent.getBroadcast(TrackerService.this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /*
     * Build and starts a new foreground notification with a pending intent that
     * return the user to the Run Activity.
     * 1. Notification Button: Add service control buttons that invoke intents on the Tracker Service.
     * 2. Start Foreground Service and build the notification
     * */
    private void buildNotification() {
        Intent notificationIntent = new Intent(this, RunActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).
                setPriority(NotificationCompat.PRIORITY_HIGH).
                setContentTitle(getString(R.string.notification_title)).
                setSmallIcon(R.drawable.ic_launcher_icon_foreground).
                setContentIntent(pendingIntent).
                // 1
                        addAction(R.drawable.ic_notification_play, SERVICE_RUNNING, notificationButtons(SERVICE_RUNNING)).
                addAction(R.drawable.ic_notification_pause, SERVICE_PAUSE, notificationButtons(SERVICE_PAUSE)).
                addAction(R.drawable.ic_notification_stop, SERVICE_FINISH, notificationButtons(SERVICE_FINISH)).
                setContentText("Duration: 00:00:00, Distance: 0.00 km");

        // 2
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("comp3018", "TrackerService onStartCommand");
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
        finishLocationUpdate();
    }
}
