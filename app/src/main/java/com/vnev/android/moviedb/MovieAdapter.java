package com.vnev.android.moviedb;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ddqqyyzz on 6/24/16.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {

    public MovieAdapter(Context context, int resource, int textViewResourceId, List<Movie> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_view_item, parent,
                                                                    false);
        }

        ImageView poster_view = (ImageView) convertView.findViewById(R.id.grid_item_img);
        Picasso.with(getContext()).load(movie.getPoster()).into(poster_view);

//        ((TextView) convertView.findViewById(R.id.grid_item_title)).setText(movie.getMovieTitle());

        return convertView;
    }
}
