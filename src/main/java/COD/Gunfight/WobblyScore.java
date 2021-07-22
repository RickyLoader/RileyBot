package COD.Gunfight;

import COD.API.CODManager;
import COD.Assets.Map;
import COD.Assets.Mode;
import COD.Match.MatchStats;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Wobbly leaderboard entry
 */
public class WobblyScore {
    private final long wobblies;
    private final String name, matchId, key, gameId;
    private final long dateMs;
    private final double metres;
    private final Map map;
    private final Mode mode;
    private static final String
            WOBBLIES_KEY = "wobblies",
            PLAYER_NAME_KEY = "player_name",
            GAME_ID_KEY = "game",
            MAP_ID_KEY = "map_id",
            MODE_ID_KEY = "mode_id",
            MATCH_ID_KEY = "match_id",
            METRES_KEY = "metres",
            DATE_KEY = "dateMs";

    /**
     * Create a wobbly score
     *
     * @param wobblies Total wobblies travelled
     * @param metres   Total metres travelled
     * @param name     Player name
     * @param dateMs   Date of achievement (in ms)
     * @param map      Map
     * @param mode     Mode
     * @param matchId  Match Id
     * @param gameId   COD game ID - e.g "MW"
     */
    public WobblyScore(long wobblies, double metres, String name, long dateMs, Map map, Mode mode, String matchId, String gameId) {
        this.wobblies = wobblies;
        this.name = name;
        this.dateMs = dateMs;
        this.map = map;
        this.mode = mode;
        this.matchId = matchId;
        this.metres = metres;
        this.gameId = gameId;
        this.key = generateKey(matchId, name);
    }

    /**
     * Generate a wobblies db key
     *
     * @param matchId    ID of a match
     * @param playerName Player name
     * @return Wobblies db key
     */
    private static String generateKey(String matchId, String playerName) {
        return matchId + playerName;
    }

    /**
     * Generate a key from the given match stats
     *
     * @param matchStats Match stats to generate key for
     * @return Wobblies db key
     */
    public static String generateKey(MatchStats matchStats) {
        return generateKey(matchStats.getId(), matchStats.getMainPlayer().getName());
    }

    /**
     * Get the unique score key (match id + name)
     *
     * @return Score key
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the match id of the match where the score was obtained
     *
     * @return Match id
     */
    public String getMatchId() {
        return matchId;
    }

    /**
     * Get the mode where the wobblies were achieved
     *
     * @return Mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Get the map where the wobblies were achieved
     *
     * @return Map
     */
    public Map getMap() {
        return map;
    }

    /**
     * Sort the leaderboard in order of wobblies
     *
     * @param leaderboard List of WobblyScore to sort
     * @param descending  Descending order of wobblies
     */
    public static void sortLeaderboard(List<WobblyScore> leaderboard, boolean descending) {
        leaderboard.sort((o1, o2) -> {
            if(descending) {
                return Long.compare(o2.getWobblies(), o1.getWobblies());
            }
            return Long.compare(o1.getWobblies(), o2.getWobblies());
        });
    }

    /**
     * Get the player name
     *
     * @return Player name
     */
    public String getPlayerName() {
        return name;
    }

    /**
     * Get the date as a String
     *
     * @return Date String
     */
    public String getDateString() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(dateMs));
    }

    /**
     * Get the wobblies travelled
     *
     * @return Wobblies travelled
     */
    public long getWobblies() {
        return wobblies;
    }

    /**
     * Get the metres travelled
     *
     * @return Metres travelled
     */
    public double getMetres() {
        return metres;
    }

    /**
     * WobblyScore -> JSON Object
     *
     * @return JSON object of WobblyScore
     */
    public JSONObject toJSON() {
        return new JSONObject()
                .put(WOBBLIES_KEY, wobblies)
                .put(PLAYER_NAME_KEY, name)
                .put(GAME_ID_KEY, gameId)
                .put(MAP_ID_KEY, map.getCodename())
                .put(MODE_ID_KEY, mode.getCodename())
                .put(MATCH_ID_KEY, matchId)
                .put(METRES_KEY, metres)
                .put(DATE_KEY, dateMs);
    }

    /**
     * Create a WobblyScore from JSON
     *
     * @param json       JSON object
     * @param codManager COD asset manager
     * @return WobblyScore
     */
    public static WobblyScore fromJSON(JSONObject json, CODManager codManager) {
        return new WobblyScore(
                json.getLong(WOBBLIES_KEY),
                json.getDouble(METRES_KEY),
                json.getString(PLAYER_NAME_KEY),
                json.getLong(DATE_KEY),
                codManager.getMapByCodename(json.getString(MAP_ID_KEY)),
                codManager.getModeByCodename(json.getString(MODE_ID_KEY)),
                json.getString(MATCH_ID_KEY),
                json.getString(GAME_ID_KEY)
        );
    }
}
