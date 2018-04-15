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
 * This class creates an adapter for showing the trailers list.
 */

public class VideoAdapter extends ArrayAdapter<Video>{

    VideoAdapter(Context context, ArrayList<Video> videos){
        super(context, 0, videos);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.video_list_item, parent, false);
        }

        // Get the {@link Word} object located at this position in the list
        Video currentVideo = getItem(position);

        TextView nameTextView = listItemView.findViewById(R.id.name_text_view);

        if (null != currentVideo) {
            nameTextView.setText(currentVideo.getVideoName());
            Log.v("getView", "setting text:" + currentVideo.getVideoName());
        }

        return listItemView;
    }
}
