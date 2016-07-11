package com.vnev.android.moviedb;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivityFragment extends Fragment {

    private MovieAdapter adapter;
    private GridView view;
    private static int PAGE = 1;

    public MainActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem sortByPopularity = menu.findItem(R.id.sort_by_popularity);
        MenuItem sortByRating = menu.findItem(R.id.sort_by_rating);

        if (!sortByPopularity.isChecked()) {
            sortByPopularity.setChecked(true);
        }
        else if (!sortByRating.isChecked()) {
                sortByRating.setChecked(true);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.sort_by_popularity:
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                updateMovies("popular");
                return true;
            case R.id.sort_by_rating:
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                updateMovies("top_rated");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateMovies(String sort_type) {
        final MovieSyncTask fetchTask = new MovieSyncTask();
        fetchTask.execute(sort_type, Integer.toString(PAGE));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        adapter = new MovieAdapter(getActivity(),
                R.layout.grid_view_item,
                R.id.grid_item_img,
                new ArrayList<Movie>());
        view = (GridView) rootView.findViewById(R.id.gridview_movie);
        view.setAdapter(adapter);
        updateMovies("popular");

        return rootView;
    }

    public class MovieSyncTask extends AsyncTask<String, Void, Movie[]> {

        private final String API_KEY = "API_KEY";
        private final String LOG_TAG = MovieSyncTask.class.getSimpleName();
        final ProgressDialog fetchProgress = new ProgressDialog(getContext());

        private Movie[] getMovieDataFromJson(String movieJson) throws JSONException {
            final String MOVIE_LIST = "results";
            final String MOVIE_TITLE = "original_title";
            final String MOVIE_DESC = "overview";
            final String MOVIE_RATING = "vote_average";
            final String MOVIE_DATE = "release_date";
            final String MOVIE_POSTER_PATH = "poster_path";
            final String MOVIE_POSTER_BASE_URL = "http://image.tmdb.org/t/p/w342/";

            JSONObject jsonObject = new JSONObject(movieJson);
            JSONArray movieArray = jsonObject.getJSONArray(MOVIE_LIST);

            Movie[] result = new Movie[movieArray.length()];
            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movie = movieArray.getJSONObject(i);
                String relativePosterPath = movie.getString(MOVIE_POSTER_PATH);

                result[i] = new Movie(movie.getString(MOVIE_TITLE),
                        movie.getString(MOVIE_DESC),
                        movie.getString(MOVIE_RATING),
                        movie.getString(MOVIE_DATE),
                        (MOVIE_POSTER_BASE_URL + relativePosterPath));
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fetchProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            fetchProgress.setMessage("Loading...");
            fetchProgress.setIndeterminate(false);
            fetchProgress.setCancelable(false);
            fetchProgress.show();
        }

        @Override
        protected Movie[] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection connection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;

            try {
                final String BASE_URL = "https://api.themoviedb.org/3/movie/";
                final String API_QUERY = "api_key";
                final String PAGE_QUERY = "page";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendEncodedPath(params[0])
                        .appendQueryParameter(PAGE_QUERY, params[1])
                        .appendQueryParameter(API_QUERY, API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream stream = connection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (stream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(stream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.d(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Movie[] movies) {
            adapter.clear();
            if (movies != null) {
                for (Movie mv : movies) {
                    adapter.add(mv);
                }
                if (fetchProgress != null && fetchProgress.isShowing()) {
                    fetchProgress.dismiss();
                }
            }
        }

        @Override
        protected void onCancelled() {
            fetchProgress.dismiss();
            super.onCancelled();
        }
    }
}
