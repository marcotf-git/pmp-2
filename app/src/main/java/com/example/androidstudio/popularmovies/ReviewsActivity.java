package com.example.androidstudio.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

public class ReviewsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        TextView mTextReview;
        mTextReview = findViewById(R.id.tv_content_review);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity.hasExtra("reviewContent")) {

            String reviewJSONtoString = intentThatStartedThisActivity.getStringExtra("reviewContent");

            Log.v("onCreate", "reviewContent:" + reviewJSONtoString);

            mTextReview.setText(reviewJSONtoString);
        }

    }

}
