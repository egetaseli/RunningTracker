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

public class SingleExerciseStatsActivity extends AppCompatActivity {
    // Variables holding the input from the user
    String id = null;
    String time = null;
    String distance = null;
    String tag = null;
    String image = null;
    String imageInfo = "No";
    String notes = null;
    String date = null;
    // Activity request code for the activity result
    static final int ACTIVITY_PHOTO_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_exercise_stats);
        // Get the payload from the intent
        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        // Retrieve and display the time
        time = bundle.getString("time");
        TextView timeView = findViewById(R.id.textView23);
        timeView.setText(time);
        // Retrieve and display the distance
        distance = bundle.getString("distance");
        TextView distanceView = findViewById(R.id.textView24);
        distanceView.setText(distance);
        // Retrieve and display the tag
        tag = bundle.getString("tag");
        EditText tagView = findViewById(R.id.editText2);
        tagView.setText(tag);
        // Retrieve and display the image
        image = bundle.getString("image");
        ImageView imageView = findViewById(R.id.imageView2);
        if (!image.equals("null")) {
            byte[] byteArrayImage = Base64.decode(image, Base64.DEFAULT);
            Bitmap bitImage = BitmapFactory.decodeByteArray(byteArrayImage, 0, byteArrayImage.length);
            imageView.setImageBitmap(bitImage);
            imageInfo = "Yes";
        }
        // Retrieve and display the notes
        notes = bundle.getString("notes");
        EditText notesView = findViewById(R.id.editText3);
        notesView.setText(notes);
        // Retrieve and display the date
        date = bundle.getString("date");
        TextView dateView = findViewById(R.id.textView27);
        dateView.setText(date);
    }

    public void OnBack(View v) {
        // Destroy the activity
        finish();
    }

    public void OnEdit(View v){
        // Get the contents of the tag field
        EditText tagView = findViewById(R.id.editText2);
        String newTag = tagView.getText().toString();
        // Get the contents of the notes field
        EditText notesView = findViewById(R.id.editText3);
        String newNotes = notesView.getText().toString();
        // Update the exercises database with the new values
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(ContentProviderContract.TAG, newTag);
        updatedValues.put(ContentProviderContract.NOTES, newNotes);
        updatedValues.put(ContentProviderContract.IMAGE, image);
        updatedValues.put(ContentProviderContract.IMAGE_INFO, imageInfo);
        getContentResolver().update(ContentProviderContract.EXERCISES_URI, updatedValues, ContentProviderContract._ID + " = " + id,null);
        // Destroy the activity
        finish();
    }

    public void OnDelete(View v){
        // Delete the exercise using its unique id
        getContentResolver().delete(ContentProviderContract.EXERCISES_URI, ContentProviderContract._ID + " = " + id, null);
        // Destroy the activity
        finish();
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
                    ImageView imageView = findViewById(R.id.imageView2);
                    imageView.setImageBitmap(imageBitmap);
                    // Turn the bitmap into a byte array
                    byte[] byteImage = outputStream.toByteArray();
                    // Encode the image into a string using Base64 for ease of storage
                    image = Base64.encodeToString(byteImage, Base64.DEFAULT);
                    // Declare that the image has been uploaded
                    imageInfo = "Yes";
                    // Inform the user that the upload was successful
                    Toast.makeText(getApplicationContext(),"Upload Successful!",Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.d("Error", "" + resultCode);
        }
    }
}
