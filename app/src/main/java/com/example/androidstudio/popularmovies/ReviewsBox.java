package com.example.androidstudio.popularmovies;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class will store the schema for the ReviewBox object
 */

class ReviewsBox {

    private JSONArray reviews;

    ReviewsBox(String reviewsJSONString) {

        try {
            if(null != reviewsJSONString && !reviewsJSONString.equals("")) {
                JSONObject results = new JSONObject(reviewsJSONString);
                reviews = results.getJSONArray("results");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    int getNumberOfReviews() {

        if (null != reviews) {
            return reviews.length();
        }

        return 0;
    }

    JSONObject getReviewJSON(int position) {

        JSONObject reviewJSON;

        try {
            if(null != reviews) {
                reviewJSON = reviews.getJSONObject(position);
                return reviewJSON;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}
