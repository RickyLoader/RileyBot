package Command.Structure;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Look up a player in a game and build an image showing their stats
 */
public abstract class LookupCommand extends DiscordCommand {
    private final ArrayList<String> currentLookups = new ArrayList<>();
    private final int maxLength;

    /**
     * Initialise the command
     *
     * @param trigger   Trigger of command
     * @param desc      Description of command
     * @param maxLength Max length of name
     */
    public LookupCommand(String trigger, String desc, int maxLength) {
        super(
                trigger,
                desc,
                (getDefaultHelpText(trigger))
        );
        this.maxLength = maxLength;
    }

    /**
     * Initialise the command
     *
     * @param trigger   Trigger of command
     * @param desc      Description of command
     * @param helpText  Help text to be appended to default lookup actions
     * @param maxLength Max length of name
     */
    public LookupCommand(String trigger, String desc, String helpText, int maxLength) {
        super(
                trigger,
                desc,
                getDefaultHelpText(trigger) + "\n\n" + helpText
        );
        this.maxLength = maxLength;
    }

    /**
     * Get a String detailing the default lookup arguments
     *
     * @param trigger Trigger to prepend to String
     * @return trigger [name/me/@someone]
     */
    public static String getDefaultLookupArgs(String trigger) {
        return trigger + " [name/me/@someone]";
    }

    /**
     * Get a String detailing the default lookup help text
     *
     * @param trigger Trigger to use in help text
     * @return trigger [name/me/@someone] trigger save [your name]
     */
    public static String getDefaultHelpText(String trigger) {
        return getDefaultLookupArgs(trigger) + "\n" + trigger + " save [your name]";
    }

    /**
     * Lookup a user in a game or save their name for later look up queries
     *
     * @param context Context of command
     */
    @Override
    public void execute(CommandContext context) {
        Message message = context.getMessage();
        String query = stripArguments(message.getContentRaw().toLowerCase().trim().replaceAll("\\s+", " "));
        MessageChannel channel = context.getMessageChannel();
        User author = context.getUser();
        List<User> mentioned = message.getMentionedUsers();

        if(!query.startsWith(getTrigger() + " ")) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        String save = getTrigger() + " save ";
        if(query.startsWith(save) && !query.replace(save, "").isEmpty()) {
            saveName(query.replaceFirst(save, ""), channel, author);
            return;
        }
        String name = query.replace(getTrigger(), "").trim();
        if(!mentioned.isEmpty()) {
            User target = mentioned.get(0);
            name = getSavedName(target.getIdLong());
            if(name == null) {
                channel.sendMessage(author.getAsMention() + " I don't have a name saved for " + target.getAsMention() + " they will need to:" + getSaveHelp(false)).queue();
                return;
            }
        }
        else if(name.equals("me")) {
            name = getSavedName(context.getUser().getIdLong());
            if(name == null) {
                channel.sendMessage(author.getAsMention() + " I don't have a name saved for you, try: " + getSaveHelp(true)).queue();
                return;
            }
        }
        if(name.length() > maxLength) {
            channel.sendMessage("Maximum username length is " + maxLength + " characters cunt").queue();
            return;
        }
        if(name.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        lookupUser(name, context);
    }

    /**
     * Strip and save extra arguments such that the query equals [trigger] [name]
     *
     * @param query String which triggered command
     * @return Query to correct format
     */
    public String stripArguments(String query) {
        return query;
    }

    /**
     * Look up the player and build the image
     *
     * @param name    Player to look up
     * @param context Command context
     */
    private void lookupUser(String name, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        if(currentLookups.contains(name.toLowerCase())) {
            channel.sendMessage("Patience is a virtue cunt").queue();
            return;
        }
        currentLookups.add(name.toLowerCase());
        new Thread(() -> {
            processName(name, context);
            currentLookups.remove(name.toLowerCase());
        }).start();
    }

    /**
     * Choose what to do with the given name
     *
     * @param name    Name
     * @param context Command context
     */
    public abstract void processName(String name, CommandContext context);

    /**
     * Get a hint on how to save the name
     *
     * @return How to save user name
     */
    public String getSaveHelp(boolean self) {
        String who = self ? "your " : "their ";
        return "```" + getTrigger() + " save [" + who + "name]```";
    }

    /**
     * Get the user's saved name for the given lookup
     *
     * @param id ID of user
     * @return User's saved name
     */
    public abstract String getSavedName(long id);

    /**
     * Save the user's name for the given lookup
     *
     * @param name    Name to save
     * @param channel Channel to send save status to
     * @param user    User to save name for
     */
    public abstract void saveName(String name, MessageChannel channel, User user);

    /**
     * Enter the command if the message begins with the trigger
     *
     * @param query Message query
     * @return Message starts with query
     */
    @Override
    public boolean matches(String query) {
        return query.startsWith(getTrigger());
    }
}
