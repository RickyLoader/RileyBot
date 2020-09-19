package Bot;

import Network.NetworkRequest;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

/**
 * Helper methods for storing and retrieving information based on User id
 */
public class DiscordUser {
    private static final String MW = "MW", CW = "CW", OSRS = "OSRS", LOL = "LOL", YT = "YT", RS3 = "RS3";

    /**
     * Retrieve name used in mwlookup command
     *
     * @param id Discord user object id
     * @return mwlookup name
     */
    public static String getMWName(long id) {
        return getName(id, MW);
    }

    /**
     * Retrieve name used in cwlookup command
     *
     * @param id Discord user object id
     * @return cwlookup name
     */
    public static String getCWName(long id) {
        return getName(id, CW);
    }

    /**
     * Retrieve name used in lollookup command
     *
     * @param id Discord user object id
     * @return lollookup name
     */
    public static String getLOLName(long id) {
        return getName(id, LOL);
    }

    /**
     * Retrieve name used in osrslookup command
     *
     * @param id Discord user object id
     * @return osrslookup name
     */
    public static String getOSRSName(long id) {
        return getName(id, OSRS);
    }

    /**
     * Retrieve name used in rs3lookup command
     *
     * @param id Discord user object id
     * @return rs3lookup name
     */
    public static String getRS3Name(long id) {
        return getName(id, RS3);
    }

    /**
     * Retrieve name used in ytlookup command
     *
     * @param id Discord user object id
     * @return ytlookup name
     */
    public static String getYTName(long id) {
        return getName(id, YT);
    }

    /**
     * Store a given name by the user's discord id for the mwlookup command
     *
     * @param name    Name to be saved
     * @param channel Channel to report status to
     * @param user    User to store by id
     */
    public static void saveMWName(String name, MessageChannel channel, User user) {
        savePlayer(name, user.getIdLong(), MW);
        channel.sendMessage(user.getAsMention() + " Your mwlookup name is now " + name).queue();
    }

    /**
     * Store a given name by the user's discord id for the rs3lookup command
     *
     * @param name    Name to be saved
     * @param channel Channel to report status to
     * @param user    User to store by id
     */
    public static void saveRS3Name(String name, MessageChannel channel, User user) {
        savePlayer(name, user.getIdLong(), RS3);
        channel.sendMessage(user.getAsMention() + " Your rs3lookup name is now " + name).queue();
    }

    /**
     * Store a given name by the user's discord id for the cwlookup command
     *
     * @param name    Name to be saved
     * @param channel Channel to report status to
     * @param user    User to store by id
     */
    public static void saveCWName(String name, MessageChannel channel, User user) {
        savePlayer(name, user.getIdLong(), CW);
        channel.sendMessage(user.getAsMention() + " Your cwlookup name is now " + name).queue();
    }

    /**
     * Store a given name by the user's discord id for the osrslookup command
     *
     * @param name    Name to be saved
     * @param channel Channel to report status to
     * @param user    User to store by id
     */
    public static void saveOSRSName(String name, MessageChannel channel, User user) {
        savePlayer(name, user.getIdLong(), OSRS);
        channel.sendMessage(user.getAsMention() + " Your osrslookup name is now " + name).queue();
    }

    /**
     * Store a given name by the user's discord id for the lollookup command
     *
     * @param name    Name to be saved
     * @param channel Channel to report status to
     * @param user    User to store by id
     */
    public static void saveLOLName(String name, MessageChannel channel, User user) {
        savePlayer(name, user.getIdLong(), LOL);
        channel.sendMessage(user.getAsMention() + " Your lollookup name is now " + name).queue();
    }

    /**
     * Store a given name by the user's discord id for the ytlookup command
     *
     * @param name    Name to be saved
     * @param channel Channel to report status to
     * @param user    User to store by id
     */
    public static void saveYTName(String name, MessageChannel channel, User user) {
        savePlayer(name, user.getIdLong(), YT);
        channel.sendMessage(user.getAsMention() + " Your ytlookup name is now " + name).queue();
    }

    /**
     * Store a string in the database by a user's id
     *
     * @param name  Name to be saved
     * @param id    ID to store by
     * @param table Table to save in [YT, MW, OSRS, CW, RS3]
     */
    private static void savePlayer(String name, long id, String table) {
        JSONObject body = new JSONObject().put("discord_id", id).put("table", table).put("name", name);
        new NetworkRequest("users/submit", true).post(body.toString());
    }

    /**
     * Retrieve a name from the database
     *
     * @param id    id the name is stored by
     * @param table Table to retrieve from [YT, MW, OSRS, LOL]
     * @return Saved name for given table
     */
    private static String getName(long id, String table) {
        String json = new NetworkRequest("users/" + table + "/" + id, true).get();
        if(json == null || json.isEmpty()) {
            return null;
        }
        return new JSONObject(json).getString("name");
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
