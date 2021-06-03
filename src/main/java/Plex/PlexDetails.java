package Plex;

import Movies.RatingIds;

import javax.annotation.Nullable;

/**
 * Basic Plex movie details
 */
public class PlexDetails {
    private final String title, plexUrl;
    private final RatingIds ratingIds;
    private final boolean onPlex;

    /**
     * Create the Plex details
     *
     * @param ratingIds Rating Ids of the movie (IMDB, TMDB, etc)
     * @param title     Title of the movie
     * @param plexUrl   URL to the movie on Plex
     * @param onPlex    Movie is on plex
     */
    public PlexDetails(RatingIds ratingIds, String title, @Nullable String plexUrl, boolean onPlex) {
        this.ratingIds = ratingIds;
        this.title = title;
        this.plexUrl = plexUrl;
        this.onPlex = onPlex;
    }

    /**
     * Get the title of the movie
     *
     * @return Movie title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Check if the movie is on Plex
     *
     * @return Movie is on Plex
     */
    public boolean isOnPlex() {
        return onPlex;
    }

    /**
     * Get the URL to the movie on Plex (if available on Plex)
     *
     * @return URL to the movie on Plex
     */
    public String getPlexUrl() {
        return plexUrl;
    }

    /**
     * Get the rating Ids of the movie
     *
     * @return Rating ids
     */
    public RatingIds getRatingIds() {
        return ratingIds;
    }
}
