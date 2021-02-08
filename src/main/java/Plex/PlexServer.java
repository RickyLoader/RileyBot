package Plex;

import Command.Structure.CommandContext;
import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Network.Secret;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PlexServer {
    private final String helpMessage;
    public static final String
            RADARR_ICON = "https://i.imgur.com/d5p0ftd.png",
            PLEX_ICON = "https://i.imgur.com/FdabwCm.png";
    private long timeFetched;
    private ArrayList<Movie> library;
    private EmoteHelper emoteHelper;
    private HashMap<String, String> plexUrls;

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
    }

    /**
     * Refresh the Radarr library and Plex URLs
     */
    public void refreshData() {
        this.plexUrls = parsePlex();
        this.library = getLibraryOverview();
        this.timeFetched = System.currentTimeMillis();
    }

    /**
     * Retrieve the Plex URL for each movie from the Plex API. Map the folder name to the URL.
     *
     * @return Folder name mapped to Plex URL
     */
    private HashMap<String, String> parsePlex() {
        HashMap<String, String> plexURLs = new HashMap<>();
        NetworkResponse response = new NetworkRequest(getPlexLibraryURL(), false).get();
        String json = response.body;
        if(json == null || response.code == 504) {
            System.out.println("Failed to contact Plex");
            return plexURLs;
        }
        JSONArray library = new JSONObject(json).getJSONObject("MediaContainer").getJSONArray("Metadata");
        for(int i = 0; i < library.length(); i++) {
            JSONObject movie = library.getJSONObject(i);
            String folder = movie
                    .getJSONArray("Media")
                    .getJSONObject(0)
                    .getJSONArray("Part")
                    .getJSONObject(0)
                    .getString("file");
            plexURLs.put(
                    folder.substring(0, folder.lastIndexOf('/')),
                    "https://app.plex.tv/desktop#!/server/"
                            + Secret.PLEX_SERVER_ID
                            + "/details?key=/library/metadata/"
                            + movie.getString("ratingKey")
            );
        }
        return plexURLs;
    }

    /**
     * Return whether the library was successfully read in
     *
     * @return Library exists
     */
    public boolean libraryEmpty() {
        return library.isEmpty() || plexUrls.isEmpty();
    }

    /**
     * Create a list of movies from the Radarr API
     *
     * @return List of movies
     */
    private ArrayList<Movie> getLibraryOverview() {
        ArrayList<Movie> library = new ArrayList<>();
        NetworkResponse response = new NetworkRequest(getRadarrLibraryURL(), false).get();
        String json = response.body;
        if(json == null || response.code == 504) {
            System.out.println("Failed to contact Radarr");
            return library;
        }
        library = parseMovies(new JSONArray(json), false);
        return library;
    }

    /**
     * Parse Radarr JSON to a list of movies
     *
     * @param movies        Radarr movie list json
     * @param ignoreLibrary Ignore results which are on Radarr (For searching outside library)
     * @return List of movies
     */
    private ArrayList<Movie> parseMovies(JSONArray movies, boolean ignoreLibrary) {
        ArrayList<Movie> library = new ArrayList<>();
        for(int i = 0; i < movies.length(); i++) {
            JSONObject movieData = movies.getJSONObject(i);
            if(ignoreLibrary && searchByID(String.valueOf(movieData.getInt("tmdbId"))).length > 0) {
                continue;
            }
            library.add(parseMovie(movieData));
        }
        return library;
    }

    /**
     * Parse Radarr JSON to a movie
     *
     * @param movie Radarr movie json
     * @return Movie
     */
    private Movie parseMovie(JSONObject movie) {
        boolean downloaded = movie.getBoolean("downloaded");
        String plexUrl = null;
        if(downloaded) {
            String folder = movie.getString("path");
            plexUrl = folder.endsWith("/") ? plexUrls.get(folder.substring(0, folder.length() - 1)) : plexUrls.get(folder);
        }
        return new Movie.MovieBuilder()
                .setImdbId(movie.has("imdbId") ? movie.getString("imdbId") : null)
                .setTmdbId(String.valueOf(movie.getInt("tmdbId")))
                .setTitle(movie.getString("title"))
                .setSummary(movie.has("overview") ? movie.getString("overview") : null)
                .setReleaseDate(movie.has("inCinemas") ? movie.getString("inCinemas") : null)
                .setPlexURL(plexUrl)
                .setOnPlex(downloaded)
                .build();
    }

    /**
     * Build a message embed detailing a Movie from Radarr
     *
     * @param movie  Movie to build embed for
     * @param adding Movie is being added to Radarr
     * @return Movie embed
     */
    public MessageEmbed getMovieEmbed(Movie movie, boolean adding) {
        if(!movie.isComplete()) {
            movie.completeMovieDetails();
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setThumbnail(movie.isOnPlex() ? PLEX_ICON : RADARR_ICON)
                .setColor(movie.isOnPlex() ? EmbedHelper.ORANGE : EmbedHelper.BLUE)
                .setTitle(adding ? movie.getTitle() + " - Added to Radarr queue" : movie.getTitle());
        if(movie.getPoster() != null) {
            builder.setImage(movie.getPoster());
        }
        builder.setDescription(
                movie.buildEmbedDescription(
                        EmoteHelper.formatEmote(emoteHelper.getIMDB()),
                        EmoteHelper.formatEmote(emoteHelper.getFacebook()),
                        EmoteHelper.formatEmote(emoteHelper.getYoutube()),
                        EmoteHelper.formatEmote(emoteHelper.getPlex())
                )
        );
        double rating = movie.getRating();
        builder.setFooter("TMDB: " + ((rating == 0) ? "N/A" : rating) + " | Content Rating: " + movie.getContentRating() + " | Release Date: " + movie.getFormattedReleaseDate(), "https://i.imgur.com/J1JGC4J.png");
        return builder.build();
    }

    /**
     * Search for a movie currently in the Radarr library
     * Display either a message embed detailing the singular movie found, or a pageable message embed
     * displaying the search results.
     *
     * @param query   Search query - id or title
     * @param context Command context for initialising pageable embed
     */
    public void searchLibrary(String query, CommandContext context) {
        Movie[] results;

        // tt12345 or td12345
        if(query.matches("([t][td])\\d+")) {
            query = query.replaceFirst("td", "");
            results = searchByID(query);
        }
        else {
            results = searchByTitle(query);
            if(results.length == 0) {
                results = searchByQuery(query);
            }
        }

        if(results.length == 1) {
            context.getMessageChannel().sendMessage(getMovieEmbed(results[0], false)).queue();
        }
        new PageableMovieSearchEmbed(context, query, results, helpMessage, true).showMessage();
    }

    /**
     * Build an embed detailing a failure to complete
     *
     * @return Embed detailing failure
     */
    private MessageEmbed buildFailedEmbed() {
        return new EmbedBuilder()
                .setTitle("Plex Movie Search")
                .setDescription("Something went wrong, try again in a bit.")
                .setColor(EmbedHelper.RED)
                .setThumbnail(PLEX_ICON)
                .setFooter(helpMessage)
                .build();
    }

    /**
     * Search Radarr for a movie not currently in the library.
     * If a singular result is found, send a request to Radarr to add it to the library.
     * Display either a message embed detailing the singular movie found, or a pageable message embed
     * displaying the search results.
     *
     * @param query   Search query - id or title
     * @param webhook Webhook URL to respond to
     * @param context Command context for initialising pageable embed
     */
    public void searchRadarr(String query, String webhook, CommandContext context) {
        boolean idSearch = query.matches("([t][td])\\d+");
        if(idSearch) {
            query = query.replaceFirst("td", "");
        }
        String response = new NetworkRequest(getRadarrSearchURL(query, idSearch), false).get().body;
        if(response == null) {
            context.getMessageChannel().sendMessage(buildFailedEmbed()).queue();
            return;
        }
        JSONArray searchResults = idSearch ? new JSONArray().put(new JSONObject(response)) : new JSONArray(response);
        Movie[] movies = parseMovies(searchResults, true).toArray(new Movie[0]);
        if(movies.length == 1) {
            for(int i = 0; i < searchResults.length(); i++) {
                JSONObject result = searchResults.getJSONObject(i);
                if(result.getInt("tmdbId") == Integer.parseInt(movies[0].getTmdbId())) {
                    context.getMessageChannel().sendMessage(
                            getMovieEmbed(addToRadarr(result, context.getMember(), webhook), true)
                    ).queue();
                    return;
                }
            }
        }
        new PageableMovieSearchEmbed(context, query, movies, helpMessage, false).showMessage();
    }

    /**
     * Add a movie to Radarr
     *
     * @param movie   Movie JSON
     * @param member  Member who requested add
     * @param webhook Webhook URL to respond to
     * @return Movie added to Radarr
     */
    private Movie addToRadarr(JSONObject movie, Member member, String webhook) {
        String body = new JSONObject()
                .put("title", movie.getString("title"))
                .put("qualityProfileId", 4)
                .put("titleSlug", movie.getString("titleSlug"))
                .put("tmdbId", movie.getInt("tmdbId"))
                .put("year", movie.getInt("year"))
                .put("rootFolderPath", "/media/Movies/Standard")
                .put("monitored", true)
                .put("images", movie.getJSONArray("images"))
                .put("addOptions", new JSONObject().put("searchForMovie", true))
                .toString();

        long start = System.currentTimeMillis();
        Movie result = parseMovie(movie);
        result.completeMovieDetails();

        // Store the movie in the database for later callback informing of download
        new NetworkRequest("plex/monitor", true)
                .post(result.toJSON(member, System.currentTimeMillis() / 1000, webhook));

        // Add the movie to Radarr
        new NetworkRequest(getRadarrLibraryURL(), false).post(body);

        library.add(result);
        System.out.println(
                result.getTitle()
                        + " (" + result.getFormattedReleaseDate() + ") has been added to Radarr: "
                        + result.getIMDBUrl() + " (" + (System.currentTimeMillis() - start) + " ms)"
        );
        return result;
    }

    /**
     * Filter movies by the title containing a query
     *
     * @param query Query to check title for
     * @return Filtered array of movies that contain the query in the title
     */
    private Movie[] searchByQuery(String query) {
        return getMatchingMovies(movie -> movie.getTitle().toLowerCase().contains(query));
    }

    /**
     * Filter movies by title
     *
     * @param title Movie title
     * @return Filtered array of movies that match the title
     */
    private Movie[] searchByTitle(String title) {
        return getMatchingMovies(movie -> movie.getTitle().equalsIgnoreCase(title));
    }

    /**
     * Filter movies by id
     *
     * @param id Movie id
     * @return Filtered array of movies that match the id
     */
    private Movie[] searchByID(String id) {
        return getMatchingMovies(
                movie -> (movie.getImdbId() != null && movie.getImdbId().equals(id)) || movie.getTmdbId().equals(id)
        );
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
        return Secret.RADARR_IP + "/api/movie?apikey=" + Secret.RADARR_KEY;
    }

    /**
     * Get the URL to fetch movie data from Plex
     *
     * @return Plex URL
     */
    private String getPlexLibraryURL() {
        return Secret.PLEX_IP + Secret.PLEX_PORT + "/library/sections/" + Secret.PLEX_LIBRARY_ID + "/all/" + Secret.PLEX_TOKEN;
    }

    /**
     * Get the URL to search for a movie to add on Radarr
     *
     * @param query    Movie title
     * @param idSearch Search by id
     * @return Radarr URL
     */
    private String getRadarrSearchURL(String query, boolean idSearch) {
        try {
            return Secret.RADARR_IP + "/api/movie/lookup"
                    + (idSearch ? ((query.startsWith("tt") ? "/imdb?imdbId=" : "/tmdb?tmdbId=") + query) : "?term="
                    + URLEncoder.encode(query, "UTF-8")) + "&apikey=" + Secret.RADARR_KEY;
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the time the data was last fetched
     *
     * @return Time fetched of Radarr data
     */
    public long getTimeFetched() {
        return timeFetched;
    }
}
