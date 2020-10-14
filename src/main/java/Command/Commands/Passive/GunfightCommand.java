package Command.Commands.Passive;

import Command.Structure.EmoteListener;
import COD.Gunfight;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

/**
 * Track Modern Warfare wins/losses with an embedded message
 */
public class GunfightCommand extends DiscordCommand {
    private Gunfight gunfight;
    private EmoteListener listener;

    public GunfightCommand() {
        super("gunfight!", "Play a fun game of gunfight!");
    }

    /**
     * Start the gunfight tracking or relocate the current message
     *
     * @param context Context of command
     */
    @Override
    public void execute(CommandContext context) {
        System.out.println("Gunfight command hit");
        if(gunfight != null && gunfight.isActive()) {
            System.out.println("Attempting to relocate Gunfight");
            gunfight.relocate();
            return;
        }
        addEmoteListener(context.getJDA());

        if(context.getLowerCaseMessage().equals("gunfight!")) {
            startNormalGame(context);
            return;
        }
        startCustomGame(context);
    }

    /**
     * Start a custom gunfight session with score initialised to the given values
     *
     * @param context Context of the message that triggered the command
     */
    private void startCustomGame(CommandContext context) {
        String[] args = context.getLowerCaseMessage().replace("gunfight! ", "").split(" ");

        if(!checkValidScore(args)) {
            wrongInputMessage(context.getMessageChannel());
            return;
        }

        gunfight = new Gunfight(
                context.getMessageChannel(),
                context.getUser(),
                context.getEmoteHelper(),
                Integer.parseInt(args[0]),
                Integer.parseInt(args[1]),
                Integer.parseInt(args[2]),
                Integer.parseInt(args[3])
        );
        gunfight.startGame();
    }

    /**
     * Check if the correct score arguments have been given and are valid
     *
     * @param args score arguments
     * @return validity of score arguments
     */
    private boolean checkValidScore(String[] args) {
        try {
            int wins = Integer.parseInt(args[0]);
            int losses = Integer.parseInt(args[1]);
            int currentStreak = Integer.parseInt(args[2]);
            int longestStreak = Integer.parseInt(args[3]);
            if(wins < 0 || losses < 0 || longestStreak < 0) {
                return false;
            }
            if(longestStreak > wins || currentStreak > longestStreak || (longestStreak != currentStreak) && (longestStreak + currentStreak) > wins) {
                return false;
            }
            if(currentStreak < 0 && currentStreak < -losses || currentStreak > 0 && currentStreak > wins) {
                return false;
            }
            if((wins - losses) > (currentStreak - longestStreak)) {
                return false;
            }
        }
        catch(Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Send a message informing the user they provided an incorrect input
     *
     * @param channel Channel to send message in
     */
    private void wrongInputMessage(MessageChannel channel) {
        channel.sendMessage("```Take a look at gunfight help! you useless cunt```").queue();
    }

    /**
     * Start a default gunfight session with score initialised to 0
     *
     * @param context Context of the message that triggered the command
     */
    private void startNormalGame(CommandContext context) {
        gunfight = new Gunfight(
                context.getMessageChannel(),
                context.getUser(),
                context.getEmoteHelper()
        );
        gunfight.startGame();
    }

    /**
     * Get an emote listener for calling the Gunfight instance when emotes are clicked
     *
     * @return Emote listener
     */
    private EmoteListener getEmoteListener() {
        return new EmoteListener() {
            @Override
            public void handleReaction(MessageReaction reaction, User user, Guild guild) {
                System.out.println("Emote added");
                long reactID = reaction.getMessageIdLong();
                if(gunfight != null && reactID == gunfight.getGameId() && gunfight.isActive() && (user == gunfight.getOwner() || (user == guild.getOwner().getUser()))) {
                    System.out.println("Sending to gunfight");
                    gunfight.reactionAdded(reaction);
                }
            }
        };
    }

    /**
     * Add an emote listener to listen for gunfight emotes if there isn't one already
     *
     * @param jda BOT
     */
    private void addEmoteListener(JDA jda) {
        if(this.listener == null) {
            this.listener = getEmoteListener();
            jda.addEventListener(this.listener);
        }
    }

    @Override
    public boolean matches(String query) {
        return super.matches(query) || query.startsWith("gunfight!") && query.split(" ").length == 5;
    }
}
