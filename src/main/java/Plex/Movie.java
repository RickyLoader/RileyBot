package Plex;

import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Network.Secret;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

/**
 * Hold information about a movie on Radarr
 */
public class Movie implements Comparator<Movie> {
    private final String tmdbId, title, summary, plexURL;
    private final boolean onPlex;
    private final Date releaseDate;
    private String contentRating, tagline, director, cast, language, genre, poster, imdbId, imdbURL, trailer, facebook;
    private double rating;
    private long budget, revenue, duration;
    private boolean complete;

    /**
     * Create a movie from the builder values
     *
     * @param builder MovieBuilder
     */
    private Movie(MovieBuilder builder) {
        this.imdbId = builder.imdbId;
        this.imdbURL = builder.imdbURL;
        this.tmdbId = builder.tmdbId;
        this.title = builder.title;
        this.summary = builder.summary;
        this.releaseDate = builder.releaseDate;
        this.onPlex = builder.onPlex;
        this.plexURL = builder.plexURL;
        this.complete = false;
    }

    public static class MovieBuilder {
        private String tmdbId, title, summary, plexURL, imdbId, imdbURL;
        private boolean onPlex;
        private Date releaseDate;

        /**
         * Set whether the movie is on Plex
         *
         * @param onPlex Movie is on Plex
         * @return Builder
         */
        public MovieBuilder setOnPlex(boolean onPlex) {
            this.onPlex = onPlex;
            return this;
        }

        /**
         * Set the release date of the movie
         *
         * @param releaseDate Movie release date String (yyyy-MM-dd)
         * @return Builder
         */
        public MovieBuilder setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate == null ? null : parseReleaseDate(releaseDate);
            return this;
        }

        /**
         * Format the release date String to a Date
         *
         * @param releaseDate String date
         * @return Date of String
         */
        private Date parseReleaseDate(String releaseDate) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(releaseDate);
            }
            catch(ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Set the URL to the movie on plex
         *
         * @param plexURL URL to movie on plex
         * @return Builder
         */
        public MovieBuilder setPlexURL(String plexURL) {
            this.plexURL = plexURL;
            return this;
        }

        /**
         * Set the movie title
         *
         * @param title Movie title
         * @return Builder
         */
        public MovieBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set the movie summary (synopsis)
         *
         * @param summary Movie summary (synopsis)
         * @return Builder
         */
        public MovieBuilder setSummary(String summary) {
            this.summary = (summary == null || summary.length() < 175) ? summary : summary.substring(0, 175) + "...";
            return this;
        }

        /**
         * Set the IMDB ID of the movie
         *
         * @param imdbId IMDB (tt6053438) id of movie
         * @return Builder
         */
        public MovieBuilder setImdbId(String imdbId) {
            this.imdbId = imdbId;
            this.imdbURL = buildIMDBUrl(imdbId);
            return this;
        }

        /**
         * Set the TMDB ID of the movie
         *
         * @param tmdbId TMDB (14161) id of movie
         * @return Builder
         */
        public MovieBuilder setTmdbId(String tmdbId) {
            this.tmdbId = tmdbId;
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

        /**
         * Build the URL to the IMDB page of the movie from the imdb id
         *
         * @return IMDB URL
         */
        public static String buildIMDBUrl(String id) {
            return "https://www.imdb.com/title/" + id;
        }
    }

    /**
     * Parse the movie to JSON
     *
     * @param member    Member who requested movie
     * @param requested Time of request
     * @param webhook   Webhook URL to respond to
     * @return JSON formatted movie
     */
    public String toJSON(Member member, long requested, String webhook) {
        JSONObject json = new JSONObject()
                .put("title", title)
                .put("tagline", tagline == null ? JSONObject.NULL : tagline)
                .put("summary", summary == null ? JSONObject.NULL : summary)
                .put("cast", cast == null ? JSONObject.NULL : cast)
                .put("language", language)
                .put("genre", genre == null ? JSONObject.NULL : genre)
                .put("poster", poster == null ? JSONObject.NULL : poster)
                .put("director", director == null ? JSONObject.NULL : director)
                .put("release_date", getFormattedReleaseDate())
                .put("content_rating", contentRating)
                .put("trailer", trailer == null ? JSONObject.NULL : trailer)
                .put("facebook", facebook == null ? JSONObject.NULL : facebook)
                .put("rating", rating == 0 ? JSONObject.NULL : rating)
                .put("budget", budget > 0 ? formatUSD(budget) : JSONObject.NULL)
                .put("revenue", revenue > 0 ? formatUSD(revenue) : JSONObject.NULL)
                .put("member", member.getAsMention())
                .put("duration", duration > 0 ? getDuration() : JSONObject.NULL)
                .put("requested", requested)
                .put("webhook", webhook);
        if(imdbId != null) {
            json.put("imdb_id", imdbId).put("imdb_url", imdbURL);
        }
        return json.toString();
    }

    /**
     * Build the youtube trailer URL from the id
     *
     * @param trailerId Youtube trailer id (Z9q1qJi1nMs)
     * @return Youtube trailer URL
     */
    private String buildTrailerURL(String trailerId) {
        return "https://www.youtube.com/watch?v=" + trailerId;
    }

    /**
     * Return whether movie is on Plex (Radarr knows it is downloaded)
     *
     * @return Movie is on Plex
     */
    public boolean isOnPlex() {
        return onPlex;
    }

    /**
     * Does the movie have the required data from The Movie Database
     *
     * @return Movie has complete data for display
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Complete the movie details that aren't provided by Radarr using The Movie Database
     */
    public void completeMovieDetails() {
        if(complete) {
            System.out.println("Movie is already complete!");
            return;
        }
        String url = "https://api.themoviedb.org/3/movie/" + tmdbId + "?api_key=" + Secret.TMDB_KEY + "&append_to_response=credits,release_dates,videos,external_ids&language=en-US";
        JSONObject movie = new JSONObject(new NetworkRequest(url, false).get().body);
        this.genre = parseGenre(movie.getJSONArray("genres"));
        this.budget = movie.getLong("budget");
        this.revenue = movie.getLong("revenue");
        this.language = parseLanguage(movie);
        this.rating = movie.getDouble("vote_average");
        this.trailer = parseTrailer(movie.getJSONObject("videos").getJSONArray("results"));
        this.facebook = parseFacebook(movie.getJSONObject("external_ids"));
        String tagline = movie.getString("tagline");
        this.tagline = tagline.isEmpty() ? null : tagline;
        this.contentRating = parseContentRating(movie.getJSONObject("release_dates").getJSONArray("results"));
        this.duration = movie.getInt("runtime");
        this.poster = movie.isNull("poster_path") ? null : "https://image.tmdb.org/t/p/original/" + movie.getString("poster_path");
        JSONObject credits = movie.getJSONObject("credits");
        this.cast = parseCast(credits.getJSONArray("cast"));
        this.director = parseDirectors(credits.getJSONArray("crew"));
        if(this.imdbId == null && !movie.isNull("imdb_id")) {
            this.imdbId = movie.getString("imdb_id");
            this.imdbURL = MovieBuilder.buildIMDBUrl(this.imdbId);
        }
        this.complete = true;
    }

    /**
     * Parse the trailer from the TMDB response
     *
     * @param videos Videos for movie
     * @return Trailer URL
     */
    private String parseTrailer(JSONArray videos) {
        for(int i = 0; i < videos.length(); i++) {
            JSONObject video = videos.getJSONObject(i);
            if(video.getString("site").equals("YouTube") && video.getString("type").equals("Trailer")) {
                return buildTrailerURL(video.getString("key"));
            }
        }
        return null;
    }

    /**
     * Parse the Facebook URL from the TMDB response
     *
     * @param socials Social media locations
     * @return Facebook URL
     */
    private String parseFacebook(JSONObject socials) {
        return socials.isNull("facebook_id") ? null : buildFacebookUrl(socials.getString("facebook_id"));
    }

    /**
     * Parse the director(s) from the TMDB response
     *
     * @param crew Crew list
     * @return String containing director(s)
     */
    private String parseDirectors(JSONArray crew) {
        ArrayList<String> directors = new ArrayList<>();
        for(int i = 0; i < crew.length(); i++) {
            JSONObject member = crew.getJSONObject(i);
            if(member.getString("department").equals("Directing") && member.getString("job").equals("Director")) {
                directors.add(member.getString("name"));
            }
        }
        return StringUtils.join(directors, ", ");
    }

    /**
     * Parse the top 3 cast members from the TMDB response
     *
     * @param cast Cast list
     * @return String containing top 3 cast members
     */
    private String parseCast(JSONArray cast) {
        int quantity = Math.min(3, cast.length());
        String[] topCast = new String[quantity];
        for(int i = 0; i < quantity; i++) {
            topCast[i] = cast.getJSONObject(i).getString("name");
        }
        return StringUtils.join(topCast, ", ");
    }

    /**
     * Parse the US content rating from the TMDB response
     *
     * @param releases Country release array
     * @return Movie content rating/certification - G, PG..
     */
    private String parseContentRating(JSONArray releases) {
        String contentRating = null;
        for(int i = 0; i < releases.length(); i++) {
            JSONObject release = releases.getJSONObject(i);
            if(!release.getString("iso_3166_1").equals("US")) {
                continue;
            }
            contentRating = release.getJSONArray("release_dates").getJSONObject(0).getString("certification");
        }
        return contentRating == null || contentRating.isEmpty() ? "N/A" : contentRating;
    }

    /**
     * Parse the genre data from The Movie Database
     *
     * @param genres Genre JSON
     * @return Movie genre(s)
     */
    private String parseGenre(JSONArray genres) {
        String[] genre = new String[genres.length()];
        for(int i = 0; i < genres.length(); i++) {
            genre[i] = genres.getJSONObject(i).getString("name");
        }
        return StringUtils.join(genre, ", ");
    }

    /**
     * Get the URL to the IMDB page of the movie from The Movie Database
     *
     * @return IMDB URL
     */
    public String getIMDBUrl() {
        return imdbURL;
    }

    /**
     * Build the URL to the Facebook page of the movie from page id
     *
     * @return Facebook URL
     */
    private String buildFacebookUrl(String id) {
        return "https://www.facebook.com/" + id;
    }

    /**
     * Get the movie genre(s)
     *
     * @return Movie genre(s)
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Get the movie poster
     *
     * @return Movie poster thumbnail
     */
    public String getPoster() {
        return poster;
    }

    /**
     * Get the movie budget
     *
     * @return Movie budget
     */
    public long getBudget() {
        return budget;
    }

    /**
     * Get the box office revenue
     *
     * @return Movie revenue
     */
    public long getRevenue() {
        return revenue;
    }

    /**
     * Format a long n to $n USD
     *
     * @param amount Long amount
     * @return Formatted USD currency string
     */
    private String formatUSD(long amount) {
        return String.format("$%,d USD", amount);
    }

    /**
     * Parse the movie language data from The Movie Database
     *
     * @param movie Movie JSON from The Movie Database
     * @return Movie language
     */
    public String parseLanguage(JSONObject movie) {
        String iso = movie.getString("original_language");
        JSONArray spokenLanguages = movie.getJSONArray("spoken_languages");

        for(int i = 0; i < spokenLanguages.length(); i++) {
            JSONObject lang = spokenLanguages.getJSONObject(i);
            if(lang.getString("iso_639_1").equals(iso)) {
                language = lang.getString("name");
                break;
            }
        }

        /*
         * Original language is based on where the movie was filmed - English movie filmed in Germany
         * would show German as the original language but wouldn't be present in spoken languages.
         */
        if(language == null) {
            if(spokenLanguages.length() > 0) {
                language = spokenLanguages.getJSONObject(0).getString("name");
            }
            else {
                language = "N/A";
            }
        }

        // Get the English name of the language
        if(!language.equals("English")) {
            language = EmbedHelper.getLanguageFromISO(iso);
        }
        return language;
    }

    /**
     * Get the movie language
     *
     * @return Movie language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Get the release date formatted to NZ standard
     *
     * @return Formatted release date
     */
    public String getFormattedReleaseDate() {
        return releaseDate == null ? "N/A" : new SimpleDateFormat("dd/MM/yyyy").format(releaseDate);
    }

    /**
     * Get the release date
     *
     * @return Release date
     */
    public Date getReleaseDate() {
        return releaseDate;
    }

    /**
     * Get the IMDB rating of the movie
     *
     * @return IMDB rating
     */
    public double getRating() {
        return rating;
    }

    /**
     * Get the duration of the movie in HH:mm:ss
     *
     * @return Formatted duration of movie
     */
    public String getDuration() {
        return EmbedHelper.formatDuration(duration * 60000);
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
     * Get the title with the id attached
     *
     * @return Title with id
     */
    public String getFormattedTitle() {
        return title + "\n(" + (imdbId == null ? "td" + tmdbId : imdbId) + ")";
    }

    /**
     * Get the content rating of the movie - G, PG..
     *
     * @return Content rating of movie
     */
    public String getContentRating() {
        return contentRating;
    }

    /**
     * Get the IMDB ID of the movie
     *
     * @return IMDB ID
     */
    public String getImdbId() {
        return imdbId;
    }

    /**
     * Get the TMDB ID of the movie
     *
     * @return TMDB ID
     */
    public String getTmdbId() {
        return tmdbId;
    }

    /**
     * Build a summary String of movie Facebook, IMDB, and trailer
     *
     * @param facebookEmote Facebook emote
     * @param imdbEmote     IMDB emote
     * @param youtubeEmote  Youtube emote
     * @param plexEmote     Plex emote
     * @return Movie social summary
     */
    private String buildSocialSummary(String facebookEmote, String imdbEmote, String youtubeEmote, String plexEmote) {
        ArrayList<String> elements = new ArrayList<>();
        if(imdbURL != null) {
            elements.add(EmbedHelper.embedURL(imdbEmote + " IMDB", imdbURL));
        }
        if(trailer != null) {
            elements.add(EmbedHelper.embedURL(youtubeEmote + " Trailer", trailer));
        }
        if(facebook != null) {
            elements.add(EmbedHelper.embedURL(facebookEmote + " Facebook", facebook));
        }
        if(plexURL != null) {
            elements.add(EmbedHelper.embedURL(plexEmote + " Plex", plexURL));
        }
        return elements.isEmpty() ? null : StringUtils.join(elements, " | ");
    }

    /**
     * Format the movie information in to a String summary
     *
     * @param facebookEmote Facebook emote
     * @param imdbEmote     IMDB emote
     * @param youtubeEmote  Youtube emote
     * @param plexEmote     Plex emote
     * @return Summary of movie information
     */
    public String buildEmbedDescription(String imdbEmote, String facebookEmote, String youtubeEmote, String plexEmote) {
        StringBuilder desc = new StringBuilder();
        desc.append(onPlex ? "Movie **is** on Plex" : "Movie **is not** on Plex but is being monitored.");

        if(summary != null) {
            desc.append("\n\n**Synopsis**: ").append(summary);
        }

        if(tagline != null) {
            desc.append("\n\n**Tagline**: ").append(tagline);
        }

        if(!director.isEmpty()) {
            desc.append("\n\n**Director**: ").append(director);
        }

        if(!cast.isEmpty()) {
            desc.append("\n\n**Cast**: ").append(cast);
        }

        if(!genre.isEmpty()) {
            desc.append("\n\n**Genre**: ").append(getGenre());
        }

        if(!language.equals("English")) {
            desc.append("\n\n**Language**: ").append(language);
        }

        if(duration > 0) {
            desc.append("\n\n**Duration**: ").append(getDuration());
        }

        if(budget > 0) {
            desc.append("\n\n**Budget**: ").append(formatUSD(budget));
        }

        if(revenue > 0) {
            desc.append("\n\n**Box Office**: ").append(formatUSD(revenue));
        }
        String infoSummary = buildSocialSummary(facebookEmote, imdbEmote, youtubeEmote, plexEmote);
        if(infoSummary != null) {
            desc.append("\n\n").append(infoSummary);
        }

        return desc.toString();
    }

    /**
     * Sort in descending order of release date
     */
    @Override
    public int compare(Movie o1, Movie o2) {
        return getSortComparator().compare(o1, o2);
    }

    /**
     * Get the comparator used to sort Movies in descending order of release date
     *
     * @return Sort comparator
     */
    public static Comparator<Movie> getSortComparator() {
        return (o1, o2) -> {
            Date a = o1.getReleaseDate();
            Date b = o2.getReleaseDate();

            // Movies without a known release date should be treated as the highest (most recent) date
            if(a == null) {
                return 1;
            }
            if(b == null) {
                return -1;
            }
            return b.compareTo(a);
        };
    }
}
