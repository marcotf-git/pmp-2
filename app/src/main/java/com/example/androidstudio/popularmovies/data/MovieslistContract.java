package com.example.androidstudio.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Thia class will store the database contract schema names
 */

public final class MovieslistContract {

     /* Add content provider constants to the Contract
        Clients need to know how to access the task data, and it's your job to provide
        these content URI's for the path to that data:
        1) Content authority,
        2) Base content URI,
        3) Path(s) to the tasks directory
        4) Content URI for data in the TaskEntry class
      */

    // The authority, which is how your code knows which Content Provider to access
    public static final String AUTHORITY = "com.example.androidstudio.popularmovies";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Define the possible paths for accessing data in this contract
    // This is the path for the "tasks" directory
    public static final String PATH_MOVIES = "movies";

    private MovieslistContract() {}

    public static class MovieslistEntry implements BaseColumns {

        // MovieEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        // Task table and column names
        public static final String TABLE_NAME = "movies";

        // Since TaskEntry implements the interface "BaseColumns", it has an automatically produced
        // "_ID" column in addition to the two below
        public static final String COLUMN_MOVIE_TITLE = "title";
        public static final String COLUMN_MOVIE_ID = "id";
        public static final String COLUMN_INFO_JSON = "info";
        public static final String COLUMN_TRAILERS_JSON = "trailers";
        public static final String COLUMN_REVIEWS_JSON = "reviews";
        public static final String COLUMN_POSTER = "poster";

    }

}
