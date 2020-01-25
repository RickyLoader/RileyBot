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
public class DiscordCommand implements Comparable<DiscordCommand>{

    // Trigger to activate command
    private String trigger;

    private String desc;

    // Descriptive helpName for cases where the trigger isn't easily readable (REGEX)
    private String helpName;

    // List of images for RANDOM type commands
    private ArrayList<String> links = new ArrayList<>();

    // Specific image for LINK type commands
    private String image;

    private String type;

    private int calls;

    // API endpoint
    private static String endPoint = "commands";

    private int id;

    // Method for INTERACTIVE type commands
    private String method;

    public DiscordCommand(String type, String trigger, String desc, String helpName, int calls, int id){
        this.trigger = trigger;
        this.desc = desc;
        this.calls = calls;
        this.id = id;
        this.helpName = helpName;
        this.type = type;
    }

    /**
     * Pulls all command information from the API and stores it as DiscordCommand objects in a map: trigger->command
     *
     * @return A map of DiscordCommand objects trigger->command
     */
    public static HashMap<String, DiscordCommand> getCommands(){
        HashMap<String, DiscordCommand> commands = new HashMap<>();

        try{
            String commandJSON = ApiRequest.executeQuery(endPoint, "GET", null, true);
            String randomJSON = ApiRequest.executeQuery(endPoint + "/" + "random", "GET", null, true);
            String linkJSON = ApiRequest.executeQuery(endPoint + "/" + "link", "GET", null, true);
            String interactiveJSON = ApiRequest.executeQuery(endPoint + "/" + "interactive", "GET", null, true);

            JSONArray ja = new JSONArray(commandJSON);
            int length = ja.length();
            System.out.println(length + " commands found, please wait...\n\n");
            for(int i = 0; i < length; i++){
                JSONObject jObject = (JSONObject) ja.get(i);
                System.out.println("Creating command " + (i + 1) + "/" + length + "... Trigger = " + jObject.getString("helpname"));

                DiscordCommand command = jsonToCommand(jObject);
                command.setType(randomJSON, linkJSON, interactiveJSON);
                commands.put(command.getTrigger(), command);
            }
        }
        catch(Exception e){
            return null;
        }
        return commands;
    }

    private void setType(String random, String link, String interactive){
        switch(type) {
            case "RANDOM":
                setRandomType(random);
                break;
            case "LINK":
                setLinkType(link);
                break;
            case "INTERACTIVE":
                setInteractiveType(interactive);
                setRandomType(random);
                break;
        }
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

        DiscordCommand command = new DiscordCommand(type, trigger, desc, helpName, calls, id);
        return command;
    }

    /**
     * Takes JSON containing information about all RANDOM type commands and attempts to locate relevant info to the
     * current command and add it.
     *
     * @param json JSON from the RANDOM type API endpoint TODO fix
     */
    private void setRandomType(String json){
        ArrayList<String> links = new ArrayList<>();
        try{
            JSONObject jo = new JSONObject(json);
            if(!jo.has(String.valueOf(id))){
                return;
            }
            JSONArray ja = jo.getJSONArray(String.valueOf(id));
            for(int i = 0; i < ja.length(); i++){
                JSONObject o = (JSONObject) ja.get(i);
                links.add(o.getString("possibility"));
            }
        }
        catch(Exception e){
            e.printStackTrace();
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
        String link = null;
        try{
            JSONObject o = new JSONObject(json).getJSONObject(String.valueOf(id));
            link = o.getString("link");
        }
        catch(Exception e){
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
            JSONObject o = new JSONObject(json).getJSONObject(String.valueOf(id));
            method = o.getString("method");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        this.method = method;
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

    public void updateCalls(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                ApiRequest.executeQuery(endPoint + "/update/calls/" + id, "UPDATE", null, true);
                calls++;
            }
        }).start();
    }

    public String getDesc(){
        return desc;
    }

    public ArrayList<String> getImages(){
        return links;
    }

    public String getImage(){
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
        return links.get(index);
    }

    public void setHelpName(String helpName){
        this.helpName = helpName;
    }

    public void setImage(String image){
        this.image = image;
    }

    @Override
    public int compareTo(DiscordCommand o){
        return getTrigger().compareTo(o.getTrigger());
    }
}
