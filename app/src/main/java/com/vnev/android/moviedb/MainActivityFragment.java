package com.vnev.android.moviedb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
    private static int PAGE = 1;
    private static final String[] SORT_OPTIONS = new String[]{"Popular", "Top Rated"};
    private static int sort_type = 0;

    public MainActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Sort by");

            builder.setItems(MainActivityFragment.SORT_OPTIONS, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivityFragment.sort_type = i;
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        adapter = new MovieAdapter(getActivity(),
                R.layout.grid_view_item,
                R.id.grid_item_img,
                new ArrayList<Movie>());
        GridView view = (GridView) rootView.findViewById(R.id.gridview_movie);
        view.setAdapter(adapter);

        final MovieSyncTask task = new MovieSyncTask();
        task.execute(SORT_OPTIONS[sort_type].toLowerCase(), Integer.toString(PAGE));

//        view.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView absListView, int i) {
//
//            }
//
//            @Override
//            public void onScroll(AbsListView absListView, int firstCount, int visibleCount,
//                                 int totalCount) {
//                if (firstCount + visibleCount >= totalCount) {
//                    PAGE += 1;
//                    task.execute("popular", Integer.toString(PAGE));
//                }
//            }
//        });

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
            fetchProgress.setMessage("Loading movies...");
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
                        .appendPath(params[0])
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
            if (movies != null) {
//                adapter.clear();
                for (Movie mv : movies) {
                    adapter.add(mv);
                }
                if (fetchProgress != null && fetchProgress.isShowing()) {
                    fetchProgress.dismiss();
                }
            }
        }
    }
}
