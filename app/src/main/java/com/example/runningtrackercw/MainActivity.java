package com.example.runningtrackercw;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    // Initialise our service variable
    private GPSService.MyBinder myService = null;
    // Flag for destroying activity while service stopped
    int flag = 0;
    // A tag that is set when the user starts running
    boolean running = false;
    // The toast variable used to inform the user
    private Toast informationToast;
    // Declare the gesture detector
    private GestureDetector homePageGestureDetector;
    // Initialize our handler
    Handler h = new Handler();
    // Declare our broadcast receiver
    private AppBroadcastReceiver receiver;

    // Initialise our service connection
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("g53mdp", "MainActivity onServiceConnected");
            myService = (GPSService.MyBinder) service;
            myService.registerCallback(callback);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("g53mdp", "MainActivity onServiceDisconnected");
            myService.unregisterCallback(callback);
            myService = null;
        }
    };

    // Set up the callback to get distance and time updates from our service
    ICallback callback = new ICallback() {
        @Override
        public void counterEvent(final float distance, final int time) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Display current time
                    final TextView currentTimeField = findViewById(R.id.textView20);
                    int minutes = time / 60;
                    int seconds = time % 60;
                    if (seconds < 10) {
                        currentTimeField.setText("" + minutes + ".0" + seconds);
                    } else {
                        currentTimeField.setText("" + minutes + "." + seconds);
                    }
                    // Display current distance
                    final TextView currentDistanceField = findViewById(R.id.textView18);
                    currentDistanceField.setText("" + distance);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Bind to our gps service with flag 0, meaning if no service exists don't create new one
        this.bindService(new Intent(this, GPSService.class), serviceConnection, 0);
        // Set up the gesture detection
        ApplicationGestureDetector applicationGestureDetector = new ApplicationGestureDetector();
        homePageGestureDetector = new GestureDetector(MainActivity.this, applicationGestureDetector);
        // Query exercises database to populate relevant fields
        queryExercises();
        // Set up our contentObserver
        getContentResolver().registerContentObserver(ContentProviderContract.ALL_URI, true, new ChangeObserver(h));
        // Register our broadcast receiver
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new AppBroadcastReceiver();
        registerReceiver(receiver, intentFilter);
        // Ask the user for required sensitive permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
    }

    public void OnRunStarted(View v) {
        // If user not already running start the run
        if (!running) {
            // Declare that the user is now running
            running = true;
            // Intent used to start the gps service
            Intent serviceStartIntent = new Intent(MainActivity.this, GPSService.class);
            // Start our service using start service so that it doesn't kill it after activity destroyed
            startService(serviceStartIntent);
            // Also bind to the gps service
            bindService(new Intent(MainActivity.this, GPSService.class), serviceConnection, Context.BIND_AUTO_CREATE);
            // Set our flag to represent that we disconnected from the service
            flag = 0;
        } else {
            // If the user is already running inform the user and don't start the running
            if(informationToast != null)
                informationToast.cancel();
            informationToast = Toast.makeText(getApplicationContext(), "" , Toast.LENGTH_SHORT);
            informationToast.setText("Already Running!");
            informationToast.show();
        }
    }

    public void OnRunStopped(View v) {
        if (myService != null) {
            // Declare that the user stopped running
            running = false;
            // Stop the service
            stopService(new Intent(MainActivity.this,GPSService.class));
            // Unbind from service, stop the callback set service status
            unbindService(serviceConnection);
            myService.unregisterCallback(callback);
            myService = null;
            // Set our flag to represent we disconnected from the service
            flag = 1;
            // Start the activity that displays the current run's stats
            Bundle bundle = new Bundle();
            // Get the current time at the end of the run from the related field
            final TextView currentTimeField = findViewById(R.id.textView20);
            String currentTime = currentTimeField.getText().toString();
            // Reset the field
            currentTimeField.setText("0.00");
            // Get the current distance at the end of the run from the related field
            final TextView currentDistanceField = findViewById(R.id.textView18);
            String currentDistance = currentDistanceField.getText().toString();
            // Reset the field
            currentDistanceField.setText("0.0");
            // Send these values to the run statistics activity
            bundle.putString("currentTime", currentTime); // key, value
            bundle.putString("currentDistance", currentDistance); // key, value
            // Start the run statistics activity giving it the 2 values
            Intent intent = new Intent(MainActivity.this, RunStatsActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        } else {
            // If there is no run run to stop inform the user
            if(informationToast != null)
                informationToast.cancel();
            informationToast = Toast.makeText(getApplicationContext(), "" , Toast.LENGTH_SHORT);
            informationToast.setText("No Run To Stop!");
            informationToast.show();
        }
    }

    public void OnAllTimeStatsClicked(View v){
        // Start the All Time Stats Activity
        Intent intent = new Intent(MainActivity.this, AllTimeStatsActivity.class);
        startActivity(intent);
    }

    public void OnDailyStatsClicked(View v){
        // Start the Daily Stats Activity
        Intent intent = new Intent(MainActivity.this, DailyStatsActivity.class);
        startActivity(intent);
    }

    public void OnMonthlyStatsClicked(View v){
        // Start the Monthly Stats Activity
        Intent intent = new Intent(MainActivity.this, MonthlyStatsActivity.class);
        startActivity(intent);
    }

    class ApplicationGestureDetector implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent motionEvent) { return false; }
        @Override
        public void onShowPress(MotionEvent motionEvent) { }
        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) { return false; }
        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) { return false; }
        @Override
        public void onLongPress(MotionEvent motionEvent) { }
        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            if (motionEvent.getX() < motionEvent1.getX()) {
                // Start the Daily Stats Activity on left to right fling
                Intent intent = new Intent(MainActivity.this, DailyStatsActivity.class);
                startActivity(intent);
            } else if (motionEvent.getX() > motionEvent1.getX()) {
                // Start the Monthly Stats Activity on right to left fling
                Intent intent = new Intent(MainActivity.this, MonthlyStatsActivity.class);
                startActivity(intent);
            }
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        homePageGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class ChangeObserver extends ContentObserver {
        public ChangeObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            queryExercises();
        }
    }

    public void queryExercises() {
        // Projection used to query the db
        String[] projection = new String[] {
                ContentProviderContract._ID,
                ContentProviderContract.TIME,
                ContentProviderContract.DISTANCE,
                ContentProviderContract.TAG,
                ContentProviderContract.IMAGE,
                ContentProviderContract.IMAGE_INFO,
                ContentProviderContract.NOTES,
                ContentProviderContract.DATE
        };
        Cursor cursor = getContentResolver().query(ContentProviderContract.EXERCISES_URI, projection, null, null,null);
        // Get the id's of all entries
        int numEntries = cursor.getCount();
        // If no exercise done  (i.e database empty) return
        if (numEntries == 0)
            return;
        // Build the exercises array and fill it with the id's of all entries
        int[] exercises = new int[numEntries];
        int count = 1;
        int index = cursor.getColumnIndex(ContentProviderContract._ID);
        cursor.moveToFirst();
        exercises[0] = cursor.getInt(index);
        while(cursor.moveToNext()){
            exercises[count] = cursor.getInt(index);
            count++;
        }
        cursor.close();
        // The string that will have all times concatenated in it
        String allTimes = "";
        // The string that will have all distances concatenated in it
        String allDistances = "";
        for (int exerciseID : exercises) {
            if (exerciseID != 0) {
                Cursor cursor1 = getContentResolver().query(ContentProviderContract.EXERCISES_URI, projection, ContentProviderContract._ID + " = " + exerciseID, null, null);
                // Handle the time field
                int timeIndex = cursor1.getColumnIndex(ContentProviderContract.TIME);
                cursor1.moveToFirst();
                allTimes += cursor1.getFloat(timeIndex) + ",";
                // Handle the distance field
                int distanceIndex = cursor1.getColumnIndex(ContentProviderContract.DISTANCE);
                cursor1.moveToFirst();
                allDistances += cursor1.getFloat(distanceIndex) + ",";
                cursor1.close();
            }
        }
        // Split the concatenated string into individual strings corresponding to each entry
        String[] timeList = allTimes.split(",");
        // Get the most recent time
        String recentTime = timeList[count-1];
        // Convert it into floats to compare and sort properly
        float[] floatTimeList = new float[timeList.length];
        for (int i = 0; i < timeList.length; i++) {
            floatTimeList[i] = Float.parseFloat(timeList[i]);
        }
        Arrays.sort(floatTimeList);
        // Get the best time (last element is the largest(best) since its sorted)
        String bestTime = "" + floatTimeList[count-1];
        // Split the concatenated string into individual strings corresponding to each entry
        String[] distanceList = allDistances.split(",");
        // Get the most recent distance
        String recentDistance = distanceList[count-1];
        // Convert it into floats to compare and sort properly
        float[] floatDistanceList = new float[distanceList.length];
        for (int i = 0; i < distanceList.length; i++) {
            floatDistanceList[i] = Float.parseFloat(distanceList[i]);
        }
        Arrays.sort(floatDistanceList);
        // Get the best distance (last element is the largest(best) since its sorted)
        String bestDistance = "" + floatDistanceList[count-1];
        // Get the number of all runs so far
        String totalRuns = "" + count;
        // Populate related fields with the information gathered from the database
        TextView bestDistanceField = findViewById(R.id.textView5);
        bestDistanceField.setText(bestDistance + " m");
        TextView recentDistanceField = findViewById(R.id.textView6);
        recentDistanceField.setText(recentDistance + " m");
        TextView bestTimeField = findViewById(R.id.textView7);
        bestTimeField.setText(bestTime);
        TextView recentTimeField = findViewById(R.id.textView8);
        recentTimeField.setText(recentTime);
        TextView totalRunsField = findViewById(R.id.textView16);
        totalRunsField.setText(totalRuns);
    }

    @Override
    public void onDestroy(){
        Log.d("g53mdp Main", "onDestroy");
        super.onDestroy();
        // Unbind from the service unless already unbound (checked by the flag)
        if(serviceConnection!=null && flag == 0) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
        // Unregister our broadcast receiver
        unregisterReceiver(receiver);
    }
}
