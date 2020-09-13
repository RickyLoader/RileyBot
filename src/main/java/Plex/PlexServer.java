package Plex;

import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Network.NetworkRequest;
import Network.Secret;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PlexServer {
    private long timeFetched;
    private ArrayList<Movie> library;
    private EmoteHelper emoteHelper;
    private final String helpMessage, plexIcon = "https://i.imgur.com/FdabwCm.png", radarrIcon = "https://i.imgur.com/d5p0ftd.png";
    private String plexEmote, radarrEmote;

    /**
     * Read in the Radarr library and remember the timestamp
     */
    public PlexServer(String helpMessage) {
        this.helpMessage = helpMessage;
        refreshData();
    }

    /**
     * Return whether the emote helper is initialised
     *
     * @return Emote helper initialised
     */
    public boolean hasEmotes() {
        return emoteHelper != null;
    }

    /**
     * Initialise the emote helper
     *
     * @param emoteHelper Emote helper
     */
    public void setEmoteHelper(EmoteHelper emoteHelper) {
        this.emoteHelper = emoteHelper;
        this.plexEmote = EmoteHelper.formatEmote(emoteHelper.getPlex());
        this.radarrEmote = EmoteHelper.formatEmote(emoteHelper.getRadarr());
    }

    /**
     * Refresh the Radarr library
     */
    public void refreshData() {
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
     * Create a list of movies from the Radarr API
     *
     * @return List of movies
     */
    private ArrayList<Movie> getLibraryOverview() {
        ArrayList<Movie> library = new ArrayList<>();
        String json = new NetworkRequest(getRadarrLibraryURL(), false).get();
        if(json == null || json.equals("err")) {
            System.out.println("Failed to contact Radarr");
            return library;
        }
        JSONArray movies = new JSONArray(json);
        for(int i = 0; i < movies.length(); i++) {
            JSONObject movie = movies.getJSONObject(i);
            if(!movie.has("imdbId")) {
                continue;
            }
            library.add(new Movie(
                    movie.getString("imdbId"),
                    String.valueOf(movie.get("tmdbId")),
                    movie.getString("title"),
                    movie.getString("overview"),
                    movie.has("inCinemas") ? movie.getString("inCinemas") : null,
                    movie.getBoolean("downloaded")
            ));
        }
        return library;
    }


    /**
     * Build a message embed detailing a Movie from Radarr
     *
     * @param movie Movie to build embed for
     * @return Movie embed
     */
    public MessageEmbed getMovieEmbed(Movie movie) {
        if(!movie.isComplete()) {
            movie.completeMovieDetails();
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(movie.isOnPlex() ? plexIcon : radarrIcon);
        builder.setColor(movie.isOnPlex() ? EmbedHelper.getOrange() : EmbedHelper.getBlue());
        builder.setTitle(movie.getTitle());
        builder.setImage(movie.getPoster());
        builder.setDescription(movie.toString());
        double rating = movie.getRating();
        builder.setFooter("TMDB: " + ((rating == 0) ? "N/A" : rating) + " | Content Rating: " + movie.getContentRating() + " | Release Date: " + movie.getFormattedReleaseDate(), "https://i.imgur.com/J1JGC4J.png");
        return builder.build();
    }

    /**
     * Search for a movie on Radarr
     *
     * @return Single movie embed or embed containing search results
     */
    public MessageEmbed search(String query) {
        Movie[] results;

        // tt12345
        if(query.matches("tt\\d+")) {
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
        Arrays.sort(results, (o1, o2) -> {
            Date a = o1.getReleaseDate();
            Date b = o2.getReleaseDate();
            if(a == null) {
                return 1;
            }
            if(b == null) {
                return -1;
            }
            return a.compareTo(b);
        });
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
        return getMatchingMovies(movie -> movie.getImdbId().equals(id));
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
        String emoteKey = plexEmote + " = On Plex\n\n" + radarrEmote + " = On Radarr - Movie **is not** on Plex but will be once released.\n\n";
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(EmbedHelper.getOrange());
        builder.setThumbnail(plexIcon);
        builder.setTitle("Plex Movie Search");
        builder.setFooter("Try: " + helpMessage);
        if(bound == 0) {
            builder.setDescription("No movie results found for: **" + query + "**, try again cunt.");
            return builder.build();
        }
        builder.setDescription(emoteKey + "I found " + movies.length + " results for: **" + query + "**\n\nNarrow it down next time cunt, here" + ((bound == movies.length) ? " they are:" : "'s " + bound + " of them:"));
        for(int i = 0; i < bound; i++) {
            Movie movie = movies[i];
            String title = movie.getFormattedTitle();
            String platform = movie.isOnPlex() ? plexEmote + " Plex" : radarrEmote + " Radarr";
            String date = movie.getFormattedReleaseDate();
            if(i == 0) {
                builder.addField(EmbedHelper.getTitleField("Platform", platform));
                builder.addField(EmbedHelper.getTitleField("Title", title));
                builder.addField(EmbedHelper.getTitleField("Release Date", date));
            }
            else {
                builder.addField(EmbedHelper.getValueField(platform));
                builder.addField(EmbedHelper.getValueField(title));
                builder.addField(EmbedHelper.getValueField(date));
            }
        }
        return builder.build();
    }

    /**
     * Get a random movie from the Radarr library that is downloaded (on Plex)
     *
     * @return Random movie
     */
    public Movie getRandomMovie() {
        List<Movie> plexLibrary = library.stream().filter(Movie::isOnPlex).collect(Collectors.toList());
        return plexLibrary.get(new Random().nextInt(plexLibrary.size()));
    }

    /**
     * Get the URL to fetch movie data from Radarr
     *
     * @return Radarr URL
     */
    private String getRadarrLibraryURL() {
        return Secret.getRadarrIp() + "/api/movie?apikey=" + Secret.getRadarrKey();
    }

    /**
     * Get the time the data was last fetched
     *
     * @return Time fetched of Radarr data
     */
    public long getTimeFetched() {
        return timeFetched;
    }

    /**
     * Hold information about a movie on Radarr
     */
    public static class Movie {
        private final String imdbId, tmdbId, title, summary, imdbURL;
        private String contentRating, tagline, director, cast, language, genre, poster;
        private final Date releaseDate;
        private double rating;
        private long budget, revenue, duration;
        private boolean complete;
        private final boolean onPlex;

        /**
         * Construct the movie
         *
         * @param imdbId  IMDB (tt6053438) id of movie
         * @param tmdbId  TMDB (14161) id of movie
         * @param title   Title of movie
         * @param summary Movie synopsis
         * @param onPlex  Movie is on Plex
         */
        public Movie(String imdbId, String tmdbId, String title, String summary, String releaseDate, boolean onPlex) {
            this.imdbId = imdbId;
            this.imdbURL = buildIMDBUrl(imdbId);
            this.tmdbId = tmdbId;
            this.title = title;
            this.summary = summary;
            this.releaseDate = releaseDate == null ? null : parseReleaseDate(releaseDate);
            this.onPlex = onPlex;
            this.complete = false;
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
            String url = "https://api.themoviedb.org/3/movie/" + tmdbId + "?api_key=" + Secret.getTMDBKey() + "&append_to_response=credits,release_dates&language=en-US";
            JSONObject movie = new JSONObject(new NetworkRequest(url, false).get());
            this.genre = parseGenre(movie.getJSONArray("genres"));
            this.budget = movie.getLong("budget");
            this.revenue = movie.getLong("revenue");
            this.language = parseLanguage(movie);
            this.rating = movie.getDouble("vote_average");

            String tagline = movie.getString("tagline");
            this.tagline = tagline.isEmpty() ? "N/A" : tagline;
            this.contentRating = parseContentRating(movie.getJSONObject("release_dates").getJSONArray("results"));
            this.duration = movie.getInt("runtime");
            this.poster = "https://image.tmdb.org/t/p/original/" + movie.getString("poster_path");
            JSONObject credits = movie.getJSONObject("credits");
            this.cast = parseCast(credits.getJSONArray("cast"));
            this.director = parseDirectors(credits.getJSONArray("crew"));
            this.complete = true;
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
            for(int i = 0; i < releases.length(); i++) {
                JSONObject release = releases.getJSONObject(i);
                if(!release.getString("iso_3166_1").equals("US")) {
                    continue;
                }
                String contentRating = release.getJSONArray("release_dates").getJSONObject(0).getString("certification");
                return contentRating.isEmpty() ? "N/A" : contentRating;
            }
            return null;
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
                language = getISOEnglishName(iso);
            }
            return language;
        }

        /**
         * Get the English name of a language from the ISO code
         *
         * @return English name of language
         */
        private String getISOEnglishName(String iso) {
            String language = null;
            try {
                JSONArray allLanguages = new JSONArray(new String(Files.readAllBytes(Paths.get("src/main/resources/Movie/languages.json")), StandardCharsets.UTF_8));
                for(int i = 0; i < allLanguages.length(); i++) {
                    JSONObject lang = allLanguages.getJSONObject(i);
                    if(lang.getString("iso_639_1").equals(iso)) {
                        language = lang.getString("english_name");
                        break;
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
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
            return EmbedHelper.formatTime(duration * 60000);
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
            return title + "\n(" + getImdbId() + ")";
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
         * Format the movie information in to a String summary
         *
         * @return Summary of movie information
         */
        @Override
        public String toString() {
            StringBuilder desc = new StringBuilder();
            desc.append("**Plex**: ").append(onPlex ? "Movie **is** on Plex" : "Movie **is not** on Plex but will be once released.");
            desc.append("\n\n**Synopsis**: ").append(summary);
            desc.append("\n\n**Tagline**: ").append(tagline);
            desc.append("\n\n**Director**: ").append(director);
            desc.append("\n\n**Cast**: ").append(cast);
            desc.append("\n\n**Genre**: ").append(getGenre());

            String language = getLanguage();

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

            desc.append("\n\n**IMDB**: ").append(EmbedHelper.embedURL("View", getIMDBUrl()));

            return desc.toString();
        }
    }
}
