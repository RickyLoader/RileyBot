package Plex;

import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Network.Secret;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlexServer {
    private long timeFetched;
    private ArrayList<Movie> library;
    private final String helpMessage, plexIcon = "https://i.imgur.com/FdabwCm.png";

    /**
     * Read in the Plex library and remember the timestamp
     */
    public PlexServer(String helpMessage) {
        this.helpMessage = helpMessage;
        this.library = getLibraryOverview();
        this.timeFetched = System.currentTimeMillis();
    }

    /**
     * Return whether the library was successfully read in
     *
     * @return Library exists
     */
    public boolean libraryEmpty() {
        return library.isEmpty();
    }

    /**
     * Create a list of movies from the Plex API
     *
     * @return List of movies
     */
    private ArrayList<Movie> getLibraryOverview() {
        ArrayList<Movie> movies = new ArrayList<>();
        String json = new NetworkRequest(getPlexLibraryURL(), false).get();
        if(json == null || json.equals("err")) {
            System.out.println("Failed to contact Plex");
            return movies;
        }
        String languages = getLanguages();
        JSONArray jsonArr = new JSONObject(json).getJSONObject("MediaContainer").getJSONArray("Metadata");
        for(int i = 0; i < jsonArr.length(); i++) {
            JSONObject movie = jsonArr.getJSONObject(i);
            if(!movieDataExists(movie)) {
                System.out.println(movie.getJSONArray("Media").getJSONObject(0).getJSONArray("Part").getJSONObject(0).getString("file") + " missing info!");
                continue;
            }
            String title = movie.getString("title");
            String guid = movie.getString("guid");
            if(guid.contains("plex://movie")) {
                json = new NetworkRequest(getPlexItemURL(movie.getString("ratingKey")), false).get();
                guid = new JSONObject(json).getJSONObject("MediaContainer").getJSONArray("Metadata").getJSONObject(0).getJSONArray("Guid").getJSONObject(0).getString("id");
            }

            movies.add(new Movie(
                    guid,
                    title,
                    movie.has("contentRating") ? movie.getString("contentRating") : "Not Rated",
                    movie.getString("summary"),
                    movie.has("tagline") ? movie.getString("tagline") : null,
                    movie.getString("originallyAvailableAt"),
                    movie.has("Director") ? stringify(movie.getJSONArray("Director")) : null,
                    movie.has("Role") ? stringify(movie.getJSONArray("Role")) : null,
                    movie.has("Genre") ? stringify(movie.getJSONArray("Genre")) : null,
                    languages,
                    movie.getLong("duration"),
                    movie.has("rating") ? movie.getDouble("rating") : 0
            ));
        }
        return movies;
    }

    /**
     * Get the languages used by TMDB
     *
     * @return Languages used by TMDB
     */
    private String getLanguages() {
        return new NetworkRequest("https://api.themoviedb.org/3/configuration/languages?api_key=" + Secret.getTMDBKey(), false).get();
    }

    /**
     * Take a JSONArray of movie properties and convert to a comma separated String
     *
     * @param arr JSONArray of movie properties
     * @return Comma separated String of elements
     */
    private String stringify(JSONArray arr) {
        String[] list = new String[arr.length()];
        for(int i = 0; i < arr.length(); i++) {
            list[i] = arr.getJSONObject(i).getString("tag");
        }
        return StringUtils.join(list, ", ");
    }

    /**
     * Check whether Plex has the required information for a movie
     *
     * @param movie JSON object representing a movie
     * @return Whether Plex has the sufficient information
     */
    private boolean movieDataExists(JSONObject movie) {
        return movie.has("guid") && movie.has("title") && movie.has("summary") && movie.has("originallyAvailableAt");
    }

    /**
     * Build a message embed detailing a random movie in the Plex library
     *
     * @param movie Movie to build embed for
     * @return Movie embed
     */
    public MessageEmbed getMovieEmbed(Movie movie) {
        if(!movie.isComplete()) {
            movie.completeMovieDetails();
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(plexIcon);
        builder.setColor(EmbedHelper.getOrange());
        builder.setTitle(movie.getTitle());
        builder.setImage(movie.getPoster());
        builder.setDescription(movie.toString());
        double rating = movie.getRating();
        builder.setFooter("IMDB: " + ((rating == 0) ? "N/A" : rating) + " | Content Rating: " + movie.getContentRating() + " | Release Date: " + movie.getFormattedReleaseDate(), movie.getRatingImage());
        return builder.build();
    }

    /**
     * Search for a movie on Plex
     *
     * @return Single movie embed or embed containing search results
     */
    public MessageEmbed search(String query) {
        Movie[] results;

        // tt12345 or td12345
        if(query.matches("([t][td])\\d+")) {
            results = searchByID(query);
        }
        else {
            results = searchByTitle(query);
            if(results.length == 0) {
                results = searchByQuery(query);
            }
        }

        if(results.length == 1) {
            return getMovieEmbed(results[0]);
        }
        Arrays.sort(results, Comparator.comparing(Movie::getReleaseDate));
        return buildSearchEmbed(query, results);
    }

    /**
     * Filter movies by the title containing a query
     *
     * @param query Query to check title for
     * @return Filtered array of movies that contain the query in the title
     */
    private Movie[] searchByQuery(String query) {
        return getMatchingMovies(movie -> movie.title.toLowerCase().contains(query));
    }

    /**
     * Filter movies by title
     *
     * @param title Movie title
     * @return Filtered array of movies that match the title
     */
    private Movie[] searchByTitle(String title) {
        return getMatchingMovies(movie -> movie.title.equalsIgnoreCase(title));
    }

    /**
     * Filter movies by id
     *
     * @param id Movie id
     * @return Filtered array of movies that match the id
     */
    private Movie[] searchByID(String id) {
        return getMatchingMovies(movie -> movie.getFormattedId().equals(id));
    }

    /**
     * Filter the library by a predicate
     *
     * @param p Predicate to filter by
     * @return Filtered array of movies
     */
    private Movie[] getMatchingMovies(Predicate<Movie> p) {
        return library.stream().filter(p).toArray(Movie[]::new);
    }

    /**
     * Build an embed displaying the search results of a query
     *
     * @param movies Movie search results
     * @return Embed displaying search results
     */
    private MessageEmbed buildSearchEmbed(String query, Movie[] movies) {
        int bound = Math.min(5, movies.length);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(EmbedHelper.getOrange());
        builder.setThumbnail(plexIcon);
        builder.setTitle("Plex Movie Search");
        builder.setFooter("Try: " + helpMessage, plexIcon);
        if(bound == 0) {
            builder.setDescription("No movie results found for: **" + query + "**, try again cunt.");
            return builder.build();
        }
        builder.setDescription("I found " + movies.length + " results for: **" + query + "**\n\nNarrow it down next time cunt, here" + ((bound == movies.length) ? " they are:" : "'s " + bound + " of them:"));
        for(int i = 0; i < bound; i++) {
            Movie movie = movies[i];
            String title = movie.getFormattedTitle();
            String index = String.valueOf(i + 1);
            String date = movie.getFormattedReleaseDate();
            if(i == 0) {
                builder.addField(EmbedHelper.getTitleField("#", index));
                builder.addField(EmbedHelper.getTitleField("Title", title));
                builder.addField(EmbedHelper.getTitleField("Release Date", date));
            }
            else {
                builder.addField(EmbedHelper.getValueField(index));
                builder.addField(EmbedHelper.getValueField(title));
                builder.addField(EmbedHelper.getValueField(date));
            }
        }
        return builder.build();
    }

    /**
     * Get a random movie from the Plex library
     *
     * @return Random movie
     */
    public Movie getRandomMovie() {
        return library.get(new Random().nextInt(library.size()));
    }

    /**
     * Get the URL to fetch movie data from plex
     *
     * @return Plex URL
     */
    private String getPlexLibraryURL() {
        return Secret.getPlexIp() + Secret.getInternalPlexPort() + "/library/sections/" + Secret.getPlexLibraryId() + "/all/" + Secret.getPlexToken();
    }

    /**
     * Get the URL to fetch the comprehensive view of a movie
     *
     * @param id Plex id for movie
     * @return Plex URL
     */
    private String getPlexItemURL(String id) {
        return Secret.getPlexIp() + Secret.getInternalPlexPort() + "/library/metadata/" + id + Secret.getPlexToken();
    }

    /**
     * Refresh the plex library
     */
    public void refreshData() {
        this.library = getLibraryOverview();
        this.timeFetched = System.currentTimeMillis();
    }

    /**
     * Get the time the data was last fetched
     *
     * @return Time fetched of Plex data
     */
    public long getTimeFetched() {
        return timeFetched;
    }

    /**
     * Hold information about a movie on Plex
     */
    public static class Movie {
        private final String id, title, contentRating, summary, tagLine, director, cast, languages;
        private final long duration;
        private final Date releaseDate;
        private final double rating;
        private long budget, revenue;
        private String language, imdbURL, poster, genre;
        private boolean complete;

        /**
         * Construct the movie
         *
         * @param id            Plex ID of movie, either IMDB (tt6053438) or TMDB (14161) formatted
         * @param title         Title of movie
         * @param contentRating Content rating - G, PG..
         * @param summary       Movie synopsis
         * @param tagLine       Movie tagline
         * @param releaseDate   Release date of movie
         * @param director      Director of movie
         * @param cast          Comma separated list of cast members
         * @param genre         Comma separated list of genres
         * @param duration      Duration of movie
         * @param rating        IMDB rating of movie
         */
        public Movie(String id, String title, String contentRating, String summary, String tagLine, String releaseDate, String director, String cast, String genre, String languages, long duration, double rating) {
            this.id = getMovieID(id);
            this.title = title;
            this.contentRating = contentRating;
            this.summary = summary;
            this.tagLine = tagLine;
            this.releaseDate = parseReleaseDate(releaseDate);
            this.director = director;
            this.cast = cast;
            this.genre = genre;
            this.languages = languages;
            this.duration = duration;
            this.rating = rating;
            this.complete = false;
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
         * Complete the movie details that aren't provided by Plex via using The Movie Database
         * Get IMDB URL, genre(s), poster, budget, revenue, and language data
         */
        public void completeMovieDetails() {
            if(complete) {
                System.out.println("Movie is already complete!");
                return;
            }
            String url = "https://api.themoviedb.org/3/movie/" + id + "?api_key=" + Secret.getTMDBKey() + "&language=en-US";
            JSONObject movie = new JSONObject(new NetworkRequest(url, false).get());
            this.imdbURL = buildIMDBUrl(movie.getString("imdb_id"));
            this.genre = parseGenre(movie.getJSONArray("genres"));
            this.poster = "https://image.tmdb.org/t/p/original/" + movie.getString("poster_path");
            this.budget = movie.getLong("budget");
            this.revenue = movie.getLong("revenue");
            this.language = parseLanguage(movie);
            this.complete = true;
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
         * Build the URL to the IMDB page of the movie from the imdb id
         *
         * @return IMDB URL
         */
        private String buildIMDBUrl(String id) {
            return "https://www.imdb.com/title/" + id;
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
                language = spokenLanguages.getJSONObject(0).getString("name");
            }

            // Get the English name of the language
            if(!language.equals("English")) {
                JSONArray allLanguages = new JSONArray(languages);
                for(int i = 0; i < allLanguages.length(); i++) {
                    JSONObject lang = allLanguages.getJSONObject(i);
                    if(lang.getString("iso_639_1").equals(iso)) {
                        language = lang.getString("english_name");
                    }
                }
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
         * Format the release date to NZ format
         *
         * @param releaseDate String date
         * @return NZ formatted release date
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
         * Get the release date formatted to NZ standard
         *
         * @return Formatted release date
         */
        public String getFormattedReleaseDate() {
            return new SimpleDateFormat("dd/MM/yyy").format(releaseDate);
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
            return EmbedHelper.formatTime(duration);
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
            return title + "\n(" + getFormattedId() + ")";
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
         * Get the IMDB logo image
         *
         * @return URL to IMDB logo image
         */
        public String getRatingImage() {
            return "https://i.imgur.com/qMCJLhJ.png";
        }

        /**
         * Get the id of the movie formatted with a prefix to assist in searching by id
         * (TMDB ID is a number sequence e.g 231456) Append "td" to make it clear when
         * an id has been provided for search
         *
         * @return movie id with prefix
         */
        public String getFormattedId() {
            return id.startsWith("tt") ? id : "td" + id;
        }

        /**
         * Extract the unique id from the guid Plex uses. E.g: "com.plexapp.agents.imdb://tt0309593?lang=en" = tt0309593
         * Either points to TMDB or IMDB, TMDB supports either, Plex supplies only one
         *
         * @param guid The rating agent used by Plex to pull the rating for the movie
         * @return id The id of the movie
         */
        private String getMovieID(String guid) {
            String regex;
            String id = null;

            // e.g: "com.plexapp.agents.imdb://tt0309593?lang=en"
            if(guid.contains("imdb")) {
                regex = "tt[0-9]+";
            }
            // e.g: "com.plexapp.agents.themoviedb://14161?lang=en"
            else {
                regex = "[0-9]+";
            }

            Matcher matcher = Pattern.compile(regex).matcher(guid);

            if(matcher.find()) {
                id = guid.substring(matcher.start(), matcher.end());
            }
            return id;
        }

        /**
         * Format the movie information in to a String summary
         *
         * @return Summary of movie information
         */
        @Override
        public String toString() {
            StringBuilder desc = new StringBuilder("**Synopsis**: " + summary);

            if(tagLine != null) {
                desc.append("\n\n**Tagline**: ").append(tagLine);
            }
            if(director != null) {
                desc.append("\n\n**Director**: ").append(director);
            }
            if(cast != null) {
                desc.append("\n\n**Cast**: ").append(cast);
            }
            desc.append("\n\n**Genre**: ").append(getGenre());

            String language = getLanguage();
            if(!language.equals("English")) {
                desc.append("\n\n**Language**: ").append(language);
            }

            desc.append("\n\n**Duration**: ").append(getDuration());


            if(budget > 0) {
                desc.append("\n\n**Budget**: ").append(formatUSD(budget));
            }
            if(revenue > 0) {
                desc.append("\n\n**Box Office**: ").append(formatUSD(revenue));
            }

            desc.append("\n\n**IMDB**: ").append(EmbedHelper.embedURL("View", getIMDBUrl()));

            return desc.toString();
        }
    }
}
