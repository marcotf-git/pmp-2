package com.example.androidstudio.popularmovies.utilities;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.androidstudio.popularmovies.data.MovieslistContract;
import com.example.androidstudio.popularmovies.data.MovieslistDbHelper;

/**
 * This class is a helper function for querying the database.
 */

public class DatabaseUtils {

    // Helper function for loading the poster
    public static Bitmap loadPosterFromDatabase(Context context, String movieId){

        Bitmap poster = null;

        MovieslistDbHelper dbHelper = new MovieslistDbHelper(context);
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        try {

            String posterQuery = "select " + MovieslistContract.MovieslistEntry.COLUMN_POSTER +
                    " from " + MovieslistContract.MovieslistEntry.TABLE_NAME +
                    " where " + MovieslistContract.MovieslistEntry.COLUMN_MOVIE_ID + " = " +
                    "'" + movieId + "'";
            Cursor cursor = database.rawQuery(posterQuery, null);

            if (cursor.moveToFirst()) {
                byte[] imgByte = cursor.getBlob(0);
                poster = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            }

            cursor.close();

        }catch (Exception e){
            e.printStackTrace();

        }

        database.close();
        return poster;
    }

}
