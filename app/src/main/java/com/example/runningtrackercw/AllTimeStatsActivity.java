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

public class AllTimeStatsActivity extends AppCompatActivity {
    // Data adapter used to populate the listView
    SimpleCursorAdapter dataAdapter;
    // Array of image strings corresponding to each entry in the database
    String[] ImageList;
    // Initialize our handler
    Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_time_stats);
        // Set up our contentObserver
        getContentResolver().registerContentObserver(ContentProviderContract.ALL_URI, true, new ChangeObserver(h));
        // Query exercises database to populate our listView
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
        // The cursor returned from the query with the various sorting
        Cursor cursor = getContentResolver().query(ContentProviderContract.EXERCISES_URI, projection, null, null,null);
        if (sort == 1){
            cursor = getContentResolver().query(ContentProviderContract.EXERCISES_URI, projection, null, null, "date DESC");
        } else if(sort == 2){
            cursor = getContentResolver().query(ContentProviderContract.EXERCISES_URI, projection, null, null, "distance DESC");
        } else if(sort == 3){
            cursor = getContentResolver().query(ContentProviderContract.EXERCISES_URI, projection, null, null, "time DESC");
        } else if(sort == 4){
            cursor = getContentResolver().query(ContentProviderContract.EXERCISES_URI, projection, null, null, "tag ASC");
        }
        // Get the ids of all entries
        int numEntries = cursor.getCount();
        // If no exercise done (i.e database empty) return
        if (numEntries == 0)
            return;
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
        // Get the image string for all entries using the unique ids
        for (int exerciseID : exercises) {
            if (exerciseID != 0) {
                Cursor cursor1 = getContentResolver().query(ContentProviderContract.EXERCISES_URI, projection, ContentProviderContract._ID + " = " + exerciseID, null, null);
                // Handle the image field
                int imageIndex = cursor1.getColumnIndex(ContentProviderContract.IMAGE);
                cursor1.moveToFirst();
                allImages += cursor1.getString(imageIndex) + ",";
            }
        }
        // Split the concatenated string into individual strings
        ImageList = allImages.split(",");
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
        // Ids of the columns to display
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
                AllTimeStatsActivity.this,
                R.layout.db_exercises_view,
                cursor,
                colsToDisplay,
                colResIds,
                0);
        // Set up the listView
        ListView listView = findViewById(R.id.listView);
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
                Intent intent = new Intent(AllTimeStatsActivity.this, SingleExerciseStatsActivity.class);
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
            case R.id.radioButton3:
                // Sort by date
                sortBy = 1;
                break;
            case R.id.radioButton4:
                // Sort by distance
                sortBy = 2;
                break;
            case R.id.radioButton5:
                // Sort by time
                sortBy = 3;
                break;
            case R.id.radioButton6:
                // Sort by tag
                sortBy = 4;
                break;
        }
        queryExercises(sortBy);
    }

    public void OnBack(View v) {
        // Destroy the activity
        finish();
    }
}
