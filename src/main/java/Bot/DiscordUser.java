package Bot;

import net.dv8tion.jda.core.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;


public class DiscordUser {
    private String alias;
    private String id;
    private static String endPoint = "users";
    private boolean target;

    public DiscordUser(String alias, String id, boolean target) {
        this.alias = alias;
        this.id = id;
        this.target = target;
    }

    public boolean isTarget() {
        return target;
    }

    public String getAlias() {
        return alias;
    }

    public String getID() {
        return id;
    }

    public boolean setAlias(String alias) {
        boolean result = false;
        String query = updateRequest(alias);
        String endpoint = endPoint + "/update";
        String json = ApiRequest.executeQuery(endpoint, "UPDATE", query, true);
        if (json.contains("Updated!")) {
            result = true;
            this.alias = alias;
        }
        return result;
    }

    public boolean remove() {
        boolean result = false;
        String deleteEndpoint = endPoint + "/delete/" + "\"" + id + "\"";
        String json = ApiRequest.executeQuery(deleteEndpoint, "DELETE", null, true);
        System.out.println(json);
        /*String banEndPoint = endPoint + "/add/banned";
        String image = user.getAvatarUrl();
        if(image == null){
            image = user.getDefaultAvatarUrl();
        }
        banRequest(user.getAsMention(), user.getName(), image);
        ApiRequest.executeQuery(banEndPoint, "ADD", body);*/
        return result;
    }

    public static DiscordUser userJoined(User user) {
        DiscordUser newUser = null;
        String url = (endPoint + "/add");
        String query = addRequest(user.getAsMention(), user.getName());
        String json = ApiRequest.executeQuery(url, "ADD", query, true);
        if (!json.contains("Failed")) {
            newUser = new DiscordUser(user.getName(), user.getAsMention(), true);
        }
        return newUser;
    }

    private static String banRequest(String id, String name, String image) {
        String quote = "\"";
        StringBuilder result = new StringBuilder("{");
        result.append(quote + "user_id" + quote);
        result.append(":");
        result.append(quote + id + quote);
        result.append(",");
        result.append(quote + "name" + quote);
        result.append(":");
        result.append(quote + name + quote);
        result.append(",");
        result.append(quote + "image" + quote);
        result.append(":");
        result.append(quote + image + quote);
        result.append("}");
        return result.toString();
    }

    private static String addRequest(String id, String name) {
        String quote = "\"";
        StringBuilder result = new StringBuilder("{");
        result.append(quote + "discord_id" + quote);
        result.append(":");
        result.append(quote + id + quote);
        result.append(",");
        result.append(quote + "name" + quote);
        result.append(":");
        result.append(quote + name + quote);
        result.append("}");
        return result.toString();
    }

    public static String getEndPoint() {
        return endPoint;
    }

    public void setID(String id) {
        this.id = id;
    }

    private String updateRequest(String desiredAlias) {
        String quote = "\"";
        StringBuilder result = new StringBuilder("{");
        result.append(quote + "discord_id" + quote);
        result.append(":");
        result.append(quote + id + quote);
        result.append(",");
        result.append(quote + "name" + quote);
        result.append(":");
        result.append(quote + desiredAlias + quote);
        result.append("}");
        return result.toString();
    }

    public static HashMap<String, DiscordUser> getUsers() {
        String json = ApiRequest.executeQuery(endPoint, "GET", null, true);
        HashMap<String, DiscordUser> users = new HashMap<>();

        try {
            JSONArray ja = new JSONArray(json);
            int length = ja.length();
            System.out.println("\n" + length + " users found, please wait...");
            for (int i = 0; i < length; i++) {
                System.out.println("Creating user " + (i + 1) + "/" + length + "...");
                JSONObject jObject = (JSONObject) ja.get(i);
                String discordID = jObject.getString("discord_id");
                String alias = jObject.getString("name");
                int target = jObject.getInt("target");
                users.put(
                        alias,
                        new DiscordUser(alias, discordID, target == 1)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean markUser() {
        String query = "{}";
        boolean result = false;
        String endpoint = endPoint + "/target/" + id;
        String json = ApiRequest.executeQuery(endpoint, "UPDATE", query, true);
        if (json.contains("Targeted!")) {
            result = true;
            this.target = true;
        }
        return result;
    }

    public boolean pardonUser() {
        boolean result = false;
        String endpoint = endPoint + "/pardon/" + id;
        String query = "{}";
        String json = ApiRequest.executeQuery(endpoint, "UPDATE", query, true);
        System.out.println(json);
        if (json.contains("Pardoned!")) {
            result = true;
            this.target = false;
        }
        return result;
    }
}
