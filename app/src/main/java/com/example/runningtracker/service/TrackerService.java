package com.example.runningtracker.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.runningtracker.R;
import com.example.runningtracker.view.RunActivity;

import java.util.Objects;

public class TrackerService extends Service {
    /* Global Tag */
    public final static String SERVICE_RUNNING = "Resume";
    public final static String SERVICE_PAUSE = "Pause";
    public final static String SERVICE_FINISH = "Finish";
    public final static String NOTIFICATION_CONTENT_UPDATE = "Notification content update";
    public final static int NOTIFICATION_ID = 1;

    private final RemoteCallbackList<MyBinder> remoteCallbackList = new RemoteCallbackList<MyBinder>();
    private String serviceStatus;
    private LocationManager locationManager;
    private MyLocationListener locationListener;

    /* Notification objects and variables */
    private final String NOTIFICATION_CHANNEL_ID = "100";
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;


    /* Binder */

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
    private void doCallbacks(Location location) {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i = 0; i < n; i++) {
            remoteCallbackList.getBroadcastItem(i).trackerCallback.runningTrackerLocationEvent(location);
        }
        remoteCallbackList.finishBroadcast();
    }

    /* Location Listener */

    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("comp3018", "location " + location.toString());
            doCallbacks(location);

            // Broadcast intent to update notification Content text in Run Activity
            Intent updateNotificationText = new Intent(NOTIFICATION_CONTENT_UPDATE);
            sendBroadcast(updateNotificationText);
        }
    }

    /* Service Lifecycle */

    @Override
    public void onCreate() {
        Log.d("comp3018", "TrackerService onCreate");
        super.onCreate();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        serviceStatus = SERVICE_PAUSE;

        buildNotificationChannel();

        startLocationUpdate();
        buildNotification();
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

    /* Location update buttons */

    private void startLocationUpdate() {
        if (!Objects.equals(serviceStatus, SERVICE_RUNNING)) {
            serviceStatus = SERVICE_RUNNING;
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        1000,
                        0,
                        locationListener);
            } catch(SecurityException e) {
                Log.d("comp3018", e.toString());
            }
        }
    }

    private void pauseLocationUpdate() {
        if (!Objects.equals(serviceStatus, SERVICE_PAUSE) && locationManager != null) {
            serviceStatus = SERVICE_PAUSE;
            // Pass null location to reset previous location to null
            doCallbacks(null);
            locationManager.removeUpdates(locationListener);
        }
    }

    private void finishLocationUpdate() {
        if (locationManager != null) {
            serviceStatus = SERVICE_FINISH;
            locationManager.removeUpdates(locationListener);
            locationManager = null;
        }

        if (locationListener != null) {
            locationListener = null;
        }

        // Stop all foreground services and cancel all the notification
        stopForeground(true);
        notificationManager.cancelAll();

    }

    /* Notification */

    // Initialise notification channel
    private void buildNotificationChannel() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        CharSequence channelName = "Running Tracker";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
        notificationManager.createNotificationChannel(channel);
    }

    // Return Pending intent notification button action
    public PendingIntent notificationButtons(String name) {
        Intent notificationIntent = new Intent(name);

        return PendingIntent.getBroadcast(TrackerService.this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /*
    * 1. Add service control buttons that invoke intents on the Tracker Service
    * 2. Start Foreground Service and build the notification
    * */
    private void buildNotification() {
        Intent notificationIntent = new Intent(this, RunActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
}
