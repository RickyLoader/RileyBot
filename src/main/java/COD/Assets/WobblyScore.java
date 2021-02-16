package COD.Assets;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Wobbly leaderboard entry
 */
public class WobblyScore {
    private final long wobblies;
    private final String name, matchId;
    private final long dateMs;
    private final double metres;
    private final Map map;
    private final Mode mode;

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
     */
    public WobblyScore(long wobblies, double metres, String name, long dateMs, Map map, Mode mode, String matchId) {
        this.wobblies = wobblies;
        this.name = name;
        this.dateMs = dateMs;
        this.map = map;
        this.mode = mode;
        this.matchId = matchId;
        this.metres = metres;
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
    public static void sortLeaderboard(ArrayList<WobblyScore> leaderboard, boolean descending) {
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
}
