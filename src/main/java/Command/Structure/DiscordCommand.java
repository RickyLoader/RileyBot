package Command.Structure;

import Bot.ResourceHandler;
import org.json.JSONObject;

import java.util.regex.Pattern;

public abstract class DiscordCommand {
    private String trigger;
    private final String desc;
    private final String helpName;

    public DiscordCommand(String trigger, String desc, String helpName) {
        System.out.println("Loading " + trigger + "...");
        this.trigger = trigger;
        this.helpName = helpName == null ? trigger : helpName;
        this.desc = desc;
    }

    public DiscordCommand(String trigger, String desc) {
        this(trigger, desc, null);
    }

    public abstract void execute(CommandContext context);

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getDesc() {
        return desc;
    }

    public String getHelpName() {
        return helpName;
    }

    public boolean matches(String query) {
        return Pattern.compile(this.getTrigger().toLowerCase()).matcher(query).matches();
    }

    public String getHelpNameCoded() {
        return "```" + helpName + "```";
    }

    /**
     * Read a file in as a JSON object
     *
     * @param filename Filename
     * @return JSON object of file
     */
    public JSONObject readJSONFile(String filename) {
        String text = new ResourceHandler().getResourceFileAsString(filename);
        if(text == null) {
            return null;
        }
        return new JSONObject(text);
    }

    /**
     * Attempts to pull an integer from the given message when a Random type command is called.
     *
     * @return The integer or 0 if not found. Determines how many times to repeat the command
     */
    public static int getQuantity(String arg) {
        try {
            return Integer.parseInt(arg);
        }
        catch(Exception e) {
            return 0;
        }
    }
}
