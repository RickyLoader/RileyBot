package Plex;

import Movies.Movie;

/**
 * Movie & Plex details pairing
 */
public class PlexMovie {
    private final PlexDetails plexDetails;
    private Movie movie;

    /**
     * Create the Plex movie
     *
     * @param plexDetails Plex details about movie
     */
    public PlexMovie(PlexDetails plexDetails) {
        this.plexDetails = plexDetails;
    }

    /**
     * Set the movie details
     *
     * @param movie Movie details to set
     */
    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    /**
     * Check if the Plex movie has the movie details
     *
     * @return Plex movie has movie details
     */
    public boolean hasMovie() {
        return movie != null;
    }

    /**
     * Get the movie
     *
     * @return Movie
     */
    public Movie getMovie() {
        return movie;
    }

    /**
     * Get the Plex details about the movie
     *
     * @return Plex details
     */
    public PlexDetails getPlexDetails() {
        return plexDetails;
    }
}
