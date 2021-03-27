package Command.Structure;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

/**
 * Look up a user and do something with their saved name
 */
public abstract class SavedNameCommand extends DiscordCommand {
    private final int maxLength;
    private final String saveTypeName;

    /**
     * Initialise the command
     *
     * @param trigger           Trigger of command
     * @param desc              Description of command
     * @param defaultLookupArgs String detailing the default lookup arguments e.g [name/me/@someone]
     * @param maxLength         Max length of name
     * @param saveTypeName      Name of save type - e.g "name"
     */
    public SavedNameCommand(String trigger, String desc, String defaultLookupArgs, int maxLength, String saveTypeName) {
        super(
                trigger,
                desc,
                getDefaultHelpText(trigger, defaultLookupArgs, saveTypeName)
        );
        this.maxLength = maxLength;
        this.saveTypeName = saveTypeName;
    }

    /**
     * Initialise the command
     *
     * @param trigger           Trigger of command
     * @param desc              Description of command
     * @param defaultLookupArgs String detailing the default lookup arguments e.g [name/me/@someone]
     * @param helpText          Help text to be appended to default lookup actions
     * @param maxLength         Max length of name
     * @param saveTypeName      Name of save type - e.g "name"
     */
    public SavedNameCommand(String trigger, String desc, String defaultLookupArgs, String helpText, int maxLength, String saveTypeName) {
        super(
                trigger,
                desc,
                getDefaultHelpText(trigger, defaultLookupArgs, saveTypeName) + "\n\n" + helpText
        );
        this.maxLength = maxLength;
        this.saveTypeName = saveTypeName;
    }

    /**
     * Get a String detailing the default lookup help text
     *
     * @param trigger           Trigger to use in help text
     * @param defaultLookupArgs String detailing the default lookup arguments e.g [name/me/@someone]
     * @param saveTypeName      Name of save type - e.g "name"
     * @return trigger [defaultLookupArgs] trigger save [your name]
     */
    public static String getDefaultHelpText(String trigger, String defaultLookupArgs, String saveTypeName) {
        return trigger + " " + defaultLookupArgs + "\n" + trigger + " save [your " + saveTypeName + "]";
    }

    /**
     * Get the maximum length of the name
     *
     * @return Maximum length of name
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Lookup a user or save their name for later look up queries
     *
     * @param context Context of command
     */
    @Override
    public void execute(CommandContext context) {
        Message message = context.getMessage();
        String query = stripArguments(message.getContentRaw().toLowerCase().trim().replaceAll("\\s+", " "));
        MessageChannel channel = context.getMessageChannel();
        User author = context.getUser();

        if(!query.startsWith(getTrigger() + " ")) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        List<User> mentioned = context.getMessage().getMentionedUsers();
        for(User mentionedUser : mentioned) {
            query = query.replace("<@!" + mentionedUser.getIdLong() + ">", "").trim();
        }

        String save = getTrigger() + " save ";
        if(query.startsWith(save)) {
            String name = query.replaceFirst(save, "").trim();
            if(name.length() > maxLength) {
                channel.sendMessage("Maximum username length is " + getMaxLength() + " characters cunt").queue();
                return;
            }
            saveName(name, channel, author);
            return;
        }
        performLookup(query.replace(getTrigger(), "").trim(), context);
    }

    /**
     * Perform an action on the given name/mentioned users
     *
     * @param query   Query sans trigger & mentions e.g "mwlookup me" -> "me"
     * @param context Command context
     */
    public abstract void performLookup(String query, CommandContext context);

    /**
     * Strip and save extra arguments such that the query equals [trigger] [name] or [trigger] save [name]
     *
     * @param query String which triggered command
     * @return Query to correct format
     */
    public String stripArguments(String query) {
        return query;
    }

    /**
     * Get a hint on how to save a name for a user who does not have one
     *
     * @param self User is the message author/command caller
     * @param user User who does not have a saved name
     * @return String explaining how the user can save their name
     */
    public String getSaveHelp(boolean self, User user) {
        return "I don't have a " + saveTypeName + " saved for "
                + (self ? "you, try: " : user.getAsMention() + ", they will need to try: ")
                + "```" + getTrigger() + " save [" + (self ? "your " : "their ") + saveTypeName + "]```";
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

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
