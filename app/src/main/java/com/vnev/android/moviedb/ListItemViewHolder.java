package com.vnev.android.moviedb;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by ddqqyyzz on 7/11/16.
 */
public final class ListItemViewHolder extends RecyclerView.ViewHolder {
    ImageView poster;

    public ListItemViewHolder(View itemView) {
        super(itemView);
        poster = (ImageView) itemView.findViewById(R.id.grid_item_img);
    }
}
