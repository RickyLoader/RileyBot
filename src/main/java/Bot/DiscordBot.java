package Bot;

import java.lang.reflect.Method;
import java.util.*;

import Exchange.ExchangeData;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Discord bot for posting images and banning cunts.
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

    // Current targets for execute order 66
    public static ArrayList<User> targets;

    // Cached Grand Exchange data
    public static ExchangeData exchangeData = new ExchangeData();

    /**
     * Accepts a Discord token as an argument to link the bot to a Discord application
     *
     * @param args String Discord token used to log bot in
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            String token = args[0];
            login(token);
        } else {
            System.out.println("No discord token specified, please try again using the token as an argument.");
        }
    }

    /**
     * Listener fires when the bot is fully initialised and can be addressed.
     * Begin monitoring updates to commands and initialise basic information.
     *
     * @param e
     */
    @Override
    public void onReady(ReadyEvent e) {
        targets = getTargets(e.getJDA().getGuilds());
        self = e.getJDA().getSelfUser();
        botName = self.getName();

        // Begin listening on new thread for updates
        new Thread(() -> checkForUpdates()).start();
    }

    /**
     * Login procedure for the bot.
     *
     * @param token Discord token to link bot to application
     */
    private static void login(String token) {
        try {

            // Read in commands/user information from the API
            getInfo();

            JDA bot = new JDABuilder(AccountType.BOT).setToken(token).build();
            bot.addEventListener(new DiscordBot());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Reads in command/user information from the API in JSON format and creates a list of
     * objects for each.
     */
    private static void getInfo() {
        System.out.println("Fetching users from database...\n");
        users = DiscordUser.getUsers();
        System.out.println("Fetching commands from database...\n");
        commands = DiscordCommand.getCommands();
    }

    /**
     * Recursively monitors an API endpoint for commands that have been updated and
     * new sick memes on reddit that have been posted. Refreshes the appropriate commands.
     */
    private static void checkForUpdates() {
        ArrayList<DiscordCommand> updates = DiscordCommand.getUpdates();
        if (!updates.isEmpty()) {
            for (DiscordCommand c : updates) {
                update(c);
            }
            System.out.println("\nUpdates complete\n");
        }
        DiscordCommand.getNewMemes(commands.get("meme ?\\d?"));
        checkForUpdates();
    }

    /**
     * Updates the given command's information which has changed in the database.
     *
     * @param update The command to be updated
     */
    private static void update(DiscordCommand update) {
        HashMap<String, DiscordCommand> updates = new HashMap<>();

        // Put all commands not matching the updated command in to a new HashMap
        for (String trigger : commands.keySet()) {
            DiscordCommand c = commands.get(trigger);
            if (c.getID() != update.getID()) {
                updates.put(c.getTrigger(), c);
            }
            // Post a message detailing the changes of the command to all Guilds the bot is a member of
            else {
                for (Guild guild : self.getJDA().getGuilds()) {
                    MessageChannel chan = guild.getDefaultChannel();
                    chan.sendMessage(getChanges(c, update)).queue();
                }
            }
        }

        // Add the updated command to the new HashMap and replace the current map of commands
        updates.put(update.getTrigger(), update);
        commands = updates;

        // Mark the update as seen
        DiscordCommand.finishedUpdate();
    }

    private static String commandToRow(DiscordCommand c, int longestDesc, int longestTrigger, boolean old) {
        String summary = "";
        String identifier = "NEW";
        if (old) {
            identifier = "OLD";
        }
        summary += identifier + getSpaces((3 - identifier.length()) + 10) +
                c.getHelpName()
                + getSpaces((longestTrigger - c.getHelpName().length()) + 10)
                + c.getDesc() + getSpaces((longestDesc - c.getDesc().length()) + 10)
                + " " + c.getCalls()
                + "\n";


        return summary;
    }

    private static String getChanges(DiscordCommand old, DiscordCommand update) {
        String intro = "@everyone\n __**COMMAND UPDATED:**__\n";
        String codeBlock = "```";
        ArrayList<DiscordCommand> compare = new ArrayList<>();
        compare.add(old);
        compare.add(update);
        int longestTrigger = calculateLongest(compare, 2, "name");
        int longestDesc = calculateLongest(compare, 2, "desc");

        String summary = commandToRow(old, longestDesc, longestTrigger, true)
                + commandToRow(update, longestDesc, longestTrigger, false);

        String header = codeBlock
                + "IDENTIFIER"
                + getSpaces((3 - "IDENTIFIER".length()) + 10)
                + "TRIGGER"
                + getSpaces((longestTrigger - "Trigger".length()) + 10)
                + "DESCRIPTION"
                + getSpaces((longestDesc - "DESCRIPTION".length()) + 10)
                + "CALLS"
                + codeBlock;

        return intro + header + codeBlock + summary + codeBlock;
    }

    private static ArrayList<User> getTargets(List<Guild> guilds) {
        System.out.println("\n\nMarking targets for extermination...\n");
        ArrayList<User> targets = new ArrayList<>();
        HashMap<String, DiscordUser> marked = new HashMap<>();

        for (String name : users.keySet()) {
            DiscordUser user = users.get(name);
            if (user.isTarget()) {
                marked.put(user.getID(), user);
                System.out.println(user.getAlias() + " is marked!");
            }
        }

        if (marked.isEmpty()) {
            System.out.println("No targets located!\n");
        } else {
            for (Guild g : guilds) {
                List<Member> members = g.getMembers();
                for (Member m : members) {
                    User user = m.getUser();
                    if (marked.containsKey(user.getAsMention())) {
                        targets.add(user);
                        marked.remove(user.getAsMention());
                        System.out.println("FOUND " + user.getName() + " in " + g.getName());
                    }
                }
            }
            if (!marked.isEmpty()) {
                for (String key : marked.keySet()) {
                    marked.get(key).remove();
                    users.remove(key);
                    System.out.println(marked.get(key).getAlias() + " was unable to be located, they are free to go!\n");
                }
            }
        }
        System.out.println("\nDiscordBot is now running!");
        return targets;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        currentChan = e.getChannel();
        User author = e.getAuthor();
        messageEvent = e;
        String message = e.getMessage().getContentDisplay().toLowerCase().replace("\n", "").replace("\r", "");
        if (author == self) {
            return;
        }

        if (users.containsKey(message)) {
            currentChan.sendMessage("Hey " + users.get(message).getID() + " bro you there?").queue();
        } else if (message.equals("help!")) {
            help(author);
        } else {
            String trigger = getTrigger(message);
            if (trigger != null) {
                executeCommand(commands.get(trigger));
            }
        }
    }


    public static DiscordUser findUser(String userID) {
        for (String name : users.keySet()) {
            DiscordUser user = users.get(name);
            if (user.getID().equals(userID)) {
                return user;
            }
        }
        return null;
    }

    public static void refactorUsers() {
        HashMap<String, DiscordUser> temp = new HashMap<>();
        for (String name : users.keySet()) {
            DiscordUser user = users.get(name);
            temp.put(user.getAlias(), user);
        }
        users = temp;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent e) {
        User author = e.getUser();
        if (!targets.contains(author)) {
            targets.add(author);
        }
        DiscordUser newUser = DiscordUser.userJoined(author);
        if (newUser != null && !users.containsKey(newUser.getAlias())) {
            users.put(newUser.getAlias(), newUser);
        }
        e.getGuild().getDefaultChannel().sendMessage(quickCommand(author)).queue();
    }

    @Override
    public void onGuildBan(GuildBanEvent e) {
        removeUser(e.getUser());
        //DiscordUser.remove(e.getUser());
    }

    private boolean removeUser(User target) {
        for (String name : users.keySet()) {
            DiscordUser u = users.get(name);
            if (u.getID().equals(target.getAsMention())) {
                users.remove(u.getAlias());
                return true;
            }
        }
        return false;
    }

    public static String getTrigger(String msg) {
        String trigger = null;
        for (String t : commands.keySet()) {
            if (msg.matches(t)) {
                trigger = t;
            }
        }
        return trigger;
    }

    private void executeCommand(DiscordCommand command) {
        String type = command.getType();
        command.updateCalls();
        switch (type) {
            case "RANDOM":
                randomCommand(command);
                break;

            case "LINK":
                linkCommand(command);
                break;

            case "PARSE":
                parseCommand(command);
                break;

            case "INTERACTIVE":
                interactiveCommand(command);
                break;
        }
    }

    /*
     * The "RANDOM" type pulls a random line from its associated text file (of new line separated content) and
     * posts it to the Discord channel (provided it has not been previously flagged) - e.g a text file of URLs.
     */
    private void randomCommand(DiscordCommand c) {
        for (int i = 0; i < getQuantity(); i++) {
            String link = c.getLink();
            currentChan.sendMessage(link).queue();
        }
    }

    private int getQuantity() {
        String text = messageEvent.getMessage().getContentDisplay();
        try {
            int quantity = Integer.valueOf(text.split(" ")[1]);
            return quantity;
        } catch (Exception e) {
            return 1;
        }
    }

    /*
     * The "LINK" type is for commands where a trigger is associated with a URL and posts this URL when called.
     */
    private void linkCommand(DiscordCommand c) {
        currentChan.sendMessage(c.getImage().getImage()).queue();
    }

    /*
     * The "PARSE" type parses through a command's associated file and posts the entire contents to the Discord
     * channel, breaking up the message if it exceeds the character limit and creating a table if the file type
     * is XML.
     */
    private void parseCommand(DiscordCommand c) {
        String build = "";
        currentChan.sendMessage(build).queue();
    }

    /*
     * "INTERACTIVE" type commands denote everything not covered by existing types, where more specificity is
     * required - e.g advanced actions such as appending to another command's files.
     */
    private void interactiveCommand(DiscordCommand c) {
        String methodName = c.getMethod();
        try {
            CommandExecution interactive = new CommandExecution(messageEvent, c);
            Method method = interactive.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(interactive);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(c.getMethod() + " does not exist!");
            //currentChan.sendMessage("The \"" + c.getHelpName() + "\" command is broken!").queue();
        }

    }

    public static String getSpaces(int n) {
        String spaces = "";
        while (spaces.length() < n) {
            spaces += " ";
        }
        return spaces;
    }

    private String quickCommand(User author) {
        ArrayList<DiscordCommand> topCommands = new ArrayList<>();
        String codeBlock = "```";
        String intro = "Hello " + author.getAsMention() + "! How's your day been bro?\n\nHere are my top 5 commands:\n\n";
        for (String key : commands.keySet()) {
            DiscordCommand c = commands.get(key);
            topCommands.add(c);
        }

        Collections.sort(topCommands, (o1, o2) -> o2.getCalls() - o1.getCalls());

        int longestName = calculateLongest(topCommands, 5, "name");
        int longestDesc = calculateLongest(topCommands, 5, "desc");
        String summary = codeBlock;

        for (int i = 0; i < 5; i++) {
            DiscordCommand c = topCommands.get(i);
            String name = c.getHelpName();
            String desc = c.getDesc();
            int count = c.getCalls();

            summary +=
                    name
                            + getSpaces((longestName - name.length()) + 10)
                            + desc + getSpaces((longestDesc - desc.length()) + 10)
                            + " " + count
                            + "\n";
        }

        summary += codeBlock;

        String header = codeBlock
                + "TRIGGER"
                + getSpaces((longestName - "Trigger".length()) + 10)
                + "DESCRIPTION"
                + getSpaces((longestDesc - "DESCRIPTION".length()) + 10)
                + "CALLS"
                + codeBlock;

        return intro + header + summary + "\n If you'd like the rest just type help! " +
                "(you're probably used to screaming that when grandad comes around anyway)\n\n" +
                " Enjoy cunt.";
    }

    private static int calculateLongest(ArrayList<DiscordCommand> list, int bound, String type) {
        int longest = 0;
        for (int i = 0; i < bound; i++) {
            DiscordCommand c = list.get(i);
            int toCompare = 0;

            switch (type) {
                case "name":
                    toCompare = c.getHelpName().length();
                    break;
                case "desc":
                    toCompare = c.getDesc().length();
                    break;
            }
            if (toCompare > longest) {
                longest = toCompare;
            }
        }
        return longest;
    }

    private void help(User author) {
        PrivateChannel pc = author.openPrivateChannel().complete();
        HashMap<String, ArrayList<DiscordCommand>> categories = new HashMap<>();
        int longest = 0;
        for (String s : commands.keySet()) {
            DiscordCommand com = commands.get(s);
            String temp = com.getHelpName() + "                    " + com.getDesc();
            int length = temp.length();
            if (length > longest) {
                longest = length;
            }
            if (!categories.containsKey(com.getType())) {
                categories.put(com.getType(), new ArrayList<>());
            }
            categories.get(com.getType()).add(com);
        }

        String help = "__**I AM " + botName.toUpperCase() + " AND I LOVE COCK, HERE ARE THE COMMANDS:**__\n\n";
        String codeBlock = "```";

        for (String category : categories.keySet()) {
            help += "\n__**" + category + " COMMANDS:**__\n\n";
            help += codeBlock;
            for (DiscordCommand com : categories.get(category)) {
                String temp = buildString(longest, "\"" + com.getHelpName() + "\"", com.getDesc() + "\n\n");

                if (help.length() + temp.length() + codeBlock.length() > 1900) {
                    pc.sendMessage(help + codeBlock).queue();
                    help = codeBlock + temp;
                } else {
                    help += temp;
                }
            }
            help += codeBlock;
        }
        pc.sendMessage(help).queue();
        pc.close();
        String msg;
        msg = "I've gone ahead and sent you my commands, you can fuck off now cunt.";
        currentChan.sendMessage(msg).queue();
    }

    private static String buildString(int longest, String trigger, String description) {
        int splitPoint = (longest / 2);
        while (trigger.length() < splitPoint) {
            trigger += " ";
        }
        trigger += "|           ";
        trigger += description;
        return trigger;
    }
}