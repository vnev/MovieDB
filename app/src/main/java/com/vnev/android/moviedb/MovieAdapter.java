package com.vnev.android.moviedb;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
public class MovieAdapter extends RecyclerView.Adapter
        <ListItemViewHolder> {

    private List<Movie> items;
    private Context context;

    MovieAdapter(List<Movie> data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null");
        }

        this.items = data;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_view_item,
                parent,
                false);
        context = parent.getContext();
        return new ListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {
        Movie movie = items.get(position);
        Picasso.with(context).load(movie.getPoster()).into(holder.poster);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
