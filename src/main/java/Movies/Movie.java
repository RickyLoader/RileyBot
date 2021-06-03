package Movies;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Movie details from The Movie Database
 */
public class Movie {
    private final String title, summary, contentRating, tagline, language, posterUrl;
    private final String[] genres;
    private final RatingIds ratingIds;
    private final SocialConnections socialConnections;
    private final Crew crew;
    private final Date releaseDate;
    private final double rating;
    private final long budget, revenue;
    private final int duration;

    /**
     * Create the movie from the builder values
     *
     * @param builder Builder to use values from
     */
    private Movie(MovieBuilder builder) {
        this.title = builder.title;
        this.summary = builder.summary;
        this.contentRating = builder.contentRating;
        this.tagline = builder.tagline;
        this.language = builder.language;
        this.genres = builder.genres;
        this.posterUrl = builder.posterUrl;
        this.ratingIds = builder.ratingIds;
        this.socialConnections = builder.socialConnections;
        this.crew = builder.crew;
        this.releaseDate = builder.releaseDate;
        this.rating = builder.rating;
        this.budget = builder.budget;
        this.revenue = builder.revenue;
        this.duration = builder.duration;
    }

    public static class MovieBuilder {
        private final String title, summary, language;
        private final String[] genres;
        private final RatingIds ratingIds;
        private final Date releaseDate;
        private String contentRating, tagline, posterUrl;
        private SocialConnections socialConnections;
        private Crew crew;
        private double rating;
        private long budget, revenue;
        private int duration;

        /**
         * Initialise the movie builder with the minimum required values
         *
         * @param title       Title of the movie
         * @param summary     Summary/synopsis of the movie
         * @param releaseDate Release date
         * @param genres      Array of genres
         * @param language    Language name - English, German, etc
         * @param ratingIds   Rating IDs (IMDB, TMDB, etc)
         */
        public MovieBuilder(String title, String summary, @Nullable Date releaseDate, String[] genres, String language, RatingIds ratingIds) {
            this.title = title;
            this.summary = summary;
            this.releaseDate = releaseDate;
            this.genres = genres;
            this.language = language;
            this.ratingIds = ratingIds;
        }

        /**
         * Set the content rating/certification
         *
         * @param contentRating Content rating - G, PG, etc
         * @return Builder
         */
        public MovieBuilder setContentRating(String contentRating) {
            this.contentRating = contentRating;
            return this;
        }

        /**
         * Set the tagline
         *
         * @param tagline Movie tagline
         * @return Builder
         */
        public MovieBuilder setTagline(String tagline) {
            this.tagline = tagline;
            return this;
        }

        /**
         * Set the URL to a poster for the movie
         *
         * @param posterUrl Poster URL
         * @return Builder
         */
        public MovieBuilder setPosterUrl(String posterUrl) {
            this.posterUrl = posterUrl;
            return this;
        }

        /**
         * Set the social connections for the movie - facebook, youtube, etc
         *
         * @param socialConnections Social connections
         * @return Builder
         */
        public MovieBuilder setSocialConnections(SocialConnections socialConnections) {
            this.socialConnections = socialConnections;
            return this;
        }

        /**
         * Set the cast/director/crew of the movie
         *
         * @param crew Movie crew
         * @return Builder
         */
        public MovieBuilder setCrew(Crew crew) {
            this.crew = crew;
            return this;
        }

        /**
         * Set the TMDB rating of the movie
         *
         * @param rating Rating score e.g 5.6
         * @return Builder
         */
        public MovieBuilder setRating(double rating) {
            this.rating = rating;
            return this;
        }

        /**
         * Set the budget of the movie
         *
         * @param budget Movie budget (in USD)
         * @return Builder
         */
        public MovieBuilder setBudget(long budget) {
            this.budget = budget;
            return this;
        }

        /**
         * Set the revenue of the movie
         *
         * @param revenue Total box office revenue (in USD)
         * @return Builder
         */
        public MovieBuilder setRevenue(long revenue) {
            this.revenue = revenue;
            return this;
        }

        /**
         * Set the duration of the movie
         *
         * @param duration Duration of movie (In minutes)
         * @return Builder
         */
        public MovieBuilder setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        /**
         * Create a movie from the builder values
         *
         * @return Movie from builder values
         */
        public Movie build() {
            return new Movie(this);
        }
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
     * Get the language of the movie
     * Language name - English, German, etc
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Get the cast/director/crew of the movie
     *
     * @return Movie crew
     */
    public Crew getCrew() {
        return crew;
    }

    /**
     * Get the theatrical release date of the movie
     *
     * @return Theatrical release date
     */
    public Date getReleaseDate() {
        return releaseDate;
    }

    /**
     * Get the TMDB rating of the movie
     *
     * @return Rating score e.g 5.6
     */
    public double getRating() {
        return rating;
    }

    /**
     * Get the budget of the movie
     *
     * @return Budget (in USD)
     */
    public long getBudget() {
        return budget;
    }

    /**
     * Get the duration of the movie
     *
     * @return Duration (in minutes)
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Get the total box office revenue of the movie
     *
     * @return Box office revenue (in USD)
     */
    public long getRevenue() {
        return revenue;
    }

    /**
     * Get the rating IDs of the movie (TMDB, IMDB, etc)
     *
     * @return Rating IDs
     */
    public RatingIds getRatingIds() {
        return ratingIds;
    }

    /**
     * Get the social connections for the movie - facebook, youtube, etc
     *
     * @return Social connections
     */
    public SocialConnections getSocialConnections() {
        return socialConnections;
    }

    /**
     * Get the content rating/certification of the movie
     *
     * @return Content rating - G, PG, etc
     */
    public String getContentRating() {
        return contentRating;
    }

    /**
     * Get an array of genres for the movie
     *
     * @return Movie genres
     */
    public String[] getGenres() {
        return genres;
    }

    /**
     * Get the URL to a poster for the movie
     *
     * @return Poster URL
     */
    public String getPosterUrl() {
        return posterUrl;
    }

    /**
     * Get the summary/synopsis of the movie truncated to 100 characters
     *
     * @return Movie summary
     */
    public String getSummary() {
        int max = 100;
        return summary.length() < max ? summary : summary.substring(0, max) + "...";
    }

    /**
     * Get the tagline of the movie
     *
     * @return Movie tagline
     */
    public String getTagline() {
        return tagline;
    }

    /**
     * Check if the movie has a poster
     *
     * @return Movie has a poster
     */
    public boolean hasPosterUrl() {
        return posterUrl != null;
    }

    /**
     * Check if the movie has a tagline
     *
     * @return Movie has a tagline
     */
    public boolean hasTagline() {
        return tagline != null;
    }

    /**
     * Get the release date formatted to NZ standard
     *
     * @return Formatted release date
     */
    public String getFormattedReleaseDate() {
        return releaseDate == null ? "N/A" : new SimpleDateFormat("dd/MM/yyyy").format(releaseDate);
    }
}
