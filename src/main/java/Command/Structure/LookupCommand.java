package Command.Structure;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.HashSet;
import java.util.List;

/**
 * Look up a user and do something with their saved name
 */
public abstract class LookupCommand extends SavedNameCommand {
    public static final String
            LOOKUP_ARGS = "name/me/@someone",
            DEFAULT_LOOKUP_ARGS = "[" + LOOKUP_ARGS + "]",
            SAVE_TYPE_NAME = "name";
    private final HashSet<String> currentLookups = new HashSet<>();

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
                DEFAULT_LOOKUP_ARGS,
                maxLength,
                SAVE_TYPE_NAME
        );
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
        this(
                trigger,
                desc,
                helpText,
                DEFAULT_LOOKUP_ARGS,
                maxLength
        );
    }

    /**
     * Initialise the command
     *
     * @param trigger           Trigger of command
     * @param desc              Description of command
     * @param helpText          Help text to be appended to default lookup actions
     * @param defaultLookupArgs Lookup arguments to display with the default help messages - e.g "[name/me/@someone]"
     * @param maxLength         Max length of name
     */
    public LookupCommand(String trigger, String desc, String helpText, String defaultLookupArgs, int maxLength) {
        super(
                trigger,
                desc,
                defaultLookupArgs,
                helpText,
                maxLength,
                SAVE_TYPE_NAME
        );
    }

    @Override
    public void performLookup(String name, CommandContext context) {
        Message message = context.getMessage();
        List<User> mentioned = message.getMentionedUsers();
        User author = message.getAuthor();
        MessageChannel channel = context.getMessageChannel();

        if(!mentioned.isEmpty()) {
            User target = mentioned.get(0);
            name = getSavedName(target.getIdLong());
            if(name == null) {
                channel.sendMessage(getSaveHelp(target == author, target)).queue();
                return;
            }
        }
        else if(name.equals("me")) {
            name = getSavedName(context.getUser().getIdLong());
            if(name == null) {
                channel.sendMessage(getSaveHelp(true, author)).queue();
                return;
            }
        }
        if(name.length() > getMaxLength()) {
            channel.sendMessage("Maximum username length is " + getMaxLength() + " characters cunt").queue();
            return;
        }
        if(name.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        lookupUser(name, context);
    }

    /**
     * Look up the player and build the image
     *
     * @param name    Player to look up
     * @param context Command context
     */
    private void lookupUser(String name, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        String nameKey = name.toLowerCase();

        // Name currently being looked up
        if(currentLookups.contains(nameKey)) {
            channel.sendMessage("Patience is a virtue cunt").queue();
            return;
        }

        // Mark name as active
        currentLookups.add(nameKey);

        new Thread(() -> {
            try {
                processName(name, context);
            }
            catch(Exception e) {
                System.out.println(e.getMessage() + " occurred with: " + name);
            }

            // Name has completed lookup
            currentLookups.remove(nameKey);
        }).start();
    }

    /**
     * Choose what to do with the given name
     *
     * @param name    Name
     * @param context Command context
     */
    public abstract void processName(String name, CommandContext context);
}
