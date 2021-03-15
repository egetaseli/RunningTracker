package com.example.runningtrackercw;

import androidx.core.app.NotificationCompat;

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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import android.util.Log;

public class GPSService extends Service {
    // Initialise the callback list used to send values to the activity
    RemoteCallbackList<MyBinder> remoteCallbackList = new RemoteCallbackList<MyBinder>();
    // Initialise the binder
    private final IBinder binder = new MyBinder();
    // Initialise the variable that holds the current location of the user
    Location currentLocation = null;
    // Initialise the time and distance that will be calculated in the service
    int time = 0;
    float totalDistance = 0;
    // Initialise the channel id and notification id that will be used to build the notification
    private final String CHANNEL_ID = "100";
    int NOTIFICATION_ID = 001;
    // Declare the location manager and location listener used for the location gathering
    LocationManager locationManager;
    GPSService.MyLocationListener locationListener;
    // Declare the thread that will be used to update location
    Thread locationUpdateThread;
    // Declare the thread that will be used to update time
    Thread timeUpdateThread;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("service", "onCreated");
        // Get location service and request updates
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new GPSService.MyLocationListener();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    50, // minimum time interval between updates, in milliseconds
                    2, // minimum distance between updates, in metres
                    locationListener);
        } catch(SecurityException e) {
            Log.d("g53mdp", e.toString());
        }
        // Set up and start the thread for updating the location of the user
        locationUpdateThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        try {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                    50, // minimum time interval between updates, in milliseconds
                                    2, // minimum distance between updates, in metres
                                    locationListener);
                        } catch(SecurityException e) {
                            Log.d("g53mdp", e.toString());
                        }
                    }
                } catch (InterruptedException e) {
                    Log.d("service", "Thread Stopped!");
                }
            }
        };
        locationUpdateThread.start();
        // Set up and start the thread to count the current time
        timeUpdateThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        time++;
                        Log.d("service", "time" + time);
                    }
                } catch (InterruptedException e) {
                    Log.d("g53mdp", "Thread Stopped!");
                }
            }
        };
        timeUpdateThread.start();
        // Notification Code
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel name";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }
        // Intent to go back to the main activity
        Intent intent = new Intent(GPSService.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        // Build notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Currently Running")
                .setContentText("Return to the Running Tracker App")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        startForeground(NOTIFICATION_ID, mBuilder.build());
    }

    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            // Declare the location variable that holds the location of the user in the previous call
            Location previousLocation;
            // Set the previous location to current location so that in the next call it will be the previous one
            previousLocation = currentLocation;
            // Update the current location
            currentLocation = location;
            // If the prev location is null don't add any distance else add the distance
            if (previousLocation == null) {
                totalDistance = 0;
            } else {
                totalDistance = totalDistance + currentLocation.distanceTo(previousLocation);
            }
            // Send the distance to the main activity via callback
            doCallbacks(totalDistance);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // information about the signal, i.e. number of satellites
            Log.d("g53mdp", "onStatusChanged: " + provider + " " + status);
        }
        @Override
        public void onProviderEnabled(String provider) {
            // the user enabled (for example) the GPS
            Log.d("g53mdp", "onProviderEnabled: " + provider);
        }
        @Override
        public void onProviderDisabled(String provider) {
            // the user disabled (for example) the GPS
            Log.d("g53mdp", "onProviderDisabled: " + provider);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("service", "service onBind");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service", "service onStartCommand");
        //Log.d("service", "intent: " + intent +" flag: " + flags + " startID: " + startId);
        return Service.START_STICKY;
    }

    public void doCallbacks(float distance) {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i = 0; i < n; i++) {
            remoteCallbackList.getBroadcastItem(i).callback.counterEvent(distance,time);
        }
        remoteCallbackList.finishBroadcast();
    }

    public class MyBinder extends Binder implements IInterface {
        // Declare the callback
        ICallback callback;
        @Override
        public IBinder asBinder() {
            return this;
        }
        void registerCallback(ICallback callback) {
            this.callback = callback;
            remoteCallbackList.register(MyBinder.this);
        }
        void unregisterCallback(ICallback callback) {
            remoteCallbackList.unregister(MyBinder.this);
        }
    }

    @Override
    public void onDestroy() {
        Log.d("service", "service onDestroy");
        super.onDestroy();
        // Stop the time counter and distance calculator thread
        locationUpdateThread.interrupt();
        timeUpdateThread.interrupt();
        // Remove notification
        stopForeground(true);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("service", "service onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // Remove notification and call onDestroy
        stopForeground(true);
        stopSelf();
    }
}
