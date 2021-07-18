package COD.Gunfight;

import COD.API.CODManager;
import COD.API.CODManager.GAME;
import COD.Assets.Map;
import COD.Assets.Mode;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Wobbly leaderboard entry
 */
public class WobblyScore {
    private final long wobblies;
    private final String name, matchId, key;
    private final long dateMs;
    private final double metres;
    private final Map map;
    private final Mode mode;
    private final GAME game;

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
     * @param game     COD game
     */
    public WobblyScore(long wobblies, double metres, String name, long dateMs, Map map, Mode mode, String matchId, GAME game) {
        this.wobblies = wobblies;
        this.name = name;
        this.dateMs = dateMs;
        this.map = map;
        this.mode = mode;
        this.matchId = matchId;
        this.metres = metres;
        this.game = game;
        this.key = matchId + name;
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
                .put("wobblies", wobblies)
                .put("player_name", name)
                .put("game", game.name().toUpperCase())
                .put("map_id", map.getCodename())
                .put("mode_id", mode.getCodename())
                .put("match_id", matchId)
                .put("metres", metres)
                .put("dateMs", dateMs);
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
                json.getLong("wobblies"),
                json.getDouble("metres"),
                json.getString("player_name"),
                json.getLong("dateMs"),
                codManager.getMapByCodename(json.getString("map_id")),
                codManager.getModeByCodename(json.getString("mode_id")),
                json.getString("match_id"),
                codManager.getGame()
        );
    }
}
