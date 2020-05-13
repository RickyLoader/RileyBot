package COD;

import Network.ApiRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

/**
 * Stores information about a gunfight game session
 */
class Session {
    private static String endPoint = "gunfight";
    private long date, duration;
    private int wins, losses, streak;
    private double ratio;

    /**
     * Constructor for building a Session instance from a completed Gunfight
     */
    Session(long startTime, long date, int wins, int losses, int streak) {
        this.date = date;
        this.wins = wins;
        this.losses = losses;

        // calculate win/loss ratio
        this.ratio = getRatio();
        this.streak = streak;

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
            this.streak = o.getInt("streak");
            this.duration = o.getLong("duration");
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Submit the gunfight session to the database
     */
    void submitGame() {
        ApiRequest.executeQuery(endPoint + "/submit", "ADD", getGameSummary(), true);
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
                .put("streak", streak)
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
    static ArrayList<Session> getHistory() {
        JSONArray matches = new JSONArray(ApiRequest.executeQuery(endPoint + "/fetch", "GET", null, true));
        ArrayList<Session> history = new ArrayList<>();
        for(int i = 0; i < matches.length(); i++) {
            history.add(new Session(matches.getJSONObject(i)));
        }
        Collections.sort(history, Comparator.comparing(Session::getRatio).thenComparing(Session::getStreak).thenComparing(Session::getWins).reversed());
        return history;
    }

    /**
     * Get the current win/loss ratio
     *
     * @return Win/loss ratio
     */
    double getRatio() {

        // Can't divide by zero
        if(wins == 0) {
            return 0.0;
        }

        if(losses == 0) {
            return wins;
        }

        return (double) wins / (double) losses;
    }

    /**
     * Fetch the total match numbers
     *
     * @return Total match numbers
     */
    static int getTotalMatches() {
        String json = ApiRequest.executeQuery(endPoint + "/total", "GET", null, true);
        return Integer.parseInt(new JSONObject(json).getString("matches"));
    }

    /**
     * Format the session ms duration to HH:MM:SS
     *
     * @return Formatted string duration
     */
    String getDuration() {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(duration),
                TimeUnit.MILLISECONDS.toMinutes(duration) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
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
    int getStreak() {
        return streak;
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
    String formatStreak() {

        // 1 win vs 2 wins
        return streak + ((streak == 1) ? " WIN" : " WINS");
    }

    /**
     * Format the ms date to dd/MM/YY
     *
     * @return Formatted session date
     */
    String formatDate() {
        return new SimpleDateFormat("dd/MM/YY").format(date);
    }

    /**
     * Get the rank of the current session within all sessions
     *
     * @return Session rank
     */
    int getRank() {
        ArrayList<Session> history = getHistory();
        int rank = -1;
        for(int i = 0; i < history.size(); i++) {
            Session session = history.get(i);
            if(session.getDate() == date) {
                rank = i;
                break;
            }
        }
        return rank + 1;
    }

    /**
     * Format the win/loss ratio to 2 decimal places
     *
     * @return win/loss ratio truncated to 2 decimal places
     */
    String formatRatio() {
        return wins + "/" + losses + " (" + new DecimalFormat("0.00").format(ratio) + ")";
    }
}
