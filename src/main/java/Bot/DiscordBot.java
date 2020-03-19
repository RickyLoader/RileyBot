package Bot;

import java.lang.reflect.Method;
import java.util.*;

import Exchange.ExchangeData;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;

import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class DiscordBot extends ListenerAdapter {

    // Holds command and user information during runtime
    public static HashMap<String, DiscordCommand> commands;
    private static User self;
    private static String botName;

    // Holds most recent message information
    private static MessageChannel currentChan;
    private GuildMessageReceivedEvent messageEvent;
    private static JDA jda;
    private Guild guild;

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
        self = e.getJDA().getSelfUser();
        botName = self.getName();
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
            System.out.println("\nBot is now running!\n");
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Reads in command/user information from the API in JSON format and creates a map of
     * each.
     * Commands = trigger->command
     */
    private static boolean getInfo() {
        System.out.println("\nFetching commands from database...\n");
        commands = DiscordCommand.getCommands();
        return commands != null;
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

    private void logMessage(GuildMessageReceivedEvent e) {
        Message message = e.getMessage();
        String reply = "\nMessage: \n    Guild: " +
                e.getGuild().getName() + "\n    Author: " + e.getAuthor().getName();
        List<Message.Attachment> attachments = message.getAttachments();
        String content = message.getContentDisplay();
        reply = (content.isEmpty()) ? reply : reply + "\n    Content: " + content;
        reply = (attachments.isEmpty()) ? reply : reply + "\n    Attachment: " + attachments.get(0).getUrl();
        System.out.println(reply);
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
        guild = e.getGuild();
        // Strip the message to an appropriate format for comparison to command triggers
        String message = e.getMessage().getContentDisplay().toLowerCase().replace("\n", "").replace("\r", "");


        // Bot doesn't respond to itself
        if(author.isBot()) {
            return;
        }

        logMessage(e);

        if(message.equals("help!")) {
            help(author);
        }
        else if(message.equals("gunfight!")) {
            gunfight = new Gunfight(currentChan, e.getGuild(), e.getAuthor());
        }
        else {
            // Compare the message to command triggers and proceed accordingly
            DiscordCommand command = getCommand(message);
            if(command != null) {
                executeCommand(command);
            }
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if(gunfight == null || event.getUser() == self) {
            return;
        }
        if(event.getMessageIdLong() == gunfight.getGameId() && event.getUser() == gunfight.getOwner()) {
            gunfight.reactionAdded(event.getReaction());
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if(gunfight == null || event.getUser() == self) {
            return;
        }
        if(event.getMessageIdLong() == gunfight.getGameId() && event.getUser() == gunfight.getOwner()) {
            gunfight.reactionAdded(event.getReaction());
        }
    }

    /**
     * Event fired when a user joins a guild the bot belongs to
     *
     * @param e Event used to obtain user and guild information
     */
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent e) {
        User author = e.getUser();
        System.out.println(author.getName() + " has joined " + e.getGuild().getName() + ", marking\n\n");
        TargetUser.addTarget(author);
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
        messageEvent.getMessage().delete().complete();
        MessageChannel memeChannel = findMemeChannel();

        if(memeChannel == null) {
            currentChan.sendMessage("I need a channel with 'meme' in the name cunt.").queue();
            return;
        }
        for(int i = 0; i < getQuantity(); i++) {
            String link = c.getLink();
            memeChannel.sendMessage(link).queue();
        }
    }

    private MessageChannel findMemeChannel() {
        for(MessageChannel chan : guild.getTextChannels()) {
            if(chan.getName().toLowerCase().contains("meme")) {
                return chan;
            }
        }
        return null;
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
            CommandExecution interactive = new CommandExecution(messageEvent, c, jda);
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
                "(you're probably used to screaming that when grandad comes around)\n\n" +
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

    /**
     * Mark a user for extermination by the bot
     */
    private void mark(User target) {

    }

    /**
     * Pardon a user from extermination by the bot
     */
    private void pardon(User target) {

    }
}