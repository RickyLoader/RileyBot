package Twitch;

import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Network.Secret;
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
            TWITCH_URL = "https://www.twitch.tv/",
            TWITCH_LOGO = "https://i.imgur.com/w1zOkVd.png";
    private final String
            baseURL = "https://api.twitch.tv/",
            helix = baseURL + "helix/",
            kraken = baseURL + "kraken/";
    private final HashMap<String, Game> gamesById, gamesByName;
    private final OAuth oAuth;

    /**
     * Initialise a map of games & OAuth credentials
     */
    public TwitchTV() {
        this.oAuth = new OAuth(Secret.TWITCH_CLIENT_ID, Secret.TWITCH_CLIENT_SECRET);
        this.gamesById = new HashMap<>();
        this.gamesByName = new HashMap<>();
    }

    /**
     * Fetch the total followers for the given Twitch channel id
     *
     * @param id Twitch channel id
     * @return Total followers
     */
    public int fetchFollowers(String id) {
        JSONObject response = new JSONObject(
                new NetworkRequest(helix + "users/follows?to_id=" + id, false)
                        .get(getAuthHeaders())
                        .body
        );
        return response.getInt("total");
    }

    /**
     * Search Twitch for streamers with the given query in their name.
     *
     * @param nameQuery Query to search for in the streamer name
     * @return List of Twitch streamer results
     */
    public ArrayList<Streamer> searchStreamersByName(String nameQuery) {
        JSONArray results = getStreamerSearchResultsByName(nameQuery);
        ArrayList<Streamer> streamers = new ArrayList<>();
        for(int i = 0; i < results.length(); i++) {
            JSONObject streamerData = results.getJSONObject(i);
            streamers.add(
                    parseHelixStreamer(
                            streamerData,
                            streamerData.getString("broadcaster_login").equalsIgnoreCase(nameQuery)
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
     * Search Twitch for online streamers with the given query in their stream title.
     *
     * @param titleQuery Query to search for in the stream title
     * @param category   Stream category
     * @return List of online Twitch streamer results
     */
    public ArrayList<Streamer> searchStreamersByStreamTitle(String titleQuery, String category) {
        ArrayList<Streamer> streamers = new ArrayList<>();
        JSONArray results = getStreamerSearchResultsByStreamTitle(titleQuery);
        for(int i = 0; i < results.length(); i++) {
            JSONObject streamer = results.getJSONObject(i);
            if(!streamer.getJSONObject("channel").getString("game").equalsIgnoreCase(category)) {
                continue;
            }
            streamers.add(parseKrakenStreamer(streamer));
        }
        return streamers;
    }

    /**
     * Parse streamer JSON from a kraken endpoint in to a Streamer
     *
     * @param streamer Streamer JSON
     * @return Streamer
     */
    private Streamer parseKrakenStreamer(JSONObject streamer) {
        JSONObject channel = streamer.getJSONObject("channel");
        String id = String.valueOf(channel.getLong("_id"));
        String loginName = channel.getString("name");
        Streamer.StreamerBuilder builder = new Streamer.StreamerBuilder()
                .setLoginName(loginName)
                .setDisplayName(channel.getString("display_name"))
                .setId(id)
                .setThumbnail(channel.getString("logo"));

        String langISO = channel.getString("broadcaster_language");
        if(!langISO.isEmpty()) {
            builder.setLanguage(EmbedHelper.getLanguageFromISO(langISO));
        }
        builder.setStream(
                new Stream(
                        channel.getString("status"),
                        fetchGameByName(channel.getString("game")),
                        parseDate(streamer.getString("created_at")),
                        fetchViewers(id),
                        Stream.getThumbnail(loginName)
                )
        );
        return builder.build();
    }

    /**
     * Parse streamer JSON from a helix endpoint in to a Streamer
     *
     * @param streamer      Streamer JSON
     * @param showFollowers Make an extra request to retrieve streamer followers
     * @return Streamer
     */
    private Streamer parseHelixStreamer(JSONObject streamer, boolean showFollowers) {
        String id = streamer.getString("id");
        String loginName = streamer.getString("broadcaster_login");
        Streamer.StreamerBuilder builder = new Streamer.StreamerBuilder()
                .setLoginName(loginName)
                .setDisplayName(streamer.getString("display_name"))
                .setId(id)
                .setThumbnail(streamer.getString("thumbnail_url"));

        String langISO = streamer.getString("broadcaster_language");
        if(!langISO.isEmpty()) {
            builder.setLanguage(EmbedHelper.getLanguageFromISO(langISO));
        }
        if(streamer.getBoolean("is_live")) {
            builder.setStream(
                    new Stream(
                            streamer.getString("title"),
                            fetchGameById(streamer.getString("game_id")),
                            parseDate(streamer.getString("started_at")),
                            fetchViewers(id),
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
     * Fetch the number of stream viewers via the streamer's unique id
     *
     * @param id Streamer id
     * @return Number of viewers watching streamer's current stream
     */
    private int fetchViewers(String id) {
        JSONObject response = new JSONObject(
                new NetworkRequest(helix + "streams?user_id=" + id, false).get(getAuthHeaders()).body
        ).getJSONArray("data").getJSONObject(0);
        return response.getInt("viewer_count");
    }

    /**
     * Fetch a game via the name
     *
     * @param name Game name
     * @return Game
     */
    private Game fetchGameByName(String name) {
        name = name.toLowerCase();
        if(gamesByName.containsKey(name)) {
            return gamesByName.get(name);
        }
        String url = kraken + "search/games?query=" + EmbedHelper.urlEncode(name);
        JSONObject response = new JSONObject(krakenRequest(url).body).getJSONArray("games").getJSONObject(0);
        Game game = new Game(
                response.getString("name"),
                String.valueOf(response.getLong("_id")),
                response.getJSONObject("box").getString("large")
        );
        mapGame(game);
        return game;
    }

    /**
     * Map the given game by id and name
     *
     * @param game Game to map
     */
    private void mapGame(Game game) {
        gamesByName.put(game.getName().toLowerCase(), game);
        gamesById.put(game.getId(), game);
    }

    /**
     * Fetch a game via the unique id and parse in to a Game object
     *
     * @param gameId Unique id of game
     * @return Game object
     */
    private Game fetchGameById(String gameId) {
        if(gamesById.containsKey(gameId)) {
            return gamesById.get(gameId);
        }
        String url = helix + "games?id=" + gameId;
        JSONObject gameData = new JSONObject(
                new NetworkRequest(url, false).get(getAuthHeaders()).body
        ).getJSONArray("data").getJSONObject(0);
        Game game = new Game(
                gameData.getString("name"),
                gameData.getString("id"),
                gameData.getString("box_art_url")
        );
        mapGame(game);
        return game;
    }

    /**
     * Get streamer search results for the given query.
     * Search by the streamer name
     *
     * @param nameQuery Query to search for in streamer name
     * @return JSONArray search results
     */
    private JSONArray getStreamerSearchResultsByName(String nameQuery) {
        String url = helix + "search/channels?first=20&query=" + nameQuery;
        JSONObject response = new JSONObject(new NetworkRequest(url, false).get(getAuthHeaders()).body);
        return response.getJSONArray("data");
    }

    /**
     * Get online streamer search results for the given query.
     * Search by the stream title
     *
     * @param titleQuery Query to search for in stream title
     * @return JSONArray search results
     */
    private JSONArray getStreamerSearchResultsByStreamTitle(String titleQuery) {
        String url = kraken + "search/streams/?query=" + EmbedHelper.urlEncode(titleQuery) + "&hls=true";
        JSONObject response = new JSONObject(krakenRequest(url).body);
        return response.getJSONArray("streams");
    }

    /**
     * Perform a Twitch kraken API request
     *
     * @param url URL to request
     * @return URL response
     */
    private NetworkResponse krakenRequest(String url) {
        HashMap<String, String> headers = getAuthHeaders();
        headers.put("Accept", "application/vnd.twitchtv.v5+json");
        return new NetworkRequest(url, false).get(headers);
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
}
