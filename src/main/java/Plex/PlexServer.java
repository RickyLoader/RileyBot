package Plex;

import Command.Structure.*;
import Movies.*;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Network.Secret;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Predicate;

public class PlexServer {
    private final String helpMessage;
    public static final String
            RADARR_ICON = "https://i.imgur.com/d5p0ftd.png",
            PLEX_ICON = "https://i.imgur.com/FdabwCm.png",
            TMDB_ID_PREFIX = "td";
    private long lastRefreshed;
    private ArrayList<PlexMovie> library;
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
        this.plexUrls = parsePlexDetails();
        this.library = getLibraryOverview();
        this.lastRefreshed = System.currentTimeMillis();
    }

    /**
     * Retrieve the Plex URL for each movie from the Plex API. Map the movie's folder name to the URL.
     *
     * @return Map of movie folder name -> Plex URL
     */
    private HashMap<String, String> parsePlexDetails() {
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
    private ArrayList<PlexMovie> getLibraryOverview() {
        ArrayList<PlexMovie> library = new ArrayList<>();
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
     * @param ignoreLibrary Ignore results which are on Radarr (For when searching outside library)
     * @return List of movies
     */
    private ArrayList<PlexMovie> parseMovies(JSONArray movies, boolean ignoreLibrary) {
        ArrayList<PlexMovie> library = new ArrayList<>();
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
    private PlexMovie parseMovie(JSONObject movie) {
        boolean downloaded = movie.getBoolean("downloaded");
        String plexUrl = null;
        if(downloaded) {
            String folder = movie.getString("path");
            if(folder.endsWith("/")) {

                // Remove the trailing slash as it is not present in Plex folder paths but can be in Radarr folder paths
                folder = folder.substring(0, folder.length() - 1);
            }
            plexUrl = plexUrls.get(folder);
        }

        RatingIds.RatingIdsBuilder builder = new RatingIds.RatingIdsBuilder()
                .setTmdbId(String.valueOf(movie.getInt("tmdbId")));

        String imdbKey = "imdbId";
        if(movie.has(imdbKey)) {
            builder.setImdbId(movie.getString(imdbKey));
        }

        return new PlexMovie(
                new PlexDetails(
                        builder.build(),
                        movie.getString("title"),
                        plexUrl,
                        downloaded
                )
        );
    }

    /**
     * Build a message embed detailing a Movie from Radarr
     *
     * @param plexMovie Plex movie to build embed for
     * @param adding    Movie is being added to Radarr
     * @return Movie embed
     */
    public MessageEmbed getMovieEmbed(PlexMovie plexMovie, boolean adding) {
        if(!plexMovie.hasMovie()) {
            completeMovie(plexMovie);
        }

        PlexDetails plexDetails = plexMovie.getPlexDetails();
        Movies.Movie movie = plexMovie.getMovie();
        MovieEmbedBuilder builder = new MovieEmbedBuilder(
                movie,
                emoteHelper,
                plexDetails.isOnPlex() ? PLEX_ICON : RADARR_ICON,
                plexDetails.isOnPlex() ? EmbedHelper.ORANGE : EmbedHelper.BLUE
        ) {

            @Override
            public String getDescription() {
                String description = super.getDescription();
                if(plexMovie.getPlexDetails().isOnPlex()) {
                    return description;
                }
                return "Movie **is not** on Plex but is being monitored." + "\n\n" + super.getDescription();
            }

            @Override
            public ArrayList<String> getSocialElements() {
                ArrayList<String> socialElements = super.getSocialElements();
                if(plexDetails.isOnPlex()) {
                    String plexEmote = emoteHelper.getPlex().getAsMention();
                    socialElements.add(EmbedHelper.embedURL(plexEmote + " Plex", plexDetails.getPlexUrl()));
                }
                return socialElements;
            }

            @Override
            public String getTitle() {
                return adding ? super.getTitle() + " - Added to Radarr queue" : super.getTitle();
            }
        };
        return builder.build();
    }

    /**
     * Set the movie details of the Plex movie
     *
     * @param plexMovie Plex movie to set movie details for
     */
    private void completeMovie(PlexMovie plexMovie) {
        plexMovie.setMovie(
                TheMovieDatabase.getMovieById(plexMovie.getPlexDetails().getRatingIds().getTmdbId().getId())
        );
    }

    /**
     * Check if the given query is a TMDB (td12345) or IMDB ID (tt12345)
     *
     * @param query Query to check
     * @return Query is an ID
     */
    private boolean isId(String query) {
        return query.matches("([t][" + TMDB_ID_PREFIX + "])\\d+");
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
        PlexMovie[] results;

        if(isId(query)) {
            query = query.replaceFirst(TMDB_ID_PREFIX, "");
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
            return;
        }
        showSearchResults(context, query, results, true);
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
     * @param context Command context for initialising pageable embed
     */
    public void searchRadarr(String query, CommandContext context) {
        boolean idSearch = isId(query);
        if(idSearch) {
            query = query.replaceFirst(TMDB_ID_PREFIX, "");
        }

        String response = new NetworkRequest(getRadarrSearchURL(query, idSearch), false).get().body;
        if(response == null) {
            context.getMessageChannel().sendMessage(buildFailedEmbed()).queue();
            return;
        }

        JSONArray searchResults = idSearch ? new JSONArray().put(new JSONObject(response)) : new JSONArray(response);
        PlexMovie[] movies = parseMovies(searchResults, true).toArray(new PlexMovie[0]);

        if(movies.length == 1) {
            for(int i = 0; i < searchResults.length(); i++) {
                JSONObject result = searchResults.getJSONObject(i);
                String tmdbId = String.valueOf(result.getInt("tmdbId"));
                if(tmdbId.equals(movies[0].getPlexDetails().getRatingIds().getTmdbId().getId())) {
                    context.getMessageChannel().sendMessage(
                            getMovieEmbed(addToRadarr(result), true)
                    ).queue();
                    return;
                }
            }
        }
        showSearchResults(context, query, movies, false);
    }

    /**
     * Add a movie to Radarr
     *
     * @param movie Movie JSON
     * @return Movie added to Radarr
     */
    private PlexMovie addToRadarr(JSONObject movie) {
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

        PlexMovie result = parseMovie(movie);
        completeMovie(result);

        // Add the movie to Radarr
        new NetworkRequest(getRadarrLibraryURL(), false).post(body);

        library.add(result);
        return result;
    }

    /**
     * Filter movies by the title containing a query
     *
     * @param query Query to check title for
     * @return Filtered array of movies that contain the query in the title
     */
    private PlexMovie[] searchByQuery(String query) {
        return getMatchingMovies(plexMovie -> plexMovie.getPlexDetails().getTitle().toLowerCase().contains(query));
    }

    /**
     * Filter movies by title
     *
     * @param title Movie title
     * @return Filtered array of movies that match the title
     */
    private PlexMovie[] searchByTitle(String title) {
        return getMatchingMovies(plexMovie -> plexMovie.getPlexDetails().getTitle().equalsIgnoreCase(title));
    }

    /**
     * Filter movies by id
     *
     * @param id Movie id
     * @return Filtered array of movies that match the id
     */
    private PlexMovie[] searchByID(String id) {
        return getMatchingMovies(plexMovie -> {
            RatingIds ratingIds = plexMovie.getPlexDetails().getRatingIds();
            return ratingIds.hasImdbId() && ratingIds.getImdbId().getId().equals(id) || ratingIds.getTmdbId().getId().equals(id);
        });
    }

    /**
     * Filter the library by a predicate
     *
     * @param p Predicate to filter by
     * @return Filtered array of movies
     */
    private PlexMovie[] getMatchingMovies(Predicate<PlexMovie> p) {
        return library.stream().filter(p).toArray(PlexMovie[]::new);
    }

    /**
     * Get a random movie from the Radarr library that is downloaded (on Plex)
     *
     * @return Random movie
     */
    public PlexMovie getRandomMovie() {
        PlexMovie[] library = getMatchingMovies(plexMovie -> plexMovie.getPlexDetails().isOnPlex());
        return library[new Random().nextInt(library.length)];
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
                    + EmbedHelper.urlEncode(query)) + "&apikey=" + Secret.RADARR_KEY;
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
    public long getLastRefreshed() {
        return lastRefreshed;
    }

    /**
     * Display the given array of Plex movies in a pageable message embed.
     *
     * @param context       Command context
     * @param query         Query used to find the movies
     * @param searchResults Array of movies found with the given query
     * @param inLibrary     Search was performed on the library (not externally on Radarr)
     */
    private void showSearchResults(CommandContext context, String query, PlexMovie[] searchResults, boolean inLibrary) {
        new PageableTableEmbed(
                context,
                Arrays.asList(searchResults),
                inLibrary ? PLEX_ICON : RADARR_ICON,
                inLibrary ? "Plex Movie Search" : "Radarr Movie Search",
                buildDescription(query, searchResults, emoteHelper, inLibrary),
                helpMessage,
                new String[]{"Platform", "Title", "ID"},
                5,
                inLibrary ? EmbedHelper.ORANGE : EmbedHelper.BLUE
        ) {

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort(new LevenshteinDistance(query, defaultSort) {
                    @Override
                    public String getString(Object o) {
                        return ((PlexMovie) o).getPlexDetails().getTitle();
                    }
                });
            }

            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                PlexDetails plexDetails = ((PlexMovie) items.get(index)).getPlexDetails();
                return new String[]{
                        plexDetails.isOnPlex()
                                ? emoteHelper.getPlex().getAsMention()
                                : emoteHelper.getRadarr().getAsMention(),
                        plexDetails.getTitle(),
                        plexDetails.getRatingIds().getFormattedId()
                };
            }
        }.showMessage();
    }

    /**
     * Build the description to display in the pageable embed. Show a key for the platform
     * icons that will be displayed in the message (Based on the platform the movies were found on).
     *
     * @param query       Search query used to find results
     * @param movies      Array of movies found for search query
     * @param emoteHelper Emote helper
     * @param inLibrary   Search was performed on the library (not externally on Radarr)
     * @return Description showing emote key
     */
    private static String buildDescription(String query, PlexMovie[] movies, EmoteHelper emoteHelper, boolean inLibrary) {
        if(movies.length == 0) {
            String description = "No movie results found for: **" + query + "**, try again cunt.";
            if(!inLibrary) {
                description += "\n\nI won't find movies that are already on Radarr.";
            }
            return description;
        }
        return buildEmoteKey(movies, emoteHelper, inLibrary)
                + "I found "
                + movies.length
                + " results for: **"
                + query
                + "**"
                + "\n\nNarrow it down next time cunt, here they are:";
    }

    /**
     * Build an emote key to explain the platform icons that are required
     * based on the platform that the movies were found on
     *
     * @param movies      Array of movies found for search query
     * @param emoteHelper Emote helper
     * @param inLibrary   Search was performed on the library (not externally on Radarr)
     * @return Emote key
     */
    private static String buildEmoteKey(PlexMovie[] movies, EmoteHelper emoteHelper, boolean inLibrary) {
        StringBuilder key = new StringBuilder();
        if(Arrays.stream(movies).anyMatch(plexMovie -> plexMovie.getPlexDetails().isOnPlex())) {
            key.append(emoteHelper.getPlex().getAsMention()).append(" = On Plex\n\n");
        }
        if(Arrays.stream(movies).anyMatch(plexMovie -> !plexMovie.getPlexDetails().isOnPlex())) {
            key
                    .append(emoteHelper.getRadarr().getAsMention())
                    .append(
                            inLibrary
                                    ? " = On Radarr - Movie **is not** on Plex but is being monitored.\n\n"
                                    : " = Available on Radarr - Search by the id to add to Plex.\n\n"
                    );
        }
        return key.toString();
    }
}
