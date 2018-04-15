package com.example.androidstudio.popularmovies.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.example.androidstudio.popularmovies.BuildConfig;
import com.example.androidstudio.popularmovies.R;


/**
 * This class will store the main application preferences and url references.
 */
public class PopularMoviesPreferences {


    //private static final String API_KEY = "API_KEY_HERE";
    private static final String API_KEY = BuildConfig.MY_MOVIE_DB_API_KEY;

    private static final String THEMOVIEDB_BASE_URL = "https://api.themoviedb.org/3/";
    private static final String PATH_POPULAR_MOVIES = "movie/popular";
    private static final String PATH_TOP_RATED_MOVIES = "movie/top_rated";

    private static final String THEMOVIEDB_POSTER_URL = "https://image.tmdb.org/t/p/";
    private static final String DEFAULT_POSTER_SIZE = "w185";


    public static String getThemoviedbMoviesUrl(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String moviesQueryOption = sharedPreferences.getString(context.getString(R.string.pref_sort_key), "");

        switch (moviesQueryOption) {
            case "popular":
                return (THEMOVIEDB_BASE_URL + PATH_POPULAR_MOVIES+ "?api_key=" + API_KEY);
            case "top_rated":
                return (THEMOVIEDB_BASE_URL + PATH_TOP_RATED_MOVIES + "?api_key=" + API_KEY);
        }
        return null;

    }


    public static String getThemoviedbPosterUrl(String posterPath) {
        return (THEMOVIEDB_POSTER_URL + DEFAULT_POSTER_SIZE + "/" + posterPath);
    }

    public static String getThemoviedbVideosUrl(String movieId) {
        return (THEMOVIEDB_BASE_URL + "movie" + "/" + movieId + "/" + "videos" + "?api_key=" + API_KEY);
    }

    public static String getThemoviedbReviewsUrl(String movieId) {
        return (THEMOVIEDB_BASE_URL + "movie" + "/" + movieId + "/" + "reviews" + "?api_key=" + API_KEY);
    }

}
