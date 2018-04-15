package com.example.androidstudio.popularmovies;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class will store the schema for the VideoBox object
 */

class VideosBox {

    private JSONArray videos;

    VideosBox(String moviesJSONString) {

        try {
            if(null != moviesJSONString && !moviesJSONString.equals("")) {
                JSONObject results = new JSONObject(moviesJSONString);
                videos = results.getJSONArray("results");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    int getNumberOfVideos() {

        if (null != videos) {
            return videos.length();
        }

        return 0;
    }

    JSONObject getVideoJSON(int position) {

        JSONObject videoJSON;

        try {
            if(null != videos) {
                videoJSON = videos.getJSONObject(position);
                return videoJSON;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}
