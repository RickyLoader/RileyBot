package Bot;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DiscordUser {

    private static String endPoint = "users";

    public static boolean deleteTarget(String id) {
        String deleteEndpoint = endPoint + "/delete/" + "\"" + id + "\"";
        return (ApiRequest.executeQuery(deleteEndpoint, "DELETE", null, true)).equals("User Deleted!");
    }

    public static boolean addTarget(User user) {
        String url = (endPoint + "/add");
        String query = addRequest(user.getId());
        return (ApiRequest.executeQuery(url, "ADD", query, true)).equals("User Added!");
    }

    /**
     * Creates the JSON body required for adding a user to the database.
     *
     * @param id The unique id of the user
     * @return A JSON body containing the user's information to be sent to the API
     */
    private static String addRequest(String id) {
        return "{\"discord_id\":\""+id+"\"}";
    }

    public static ArrayList<Member> getTargets(List<Guild> guilds) {
        HashSet<String> wanted = new HashSet<>();
        try {
            System.out.println("\n\nFetching targets for extermination...\n");
            String json = ApiRequest.executeQuery(endPoint, "GET", null, true);
            JSONArray ja = new JSONArray(json);
            int length = ja.length();
            if(length == 0) {
                System.out.println("\nNo targets found\n");
                return new ArrayList<>();
            }
            System.out.println("\n" + length + " targets found, attempting to locate...\n\n");
            for(int i = 0; i < length; i++) {
                JSONObject jObject = (JSONObject) ja.get(i);
                String id = jObject.getString("discord_id");
                wanted.add(id);
            }
        }
        catch(Exception e) {
            return null;
        }
        return findMembers(wanted, guilds);
    }

    private static ArrayList<Member> findMembers(HashSet<String> wanted, List<Guild> guilds) {
        ArrayList<Member> members = new ArrayList<>();
        for(Guild guild : guilds) {
            for(Member m : guild.getMembers()) {
                if(wanted.contains(m.getUser().getId())) {
                    members.add(m);
                    System.out.println(m.getEffectiveName() + " found.");
                    wanted.remove(m.getUser().getId());
                }
            }
        }
        if(wanted.size() > 0) {
            System.out.println("Removing " + wanted.size() + " targets, cannot locate\n");
            for(String id : wanted) {
                deleteTarget(id);
            }
        }
        return members;
    }
}
