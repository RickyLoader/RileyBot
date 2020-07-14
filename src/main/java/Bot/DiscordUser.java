package Bot;

import Network.ApiRequest;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

public class DiscordUser {


    public static String getMWName(long id) {
        return getName(id, "mw");
    }

    public static String getOSRSName(long id) {
        return getName(id, "osrs");
    }

    public static void saveMWName(String name, MessageChannel channel, User user) {
        if(name.length() > 17) {
            channel.sendMessage("Maximum username length is 18 characters cunt").queue();
            return;
        }
        savePlayer(name, user.getIdLong(), "MW");
        channel.sendMessage(user.getAsMention() + " Your mwlookup name is now " + name).queue();
    }

    public static void saveOSRSName(String name, MessageChannel channel, User user) {
        if(name.length() > 12) {
            channel.sendMessage("Maximum username length is 12 characters cunt").queue();
            return;
        }
        savePlayer(name, user.getIdLong(), "OSRS");
        channel.sendMessage(user.getAsMention() + " Your osrslookup name is now " + name).queue();
    }

    private static void savePlayer(String name, long id, String table) {
        JSONObject body = new JSONObject().put("discord_id", id).put("table", table).put("name", name);
        ApiRequest.executeQuery("users/submit", "ADD", body.toString(), true);
    }

    private static String getName(long id, String endpoint) {
        String json = ApiRequest.executeQuery("users/" + endpoint + "/" + id, "GET", null, true);
        if(json == null || json.isEmpty()) {
            return null;
        }
        return new JSONObject(json).getString("name");
    }

    public static String getUserData(long id) {
        return ApiRequest.executeQuery("users/all/" + id, "GET", null, true);
    }
}
