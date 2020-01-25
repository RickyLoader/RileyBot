package Bot;

import net.dv8tion.jda.core.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * DiscordUser.java Object for holding information about a user, stored in a database and used to locate the user
 * when the bot is activated.
 *
 * @author Ricky Loader
 * @version 5000.0
 */
public class DiscordUser{

    // Name the user goes by, by default their current nickname but can be changed by the user
    private String alias;

    // getAsMention() value of a User object
    private String id;

    // API endpoint
    private static String endPoint = "users";

    // Marked status of the user
    private boolean target;

    public DiscordUser(String alias, String id, boolean target){
        this.alias = alias;
        this.id = id;
        this.target = target;
    }

    /**
     * Returns whether the user is marked for extermination.
     *
     * @return boolean target
     */
    public boolean isTarget(){
        return target;
    }

    public String getAlias(){
        return alias;
    }

    public String getID(){
        return id;
    }

    /**
     * Updates the user's alias, a user's alias is used to @ them without using @. User may be "bob" in discord
     * but want the bot to respond to "dave".
     *
     * @param alias The desired new alias
     * @return The result of updating the alias
     */
    public boolean setAlias(String alias){
        boolean result = false;
        String query = updateRequest(alias);
        String endpoint = endPoint + "/update";
        String json = ApiRequest.executeQuery(endpoint, "UPDATE", query, true);

        // Only considered updated if the database update was successful
        if(json.contains("Updated!")){
            result = true;
            this.alias = alias;
        }
        return result;
    }

    /**
     * Deletes a user from the database.
     * TODO fix
     *
     * @return The result of deleting the user.
     */
    public boolean remove(){
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

    /**
     * Process to occur when a user has joined any of the bot's guilds. Add them to the database and create an object
     * containing their information if it does not already exist.
     *
     * @param user The joined user
     * @return The new user object to be stored with the others, or null if they exist
     */
    public static DiscordUser userJoined(User user){
        DiscordUser newUser = null;
        String url = (endPoint + "/add");
        String query = addRequest(user.getAsMention(), user.getName());
        String json = ApiRequest.executeQuery(url, "ADD", query, true);
        if(!json.contains("Failed")){
            newUser = new DiscordUser(user.getName(), user.getAsMention(), true);
        }
        return newUser;
    }

    //TODO something
    private static String banRequest(String id, String name, String image){
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

    /**
     * Creates the JSON body required for adding a user to the database.
     *
     * @param id   The unique id of the user
     * @param name The current name of the user (can be updated later)
     * @return A JSON body containing the user's information to be sent to the API
     */
    private static String addRequest(String id, String name){
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

    /**
     * Creates the JSON body required for updating a user's alias in the database.
     *
     * @param desiredAlias The desired new alias
     * @return A JSON body containing the user's information to be sent to the API
     */
    private String updateRequest(String desiredAlias){
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

    /**
     * Read in Users from the API and create objects for each.
     *
     * @return A HashMap of users, mapped alias->object
     */
    public static HashMap<String, DiscordUser> getUsers(){
        HashMap<String, DiscordUser> users = new HashMap<>();

        try{
            String json = ApiRequest.executeQuery(endPoint, "GET", null, true);
            JSONArray ja = new JSONArray(json);
            int length = ja.length();
            System.out.println("\n" + length + " users found, please wait...\n\n");
            for(int i = 0; i < length; i++){
                JSONObject jObject = (JSONObject) ja.get(i);
                String discordID = jObject.getString("discord_id");
                String alias = jObject.getString("name");
                int target = jObject.getInt("target");
                System.out.println("Creating user " + (i + 1) + "/" + length + "... Name = " + alias);

                // SQL uses bit data type for boolean. 0 considered false, 1 considered true for this scenario
                users.put(
                        alias,
                        new DiscordUser(alias, discordID, target == 1)
                );
            }
        }
        catch(Exception e){
            return null;
        }
        return users;
    }

    /**
     * Mark an existing user for extermination. Update the database.
     *
     * @return Boolean success of marking the user.
     */
    public boolean markUser(){
        String query = "{}";
        boolean result = false;
        String endpoint = endPoint + "/target/" + id;
        String json = ApiRequest.executeQuery(endpoint, "UPDATE", query, true);
        if(json.contains("Targeted!")){
            System.out.println(alias + " has been marked in the database\n\n");
            result = true;
            this.target = true;
        }
        return result;
    }

    /**
     * Pardon the user from extermination. Update the database.
     *
     * @return Boolean success of pardoning the user.
     */
    public boolean pardonUser(){
        boolean result = false;
        String endpoint = endPoint + "/pardon/" + id;
        String query = "{}";
        String json = ApiRequest.executeQuery(endpoint, "UPDATE", query, true);
        if(json.contains("Pardoned!")){
            result = true;
            this.target = false;
        }
        return result;
    }
}
