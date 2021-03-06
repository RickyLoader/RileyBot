package COD;

import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Stores information about a previous gunfight game session
 */
public class Session {
    private static final String endPoint = "gunfight";
    private long date, duration;
    private int wins, losses, longestStreak;
    private double ratio;

    /**
     * Constructor for building a Session instance from a completed Gunfight
     *
     * @param startTime Start time of session
     * @param score     Session score
     */
    Session(long startTime, GunfightScore score) {
        this.date = score.getLastUpdate();
        this.wins = score.getWins();
        this.losses = score.getLosses();

        // calculate win/loss ratio
        this.ratio = getRatio();
        this.longestStreak = score.getLongestStreak();

        // duration of session
        this.duration = date - startTime;
    }

    /**
     * Constructor for building a Session instance from the API
     *
     * @param o JSONObject representing a Session
     */
    private Session(JSONObject o) {
        try {
            this.date = o.getLong("date");
            this.wins = o.getInt("win");
            this.losses = o.getInt("loss");
            this.ratio = o.getDouble("ratio");
            this.longestStreak = o.getInt("streak");
            this.duration = o.getLong("duration");
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sort the gunfight sessions for leaderboard purposes
     *
     * @param sessions  Sessions to sort
     * @param ascending Ascending rank
     */
    public static void sortSessions(List<Session> sessions, boolean ascending) {
        Comparator<Session> sort = Comparator.comparing(Session::getLongestStreak)
                .thenComparing(Session::getRatio)
                .thenComparing(Session::getWins)
                .thenComparing(Comparator.comparingInt(Session::getGamesPlayed).reversed());

        sessions.sort(ascending ? sort.reversed() : sort);
    }

    /**
     * Submit the gunfight session to the database
     */
    void submitGame() {
        new NetworkRequest(endPoint + "/submit", true).post(getGameSummary());
    }

    /**
     * Summarise the game score in JSON format
     *
     * @return JSON formatted game summary
     */
    private String getGameSummary() {
        return new JSONObject()
                .put("win", wins)
                .put("loss", losses)
                .put("streak", longestStreak)
                .put("ratio", ratio)
                .put("date", date)
                .put("duration", duration)
                .toString();
    }

    /**
     * Fetch previous gunfight sessions from the API
     *
     * @return Gunfight history
     */
    public static ArrayList<Session> getHistory() {
        String json = new NetworkRequest(endPoint + "/fetch", true).get().body;
        if(json == null) {
            return null;
        }
        JSONArray matches = new JSONArray(json);
        ArrayList<Session> history = new ArrayList<>();
        for(int i = 0; i < matches.length(); i++) {
            history.add(new Session(matches.getJSONObject(i)));
        }
        Session.sortSessions(history, true);
        return history;
    }

    /**
     * Get the current win/loss ratio
     *
     * @return Win/loss ratio
     */
    double getRatio() {
        if(wins == 0) {
            return 0.0;
        }

        if(losses == 0) {
            return wins;
        }

        return (double) wins / (double) losses;
    }

    /**
     * Get the wins and losses combined
     *
     * @return Wins and losses combined
     */
    public int getGamesPlayed() {
        return wins + losses;
    }

    /**
     * Fetch the total match numbers
     *
     * @return Total match numbers
     */
    static int getTotalMatches() {
        String json = new NetworkRequest(endPoint + "/total", true).get().body;
        if(json == null) {
            return 0;
        }
        return Integer.parseInt(new JSONObject(json).getString("matches"));
    }

    /**
     * Format the session ms duration to HH:MM:SS
     *
     * @return Formatted string duration
     */
    public String getDuration() {
        return EmbedHelper.formatDuration(duration);
    }

    /**
     * Get the number of wins
     *
     * @return Number of wins
     */
    int getWins() {
        return wins;
    }

    /**
     * Get the number of losses
     *
     * @return Number of losses
     */
    int getLosses() {
        return losses;
    }

    /**
     * Get the longest win streak of the session
     *
     * @return Longest win streak
     */
    public int getLongestStreak() {
        return longestStreak;
    }

    /**
     * Gets the ms date of session
     *
     * @return ms date of session
     */
    long getDate() {
        return date;
    }

    /**
     * Stringify the longest win streak of the session
     *
     * @return String version of win streak
     */
    public String formatStreak() {
        return longestStreak + ((longestStreak == 1) ? " WIN" : " WINS");
    }

    /**
     * Format the ms date to dd/MM/YY
     *
     * @return Formatted session date
     */
    public String getFormattedDate() {
        return new SimpleDateFormat("dd/MM/yy").format(date);
    }

    /**
     * Format the win/loss ratio to 2 decimal places
     *
     * @return win/loss ratio truncated to 2 decimal places
     */
    public String getFormattedRatio() {
        return new DecimalFormat("0.00").format(ratio);
    }

    /**
     * Get a String containing the win/loss and ratio
     *
     * @return Formatted win/loss summary String
     */
    public String getWinLossSummary() {
        return getFormattedWinLoss() + " (" + getFormattedRatio() + ")";
    }

    /**
     * Format the wins and losses to wins/losses
     *
     * @return Formatted win loss String
     */
    public String getFormattedWinLoss() {
        return wins + "/" + losses;
    }
}
