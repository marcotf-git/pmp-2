package com.example.androidstudio.popularmovies;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.androidstudio.popularmovies.data.MovieslistContract;
import com.example.androidstudio.popularmovies.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;


public class MainActivity extends AppCompatActivity
        implements GreenAdapter.ListItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderManager.LoaderCallbacks<String>{

    /* This number will uniquely identify our Loader and is chosen arbitrarily. */
    private static final int THEMOVIEDB_LOADER_ID = 22;

    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    private GreenAdapter mAdapter;
    private RecyclerView mMoviesList;

    // flag for preference updates
    private static boolean flag_preferences_updates = false;

    // This flag will control the app behaviour based on the type of the data being viewed and sorted
    private static boolean flag_show_favorites;

    private ItemTouchHelper mItemTouchHelper = null;

    private Cursor mData;

    // This variables will handle the saving and restoring of the recycler view state
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";
    private Parcelable mSavedRecyclerLayoutState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.v("onCreate", "on create");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register MainActivity as a OnSharedPreferenceChangedListener in onCreate
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        String moviesQueryOption = sharedPreferences.getString(this.getString(R.string.pref_sort_key), this.getString(R.string.pref_sort_popular));

        // Set the show favorites flag
        // This flag will control the app behaviour based on the type of the data being viewed and sorted
        if (moviesQueryOption.equals(this.getString(R.string.pref_sort_popular)) ||
                moviesQueryOption.equals(this.getString(R.string.pref_sort_top_rated))) {
            flag_show_favorites = false;
        } else if (moviesQueryOption.equals(this.getString(R.string.pref_sort_favorites))){
            flag_show_favorites = true;
        }

        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mMoviesList = findViewById(R.id.rv_movies);

        /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list depending on which parameter you pass in to the LinearLayoutManager
         * constructor. By default, if you don't specify an orientation, you get a vertical list.
         * In our case, we want a vertical list, so we don't need to pass in an orientation flag to
         * the LinearLayoutManager constructor.
         *
         * There are other LayoutManagers available to display your data in uniform grids,
         * staggered grids, and more! See the developer documentation for more details.
         *
         * We are using the GridLayoutManager.
         */
        int nColumns = numberOfColumns();
        GridLayoutManager layoutManager = new GridLayoutManager(this, nColumns);

        mMoviesList.setLayoutManager(layoutManager);

         /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mMoviesList.setHasFixedSize(true);

        /*
         * The GreenAdapter is responsible for displaying each item in the list.
         */
        mAdapter = new GreenAdapter(this, flag_show_favorites);
        mMoviesList.setAdapter(mAdapter);

        /*
         * This ID will uniquely identify the Loader. We can use it, for example, to get a handle
         * on our Loader at a later point in time through the support LoaderManager.
         */
        int loaderId = THEMOVIEDB_LOADER_ID;

        /*
         * From MainActivity, we have implemented the LoaderCallbacks interface with the type of
         * String array. (implements LoaderCallbacks<String[]>) The variable callback is passed
         * to the call to initLoader below. This means that whenever the loaderManager has
         * something to notify us of, it will do so through this callback.
         */
        LoaderManager.LoaderCallbacks<String> callback = MainActivity.this;

        /*
         * The second parameter of the initLoader method below is a Bundle. Optionally, you can
         * pass a Bundle to initLoader that you can then access from within the onCreateLoader
         * callback. In our case, we don't actually use the Bundle, but it's here in case we wanted
         * to.
         */
        Bundle bundleForLoader = null;

        /*
         *
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        if (!flag_show_favorites) {
            getSupportLoaderManager().initLoader(loaderId, bundleForLoader, callback);
        }

        updateView();

    }


    // This method is saving the state of the recycler view
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mMoviesList.getLayoutManager().onSaveInstanceState());
    }

    // This method is loading the saved state of the recycler view
    // There is also a call on the post execute method in the loader, for updating the view
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null)
        {
            mSavedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            //mMoviesList.getLayoutManager().onRestoreInstanceState(mSavedRecyclerLayoutState);
            // The state will be reloaded only in the loader
        }
    }


    /**
     * This method will make the View for the JSON data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showMoviesDataView() {
        // First, make sure the error is invisible
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        // Then, make sure the JSON data is visible
        mMoviesList.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the JSON
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        // First, hide the currently visible data
        mMoviesList.setVisibility(View.INVISIBLE);
        // Then, show the error
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
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
    public Loader<String> onCreateLoader(int id, final Bundle loaderArgs) {

        Log.v("onCreateLoader", "flag_preferences_updates:" + flag_preferences_updates);

        return new AsyncTaskLoader<String>(this) {

            /* This String will contain the raw JSON from the results of our search */
            private String mSearchJson = null;

            /**
             * Subclasses of AsyncTaskLoader must implement this to take care of loading their data.
             */
            @Override
            protected void onStartLoading() {

                if(mSearchJson != null){
                    deliverResult(mSearchJson);
                } else {
                    // This if is the solution founded to the error when we are in the detail activity
                    // and return to the main, when we are reading the favorites details, without internet,
                    // and after switching the sort order.
                    // This flag apparently stops the loading that the loader was doing.
                    if(!flag_show_favorites) {
                        mLoadingIndicator.setVisibility(View.VISIBLE);
                        forceLoad();
                    }
                }
            }

            /**
             * This is the method of the AsyncTaskLoader that will load the JSON data
             * from themoviedb.org in the background.
             *
             * @return Movies data from themoviedb.org as an String.
             *         null if an error occurs
             */
            @Override
            public String loadInBackground() {

                URL themoviedbSearchUrl = NetworkUtils.buildUrl(MainActivity.this);

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

    // When the load is finished, show either the data or an error message if there is no data.
    @Override
    public void onLoadFinished(Loader<String> loader, String themoviedbSearchResults) {

        Log.v("onLoadFinished", "loader id:" + loader.getId());

        mLoadingIndicator.setVisibility(View.INVISIBLE);

        if (themoviedbSearchResults != null && !themoviedbSearchResults.equals("")) {
            showMoviesDataView();
            MoviesBox moviesBox = new MoviesBox(themoviedbSearchResults);
            mAdapter.setMoviesHttpQueryData(moviesBox);

            // This will restore the state of the recycler view, only in case of the screen rotation.
            // If the user just updates the preferences, the state will not be restored.
            if (!flag_preferences_updates) {
                mMoviesList.getLayoutManager().onRestoreInstanceState(mSavedRecyclerLayoutState);
            }

        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    // In onStart, if preferences have been changed, refresh the data and set the flag to false
    @Override
    protected void onStart() {
        super.onStart();

        Log.v("onStart", "on start");

        if (flag_preferences_updates) {
            Log.v("onStart", "preferences changed");
            updateView();
            flag_preferences_updates = false;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

        // Prevents problems with database
        if(null != mData) {
            mData.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * This method sets the option menu that choose which kind of movie search will be executed,
     * if popular or top rated
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String queryOption = sharedPreferences.getString(this.getString(R.string.pref_sort_key),
                this.getString(R.string.pref_sort_popular));

        if (queryOption.equals(this.getString(R.string.pref_sort_popular))) {
            menu.findItem(R.id.select_popular).setChecked(true);
        }

        if (queryOption.equals(this.getString(R.string.pref_sort_top_rated))) {
            menu.findItem(R.id.select_top_rated).setChecked(true);
        }

        if (queryOption.equals(this.getString(R.string.pref_sort_favorites))) {
            menu.findItem(R.id.select_favorites).setChecked(true);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemThatWasClickedId = item.getItemId();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        switch (itemThatWasClickedId) {

            case R.id.select_popular:
                sharedPreferences.edit()
                        .putString(this.getString(R.string.pref_sort_key), this.getString(R.string.pref_sort_popular)).apply();
                break;

            case R.id.select_top_rated:
                sharedPreferences.edit()
                        .putString(this.getString(R.string.pref_sort_key), this.getString(R.string.pref_sort_top_rated)).apply();
                break;

            case R.id.select_favorites:
                sharedPreferences.edit()
                        .putString(this.getString(R.string.pref_sort_key), this.getString(R.string.pref_sort_favorites)).apply();
                break;
        }

        updateView();
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is used when we are resetting data, so that at one point in time during a
     * refresh of our data, you can see that there is no data showing.
     */
    private void invalidateData() {
        mAdapter = new GreenAdapter(this, flag_show_favorites);
        mMoviesList.setAdapter(mAdapter);
    }

    /**
     * Helper function to reload data and update the view
     */
    public void updateView() {

        Log.v("updateView", "updateView");

        // clear data on the adapter
        invalidateData();

        if(null != mItemTouchHelper) {
            Log.v("updateView", "detaching item touch helper");
            mItemTouchHelper.attachToRecyclerView(null);
        }

        // reload data, selecting by the type of data

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String moviesQueryOption = sharedPreferences
                .getString(this.getString(R.string.pref_sort_key), this.getString(R.string.pref_sort_popular));

        if (moviesQueryOption.equals(this.getString(R.string.pref_sort_popular)) ||
                moviesQueryOption.equals(this.getString(R.string.pref_sort_top_rated))) {

            Log.v("updateView", "popular or top_rated view update");

            // clear item touch helper (only used for local data), if exist
            if(null != mItemTouchHelper) {
                Log.v("updateView", "detaching item touch helper");
                mItemTouchHelper.attachToRecyclerView(null);
            }

            // reload data from http
            LoaderManager.LoaderCallbacks<String> callback = MainActivity.this;
            getSupportLoaderManager().restartLoader(THEMOVIEDB_LOADER_ID, null, callback);

        } else if (moviesQueryOption.equals(this.getString(R.string.pref_sort_favorites))){

            Log.v("updateView", "favorites view update");

            if(null != mData) {
                mData.close();
            }
            new MoviesFetchTask().execute();

            // Load item touch helper (is used for local data)
            ItemTouchHelper.Callback touchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT |
                    ItemTouchHelper.RIGHT) {

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                    // remove the movie
                    String movieId = (String) viewHolder.itemView.getTag();

                    new MovieDeleteTask().execute(movieId);

                    // update view
                    if(null != mData) {
                        mData.close();
                    }
                    new MoviesFetchTask().execute();

                }

            };

            mItemTouchHelper = new ItemTouchHelper(touchCallback);
            mItemTouchHelper.attachToRecyclerView(mMoviesList);

        }

        // Set the visibility for the view
        showMoviesDataView();
    }

    /**
     * This is where we receive our callback from
     * {@link com.example.androidstudio.popularmovies.GreenAdapter.ListItemClickListener}
     *
     * This callback is invoked when you click on an item in the list.
     *
     * @param clickedItemIndex Index in the list of the item that was clicked.
     */
    @Override
    public void onListItemClick(int clickedItemIndex,
                                String movieInfoStringJSON,
                                String movieTrailersStringJSON,
                                String movieReviewsStringJSON,
                                boolean showFavorites) {

        /*
         * Storing the Context in a variable in this case is redundant since we could have
         * just used "this" or "MainActivity.this" in the method call below. However, we
         * wanted to demonstrate what parameter we were using "MainActivity.this" for as
         * clear as possible.
         */
        Context context = MainActivity.this;

        /* This is the class that we want to start (and open) when the button is clicked. */
        Class destinationActivity = DetailActivity.class;

        /*
         * Here, we create the Intent that will start the Activity we specified above in
         * the destinationActivity variable. The constructor for an Intent also requires a
         * context, which we stored in the variable named "context".
         */
        Intent startChildActivityIntent = new Intent(context, destinationActivity);

        /*
         * We use the putExtra method of the Intent class to pass some extra stuff to the
         * Activity that we are starting. Generally, this data is quite simple, such as
         * a String or a number. However, there are ways to pass more complex objects.
         */
        startChildActivityIntent.putExtra("movieInfoStringJSON", movieInfoStringJSON);
        startChildActivityIntent.putExtra("movieTrailersStringJSON", movieTrailersStringJSON);
        startChildActivityIntent.putExtra("movieReviewsStringJSON", movieReviewsStringJSON);
        startChildActivityIntent.putExtra("showFavorites", showFavorites);

        /*
         * Once the Intent has been created, we can use Activity's method, "startActivity"
         * to start the DetailActivity.
         */
        startActivity(startChildActivityIntent);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        flag_preferences_updates = true;

        String moviesQueryOption = sharedPreferences.getString(this.getString(R.string.pref_sort_key),
                this.getString(R.string.pref_sort_popular));

        if (moviesQueryOption.equals(this.getString(R.string.pref_sort_popular)) ||
                moviesQueryOption.equals(this.getString(R.string.pref_sort_top_rated))) {
            flag_show_favorites = false;
        } else if (moviesQueryOption.equals(this.getString(R.string.pref_sort_favorites))){
            flag_show_favorites = true;
        }

    }


    // Use an async task to do the data fetch off of the main thread.
    public class MoviesFetchTask extends AsyncTask<Void, Void, Cursor> {

        // Invoked on a background thread
        @Override
        protected Cursor doInBackground(Void... params) {
            // Make the query to get the data
            // Get the content resolver
            ContentResolver resolver = getContentResolver();
            // Call the query method on the resolver with the correct Uri from the contract class
            Cursor cursor = resolver.query(MovieslistContract.MovieslistEntry.CONTENT_URI,
                    null, null, null, null);
            return cursor;
        }

        // Invoked on UI thread
        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            // Set the data for MainActivity
            mData = cursor;
            // Set the data for the adapter
            mAdapter.setMoviesCursorData(cursor);
        }
    }


    // Use an async task to do the data deletion off of the main thread.
    public class MovieDeleteTask extends AsyncTask<String, Void, Integer> {

        // Invoked on a background thread
        @Override
        protected Integer doInBackground(String... params) {

            // Make the query to get the data

            // Get the content resolver
            ContentResolver resolver = getContentResolver();
            String movieId = params[0];

            Uri deleteUri = MovieslistContract.MovieslistEntry.CONTENT_URI.buildUpon()
                    .appendPath(movieId).build();

            // Call the delete method on the resolver with the correct Uri from the contract class
            int rowsDeleted = resolver.delete(deleteUri, null, null);

            return rowsDeleted;
        }

        // Invoked on UI thread
        @Override
        protected void onPostExecute(Integer rowsDeleted) {
            super.onPostExecute(rowsDeleted);
            Log.v("onPostExecute","rows deleted:" + rowsDeleted);
        }

    }

    // Helper method for calc the number of columns based on screen
    private int numberOfColumns() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // You can change this divider to adjust the size of the poster
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2; //to keep the grid aspect

        return nColumns;
    }




}
