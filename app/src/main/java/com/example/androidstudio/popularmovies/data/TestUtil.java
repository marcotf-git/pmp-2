package com.example.androidstudio.popularmovies.data;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class TestUtil {

    public static void insertFakeData(SQLiteDatabase db){

        if(db == null){
            return;
        }
        //create a list of fake guests
        List<ContentValues> list = new ArrayList<>();

        ContentValues cv = new ContentValues();
        cv.put(MovieslistContract.MovieslistEntry.COLUMN_MOVIE_TITLE, "Movie 1");
        list.add(cv);

        cv = new ContentValues();
        cv.put(MovieslistContract.MovieslistEntry.COLUMN_MOVIE_TITLE, "Movie 2");
        list.add(cv);

        cv = new ContentValues();
        cv.put(MovieslistContract.MovieslistEntry.COLUMN_MOVIE_TITLE, "Movie 3");
        list.add(cv);

        cv = new ContentValues();
        cv.put(MovieslistContract.MovieslistEntry.COLUMN_MOVIE_TITLE, "Movie 4");
        list.add(cv);

        cv = new ContentValues();
        cv.put(MovieslistContract.MovieslistEntry.COLUMN_MOVIE_TITLE, "Movie 5");
        list.add(cv);

        //insert all movies in one transaction
        try
        {
            db.beginTransaction();
            //clear the table first
            db.delete (MovieslistContract.MovieslistEntry.TABLE_NAME,null,null);
            //go through the list and add one by one
            for(ContentValues column:list){
                db.insert(MovieslistContract.MovieslistEntry.TABLE_NAME, null, column);
            }
            db.setTransactionSuccessful();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally
        {
            db.endTransaction();
        }

    }
}