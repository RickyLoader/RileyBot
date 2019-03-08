package Bot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class DiscordCommand {

    private String trigger;
    private String desc;
    private String helpName;
    private ArrayList<DiscordImage> links = new ArrayList<>();
    private DiscordImage image;
    private String type;
    private int calls = 0;
    private static String endPoint = "commands";
    private int id;
    private String method;

    public DiscordCommand(String type, String trigger, String desc) {
        this.trigger = trigger;
        this.desc = desc;
        this.helpName = trigger;
        this.type = type;
    }

    public static void finishedUpdate() {
        String updatesJSON = ApiRequest.executeQuery(endPoint + "/unmark", "UPDATE", null, true);
        System.out.println(updatesJSON);
    }

    private void updateCommand(ArrayList<String> images) {
        String q = "\"";
        StringBuilder json = new StringBuilder("{" + q + "images" + q + ":" + "[");
        for (int i = 0; i < images.size(); i++) {
            String image = images.get(i);
            json.append("{" + q + "image" + q + ":" + q + image + q + "}");
            if (i != images.size() - 1) {
                json.append(",");
            }
        }
        json.append("]}");
        System.out.println(ApiRequest.executeQuery(endPoint + "/random/update/" + id, "ADD", json.toString(), true));
    }

    public static int getNewMemes(DiscordCommand memeCommand) {
        String subreddit = "dankmemes";
        String url = "https://www.reddit.com/r/" + subreddit + "/hot.json?limit=50";
        String json = ApiRequest.executeQuery(url, "GET", null, false);
        ArrayList<String> updates = new ArrayList<>();
        try {
            JSONArray posts = new JSONObject(json).getJSONObject("data").getJSONArray("children");
            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = (JSONObject) posts.get(i);
                JSONObject postData = post.getJSONObject("data");
                String image = postData.getString("url");
                if (!postData.getBoolean("is_self") && !memeCommand.getLinks().contains(image)) {
                    updates.add(image);
                    memeCommand.getImages().add(new DiscordImage(image, "null"));
                }
            }
            if (updates.size() > 0) {
                System.out.println(updates.size() + " new memes found, updating!");
                for (int i = 0; i < updates.size(); i++) {
                    System.out.println(i + 1 + ": " + updates.get(i));
                }
                memeCommand.updateCommand(updates);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updates.size();
    }

    public ArrayList<String> getLinks() {
        ArrayList<String> result = new ArrayList<>();
        for (DiscordImage image : links) {
            result.add(image.getImage());
        }
        return result;
    }

    public static ArrayList<DiscordCommand> getUpdates() {
        String updatesJSON = ApiRequest.executeQuery(endPoint + "/new", "GET", null, true);
        ArrayList<DiscordCommand> updates = new ArrayList<>();
        try {
            JSONArray ja = new JSONArray(updatesJSON);
            int length = ja.length();
            for (int i = 0; i < length; i++) {
                JSONObject jObject = (JSONObject) ja.get(i);
                System.out.println("Found updated command " + (i + 1) + "/" + length + "... Trigger = " + jObject.getString("trigger"));
                DiscordCommand command = jsonToCommand(jObject);
                switch (command.getType()) {
                    case "LINK":
                        command.setLinkType(ApiRequest.executeQuery(endPoint + "/link", "GET", null, true));
                        break;
                    case "INTERACTIVE":
                        command.setInteractiveType(ApiRequest.executeQuery(endPoint + "/interactive", "GET", null, true));
                        break;
                    case "RANDOM":
                        command.setRandomType(ApiRequest.executeQuery(endPoint + "/data", "GET", null, true));
                        break;
                }
                updates.add(command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updates;
    }

    public static HashMap<String, DiscordCommand> getCommands() {

        String commandJSON = ApiRequest.executeQuery(endPoint, "GET", null, true);
        String interactiveJSON = ApiRequest.executeQuery(endPoint + "/interactive", "GET", null, true);
        String linkJSON = ApiRequest.executeQuery(endPoint + "/link", "GET", null, true);
        String randomJSON = ApiRequest.executeQuery(endPoint + "/data", "GET", null, true);

        HashMap<String, DiscordCommand> commands = new HashMap<>();

        try {
            JSONArray ja = new JSONArray(commandJSON);
            int length = ja.length();
            System.out.println(length + " commands found, please wait...");
            for (int i = 0; i < length; i++) {
                JSONObject jObject = (JSONObject) ja.get(i);
                System.out.println("Creating command " + (i + 1) + "/" + length + "... Trigger = " + jObject.getString("trigger"));
                DiscordCommand command = jsonToCommand(jObject);
                command.setLinkType(linkJSON);
                command.setRandomType(randomJSON);
                command.setInteractiveType(interactiveJSON);
                commands.put(command.getTrigger(), command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return commands;
    }

    private static DiscordCommand jsonToCommand(JSONObject o) {
        String trigger = o.getString("trigger");
        String desc = o.getString("desc");
        String helpName = o.getString("helpname");
        String type = o.getString("type");
        int calls = o.getInt("calls");
        int id = o.getInt("id");
        if (type.equals("RANDOM")) {
            trigger = trigger + " ?\\d?";
        }

        DiscordCommand command = new DiscordCommand(type, trigger, desc);
        command.setHelpName(helpName);
        command.setCalls(calls);
        command.setID(id);
        return command;
    }

    private void setRandomType(String json) {
        ArrayList<DiscordImage> links = new ArrayList<>();
        JSONObject test = null;
        try {
            JSONArray ja = new JSONArray(json);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject o = (JSONObject) ja.get(i);
                int id = o.getInt("id");
                if (this.id == id) {
                    String image = o.getString("possibility");
                    String data = o.getString("info").toLowerCase();
                    test = o;
                    links.add(new DiscordImage(image, data));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(test.toString());
        }
        this.links = links;
    }

    private void setLinkType(String json) {
        DiscordImage link = null;
        try {
            JSONArray ja = new JSONArray(json);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject o = (JSONObject) ja.get(i);
                int id = o.getInt("id");
                if (this.id == id) {
                    String image = o.getString("link");
                    String data = o.getString("info").toLowerCase();
                    link = new DiscordImage(image, data);
                }
            }
        } catch (Exception e) {
            System.out.println(trigger);
            e.printStackTrace();
        }
        this.image = link;
    }

    private void setInteractiveType(String json) {
        String method = null;
        try {
            JSONArray ja = new JSONArray(json);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject o = (JSONObject) ja.get(i);
                int id = o.getInt("id");
                if (this.id == id) {
                    method = o.getString("method");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.method = method;
    }

    public void updateData(String data) {
        String json = ApiRequest.executeQuery(endPoint + "/add/data", "ADD", getAddJSON(data), true);
        if (json != null) {
            String update = ApiRequest.executeQuery(endPoint + "/data", "GET", null, true);
            setRandomType(update);
        }
    }

    private String getAddJSON(String data) {
        String quote = "\"";
        StringBuilder result = new StringBuilder("{");
        result.append(quote + "command_id" + quote);
        result.append(":");
        result.append(id);
        result.append(",");
        result.append(quote + "data" + quote);
        result.append(":");
        result.append(quote + data + quote);
        result.append("}");
        return result.toString();
    }

    public static boolean addCommand(DiscordCommand c, HashMap<String, DiscordCommand> commands) {
        for (String trigger : commands.keySet()) {
            DiscordCommand possibility = commands.get(trigger);
            if (possibility.getType().equals("RANDOM")) {
                for (DiscordImage image : possibility.getImages()) {
                    if (image.getImage().equals(c.getImage())) {
                        System.out.println("we found him in" + possibility.getTrigger() + "!");
                    }
                }

            }
        }
        return false;
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public int getCalls() {
        return calls;
    }

    public String summary() {
        return "```" + "The " + helpName + " command has been called " + calls + " times!" + "```";
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public void setCalls(int calls) {
        this.calls = calls;
    }

    public void updateCalls() {
        calls++;
    }

    public String getDesc() {
        return desc;
    }

    public ArrayList<DiscordImage> getImages() {
        return links;
    }

    public DiscordImage getImage() {
        return image;
    }

    public int getLinkCount() {
        return links.size();
    }

    public String getHelpName() {
        return helpName;
    }

    public String getType() {
        return type;
    }

    public String getTrigger() {
        return trigger;
    }


    public String getLink() {
        Random rand = new Random();
        int index = rand.nextInt(getLinkCount());
        return links.get(index).getImage();
    }


    public void setHelpName(String helpName) {
        this.helpName = helpName;
    }


    public void setImage(String image) {
        this.image = new DiscordImage(image, "");
    }
}
