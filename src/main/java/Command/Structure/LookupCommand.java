package Command.Structure;

import net.dv8tion.jda.api.entities.Guild;
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
        super(trigger, desc, (trigger + " [name/me/@someone]\n" + trigger + " save [your name]"));
        this.maxLength = maxLength;
    }

    /**
     * Initialise the command
     *
     * @param trigger   Trigger of command
     * @param desc      Description of command
     * @param prefix    Prefix argument
     * @param maxLength Max length of name
     */
    public LookupCommand(String trigger, String desc, String prefix, int maxLength) {
        super(trigger, desc, (trigger + " [name/me/@someone]\n[" + prefix + "] " + trigger + " [name/me/@someone]\n" + trigger + " save [your name]"));
        this.maxLength = maxLength;
    }

    /**
     * Lookup a user in a game or save their name for later look up queries
     *
     * @param context Context of command
     */
    @Override
    public void execute(CommandContext context) {
        String query = stripArguments(context.getLowerCaseMessage().trim());
        MessageChannel channel = context.getMessageChannel();
        User author = context.getUser();
        List<User> mentioned = context.getMessage().getMentionedUsers();

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
        lookupUser(name, channel, context.getHomeGuild());
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
     * @param channel Channel to send result to
     */
    private void lookupUser(String name, MessageChannel channel, Guild guild) {
        if(currentLookups.contains(name.toLowerCase())) {
            channel.sendMessage("Patience is a virtue cunt").queue();
            return;
        }
        currentLookups.add(name.toLowerCase());
        new Thread(() -> {
            processName(name, channel, guild);
            currentLookups.remove(name.toLowerCase());
        }).start();
    }

    /**
     * Choose what to do with the given name
     */
    public abstract void processName(String name, MessageChannel channel, Guild guild);

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
