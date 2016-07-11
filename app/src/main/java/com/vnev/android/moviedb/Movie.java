package com.vnev.android.moviedb;


/**
 * Created by ddqqyyzz on 6/24/16.
 */
public class Movie {

    private String movieTitle, plot, userRating, releaseDate, posterUrl;

    public Movie(String title, String plot, String rating, String release, String poster) {
        this.movieTitle = title;
        this.plot = plot;
        this.userRating = rating;
        this.releaseDate = release;
        this.posterUrl = poster;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getPlot() {
        return plot;
    }

    public String getUserRating() {
        return userRating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getPoster() {
        return posterUrl;
    }
}
