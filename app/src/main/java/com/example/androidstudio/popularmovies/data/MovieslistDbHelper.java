package com.example.androidstudio.popularmovies.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.androidstudio.popularmovies.data.MovieslistContract.*;

public class MovieslistDbHelper extends SQLiteOpenHelper {

    // The database name
    private static final String DATABASE_NAME = "movieslist.db";

    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 7;

    // Constructor
    public MovieslistDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // Create a table to hold waitlist data
        final String SQL_CREATE_MOVIESLIST_TABLE = "CREATE TABLE " +
                MovieslistEntry.TABLE_NAME + " (" +
                MovieslistEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieslistEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL," +
                MovieslistEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL," +
                MovieslistEntry.COLUMN_INFO_JSON + " TEXT," +
                MovieslistEntry.COLUMN_TRAILERS_JSON + " TEXT," +
                MovieslistEntry.COLUMN_REVIEWS_JSON + " TEXT," +
                MovieslistEntry.COLUMN_POSTER + " BLOB," +
                /*
                 * To ensure this table can only contain one film title, we declare
                 * the title column to be unique. We also specify "ON CONFLICT REPLACE". This tells
                 * SQLite that if we have a film entry, we replace the old.
                 */
                " UNIQUE (" + MovieslistEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";


        sqLiteDatabase.execSQL(SQL_CREATE_MOVIESLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // For now simply drop the table and create a new one. This means if you change the
        // DATABASE_VERSION the table will be dropped.
        // In a production app, this method might be modified to ALTER the table
        // instead of dropping it, so that existing data is not deleted.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieslistEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}