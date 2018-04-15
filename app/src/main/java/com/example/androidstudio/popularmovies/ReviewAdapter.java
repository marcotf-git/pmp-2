package com.example.androidstudio.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This class creates a video adapter for the reviews list
 */

public class ReviewAdapter extends ArrayAdapter<Review>{

    ReviewAdapter(Context context, ArrayList<Review> videos){
        super(context, 0, videos);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.review_list_item, parent, false);
        }

        // Get the {@link Word} object located at this position in the list
        Review currentReview = getItem(position);

        TextView authorTextView = listItemView.findViewById(R.id.author_text_view);

        if(null != currentReview) {
            authorTextView.setText(currentReview.getAuthor());
            Log.v("getView", "setting text:" + currentReview.getAuthor());
        }



        return listItemView;
    }
}
