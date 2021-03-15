package com.example.runningtrackercw;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        Log.d("g53mdp", "DBHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("g53mdp", "onCreate");
        db.execSQL("CREATE TABLE exercises (" +
                "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT ," +
                "time REAL NOT NULL," +
                "distance REAL NOT NULL," +
                "tag VARCHAR(128)," +
                "image TEXT," +
                "image_info VARCHAR(64)," +
                "notes VARCHAR(128)," +
                "date VARCHAR(128) NOT NULL" +
                ");");
        db.execSQL("INSERT INTO exercises (time, distance, tag, image, image_info, notes, date) VALUES ('3.00', '7.77765', 'Good!', null, 'No','It was nice weather, ejoyed the run and had a great time.', '01/01/2019 12:26:18');");
        db.execSQL("INSERT INTO exercises (time, distance, tag, image, image_info, notes, date) VALUES ('14.54', '1.7723', 'Bad! ', null, 'No','It was bad weather, did not enjoy the run and had a bad time.', '02/01/2020 12:26:18');");
        db.execSQL("INSERT INTO exercises (time, distance, tag, image, image_info, notes, date) VALUES ('1.19', '2.54321', 'Good!', null, 'No' ,'It was nice weather, enjoyed the run and had a great time.', '05/01/2020 12:26:18');");
        db.execSQL("INSERT INTO exercises (time, distance, tag, image, image_info, notes, date) VALUES ('2.30', '3.67753', 'Bad! ', null, 'No','It was bad weather, did not enjoy the run and had a bad time.', '06/01/2020 12:26:18');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS exercises");
        onCreate(db);
    }
}
