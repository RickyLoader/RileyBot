package Twitch;

import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Network.Secret;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Twitch.tv
 */
public class TwitchTV {
    public static final String
            TWITCH_DOMAIN = "twitch.tv",
            TWITCH_URL = "https://www." + TWITCH_DOMAIN + "/",
            TWITCH_URL_REGEX = "https://(www|m)." + TWITCH_DOMAIN + "/",
            STREAMER_URL_REGEX = TWITCH_URL_REGEX + "(\\w)+/?(\\?.+)?",
            TWITCH_LOGO = "https://i.imgur.com/w1zOkVd.png";

    private final static String
            LOGIN_KEY = "broadcaster_login",
            DATA_KEY = "data",
            VIEWER_KEY = "viewer_count";

    private final HashMap<String, Game> gamesById;
    private final OAuth oAuth;
    private static TwitchTV instance;

    /**
     * Initialise a map of games & OAuth credentials
     */
    private TwitchTV() {
        this.oAuth = new OAuth(Secret.TWITCH_CLIENT_ID, Secret.TWITCH_CLIENT_SECRET);
        this.gamesById = new HashMap<>();
    }

    /**
     * Get an instance of the TwitchTV class
     *
     * @return Instance
     */
    public static TwitchTV getInstance() {
        if(instance == null) {
            instance = new TwitchTV();
        }
        return instance;
    }

    /**
     * Fetch the total followers for the given Twitch channel id
     *
     * @param id Twitch channel id
     * @return Total followers or null
     */
    @Nullable
    public Integer fetchFollowers(String id) {
        final JSONObject response = twitchRequest("users/follows?to_id=" + id);
        return response == null ? null : response.getInt("total");
    }

    /**
     * Search Twitch for streamers with the given query in their name.
     *
     * @param nameQuery Query to search for in the streamer name
     * @return List of Twitch streamer results (may be empty if results are unable to be retrieved)
     */
    public ArrayList<Streamer> searchStreamersByName(String nameQuery) {
        final ArrayList<Streamer> streamers = new ArrayList<>();
        final JSONObject response = twitchRequest("search/channels?first=20&query=" + nameQuery);

        // Failed to retrieve any results
        if(response == null) {
            return streamers;
        }

        final JSONArray streamerList = response.getJSONArray(DATA_KEY);

        for(int i = 0; i < streamerList.length(); i++) {
            JSONObject streamerData = streamerList.getJSONObject(i);
            streamers.add(
                    parseStreamer(
                            streamerData,
                            streamerData.getString(LOGIN_KEY).equalsIgnoreCase(nameQuery),
                            true
                    )
            );
        }

        ArrayList<Streamer> matching = streamers
                .stream()
                .filter(s -> s.getLoginName().equalsIgnoreCase(nameQuery))
                .collect(Collectors.toCollection(ArrayList::new));

        return matching.isEmpty() ? streamers : matching;
    }

    /**
     * Get the top 20 online streamers in the given category.
     *
     * @param categoryId ID of category to get streamers from
     * @return List of online Twitch streamer results (may be empty if results are unable to be retrieved)
     */
    public ArrayList<Streamer> getStreamersByCategoryId(long categoryId) {
        final ArrayList<Streamer> streamers = new ArrayList<>();
        final JSONObject response = twitchRequest("streams?game_id=" + categoryId);

        // Failed to retrieve any results
        if(response == null) {
            return streamers;
        }

        final JSONArray results = response.getJSONArray(DATA_KEY);

        for(int i = 0; i < results.length(); i++) {
            JSONObject streamer = results.getJSONObject(i);
            streamers.add(parseStreamer(streamer, false, false));
        }
        return streamers;
    }

    /**
     * Get the profile picture for the given streamer ID
     *
     * @param id Streamer ID
     * @return Streamer profile picture or null
     */
    @Nullable
    public String getStreamerProfilePicture(String id) {
        final JSONObject response = twitchRequest("users?id=" + id);

        // Failed to retrieve image
        if(response == null) {
            return null;
        }

        return response
                .getJSONArray(DATA_KEY)
                .getJSONObject(0)
                .getString("profile_image_url");
    }

    /**
     * Parse streamer JSON in to a Streamer object
     *
     * @param streamer      Streamer JSON
     * @param showFollowers Make an extra request to retrieve streamer followers
     * @param fromSearch    Streamer JSON is from a search request
     * @return Streamer
     */
    private Streamer parseStreamer(JSONObject streamer, boolean showFollowers, boolean fromSearch) {
        String id = fromSearch ? streamer.getString("id") : streamer.getString("user_id");

        String loginName = fromSearch
                ? streamer.getString(LOGIN_KEY)
                : streamer.getString("user_login");

        Streamer.StreamerBuilder builder = new Streamer.StreamerBuilder()
                .setLoginName(loginName)
                .setDisplayName(
                        fromSearch
                                ? streamer.getString("display_name")
                                : streamer.getString("user_name")
                )
                .setId(id);

        if(fromSearch) {
            builder.setThumbnail(streamer.getString("thumbnail_url"));
        }

        String langISO = fromSearch
                ? streamer.getString("broadcaster_language")
                : streamer.getString("language");

        if(!langISO.isEmpty()) {
            builder.setLanguage(EmbedHelper.getLanguageFromISO(langISO));
        }

        if(!fromSearch || streamer.getBoolean("is_live")) {
            final Integer viewers;

            if(streamer.has(VIEWER_KEY)) {
                viewers = streamer.getInt(VIEWER_KEY);
            }

            // Make an extra request for viewers
            else {
                viewers = fetchViewers(id);
            }

            builder.setStream(
                    new Stream(
                            streamer.getString("title"),
                            fetchGameById(streamer.getString("game_id")),
                            parseDate(streamer.getString("started_at")),
                            viewers,
                            Stream.getThumbnail(loginName)
                    )
            );
        }

        if(showFollowers) {
            builder.setFollowers(fetchFollowers(id));
        }

        return builder.build();
    }

    /**
     * Parse a Twitch.tv stream date from a String
     *
     * @param dateString Date String
     * @return Date of String
     */
    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat.parse(dateString);
        }
        catch(ParseException e) {
            return new Date();
        }
    }

    /**
     * Attempt to fetch the number of stream viewers via the streamer's unique id.
     * The request for viewers may fail to return any for some reason blame Red Bull.
     *
     * @param id Streamer id
     * @return Number of viewers watching streamer's current stream (or null)
     */
    @Nullable
    private Integer fetchViewers(String id) {
        final JSONObject response = twitchRequest("streams?user_id=" + id);
        try {

            // Unable to retrieve response for current stream viewers
            if(response == null) {
                throw new Exception("No response for current stream: " + id);
            }

            return response.getJSONArray(DATA_KEY).getJSONObject(0).getInt(VIEWER_KEY);
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Fetch a game via the unique id and parse in to a Game object
     *
     * @param gameId Unique id of game
     * @return Game object
     */
    @Nullable
    private Game fetchGameById(String gameId) {
        if(gamesById.containsKey(gameId)) {
            return gamesById.get(gameId);
        }

        final JSONObject response = twitchRequest("games?id=" + gameId);

        // No response for game ID
        if(response == null) {
            return null;
        }

        final JSONObject gameData = response.getJSONArray(DATA_KEY).getJSONObject(0);
        Game game = new Game(
                gameData.getString("name"),
                gameData.getString("id"),
                gameData.getString("box_art_url")
        );
        gamesById.put(game.getId(), game);
        return game;
    }

    /**
     * Perform a Twitch API request and return the response JSON.
     *
     * @param endpoint Twitch endpoint - e.g "search/channels?first=20&query=channel%20name"
     * @return API JSON response or null
     */
    @Nullable
    private JSONObject twitchRequest(String endpoint) {
        final String url = "https://api.twitch.tv/helix/" + endpoint;

        try {
            return new JSONObject(new NetworkRequest(url, false).get(getAuthHeaders()).body);
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the auth headers required to make requests to Twitch.tv
     *
     * @return Map of header name -> header value
     */
    private HashMap<String, String> getAuthHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Client-Id", oAuth.getClientId());
        headers.put("Authorization", "Bearer " + oAuth.getAccessToken());
        return headers;
    }

    /**
     * Check if the given URL is a URL to a Twitch.tv streamer.
     *
     * @param url URL to check
     * @return URL is a URL to a streamer
     */
    public static boolean isStreamerUrl(String url) {
        return url.matches(STREAMER_URL_REGEX);
    }
}
