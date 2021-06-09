package Command.Structure;

import Bot.ResourceHandler;
import net.dv8tion.jda.api.entities.Message;
import org.json.JSONObject;

import java.util.regex.Pattern;

public abstract class DiscordCommand {
    private String trigger;
    private final String desc;
    private final String helpName;
    private boolean botInput, secret;

    public DiscordCommand(String trigger, String desc, String helpName) {
        System.out.println("Loading " + trigger + "...");
        this.trigger = trigger;
        this.helpName = helpName == null ? trigger : helpName;
        this.desc = desc;
        this.botInput = false;
        this.secret = false;
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

    /**
     * Check whether the command matches the given message
     *
     * @param query   Lower case message content
     * @param message Message object
     * @return Query matches command
     */
    public boolean matches(String query, Message message) {
        return Pattern.compile(this.getTrigger().toLowerCase()).matcher(query).matches();
    }

    public String getHelpNameCoded() {
        return "```" + getHelpName() + "```";
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
     * Attempts to convert the given String to an integer
     *
     * @param arg String to convert
     * @return 0 (if not int) or int value of String
     */
    public static int toInteger(String arg) {
        try {
            return Integer.parseInt(arg);
        }
        catch(Exception e) {
            return 0;
        }
    }

    /**
     * Attempts to convert the given String to a long
     *
     * @param arg String to convert
     * @return 0 (if not long) or long value of String
     */
    public static long toLong(String arg) {
        try {
            return Long.parseLong(arg);
        }
        catch(Exception e) {
            return 0;
        }
    }

    /**
     * Set whether the command accepts bot input
     *
     * @param botInput Command accepts bot input
     */
    public void setBotInput(boolean botInput) {
        this.botInput = botInput;
    }

    /**
     * Set whether the command is secret - Prevents it from displaying in help command
     *
     * @param secret Command is secret
     */
    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    /**
     * Check whether the command is secret
     *
     * @return Command is secret
     */
    public boolean isSecret() {
        return secret;
    }

    /**
     * Check whether the command accepts bot input
     *
     * @return Command accepts bot input
     */
    public boolean acceptsBotInput() {
        return botInput;
    }
}
