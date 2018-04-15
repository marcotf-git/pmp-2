package com.example.androidstudio.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidstudio.popularmovies.data.MovieslistContract;
import com.example.androidstudio.popularmovies.data.MovieslistDbHelper;
import com.example.androidstudio.popularmovies.data.PopularMoviesPreferences;
import com.example.androidstudio.popularmovies.utilities.DatabaseUtils;
import com.example.androidstudio.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * This activity will render the movie details
 */
public class DetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String>{

    private TextView mDisplayTitle;
    private ImageView mDisplayPoster;
    private TextView mDisplayOverview;
    private TextView mDisplayVoteAverage;
    private TextView mDisplayReleaseDate;
    private Button mAddToFavorites;

    // The id and data from the movie being viewed in this activity
    private String movieId = "";
    private String movieTitle = "";

    private String movieInfoStringJSON;
    private String movieTrailersStringJSON;
    private String movieReviewsStringJSON;

    private byte[] posterImageArray;

    // The array for storing information about the trailers (videos)
    private final ArrayList<Video> videos = new ArrayList<>();

    // The array for storing information about the trailers (videos)
    private final ArrayList<Review> reviews = new ArrayList<>();

    /* This number will uniquely identify our Loader and is chosen arbitrarily. */
    private static final int THEMOVIEDB_TRAILERS_LOADER_ID = 23;

    /* This number will uniquely identify our Loader and is chosen arbitrarily. */
    private static final int THEMOVIEDB_REVIEWS_LOADER_ID = 24;

    // flag that controls if is showing the favorites (will load from stored JSON)
    private boolean showingFavorites;

    private SQLiteDatabase mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDisplayTitle = findViewById(R.id.tv_title);
        mDisplayPoster = findViewById(R.id.iv_poster);
        mDisplayOverview = findViewById(R.id.tv_overview);
        mDisplayVoteAverage = findViewById(R.id.tv_vote_average);
        mDisplayReleaseDate = findViewById(R.id.tv_release_date);

        mAddToFavorites = findViewById(R.id.bt_add_favorites);

        /*
         * Here is where all the magic happens. The getIntent method will give us the Intent that
         * started this particular Activity.
         */
        Intent intentThatStartedThisActivity = getIntent();

         /*
         * Although there is always an Intent that starts any particular Activity, we can't
         * guarantee that the extra we are looking for was passed as well. Because of that, we need
         * to check to see if the Intent has the extra that we specified when we created the
         * Intent that we use to start this Activity. Note that this extra may not be present in
         * the Intent if this Activity was started by any other method.
         * */
        if (intentThatStartedThisActivity.hasExtra("movieInfoStringJSON")) {

            /*
             * Now that we've checked to make sure the extra we are looking for is contained within
             * the Intent, we can extract the extra. To do that, we simply call the getStringExtra
             * method on the Intent. There are various other get*Extra methods you can call for
             * different types of data. Please feel free to explore those yourself.
             */
            movieInfoStringJSON = intentThatStartedThisActivity.getStringExtra("movieInfoStringJSON");
            movieTrailersStringJSON = intentThatStartedThisActivity.getStringExtra("movieTrailersStringJSON");
            movieReviewsStringJSON = intentThatStartedThisActivity.getStringExtra("movieReviewsStringJSON");

            showingFavorites = intentThatStartedThisActivity.getBooleanExtra("showFavorites", false);


            Log.v("onCreate", "movieTrailersStringJSON:" + "-" + movieTrailersStringJSON + "-");
            Log.v("onCreate", "movieReviewsStringJSON:" + "-" + movieReviewsStringJSON + "-");

            updateInfoView(movieInfoStringJSON);


            if (showingFavorites) {

                Log.v("onCreate", "update view for favorites");

                mAddToFavorites.setVisibility(View.INVISIBLE);

                // Load trailers and reviews from the stored JSON string
                if(null != movieTrailersStringJSON && !movieTrailersStringJSON.equals("")) {
                    updateTrailersView(movieTrailersStringJSON);
                }

                if(null != movieReviewsStringJSON && !movieReviewsStringJSON.equals("")) {
                    updateReviewsView(movieReviewsStringJSON);
                }

            } else {

                Log.v("onCreate", "update view for popular or top rated");

                // Load the trailers information (based on movieId), store on videos array, and show on list view
                int videosLoaderId = THEMOVIEDB_TRAILERS_LOADER_ID;
                LoaderManager.LoaderCallbacks<String> callbackVideosLoader = DetailActivity.this;
                Bundle bundleForVideosLoader = null;
                getSupportLoaderManager().initLoader(videosLoaderId, bundleForVideosLoader, callbackVideosLoader);

                // Load the reviews information (based on movieId), store on reviews array, and show on list view
                int reviewsLoaderId = THEMOVIEDB_REVIEWS_LOADER_ID;
                LoaderManager.LoaderCallbacks<String> callbackReviewsLoader = DetailActivity.this;
                Bundle bundleForReviewsLoader = null;
                getSupportLoaderManager().initLoader(reviewsLoaderId, bundleForReviewsLoader, callbackReviewsLoader);

            }

        }

    }


    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id The ID whose loader is to be created.
     * @param loaderArgs Any arguments supplied by the caller.
     *
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<String> onCreateLoader(final int id, final Bundle loaderArgs) {


        return new AsyncTaskLoader<String>(this) {

            /* This String will contain the raw JSON from the results of our search */
            String mSearchJson = null;

            /**
             * Subclasses of AsyncTaskLoader must implement this to take care of loading their data.
             */
            @Override
            protected void onStartLoading() {
                if (mSearchJson != null) {
                    deliverResult(mSearchJson);
                } else {
                    //mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            /**
             * This is the method of the AsyncTaskLoader that will load the JSON data
             * from themoviedb.org in the background.
             *
             * @return Movies data from themoviedb.org as an String.
             * null if an error occurs
             */
            @Override
            public String loadInBackground() {

                URL themoviedbSearchUrl;

                if (id == THEMOVIEDB_TRAILERS_LOADER_ID) {
                    themoviedbSearchUrl = NetworkUtils.buildVideosUrl(movieId);
                } else if (id == THEMOVIEDB_REVIEWS_LOADER_ID) {
                    themoviedbSearchUrl = NetworkUtils.buildReviewsUrl(movieId);
                } else {
                    return null;
                }

                try {
                    String themoviedbSearchResults = NetworkUtils.getResponseFromHttpUrl(themoviedbSearchUrl);
                    return themoviedbSearchResults;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            /**
             * Sends the result of the load to the registered listener.
             *
             * @param data The result of the load
             */
            @Override
            public void deliverResult(String data) {
                mSearchJson = data;
                super.deliverResult(data);
            }
        };

    }

    /* When the load is finished, show either the data or an error message if there is no data. */
    @Override
    public void onLoadFinished(Loader<String> loader, String themoviedbSearchResults) {

        //mLoadingIndicator.setVisibility(View.INVISIBLE);

        if (loader.getId() == THEMOVIEDB_TRAILERS_LOADER_ID) {

            if (themoviedbSearchResults != null && !themoviedbSearchResults.equals("")) {
                movieTrailersStringJSON = themoviedbSearchResults;
                updateTrailersView(themoviedbSearchResults);
            } else {
                //showErrorMessage();
                Log.v("onLoadFinished", "error on loading trailers");
            }

        } else if (loader.getId() == THEMOVIEDB_REVIEWS_LOADER_ID) {

            if (themoviedbSearchResults != null && !themoviedbSearchResults.equals("")) {
                movieReviewsStringJSON = themoviedbSearchResults;
                updateReviewsView(themoviedbSearchResults);
            } else {
                //showErrorMessage();
                Log.v("onLoadFinished", "error on loading reviews");
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }


    public void updateInfoView(String infoStringJSON){

        Context context = DetailActivity.this;

        try {
            JSONObject movieJSON = new JSONObject(infoStringJSON);

            // Extract the movie id
            movieId = movieJSON.getString("id");

            // Update views
            movieTitle = movieJSON.getString("title");
            mDisplayTitle.setText(movieTitle);

            String movieOverview = movieJSON.getString("overview");
            mDisplayOverview.setText(movieOverview);

            String movieVoteAverage = movieJSON.getString("vote_average");
            mDisplayVoteAverage.setText(movieVoteAverage);

            String movieReleaseDate = movieJSON.getString("release_date");
            mDisplayReleaseDate.setText(movieReleaseDate);

            // Load the poster and update poster view
            if(!showingFavorites) {
                try {

                    String posterPath = movieJSON.getString("poster_path");
                    String urlString = PopularMoviesPreferences.getThemoviedbPosterUrl(posterPath);

                    Picasso.with(context)
                            .load(urlString)
                            .placeholder(R.mipmap.ic_launcher)
                            .into(mDisplayPoster);

                    Bitmap poster = ((BitmapDrawable) mDisplayPoster.getDrawable()).getBitmap();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    poster.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    posterImageArray = outputStream.toByteArray();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

                Bitmap poster = DatabaseUtils.loadPosterFromDatabase(context, movieId);
                mDisplayPoster.setImageBitmap(poster);

            }

            if(isMovieInFavorites(movieTitle)){
                mAddToFavorites.setEnabled(false);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void updateTrailersView(String trailersStringJSON){

        VideosBox videosBox = new VideosBox(trailersStringJSON);

        Log.v("updateTrailersView", "movie id:" + movieId);
        Log.v("updateTrailersView", "videos search results:" + trailersStringJSON);

        int nVideos = videosBox.getNumberOfVideos();

        for (int i = 0; i < nVideos; i++) {

            JSONObject videoJSON = videosBox.getVideoJSON(i);
            String videoKey;
            String videoName;
            String videoSite;
            String videoType;

            try {

                videoKey = videoJSON.getString("key");
                Log.v("updateTrailersView", "video key:" + videoKey);

                videoName = videoJSON.getString("name");
                videoSite = videoJSON.getString("site");
                videoType = videoJSON.getString("type");

                if (videoType.equals("Trailer")) {
                    Video video = new Video(videoKey, videoName, videoSite);
                    videos.add(video);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        // At this point, we have an Array with the videos information
        Log.v("updateTrailersView", " videos array:" + videos);

        // Set the adapter to show the array on the list view
        VideoAdapter videoAdapter = new VideoAdapter(this, videos);
        ListView listView = findViewById(R.id.videos_list);
        listView.setAdapter(videoAdapter);

        // Adjust the size of the list view to show all the trailers
        float listItemheight = getResources().getDimension(R.dimen.list_item_height);
        Log.v("updateTrailersView", " list item height:" + listItemheight);
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int totalHeight = Math.round(listItemheight) * (videos.size());
        params.height = totalHeight;
        Log.v("updateTrailersView", " total list item height:" + totalHeight);
        listView.setLayoutParams(params);

        // Set listener to play trailer
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Video video = videos.get(position);

                String videoKey = video.getVideoKey();
                String videoSite = video.getVideoSite();

                Uri trailerUri = NetworkUtils.buildTrailerUri(videoKey, videoSite);

                Log.v("onItemClick", "trailerUri:" + trailerUri);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(trailerUri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }

        });
    }


    public void updateReviewsView(String reviewsStringJSON){

        ReviewsBox reviewsBox = new ReviewsBox(reviewsStringJSON);

        Log.v("updateReviewsView", "movie id:" + movieId);
        Log.v("updateReviewsView", "reviews search results:" + reviewsStringJSON);

        int nReviews = reviewsBox.getNumberOfReviews();

        for (int i = 0; i < nReviews; i++) {

            JSONObject reviewJSON = reviewsBox.getReviewJSON(i);
            String reviewAuthor;
            String reviewContent;
            String reviewUrl;

            try {

                reviewAuthor = reviewJSON.getString("author");
                Log.v("updateReviewsView", "video author:" + reviewAuthor);

                reviewContent = reviewJSON.getString("content");
                reviewUrl = reviewJSON.getString("url");

                Review review = new Review(reviewAuthor, reviewContent, reviewUrl);
                reviews.add(review);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // At this point, we have an Array with the videos information
        Log.v("updateReviewsView", " reviews array:" + reviews);

        // Set the adapter to show the array on the list view
        ReviewAdapter reviewAdapter = new ReviewAdapter(this, reviews);
        ListView listView = findViewById(R.id.reviews_list);
        listView.setAdapter(reviewAdapter);

        // Adjust the size of the list view to show all the trailers
        float listItemHeight = getResources().getDimension(R.dimen.list_item_height);
        Log.v("updateReviewsView", " list item height:" + listItemHeight);
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int totalHeight = Math.round(listItemHeight) * (reviews.size());
        params.height = totalHeight;
        Log.v("updateReviewsView", " total list item height:" + totalHeight);
        listView.setLayoutParams(params);

        // Set listener to read reviews
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Review review = reviews.get(position);

                String reviewContent = review.getContent();
                String reviewUrl = review.getUrl();

                Log.v("onItemClick", "reviewUrl:" + reviewUrl);

                Context context = DetailActivity.this;
                Class destinationActivity = ReviewsActivity.class;
                Intent startChildActivityIntent = new Intent(context, destinationActivity);

                startChildActivityIntent.putExtra("reviewContent", reviewContent);
                startChildActivityIntent.putExtra("reviewUrl", reviewUrl);

                startActivity(startChildActivityIntent);
            }

        });
    }


    /**
     * This method is called when user clicks on the Add fo favoriteslist button
     * @param view The calling view (button)
     */
    public void addToFavoriteslist(View view){

        long added = addNewMovie();

        if (added > 0) {
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
            mAddToFavorites.setEnabled(false);
        } else {
            Toast.makeText(this, "Error: the item was not added!", Toast.LENGTH_SHORT).show();
        }

    }


    private long addNewMovie() {

        MovieslistDbHelper dbHelper = new MovieslistDbHelper(this);
        mDb = dbHelper.getWritableDatabase();

        // This is necessary to pass the values onto the insert query
        ContentValues cv = new ContentValues();

        cv.put(MovieslistContract.MovieslistEntry.COLUMN_MOVIE_TITLE, movieTitle);
        cv.put(MovieslistContract.MovieslistEntry.COLUMN_MOVIE_ID, movieId);
        cv.put(MovieslistContract.MovieslistEntry.COLUMN_INFO_JSON, movieInfoStringJSON);
        cv.put(MovieslistContract.MovieslistEntry.COLUMN_TRAILERS_JSON, movieTrailersStringJSON);
        cv.put(MovieslistContract.MovieslistEntry.COLUMN_REVIEWS_JSON, movieReviewsStringJSON);
        cv.put(MovieslistContract.MovieslistEntry.COLUMN_POSTER, posterImageArray);

        Log.v("addNewMovie", " size of the poster byte array:" + posterImageArray.length);

        long rows = -1;

        try
        {
            mDb.beginTransaction();
            rows = mDb.insert(MovieslistContract.MovieslistEntry.TABLE_NAME, null, cv);
            mDb.setTransactionSuccessful();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally
        {
            mDb.endTransaction();
        }

        mDb.close();

        return rows;

    }


    private boolean isMovieInFavorites(String movieTitle){

        MovieslistDbHelper dbHelper = new MovieslistDbHelper(this);
        mDb = dbHelper.getReadableDatabase();

        String[] selectionArgs = { movieTitle };

        Cursor cursor = mDb.query(
                MovieslistContract.MovieslistEntry.TABLE_NAME,
                null,
                MovieslistContract.MovieslistEntry.COLUMN_MOVIE_TITLE + " = ? ",
                selectionArgs,
                null,
                null,
                null
        );

        long count =  cursor.getCount();

        cursor.close();

        return (count > 0);
    }


 }
