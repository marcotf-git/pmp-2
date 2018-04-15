package com.example.androidstudio.popularmovies;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Class that has some movies titles
 */

class MoviesBox {

    private JSONArray movies;

    MoviesBox(String moviesJSONString) {

        try {
            JSONObject results = new JSONObject(moviesJSONString);
            movies = results.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    int getNumberOfMovies() {

        if (null != movies) {
            return movies.length();
        }

        return 0;
    }

    JSONObject getMovieJSON(int position) {

        JSONObject movieJSON;

        try {
            movieJSON = movies.getJSONObject(position);
            return movieJSON;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}


