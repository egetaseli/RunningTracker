package com.example.runningtrackercw;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileNotFoundException;

public class AppContentProvider extends ContentProvider {
    // Initialise our database helper
    private DBHelper dbHelper = null;
    // Declare the uriMatcher used to match uri's
    private static final UriMatcher uriMatcher;

    // Add relevant uri's
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(ContentProviderContract.AUTHORITY, "exercises", 1);
        uriMatcher.addURI(ContentProviderContract.AUTHORITY, "exercises/#", 2);
        uriMatcher.addURI(ContentProviderContract.AUTHORITY, "*", 3);
    }

    @Override
    public boolean onCreate() {
        Log.d("g53mdp", "ContentProvider onCreate");
        this.dbHelper = new DBHelper(this.getContext(), "RunningTrackerDB", null, 53);
        return true;
    }

    @Override
    public String getType(Uri uri) {
        String contentType;
        if (uri.getLastPathSegment()==null) {
            contentType = ContentProviderContract.CONTENT_TYPE_MULTIPLE;
        } else {
            contentType = ContentProviderContract.CONTENT_TYPE_SINGLE;
        }
        return contentType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Insert into the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName = "exercises";
        long id = db.insert(tableName, null, values);
        db.close();
        Uri nu = ContentUris.withAppendedId(uri, id);
        Log.d("g53mdp", nu.toString());
        getContext().getContentResolver().notifyChange(nu, null);
        return nu;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Query the database
        Log.d("g53mdp", uri.toString() + " " + uriMatcher.match(uri));
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch(uriMatcher.match(uri)) {
            case 2:
                selection = "_ID = " + uri.getLastPathSegment();
            case 1:
                return db.query("exercises", projection, selection, selectionArgs, null, null, sortOrder);
            case 3:
                String q1 = "SELECT * FROM exercises";
                return db.rawQuery(q1, selectionArgs);
            default:
                return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Update an entry in the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int i;
        String tableName = "exercises";
        i = db.update(tableName, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return i;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Delete an entry in the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int i;
        String tableName = "exercises";
        i = db.delete(tableName, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return i;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        throw new UnsupportedOperationException("not implemented");
    }
}
