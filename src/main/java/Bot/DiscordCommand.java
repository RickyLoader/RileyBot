package Bot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * DiscordCommand.java Object for holding information about a command stored in the database.
 *
 * @author Ricky Loader
 * @version 5000.0
 */
public class DiscordCommand{

    // Trigger to activate command
    private String trigger;

    private String desc;

    // Descriptive helpName for cases where the trigger isn't easily readable (REGEX)
    private String helpName;

    // List of images for RANDOM type commands
    private ArrayList<DiscordImage> links = new ArrayList<>();

    // Specific image for LINK type commands
    private DiscordImage image;

    private String type;
    private int calls = 0;

    // API endpoint
    private static String endPoint = "commands";

    // PK in DB
    private int id;

    // Method for INTERACTIVE type commands
    private String method;

    public DiscordCommand(String type, String trigger, String desc){
        this.trigger = trigger;
        this.desc = desc;

        // helpName is by default the trigger
        this.helpName = trigger;
        this.type = type;
    }

    /**
     * Marks updated commands as seen and completed in the database
     */
    public static void finishedUpdate(){
        String updatesJSON = ApiRequest.executeQuery(endPoint + "/unmark", "UPDATE", null, true);
        System.out.println(updatesJSON);
    }

    /**
     * Updates a RANDOM type command. Takes a list of images and adds them to the database.
     *
     * @param images A list of images to be appended to an existing command
     */
    private void updateCommand(ArrayList<String> images){
        String q = "\"";
        StringBuilder json = new StringBuilder("{" + q + "images" + q + ":" + "[");
        for(int i = 0; i < images.size(); i++){
            String image = images.get(i);
            json.append("{" + q + "image" + q + ":" + q + image + q + "}");

            // Comma required for all but final JSON object
            if(i != images.size() - 1){
                json.append(",");
            }
        }
        json.append("]}");
        System.out.println(ApiRequest.executeQuery(endPoint + "/random/update/" + id, "ADD", json.toString(), true));
    }

    /**
     * Pull the top 50 posts from meme subreddit and any that aren't currently part of the meme command
     * are sent to the API for adding.
     *
     * @param memeCommand The object holding the meme command
     * @return The number of images added
     */
    public static int getNewMemes(DiscordCommand memeCommand){
        String subreddit = "dankmemes";
        String url = "https://www.reddit.com/r/" + subreddit + "/hot.json?limit=50";
        String json = ApiRequest.executeQuery(url, "GET", null, false);
        ArrayList<String> updates = new ArrayList<>();
        try{
            JSONArray posts = new JSONObject(json).getJSONObject("data").getJSONArray("children");
            for(int i = 0; i < posts.length(); i++){
                JSONObject post = (JSONObject) posts.get(i);
                JSONObject postData = post.getJSONObject("data");
                String image = postData.getString("url");
                if(!postData.getBoolean("is_self") && !memeCommand.getLinks().contains(image)){
                    updates.add(image);
                    memeCommand.getImages().add(new DiscordImage(image));
                }
            }
            if(updates.size() > 0){
                System.out.println(updates.size() + " new memes found, updating!");
                for(int i = 0; i < updates.size(); i++){
                    System.out.println(i + 1 + ": " + updates.get(i));
                }
                memeCommand.updateCommand(updates);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return updates.size();
    }

    /**
     * Returns a list of the images of a RANDOM type command
     *
     * @return List of String URLS to images
     */
    public ArrayList<String> getLinks(){
        ArrayList<String> result = new ArrayList<>();
        for(DiscordImage image : links){
            result.add(image.getImage());
        }
        return result;
    }

    /**
     * Queries an API endpoint containing commands which have been updated. Locates the commands and updates their
     * information accordingly.
     *
     * @return List of the updated commands
     */
    public static ArrayList<DiscordCommand> getUpdates(){
        String updatesJSON = ApiRequest.executeQuery(endPoint + "/new", "GET", null, true);
        ArrayList<DiscordCommand> updates = new ArrayList<>();
        try{
            JSONArray ja = new JSONArray(updatesJSON);
            int length = ja.length();
            for(int i = 0; i < length; i++){
                JSONObject jObject = (JSONObject) ja.get(i);
                System.out.println("Found updated command " + (i + 1) + "/" + length + "... Trigger = " + jObject.getString("trigger"));

                // Create a command object
                DiscordCommand command = jsonToCommand(jObject);

                // Receive the new information for the command
                switch(command.getType()) {
                    case "LINK":
                        command.setLinkType(ApiRequest.executeQuery(endPoint + "/link", "GET", null, true));
                        break;
                    case "INTERACTIVE":
                        command.setInteractiveType(ApiRequest.executeQuery(endPoint + "/interactive", "GET", null, true));
                        break;
                    case "RANDOM":
                        command.setRandomType(ApiRequest.executeQuery(endPoint + "/random", "GET", null, true));
                        break;
                }
                updates.add(command);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return updates;
    }

    /**
     * Pulls all command information from the API and stores it as DiscordCommand objects in a map: trigger->command
     *
     * @return A map of DiscordCommand objects trigger->command
     */
    public static HashMap<String, DiscordCommand> getCommands(){

        // JSON containing basic command info
        String commandJSON = ApiRequest.executeQuery(endPoint, "GET", null, true);

        // JSON containing command info related to a specific type
        String interactiveJSON = ApiRequest.executeQuery(endPoint + "/interactive", "GET", null, true);
        String linkJSON = ApiRequest.executeQuery(endPoint + "/link", "GET", null, true);
        String randomJSON = ApiRequest.executeQuery(endPoint + "/random", "GET", null, true);

        HashMap<String, DiscordCommand> commands = new HashMap<>();

        try{
            JSONArray ja = new JSONArray(commandJSON);
            int length = ja.length();
            System.out.println(length + " commands found, please wait...");
            for(int i = 0; i < length; i++){
                JSONObject jObject = (JSONObject) ja.get(i);
                System.out.println("Creating command " + (i + 1) + "/" + length + "... Trigger = " + jObject.getString("trigger"));

                // Create the initial command with the basic JSON
                DiscordCommand command = jsonToCommand(jObject);

                // Find all other info related to the command TODO clean this up cunt
                command.setLinkType(linkJSON);
                command.setRandomType(randomJSON);
                command.setInteractiveType(interactiveJSON);

                // Add to the map
                commands.put(command.getTrigger(), command);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return commands;
    }

    /**
     * Takes a JSON object containing information about a command and creates a DiscordCommand object to hold
     * the information.
     *
     * @param o JSON object containing command info
     * @return A DiscordCommand object
     */
    private static DiscordCommand jsonToCommand(JSONObject o){
        String trigger = o.getString("trigger");
        String desc = o.getString("desc");
        String helpName = o.getString("helpname");
        String type = o.getString("type");
        int calls = o.getInt("calls");
        int id = o.getInt("id");

        // Allows "meme 3"
        if(type.equals("RANDOM")){
            trigger = trigger + " ?\\d?";
        }

        DiscordCommand command = new DiscordCommand(type, trigger, desc);
        command.setHelpName(helpName);
        command.setCalls(calls);
        command.setID(id);
        return command;
    }

    /**
     * Takes JSON containing information about all RANDOM type commands and attempts to locate relevant info to the
     * current command and add it.
     *
     * @param json JSON from the RANDOM type API endpoint TODO fix
     */
    private void setRandomType(String json){
        ArrayList<DiscordImage> links = new ArrayList<>();
        JSONObject test = null;
        try{
            JSONArray ja = new JSONArray(json);
            for(int i = 0; i < ja.length(); i++){
                JSONObject o = (JSONObject) ja.get(i);
                int id = o.getInt("id");

                // Found this command
                if(this.id == id){
                    String image = o.getString("possibility");
                    test = o;
                    links.add(new DiscordImage(image));
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println(test.toString());
        }
        this.links = links;
    }

    /**
     * Takes JSON containing information about all LINK type commands and attempts to locate relevant info to the
     * current command and add it.
     *
     * @param json JSON from the LINK type API endpoint TODO fix
     */
    private void setLinkType(String json){
        DiscordImage link = null;
        try{
            JSONArray ja = new JSONArray(json);
            for(int i = 0; i < ja.length(); i++){
                JSONObject o = (JSONObject) ja.get(i);
                int id = o.getInt("id");

                // Found this command
                if(this.id == id){
                    String image = o.getString("link");
                    link = new DiscordImage(image);
                }
            }
        }
        catch(Exception e){
            System.out.println(trigger);
            e.printStackTrace();
        }
        this.image = link;
    }

    /**
     * Takes JSON containing information about all INTERACTIVE type commands and attempts to locate relevant info to the
     * current command and add it.
     *
     * @param json JSON from the INTERACTIVE type API endpoint TODO fix
     */
    private void setInteractiveType(String json){
        String method = null;
        try{
            JSONArray ja = new JSONArray(json);
            for(int i = 0; i < ja.length(); i++){
                JSONObject o = (JSONObject) ja.get(i);
                int id = o.getInt("id");

                // Found this command
                if(this.id == id){
                    method = o.getString("method");
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        this.method = method;
    }

    /**
     * TODO implement
     *
     * @param c
     * @param commands
     * @return
     */
    public static boolean addCommand(DiscordCommand c, HashMap<String, DiscordCommand> commands){
        for(String trigger : commands.keySet()){
            DiscordCommand possibility = commands.get(trigger);
            if(possibility.getType().equals("RANDOM")){
                for(DiscordImage image : possibility.getImages()){
                    if(image.getImage().equals(c.getImage())){
                        System.out.println("we found him in" + possibility.getTrigger() + "!");
                    }
                }

            }
        }
        return false;
    }

    public void setID(int id){
        this.id = id;
    }

    public int getID(){
        return id;
    }

    public int getCalls(){
        return calls;
    }

    /**
     * @return A codeblock enclosed summary of the current command, displays how many times the command has been called
     */
    public String summary(){
        return "```" + "The " + helpName + " command has been called " + calls + " times!" + "```";
    }

    public String getMethod(){
        return method;
    }

    public void setCalls(int calls){
        this.calls = calls;
    }

    public void updateCalls(){
        calls++;
    }

    public String getDesc(){
        return desc;
    }

    public ArrayList<DiscordImage> getImages(){
        return links;
    }

    public DiscordImage getImage(){
        return image;
    }

    public int getLinkCount(){
        return links.size();
    }

    public String getHelpName(){
        return helpName;
    }

    public String getType(){
        return type;
    }

    public String getTrigger(){
        return trigger;
    }

    /**
     * Used in RANDOM type commands, returns a random image from the list of images.
     *
     * @return A String URL
     */
    public String getLink(){
        Random rand = new Random();
        int index = rand.nextInt(getLinkCount());
        return links.get(index).getImage();
    }

    public void setHelpName(String helpName){
        this.helpName = helpName;
    }

    public void setImage(String image){
        this.image = new DiscordImage(image);
    }
}
