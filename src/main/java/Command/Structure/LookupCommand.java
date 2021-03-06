package Command.Structure;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Look up a user and do something with their saved name
 */
public abstract class LookupCommand extends SavedNameCommand {
    public static final String DEFAULT_LOOKUP_ARGS = "[name/me/@someone]", SAVE_TYPE_NAME = "name";
    private final ArrayList<String> currentLookups = new ArrayList<>();

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
        super(
                trigger,
                desc,
                DEFAULT_LOOKUP_ARGS,
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
}
