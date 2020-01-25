package Bot;

import java.lang.reflect.Method;
import java.util.*;

import Audio.TrackEndListener;
import Exchange.ExchangeData;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import org.json.JSONObject;

/**
 * DiscordBot.java A bot for Discord posts images and bans cunts.
 *
 * @author Ricky Loader
 * @version 5000.0
 */
public class DiscordBot extends ListenerAdapter {

    // Holds command and user information during runtime
    public static HashMap<String, DiscordCommand> commands;
    public static HashMap<String, DiscordUser> users;
    private static User self;
    private static String botName;

    // Holds most recent message information
    private static MessageChannel currentChan;
    private GuildMessageReceivedEvent messageEvent;
    private static JDA jda;

    // Current targets for execute order 66
    public static ArrayList<User> targets;

    // Cached Grand Exchange data
    public static ExchangeData exchangeData = new ExchangeData();

    private HashMap<String, Message> messageHistory = new HashMap<>();

    private Gunfight gunfight;

    /**
     * Accepts a Discord token as an argument to link the bot to a Discord application
     *
     * @param args String Discord token used to log bot in
     */
    public static void main(String[] args) {
        if(args.length == 1) {
            String token = args[0];
            login(token);
        }
        else {
            System.out.println("No discord token specified, please try again using the token as an argument.");
        }
    }

    /**
     * Listener fires when the bot is fully initialised and can be addressed.
     * Begin monitoring for updates and initialise basic information.
     *
     * @param e Event
     */
    @Override
    public void onReady(ReadyEvent e) {
        jda = e.getJDA();
        targets = getTargets();
        self = e.getJDA().getSelfUser();
        botName = self.getName();
        // Begin listening on new thread for updates
        //new Thread(() -> checkForUpdates()).start();
    }

    /**
     * Login procedure for the bot.
     *
     * @param token Discord token to link bot to application
     */
    private static void login(String token) {
        try {
            while(!getInfo()) {
                System.out.println("\n\nUnable to reach API, trying again in 5 seconds...\n\n");
                Thread.sleep(5000);
            }
            JDA bot = new JDABuilder(AccountType.BOT).setToken(token).build();
            bot.addEventListener(new DiscordBot());
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Reads in command/user information from the API in JSON format and creates a map of
     * each.
     * Commands = trigger->command
     * Users = alias->user
     */
    private static boolean getInfo() {
        System.out.println("\nFetching users from database...\n");
        users = DiscordUser.getUsers();
        System.out.println("\nFetching commands from database...\n");
        commands = DiscordCommand.getCommands();
        return commands != null && users != null;
    }

    /**
     * Creates a column separated code block displaying the information of a command. Used in creating
     * blocks which contain information of multiple commands.
     *
     * @param c              The command to be displayed
     * @param longestDesc    Length of the longest description of all commands that will be displayed in the final block
     * @param longestTrigger Length of the longest trigger of all commands that will be displayed in the final block
     * @param identifier     Row value for "IDENTIFIER" column header.
     * @return String row for displaying in a command update table
     */
    private static String commandToRow(DiscordCommand c, int longestDesc, int longestTrigger, String identifier) {
        String summary = "";

        summary += identifier + getSpaces((3 - identifier.length()) + 10) +
                c.getHelpName()
                + getSpaces((longestTrigger - c.getHelpName().length()) + 10)
                + c.getDesc() + getSpaces((longestDesc - c.getDesc().length()) + 10)
                + " " + c.getCalls()
                + "\n";


        return summary;
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent e) {
        System.out.println("\nEvent:\n    Guild: " + e.getGuild().getName() + "\n    Event: MESSAGE DELETED:");
        Message m = messageHistory.get(e.getMessageId());
        List<AuditLogEntry> logs = e.getGuild().getAuditLogs().complete();
        String name = logs.get(0).getUser().getName();
        System.out.println(m == null
                ? "        Message not found:\n            Admin: " + name
                : "        Message found:\n            Admin: " + name + "\n            Author: " + m.getAuthor().getName() + "\n            Contents: " + m.getContentDisplay()
        );
    }

    /**
     * Attempts to locate and store targets marked for slamming. Searches all guilds the bot is a member of,
     * if the target cannot be found (marked but no longer a member of any mutual guilds) they are unmarked.
     *
     * @return List of marked users
     */
    private static ArrayList<User> getTargets() {
        List<Guild> guilds = jda.getGuilds();
        System.out.println("\n\nMarking targets for extermination...\n");
        ArrayList<User> targets = new ArrayList<>();
        HashMap<String, DiscordUser> pendingTargets = new HashMap<>();

        // Store all marked users by their unique id
        for(String name : users.keySet()) {
            DiscordUser user = users.get(name);
            if(user.isTarget()) {
                pendingTargets.put(user.getID(), user);
                System.out.println(user.getAlias() + " added to the kill list!");
            }
        }

        // No targets
        if(pendingTargets.isEmpty()) {
            System.out.println("No targets located!\n\n");
        }
        // Some targets
        else {
            // Get a list of members from every guild
            for(Guild g : guilds) {
                List<Member> members = g.getMembers();

                // Search the members for marked users
                for(Member m : members) {
                    User user = m.getUser();

                    // User is found
                    if(pendingTargets.containsKey(user.getAsMention())) {
                        targets.add(user);
                        pendingTargets.remove(user.getAsMention());
                        System.out.println("FOUND user: " + user.getName() + " in server: " + g.getName());
                    }
                }
            }

            // Not all pending targets were located
            if(!pendingTargets.isEmpty()) {

                // Remove the users from the database
                for(String key : pendingTargets.keySet()) {
                    pendingTargets.get(key).remove();
                    users.remove(key);
                    System.out.println(pendingTargets.get(key).getAlias() + " was unable to be located, they are free to go!\n");
                }
            }
        }
        System.out.println("DiscordBot is now running!\n");
        return targets;
    }

    /**
     * Core listener of the bot, fires when a message is received on any guild the bot belongs to. Checks if the
     * message is a command and responds appropriately.
     *
     * @param e Event used to obtain message, user, and guild information.
     */
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        // Store information about the current message (for use by the command if required)
        currentChan = e.getChannel();
        User author = e.getAuthor();
        messageHistory.put(e.getMessage().getId(), e.getMessage());
        messageEvent = e;
        // Strip the message to an appropriate format for comparison to command triggers
        String message = e.getMessage().getContentDisplay().toLowerCase().replace("\n", "").replace("\r", "");


        // Bot doesn't respond to itself
        if(author == self) {
            return;
        }

        System.out.println("\nMessage: \n    Guild: " +
                e.getGuild().getName() + "\n    Author: " + author.getName() + "\n    Content: " + message);

        // Message was the alias of a stored user, @mention the user
        if(users.containsKey(message)) {
            currentChan.sendMessage("Hey " + users.get(message).getID() + " bro you there?").queue();
        }
        // Help command
        else if(message.equals("help!")) {
            help(author);
        }
        else if(message.contains("poll: ")) {
            createPoll(message.replaceFirst("poll: ", ""));
        }
        else if(message.equals("gunfight!")) {
            gunfight = new Gunfight(currentChan, e.getGuild());
        }
        else if(message.contains("playing: ")){
            setPresence(e.getMessage().getContentDisplay());
        }
        else {
            // Compare the message to command triggers and proceed accordingly
            DiscordCommand command = getCommand(message);
            if(command != null) {
                executeCommand(command);
            }
        }
    }

    private void setPresence(String msg){
        String presence = msg.replace("playingu: ","");
        jda.getPresence().setGame(Game.playing(presence));
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if(gunfight == null || event.getUser() == self) {
            return;
        }
        if(event.getMessageIdLong() == gunfight.getGameId()) {
            gunfight.reactionAdded(event.getReaction());
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if(gunfight == null || event.getUser() == self) {
            return;
        }
        if(event.getMessageIdLong() == gunfight.getGameId()) {
            gunfight.reactionAdded(event.getReaction());
        }
    }

    private static void createPoll(String title) {
        String query = "{" + "\"title\":\"" + title + "\",\"options\":[\"YES\",\"NO\"],\"multi\":\"false\"}";
        String json = ApiRequest.executeQuery("https://www.strawpoll.me/api/v2/polls", "ADD", query, false);
        try {
            JSONObject o = new JSONObject(json);
            currentChan.sendMessage("@everyone https://www.strawpoll.me/" + o.getLong("id")).queue();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Users are stored by their alias, method allows to locate a user by user id (getAsMention())
     *
     * @param userID The getAsMention() value of a User object
     * @return The User or null if not found
     */
    public static DiscordUser findUser(String userID) {
        for(String name : users.keySet()) {
            DiscordUser user = users.get(name);
            if(user.getID().equals(userID)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Event fired when a user joins a guild the bot belongs to
     *
     * @param e Event used to obtain user and guild information
     */
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent e) {
        User author = e.getUser();

        // If the user is not already marked for execution, mark them (marked user may have left and joined back later)
        if(!targets.contains(author)) {
            System.out.println(author.getName() + " has joined " + e.getGuild().getName() + ", marking\n\n");
            targets.add(author);
        }

        // Create an object to hold the new user and store them in the database if they don't exist
        DiscordUser newUser = DiscordUser.userJoined(author);
        if(newUser != null && !users.containsKey(newUser.getAlias())) {
            users.put(newUser.getAlias(), newUser);
        }
        // Send a message to the guild containing a breakdown of popular commands
        e.getGuild().getDefaultChannel().sendMessage(quickCommand(author)).queue();
    }


    /**
     * Checks if a message received is a trigger to a command, returns the trigger. Trigger commonly contains REGEX
     * which containsKey() does not compare, a search is required.
     *
     * @param msg The message received
     * @return Command if found, null if not
     */
    public static DiscordCommand getCommand(String msg) {
        DiscordCommand command = commands.get(msg);
        if(command == null) {
            for(String trigger : commands.keySet()) {
                if(msg.matches(trigger)) {
                    command = commands.get(trigger);
                }
            }
        }
        return command;
    }

    /**
     * Commands are identified by type as many commands perform identical actions on different data. Find and execute
     * the command by type.
     *
     * @param command The command to be executed
     */
    private void executeCommand(DiscordCommand command) {
        String type = command.getType();

        command.updateCalls();
        System.out.println("    Command: " + command.getHelpName().toUpperCase());
        switch(type) {
            case "RANDOM":
                randomCommand(command);
                break;

            case "LINK":
                linkCommand(command);
                break;

            case "INTERACTIVE":
                interactiveCommand(command);
                break;
        }
    }

    /**
     * "RANDOM" type  commands post a random URL from a list of URLs. An integer 0-9 can also be provided to
     * repeat this x times.
     *
     * @param c The random type command
     */
    private void randomCommand(DiscordCommand c) {
        for(int i = 0; i < getQuantity(); i++) {
            String link = c.getLink();
            currentChan.sendMessage(link).queue();
        }
    }

    /**
     * Attempts to pull an integer from the given message when a Random type command is called.
     *
     * @return The integer or 1 if not found. Determines how many times to repeat the command
     */
    private int getQuantity() {
        String text = messageEvent.getMessage().getContentDisplay();
        try {
            int quantity = Integer.valueOf(text.split(" ")[1]);
            return quantity;
        }
        catch(Exception e) {
            return 1;
        }
    }

    /**
     * "LINK" type commands are where a trigger is associated with a URL, and posts this URL when called.
     *
     * @param c The link type command
     */
    private void linkCommand(DiscordCommand c) {
        currentChan.sendMessage(c.getImage()).queue();
    }

    /**
     * "INTERACTIVE" type commands are commands which perform unique actions outside the current type definitions.
     * Attempt to invoke the command's associated method via reflection.
     *
     * @param c The interactive type command
     */
    private void interactiveCommand(DiscordCommand c) {
        String methodName = c.getMethod();
        try {
            CommandExecution interactive = new CommandExecution(messageEvent, c);
            Method method = interactive.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(interactive);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Returns a String containing n spaces.
     *
     * @param n The number of spaces desired
     * @return A String containing n spaces
     */
    public static String getSpaces(int n) {
        String spaces = "";
        while(spaces.length() < n) {
            spaces += " ";
        }
        return spaces;
    }

    /**
     * Creates a table of the top 5 most called commands.
     *
     * @param author The user who has joined the server and will be provided the information
     * @return A column separated table containing information about the most popular commands
     */
    private String quickCommand(User author) {
        ArrayList<DiscordCommand> topCommands = new ArrayList<>();
        String codeBlock = "```";
        String intro = "Hello " + author.getAsMention() + "! How's your day been bro?\n\nHere are my top 5 commands:\n\n";

        // Create a list of all commands
        for(String key : commands.keySet()) {
            DiscordCommand c = commands.get(key);
            topCommands.add(c);
        }

        // Sort the commands by the number of calls, descending
        Collections.sort(topCommands, (o1, o2) -> o2.getCalls() - o1.getCalls());

        // Find the longest helpName and description of the first 5 commands (required to calculate spacing in table)
        int longestName = calculateLongest(topCommands, 5, "name");
        int longestDesc = calculateLongest(topCommands, 5, "desc");

        String summary = codeBlock;

        // Create a row for each of the top 5 commands containing the name, description, and number of calls
        for(int i = 0; i < 5; i++) {
            DiscordCommand c = topCommands.get(i);
            String name = c.getHelpName();
            String desc = c.getDesc();
            int count = c.getCalls();

            /*
              The spaces required between attributes is equal to the longest possible - the current. The +10 is to pad
              the attributes out and not have them right beside each other.

              E.g if the longest had a length of 10 and the current a length of 5, the next column of the current
              (with +10 padding) would begin at 15, while the next column of the longest would begin at 20 and not line up.
             */
            summary +=
                    name
                            + getSpaces((longestName - name.length()) + 10)
                            + desc + getSpaces((longestDesc - desc.length()) + 10)
                            + " " + count
                            + "\n";
        }

        summary += codeBlock;

        // Header of the table, spacing of the columns calculated the same way to line up with the row values.
        String header = codeBlock
                + "TRIGGER"
                + getSpaces((longestName - "TRIGGER".length()) + 10)
                + "DESCRIPTION"
                + getSpaces((longestDesc - "DESCRIPTION".length()) + 10)
                + "CALLS"
                + codeBlock;

        return intro + header + summary + "\n If you'd like the rest just type help! " +
                "(you're probably used to screaming that when grandad comes around anyway)\n\n" +
                " Enjoy cunt.";
    }

    /**
     * Calculate the longest length attribute of a list of commands.
     *
     * @param list  List of commands
     * @param bound How many to look at
     * @param type  The attribute to be compared
     * @return The length of the longest attribute
     */
    private static int calculateLongest(ArrayList<DiscordCommand> list, int bound, String type) {
        int longest = 0;
        for(int i = 0; i < bound; i++) {
            DiscordCommand c = list.get(i);
            int toCompare = 0;

            switch(type) {
                case "name":
                    toCompare = c.getHelpName().length();
                    break;
                case "desc":
                    toCompare = c.getDesc().length();
                    break;
            }
            if(toCompare > longest) {
                longest = toCompare;
            }
        }
        return longest;
    }

    /**
     * Private message a user with a breakdown of all commands in a table.
     *
     * @param author The user who called the "help!" command
     */
    private void help(User author) {
        PrivateChannel pc = author.openPrivateChannel().complete();
        HashMap<String, ArrayList<DiscordCommand>> categories = new HashMap<>();

        // Find the longest helpName of the commands and store each command by their category
        ArrayList<DiscordCommand> helpCommands = new ArrayList<>(commands.values());
        int longestName = calculateLongest(helpCommands, helpCommands.size(), "name");
        int longestDesc = calculateLongest(helpCommands, helpCommands.size(), "desc");

        for(String s : commands.keySet()) {
            DiscordCommand com = commands.get(s);
            if(!categories.containsKey(com.getType())) {
                categories.put(com.getType(), new ArrayList<>());
            }
            categories.get(com.getType()).add(com);
        }

        String help = "__**I AM " + botName.toUpperCase() + " AND I LOVE COCK, HERE ARE THE COMMANDS:**__\n\n";
        String codeBlock = "```";

        // Create a separate block with a header for each type of command
        for(String category : categories.keySet()) {
            help += "\n__**" + category + " COMMANDS:**__\n\n";
            String header = codeBlock
                    + "TRIGGER"
                    + getSpaces((longestName - "TRIGGER".length()) + 5)
                    + "DESCRIPTION"
                    + getSpaces((longestDesc - "DESCRIPTION".length()) + 5)
                    + "CALLS"
                    + codeBlock;
            help += header;
            help += codeBlock;
            ArrayList<DiscordCommand> commands = categories.get(category);
            Collections.sort(commands);
            for(DiscordCommand com : commands) {
                String temp = com.getHelpName()
                        + getSpaces((longestName - com.getHelpName().length()) + 5)
                        + com.getDesc()
                        + getSpaces((longestDesc - com.getDesc().length()) + 5)
                        + com.getCalls()
                        + "\n\n";

                // Send the current block if it exceeds the character limit
                if(help.length() + temp.length() + codeBlock.length() > 1900) {
                    pc.sendMessage(help + codeBlock).queue();
                    help = codeBlock + temp;
                }
                else {
                    help += temp;
                }
            }
            help += codeBlock;
        }
        pc.sendMessage(help).queue();
        pc.close();
        currentChan.sendMessage("I've gone ahead and sent you my commands, you can fuck off now cunt.").queue();
    }

    @Override
    public void onGuildVoiceMute(GuildVoiceMuteEvent event) {
        if(event.getMember() == event.getGuild().getMember(self)) {
            System.out.println("Someone muted me, attempting to unmute");
            event.getGuild().getController().setMute(event.getMember(), false);
        }
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if(event.getMember() == event.getGuild().getMember(self)) {
            System.out.println("I was moved, coming back!");
            System.out.println(event.getResponseNumber());
            event.getGuild().getController().moveVoiceMember(event.getMember(), event.getChannelLeft());
        }
    }
}