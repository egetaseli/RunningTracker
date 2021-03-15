package com.example.runningtrackercw;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class MonthlyStatsActivity extends AppCompatActivity {
    // Data adapter used to populate the listView
    SimpleCursorAdapter dataAdapter;
    // Array of image strings corresponding to each entry in the database
    String[] ImageList;
    // Initialize our handler
    Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_stats);
        // Set up our contentObserver
        getContentResolver().registerContentObserver(ContentProviderContract.ALL_URI, true, new ChangeObserver(h));
        queryExercises(0);
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
            queryExercises(0);
        }
    }

    public void queryExercises(int sort) {
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
        // Get the current date and format it to facilitate getting current month
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/yyyy");
        Date currentDate = new Date();
        String date = dateFormatter.format(currentDate);
        // Display the date
        TextView dateView = findViewById(R.id.textView54);
        dateView.setText(date);
        // The cursor returned from the query with the selection by month
        Cursor cursor = getContentResolver().query(ContentProviderContract.EXERCISES_URI, projection, ContentProviderContract.DATE + " LIKE ?", new String[] {"%" + date + "%"} , null);
        if(sort == 1) {
            cursor = getContentResolver().query(ContentProviderContract.EXERCISES_URI, projection, ContentProviderContract.DATE + " LIKE ?", new String[] {"%" + date + "%"} , "time DESC");
        } else if(sort == 2) {
            cursor = getContentResolver().query(ContentProviderContract.EXERCISES_URI, projection, ContentProviderContract.DATE + " LIKE ?", new String[] {"%" + date + "%"} , "distance DESC");
        }
        // Get the ids of all entries
        int numEntries = cursor.getCount();
        // If no exercise done this month (i.e database empty where date is this month) return
        if (numEntries == 0)
            return;
        // Build the exercises array and fill it with the id's of all entries
        int index = cursor.getColumnIndex(ContentProviderContract._ID);
        cursor.moveToFirst();
        int[] exercises = new int[numEntries];
        exercises[0] = cursor.getInt(index);
        int count = 1;
        while(cursor.moveToNext()){
            exercises[count] = cursor.getInt(index);
            count++;
        }
        // The string that stores all image strings concatenated by ,
        String allImages = "";
        // The string that will have all times concatenated by ,
        String allTimes = "";
        // The string that will have all distances concatenated by ,
        String allDistances = "";
        // Get the related values for all entries using the unique id's
        for (int exerciseID : exercises) {
            if (exerciseID != 0) {
                Cursor cursor1 = getContentResolver().query(ContentProviderContract.EXERCISES_URI, projection, ContentProviderContract._ID + " = " + exerciseID + " AND " + ContentProviderContract.DATE + " LIKE ?", new String[] {"%" + date + "%"}, null );
                // Handle the image field
                int imageIndex = cursor1.getColumnIndex(ContentProviderContract.IMAGE);
                cursor1.moveToFirst();
                allImages += cursor1.getString(imageIndex) + ",";
                // Handle the time field
                int timeIndex = cursor1.getColumnIndex(ContentProviderContract.TIME);
                cursor1.moveToFirst();
                allTimes += cursor1.getFloat(timeIndex) + ",";
                // Handle the distance field
                int distanceIndex = cursor1.getColumnIndex(ContentProviderContract.DISTANCE);
                cursor1.moveToFirst();
                allDistances += cursor1.getFloat(distanceIndex) + ",";
            }
        }
        // Split the concatenated image string into individual strings corresponding to each entry
        ImageList = allImages.split(",");
        // Split the concatenated time string into individual strings corresponding to each entry
        String[] timeList = allTimes.split(",");
        // Convert it into floats to compare and sort properly
        float[] floatTimeList = new float[timeList.length];
        // Store the sum of all the times
        float sumTime = 0;
        for (int i = 0; i < timeList.length; i++) {
            floatTimeList[i] = Float.parseFloat(timeList[i]);
            sumTime = sumTime + floatTimeList[i];
        }
        // Get the fraction and the integer part of the sum of times
        float sumSecs = sumTime % 1;
        int sumMins = (int) sumTime;
        // Since seconds go up to 60 and floats go up to 100, we need the following translation to make sure we don't have second values more than 60
        if (sumSecs > 0.60) {
            sumMins = sumMins + 1;
            sumSecs = (float) (sumSecs - 0.60);
        }
        sumTime = sumMins + sumSecs;
        // Sort the list
        Arrays.sort(floatTimeList);
        // Get the best time (last element is the largest(best) since its sorted)
        String bestTime = "" + floatTimeList[count-1];
        // Split the concatenated distance string into individual strings corresponding to each entry
        String[] distanceList = allDistances.split(",");
        // Convert it into floats to compare and sort properly
        float[] floatDistanceList = new float[distanceList.length];
        // Store the sum of all the distances
        float sumDistance = 0;
        for (int i = 0; i < distanceList.length; i++) {
            floatDistanceList[i] = Float.parseFloat(distanceList[i]);
            sumDistance = sumDistance + floatDistanceList[i];
        }
        Arrays.sort(floatDistanceList);
        // Get the best distance (last element is the largest(best) since its sorted)
        String bestDistance = "" + floatDistanceList[count-1];
        // Get the total time
        String totalTime = ""+ sumTime;
        // Get the total distance
        String totalDistance = ""+ sumDistance;
        // Populate related fields with the information gathered from the database
        TextView bestDistanceField = findViewById(R.id.textView58);
        bestDistanceField.setText(bestDistance + " m");
        TextView totalDistanceField = findViewById(R.id.textView56);
        totalDistanceField.setText(totalDistance + " m");
        TextView totalTimeField = findViewById(R.id.textView60);
        totalTimeField.setText(totalTime);
        TextView bestTimeField = findViewById(R.id.textView62);
        bestTimeField.setText(bestTime);
        // Content of the columns to display
        String[] colsToDisplay = new String[] {
                ContentProviderContract._ID,
                ContentProviderContract.TIME,
                ContentProviderContract.DISTANCE,
                ContentProviderContract.TAG,
                ContentProviderContract.IMAGE_INFO,
                ContentProviderContract.NOTES,
                ContentProviderContract.DATE
        };
        // Id's of the columns to display
        int[] colResIds = new int[] {
                R.id.idView,
                R.id.timeView,
                R.id.distanceView,
                R.id.tagView,
                R.id.imageInfoView,
                R.id.notesView,
                R.id.dateView
        };
        // Set up the data adapter for out listView
        dataAdapter = new SimpleCursorAdapter(
                MonthlyStatsActivity.this,
                R.layout.db_exercises_view,
                cursor,
                colsToDisplay,
                colResIds,
                0);
        // Set up the listView
        ListView listView = findViewById(R.id.monthlyListView);
        listView.setAdapter(dataAdapter);
        listView.setClickable(true);
        listView.setFastScrollEnabled(true);
        listView.setScrollingCacheEnabled(false);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                // Find the textViews in db_exercises_view.xml
                TextView idView = view.findViewById(R.id.idView);
                TextView timeView = view.findViewById(R.id.timeView);
                TextView distanceView = view.findViewById(R.id.distanceView);
                TextView tagView = view.findViewById(R.id.tagView);
                TextView notesView = view.findViewById(R.id.notesView);
                TextView dateView = view.findViewById(R.id.dateView);
                // Send these 6 fields and the image string to the individual recipe viewing activity
                Intent intent = new Intent(MonthlyStatsActivity.this, SingleExerciseStatsActivity.class);
                Bundle dataBundle = new Bundle();
                dataBundle.putString("id", idView.getText().toString());
                dataBundle.putString("time", timeView.getText().toString());
                dataBundle.putString("distance", distanceView.getText().toString());
                dataBundle.putString("tag", tagView.getText().toString());
                dataBundle.putString("image", ImageList[position]);
                dataBundle.putString("notes", notesView.getText().toString());
                dataBundle.putString("date", dateView.getText().toString());
                intent.putExtras(dataBundle);
                // Start recipeActivity
                startActivity(intent);
            }
        });
    }

    public void OnSort(View v) {
        // Switch on the 4 sorting options
        int sortBy = 0;
        switch (v.getId()) {
            case R.id.radioButton9:
                // Sort by time
                sortBy = 1;
                break;
            case R.id.radioButton10:
                // Sort by distance
                sortBy = 2;
                break;
        }
        queryExercises(sortBy);
    }

    public void OnBack(View v) {
        // Destroy the activity
        finish();
    }
}
