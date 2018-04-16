package com.example.androidstudio.popularmovies;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.androidstudio.popularmovies.data.MovieslistContract;
import com.example.androidstudio.popularmovies.data.PopularMoviesPreferences;
import com.example.androidstudio.popularmovies.utilities.DatabaseUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Using Recycler VIew and ViewHolders
 */

public class GreenAdapter extends RecyclerView.Adapter<GreenAdapter.MovieViewHolder> {

    private static final String TAG = GreenAdapter.class.getSimpleName();

    // Store the count of items to be displayed in the recycler view
    private static int viewHolderCount;

    // Store the data to be displayed
    private MoviesBox moviesBox;
    private Cursor moviesCursor;

    // Store what type of the data to be displayed
    private boolean showFavorites;


    /**
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    final private ListItemClickListener mOnClickListener;

    /**
     * The interface that receives onClick messages.
     */
    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex,
                             String movieInfoStringJSON,
                             String movieTrailersStringJSON,
                             String movieReviewsStringJSON,
                             boolean showFavorites);
    }


    /**
     * Constructor for GreenAdapter that accepts a number of items to display and the specification
     * for the ListItemClickListener.
     *
     * @param listener Listener for list item clicks
     */
    GreenAdapter(ListItemClickListener listener, boolean showFavorites) {
        mOnClickListener = listener;
        this.showFavorites = showFavorites;
        viewHolderCount = 0;
    }

    /**
     *
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new NumberViewHolder that holds the View for each list item
     */
    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();

        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        MovieViewHolder viewHolder = new MovieViewHolder(view);

        viewHolderCount++;
        Log.d(TAG, "onCreateViewHolder: number of ViewHolders created: "
                + viewHolderCount);

        return viewHolder;
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the correct
     * indices in the list for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(final MovieViewHolder holder, int position) {

        Log.d(TAG, "#" + position);

        if (!showFavorites) {

            JSONObject movieJSON = moviesBox.getMovieJSON(position);

            String posterPath;
            final String posterTitle;

            try {
                posterPath = movieJSON.getString("poster_path");
                posterTitle = movieJSON.getString("title");

                String urlString = PopularMoviesPreferences.getThemoviedbPosterUrl(posterPath);

                Log.v(TAG, "urlString:" + urlString);

                /*
                 * Use the call back of picasso to manage the error in loading poster.
                 * On error, write the movie title in the text view that is together with the
                 * image view, and make it visible.
                 */
                Picasso.with(holder.context)
                        .load(urlString)
                        .placeholder(R.drawable.ic_poster)
                        .into(holder.posterImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                Log.v(TAG, "Poster loaded");
                                holder.posterTextView.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onError() {
                                Log.e(TAG, "Error in loading poster");
                                holder.posterTextView.setText(posterTitle);
                                holder.posterTextView.setVisibility(View.VISIBLE);
                            }
                        });

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {

            Log.v("onBindViewHolder", " binding for favorites");

            // Move the cursor to the point that have the data we are interested in
            if(!moviesCursor.moveToPosition(position))
                return;

            String movieId = moviesCursor.getString(moviesCursor.
                    getColumnIndex(MovieslistContract.MovieslistEntry.COLUMN_MOVIE_ID));
            Bitmap poster = DatabaseUtils.loadPosterFromDatabase(holder.context, movieId);
            String movieTitle = moviesCursor.getString(moviesCursor.
                    getColumnIndex(MovieslistContract.MovieslistEntry.COLUMN_MOVIE_TITLE));

            if (null != poster) {
                holder.posterImageView.setImageBitmap(poster);
            } else {
                holder.posterTextView.setText(movieTitle);
                holder.posterTextView.setVisibility(View.VISIBLE);
            }

            holder.itemView.setTag(movieId);
        }
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {

        if (!showFavorites) {
            if (null == moviesBox) return 0;
            return moviesBox.getNumberOfMovies();
        } else {

            Log.v("getItemCount", " getting items from cursor");

            if (null != moviesCursor) {
                return moviesCursor.getCount();
            } else {
                return 0;
            }
        }
    }

    /**
     * This method is used to set the movies list on a GreenAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new GreenAdapter to display it.
     *
     * @param moviesBox The object with the result of the query to the themoviedb.org to be displayed.
     */
    void setMoviesHttpQueryData(MoviesBox moviesBox) {
        this.moviesBox = moviesBox;
        notifyDataSetChanged();
    }


    void setMoviesCursorData(Cursor cursor) {
        this.moviesCursor = cursor;
        notifyDataSetChanged();
    }


    /**
     * Cache of the children views for a list item.
     */
    class MovieViewHolder extends RecyclerView.ViewHolder
            implements OnClickListener {

        ImageView posterImageView;
        TextView posterTextView;
        Context context;

        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         * @param itemView The View that you inflated in
         *                 {@link GreenAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        private MovieViewHolder(View itemView) {

            super(itemView);

            context = itemView.getContext();
            posterImageView = itemView.findViewById(R.id.iv_main_poster);
            posterTextView = itemView.findViewById(R.id.tv_main_poster);

            // Call setOnClickListener on the View passed into the constructor (use 'this' as the OnClickListener)
            itemView.setOnClickListener(this);
        }

        /**
         * Called whenever a user clicks on an item in the list.
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {

            // Test if preferences is for favorite movies
            // If it is, start new activity to show data from database

            if (!showFavorites) {
                Log.v("GreenAdapter onClick", " for no favorites");
                int clickedPosition = getAdapterPosition();

                JSONObject movieJSON;
                movieJSON = moviesBox.getMovieJSON(clickedPosition);

                mOnClickListener.onListItemClick(
                        clickedPosition,
                        movieJSON.toString(),
                        "",
                        "",
                        showFavorites);

                Log.v("GreenAdapter onClick", " movieJSON:" + movieJSON.toString());

            } else {

                Log.v("GreenAdapter onClick", " for favorites");

                JSONObject movieJSON = new JSONObject();

                // Pass the actual data to the JSON object
                int clickedPosition = getAdapterPosition();

                // Move the cursor to the point that have the data we are interested in
                if(!moviesCursor.moveToPosition(clickedPosition))
                    return;

                String movieInfo = "";
                String movieTrailers = "";
                String movieReviews = "";

                int index = moviesCursor.getColumnIndex(MovieslistContract.MovieslistEntry.COLUMN_INFO_JSON);
                if (index>0){
                    movieInfo = moviesCursor.getString(index);
                }

                index = moviesCursor.getColumnIndex(MovieslistContract.MovieslistEntry.COLUMN_TRAILERS_JSON);
                if (index>0){
                    movieTrailers = moviesCursor.getString(index);
                }


                index = moviesCursor.getColumnIndex(MovieslistContract.MovieslistEntry.COLUMN_REVIEWS_JSON);
                if (index>0){
                    movieReviews = moviesCursor.getString(index);
                }

                Log.v("GreenAdapter onClick", " movieJSON:" + movieJSON.toString());

                mOnClickListener.onListItemClick(
                        clickedPosition,
                        movieInfo,
                        movieTrailers,
                        movieReviews,
                        showFavorites);
            }

        }
    }

}
