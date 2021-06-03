package Movies;

/**
 * Movie crew
 */
public class Crew {
    private final String[] directors, cast;

    /**
     * Initialise the movie crew with the builder values
     *
     * @param builder Builder to use values from
     */
    private Crew(CrewBuilder builder) {
        this.directors = builder.directors;
        this.cast = builder.cast;
    }

    /**
     * Check if the crew has any cast members
     *
     * @return Crew has cast members
     */
    public boolean hasCast() {
        return cast.length > 0;
    }

    /**
     * Check if the crew has any directors
     *
     * @return Crew has director(s)
     */
    public boolean hasDirector() {
        return directors.length > 0;
    }

    /**
     * Get an array of cast members
     *
     * @return Array of cast members
     */
    public String[] getCast() {
        return cast;
    }

    /**
     * Get an array of directors
     *
     * @return Array of directors
     */
    public String[] getDirectors() {
        return directors;
    }

    public static class CrewBuilder {
        private String[] directors, cast;

        /**
         * Set the directors of the movie
         *
         * @param directors Array of directors
         * @return Builder
         */
        public CrewBuilder setDirectors(String[] directors) {
            this.directors = directors;
            return this;
        }

        /**
         * Set the cast of the movie
         *
         * @param cast Array of cast members
         * @return Builder
         */
        public CrewBuilder setCast(String[] cast) {
            this.cast = cast;
            return this;
        }

        /**
         * Create the crew from the builder values
         *
         * @return Crew from builder values
         */
        public Crew build() {
            if(cast == null) {
                cast = new String[0];
            }
            if(directors == null) {
                directors = new String[0];
            }
            return new Crew(this);
        }
    }
}
