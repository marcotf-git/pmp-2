package com.example.androidstudio.popularmovies.utilities;


import android.content.Context;
import android.net.Uri;

import com.example.androidstudio.popularmovies.data.PopularMoviesPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the themoviedb.org server
 */
public class NetworkUtils {

    /**
     * Builds the URL used to query themoviedb.org.
     *
     * @return The URL to use to query the weather server.
     */
    public static URL buildUrl(Context context) {

        String moviesQueryUrl = PopularMoviesPreferences.getThemoviedbMoviesUrl(context);

        URL url = null;

        if(null != moviesQueryUrl){
            Uri builtUri = Uri.parse(moviesQueryUrl);

            try {
                url = new URL(builtUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return url;
    }


    public static URL buildVideosUrl(String movieId) {

        String reviewQueryUrl = PopularMoviesPreferences.getThemoviedbVideosUrl(movieId);
        Uri builtUri = Uri.parse(reviewQueryUrl).buildUpon().build();
        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }


    public static URL buildReviewsUrl(String movieId) {

        String reviewQueryUrl = PopularMoviesPreferences.getThemoviedbReviewsUrl(movieId);
        Uri builtUri = Uri.parse(reviewQueryUrl).buildUpon().build();
        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }


    public static Uri buildTrailerUri(String videoKey, String videoSite) {

        String youtubeWatchUrl = "https://youtu.be/" + videoKey;
        Uri builtUri = Uri.parse(youtubeWatchUrl).buildUpon().build();
        return  builtUri;
    }


    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {

        if(null == url){
            return null;
        }

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();

            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }

        } finally {

            urlConnection.disconnect();
        }

    }
}
