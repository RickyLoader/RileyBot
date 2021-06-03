package Movies;

/**
 * Movie social connections - Facebook, Youtube etc
 */
public class SocialConnections {
    private final String trailerUrl, facebookUrl;

    /**
     * Initialise the social connections with the builder values
     *
     * @param builder Builder to use values from
     */
    private SocialConnections(SocialConnectionsBuilder builder) {
        this.trailerUrl = builder.trailerUrl;
        this.facebookUrl = builder.facebookUrl;
    }

    /**
     * Check if the movie has a Facebook page URL
     *
     * @return Movie has a Facebook page URL
     */
    public boolean hasFacebookUrl() {
        return facebookUrl != null;
    }

    /**
     * Check if the movie has a trailer URL
     *
     * @return Movie has trailer URL
     */
    public boolean hasTrailerUrl() {
        return trailerUrl != null;
    }

    /**
     * Get the URL to the movie's Facebook page
     *
     * @return URL to movie Facebook page
     */
    public String getFacebookUrl() {
        return facebookUrl;
    }

    /**
     * Get the URL to the movie trailer on Youtube
     *
     * @return URL to movie trailer
     */
    public String getTrailerUrl() {
        return trailerUrl;
    }

    public static class SocialConnectionsBuilder {
        private String facebookUrl, trailerUrl;

        /**
         * Set the URL to the movie's Facebook page
         *
         * @param facebookId ID of movie Facebook page
         * @return Builder
         */
        public SocialConnectionsBuilder setFacebookUrl(String facebookId) {
            this.facebookUrl = "https://www.facebook.com/" + facebookId;
            return this;
        }

        /**
         * Set the URL to the movie trailer
         *
         * @param youtubeId Movie trailer youtubeId
         * @return Builder
         */
        public SocialConnectionsBuilder setTrailerUrl(String youtubeId) {
            this.trailerUrl = "https://www.youtube.com/watch?v=" + youtubeId;
            return this;
        }

        public SocialConnections build() {
            return new SocialConnections(this);
        }
    }
}
