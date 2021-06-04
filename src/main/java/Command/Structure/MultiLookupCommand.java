package Command.Structure;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;

/**
 * Look up multiple users and do something with their saved names
 */
public abstract class MultiLookupCommand extends SavedNameCommand {
    private final static String DEFAULT_LOOKUP_ARGS = "[me] [@user1, @user2...]";

    /**
     * Initialise the command
     *
     * @param trigger      Trigger of command
     * @param desc         Description of command
     * @param helpText     Help text to be appended to default lookup actions
     * @param maxLength    Max length of name
     * @param saveTypeName Name of save type - e.g "name"
     */
    public MultiLookupCommand(String trigger, String desc, String helpText, int maxLength, String saveTypeName) {
        super(trigger, desc, DEFAULT_LOOKUP_ARGS, helpText, maxLength, saveTypeName);
    }

    /**
     * Get a list of all mentioned users, including the author if "me" is specified in the query.
     * If all users have a saved name, pass along to be processed.
     *
     * @param query   Query sans trigger e.g "mwlookup me" -> "me"
     * @param context Command context
     */
    @Override
    public void performLookup(String query, CommandContext context) {
        Message message = context.getMessage();
        ArrayList<User> mentioned = new ArrayList<>(message.getMentionedUsers());
        ArrayList<UserSavedData> savedNames = new ArrayList<>();
        MessageChannel channel = context.getMessageChannel();
        User author = context.getUser();

        if(query.isEmpty() && mentioned.isEmpty() || !query.isEmpty() && !query.equals("me")) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        if(query.equals("me") && !mentioned.contains(author)) {
            mentioned.add(author);
        }
        if(mentioned.size() > 5) {
            channel.sendMessage(author.getAsMention() + " Max of 5 people, settle down").queue();
            return;
        }

        channel.sendTyping().queue();
        for(User user : mentioned) {
            String name = getSavedName(user.getIdLong());
            if(name == null) {
                channel.sendMessage(getSaveHelp(user == message.getAuthor(), user)).queue();
                return;
            }
            savedNames.add(new UserSavedData(user, name));
        }
        processNames(savedNames, context);
    }

    /**
     * Process the given pairings of users and their saved names
     *
     * @param savedNames Pairs of user & saved name
     * @param context    Command context
     */
    public abstract void processNames(ArrayList<UserSavedData> savedNames, CommandContext context);

    /**
     * User & saved data pair
     */
    public static class UserSavedData {
        private final User user;
        private final String data;

        /**
         * Create the saved user name
         *
         * @param user User with saved name
         * @param data Saved name/id of user
         */
        public UserSavedData(User user, String data) {
            this.user = user;
            this.data = data;
        }

        /**
         * Get the saved name/id of the user
         *
         * @return Saved name/id of the user
         */
        public String getData() {
            return data;
        }

        /**
         * Get the user with the saved name
         *
         * @return User with saved name
         */
        public User getUser() {
            return user;
        }
    }
}
