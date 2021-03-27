package Bot;

import COD.Assets.WobblyScore;
import COD.CODManager.GAME;
import Network.NetworkRequest;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Helper methods for storing and retrieving information based on User id
 */
public class DiscordUser {
    public static final String
            MW = "mw",
            CW = "cw",
            OSRS = "osrslookup",
            LOL = "lollookup",
            YT = "ytlookup",
            TTV = "ttvlookup",
            STEAM_ID = "steamid",
            RS3 = "rs3lookup";

    /**
     * Store a string in the database by a user's id
     *
     * @param name     Name to be saved
     * @param nameType Name type to save
     * @param channel  Channel to notify of save
     * @param user     User to save name for
     */
    public static void saveName(String name, String nameType, MessageChannel channel, User user) {
        JSONObject body = new JSONObject().put("discord_id", user.getIdLong()).put("name_type", nameType).put("name", name);
        new NetworkRequest("users/submit", true).post(body.toString());
        String message = user.getAsMention() + " Your **" + nameType + "** ";
        if(!nameType.equals(STEAM_ID)) {
            message += "name ";
        }
        message += "is now **" + name + "**";
        channel.sendMessage(message).queue();
    }

    /**
     * Retrieve a name from the database
     *
     * @param id       id the name is stored by
     * @param nameType Name type to retrieve
     * @return Saved name for given table
     */
    public static String getSavedName(long id, String nameType) {
        String json = new NetworkRequest("users/names/" + nameType + "/" + id, true).get().body;
        return json.isEmpty() || new JSONObject(json).isNull(nameType) ? null : new JSONObject(json).getString(nameType);
    }

    /**
     * Get a user's millionaire bank stats
     *
     * @param id User id of millionaire contestant
     * @return User's millionaire bank stats
     */
    public static String getMillionaireBankData(long id) {
        String json = new NetworkRequest("millionaire/bank/" + id, true).get().body;
        return json.isEmpty() ? null : json;
    }

    /**
     * Get millionaire bank stats for all users
     *
     * @return All user millionaire bank stats
     */
    public static String getMillionaireBankLeaderboard() {
        String json = new NetworkRequest("millionaire/leaderboard", true).get().body;
        return json.isEmpty() ? null : json;
    }

    /**
     * Get the wobbly leaderboard
     *
     * @param game COD game
     * @return Wobbly leaderboard
     */
    public static String getWobbliesLeaderboard(GAME game) {
        String json = new NetworkRequest("wobblies/" + game.name().toUpperCase(), true).get().body;
        return json.isEmpty() ? null : json;
    }

    /**
     * Add wobbly scores to the wobblies leaderboard (async)
     *
     * @param scores Scores to add
     */
    public static void addWobbliesToLeaderboard(ArrayList<WobblyScore> scores) {
        JSONArray scoreArray = new JSONArray();
        for(WobblyScore score : scores) {
            scoreArray.put(score.toJSON());
        }
        JSONObject body = new JSONObject().put("wobblies", scoreArray);
        new NetworkRequest("wobblies", true).post(body.toString(), true);
    }

    /**
     * Retrieve all saved names by a user id
     *
     * @param id ID of user to look up
     * @return Saved names of user
     */
    public static String getUserData(long id) {
        return new NetworkRequest("users/info/" + id, true).get().body;
    }

    /**
     * Get the OSRS league data for the given player name
     *
     * @param playerName Player name
     * @return OSRS league data
     */
    public static String getOSRSLeagueData(String playerName) {
        return new NetworkRequest("osrs/league/player/" + playerName, true).get().body;
    }
}
