package Movies;

/**
 * Hold various movie rating Ids
 */
public class RatingIds {
    private final RatingId imdbId, tmdbId;

    /**
     * Initialise the rating IDs from the builder values
     *
     * @param builder Builder to use values from
     */
    private RatingIds(RatingIdsBuilder builder) {
        this.imdbId = builder.imdbId;
        this.tmdbId = builder.tmdbId;
    }

    /**
     * Get the IMDB ID
     *
     * @return IMDB ID
     */
    public RatingId getImdbId() {
        return imdbId;
    }

    /**
     * Get the TMDB ID
     *
     * @return TMDB ID
     */
    public RatingId getTmdbId() {
        return tmdbId;
    }

    /**
     * Check if the TMDB ID is available
     *
     * @return TMDB ID is available
     */
    public boolean hasTmdbId() {
        return tmdbId != null;
    }

    /**
     * Check if the IMDB ID is available
     *
     * @return IMDB ID is available
     */
    public boolean hasImdbId() {
        return imdbId != null;
    }

    public static class RatingIdsBuilder {
        private RatingId imdbId, tmdbId;

        /**
         * Set the IMDB ID (Internet Movie Database)
         *
         * @param imdbId IMDB ID
         * @return Builder
         */
        public RatingIdsBuilder setImdbId(String imdbId) {
            this.imdbId = new RatingId(imdbId, "https://www.imdb.com/title/" + imdbId);
            return this;
        }

        /**
         * Set the TMDB ID (The Movie Database)
         *
         * @param tmdbId TMDB ID
         * @return Builder
         */
        public RatingIdsBuilder setTmdbId(String tmdbId) {
            this.tmdbId = new RatingId(tmdbId, "https://www.themoviedb.org/movie/" + tmdbId);
            return this;
        }

        /**
         * Create the RatingIds using the builder values
         *
         * @return RatingsIds from builder values
         */
        public RatingIds build() {
            return new RatingIds(this);
        }
    }

    /**
     * Rating ID & URL pair
     */
    public static class RatingId {
        private final String id, url;

        /**
         * Create the rating ID
         *
         * @param id  Rating ID - IMDB ID etc
         * @param url URL to the rating page
         */
        public RatingId(String id, String url) {
            this.id = id;
            this.url = url;
        }

        /**
         * Get the URL to the rating
         *
         * @return URL to rating
         */
        public String getUrl() {
            return url;
        }

        /**
         * Get the rating ID
         *
         * @return Rating ID
         */
        public String getId() {
            return id;
        }
    }
}
