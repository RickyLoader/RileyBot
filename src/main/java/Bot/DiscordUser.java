package Bot;

import Network.NetworkRequest;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

/**
 * Helper methods for storing and retrieving information based on User id
 */
public class DiscordUser {
    public static final String MW = "mwlookup", OSRS = "osrslookup", LOL = "lollookup", YT = "ytlookup", RS3 = "rs3lookup";

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
        channel.sendMessage(user.getAsMention() + " Your **" + nameType + "** name is now **" + name + "**").queue();
    }

    /**
     * Retrieve a name from the database
     *
     * @param id       id the name is stored by
     * @param nameType Name type to retrieve
     * @return Saved name for given table
     */
    public static String getSavedName(long id, String nameType) {
        String json = new NetworkRequest("users/names/" + nameType + "/" + id, true).get();
        return json == null ? null : new JSONObject(json).getString(nameType);
    }

    /**
     * Retrieve all saved names by a user id
     *
     * @param id ID of user to look up
     * @return Saved names of user
     */
    public static String getUserData(long id) {
        return new NetworkRequest("users/info/" + id, true).get();
    }
}
