package com.example.runningtrackercw;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RunStatsActivity extends AppCompatActivity {
    // Initialise the variables storing the input from the user
    String time = null;
    String distance = null;
    String tag = "None";
    String image = null;
    String notes = null;
    String imageInfo = "No";
    // Activity request code for the activity result
    static final int ACTIVITY_PHOTO_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_stats);
        // Get the payload from the intent
        Bundle bundle = getIntent().getExtras();
        // Retrieve and display the current time
        time = bundle.getString("currentTime");
        TextView timeView = findViewById(R.id.textView9);
        timeView.setText(time);
        // Retrieve and display the current distance
        distance = bundle.getString("currentDistance");
        TextView distanceView = findViewById(R.id.textView13);
        distanceView.setText(distance + " m");
    }

    public void OnExerciseTagged(View v) {
        // Switch on the 2 choices the user has
        switch (v.getId()) {
            case R.id.radioButton:
                // Tag as good
                tag = "Good!";
                break;
            case R.id.radioButton2:
                // Tag as bad
                tag = "Bad!";
                break;
        }
    }

    public void OnImageUpload(View v) {
        // Open the gallery app for the user to select images to upload via intent
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, ACTIVITY_PHOTO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result from the photo gallery
        if (requestCode == ACTIVITY_PHOTO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get the photo selected by the user
                Uri imageUri = data.getData();
                // Declare the bitmap we will use to display the image
                Bitmap imageBitmap;
                try {
                    // Decode the given image into a bitmap
                    imageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 0, outputStream);
                    // Set the imageView to the uploaded image
                    ImageView imageView = findViewById(R.id.imageView);
                    imageView.setImageBitmap(imageBitmap);
                    // Turn the bitmap into a byte array
                    byte[] byteImage = outputStream.toByteArray();
                    // Encode the image into a string using Base64 for ease of storage
                    image = Base64.encodeToString(byteImage, Base64.DEFAULT);
                    // Inform the user that the upload was successful
                    Toast.makeText(getApplicationContext(),"Upload Successful!",Toast.LENGTH_SHORT).show();
                    // Declare that the image has been uploaded
                    imageInfo = "Yes";
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.d("Error", "" + resultCode);
        }
    }

    public void OnSaveAndExit(View v) {
        // Get the note entered in the notes field
        final EditText notesField = findViewById(R.id.editText);
        notes = notesField.getText().toString();
        // Get the current time and date and format it
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date currentDate = new Date();
        String date = dateFormatter.format(currentDate);
        // Upload the related fields to the database
        ContentValues exerciseValues = new ContentValues();
        exerciseValues.put(ContentProviderContract.TIME, Float.parseFloat(time));
        exerciseValues.put(ContentProviderContract.DISTANCE, Float.parseFloat(distance));
        exerciseValues.put(ContentProviderContract.TAG, tag);
        exerciseValues.put(ContentProviderContract.IMAGE, image);
        exerciseValues.put(ContentProviderContract.IMAGE_INFO, imageInfo);
        exerciseValues.put(ContentProviderContract.NOTES, notes);
        exerciseValues.put(ContentProviderContract.DATE, date);
        // Insert into the exercises database
        getContentResolver().insert(ContentProviderContract.EXERCISES_URI, exerciseValues);
        // Destroy the activity
        finish();
    }

    public void OnExitWithoutSave(View v) {
        // Destroy the activity
        finish();
    }
}
