package com.example.androidstudio.popularmovies.data;

import android.provider.BaseColumns;

/**
 * Thia class will store the database contract schema names
 */

public final class MovieslistContract {

    private MovieslistContract() {}

    public static class MovieslistEntry implements BaseColumns {
        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_MOVIE_TITLE = "title";
        public static final String COLUMN_MOVIE_ID = "id";
        public static final String COLUMN_INFO_JSON = "info";
        public static final String COLUMN_TRAILERS_JSON = "trailers";
        public static final String COLUMN_REVIEWS_JSON = "reviews";
        public static final String COLUMN_POSTER = "poster";
    }

}
