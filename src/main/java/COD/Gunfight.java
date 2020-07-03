package COD;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Interactive win/loss tracker - use emotes to track score
 */
public class Gunfight {

    // ID of the game message, used to find the message in the channel
    private MessageChannel channel;
    private Emote win, loss, stop, undo;
    private int wins, losses, streak, rank = 0, longestStreak = 0;
    private Guild server;
    private long lastUpdate = 0, startTime = 0, gameID;
    private String lastMessage;
    private static final String thumb = "https://bit.ly/2YTzfTQ";
    private boolean active;
    private LinkedList<Gunfight> matchHistory;

    // User who started game, only user allowed to register score
    private User owner;

    public Gunfight(MessageChannel channel, Guild server, User owner) {
        this.channel = channel;
        this.server = server;
        this.owner = owner;
        this.active = true;
        matchHistory = new LinkedList<>();
        if(checkEmotes()) {
            startGame();
        }
        else {
            channel.sendMessage("This server needs emotes named \"victory\", \"defeat\", \"stop\", and \"undo\" to play gunfight cunt.").queue();
        }
    }

    public Gunfight(MessageChannel channel, Guild server) {
        this.channel = channel;
        this.server = server;
        if(checkEmotes()) {
            showHelpMessage();
        }
    }

    /**
     * Constructor to keep a history of game score for undo purposes
     *
     * @param wins       Current wins
     * @param losses     Current losses
     * @param streak     Current streak
     * @param lastUpdate Time of update
     */
    public Gunfight(int wins, int losses, int streak, long lastUpdate, int rank, int longestStreak) {
        this.wins = wins;
        this.losses = losses;
        this.streak = streak;
        this.rank = rank;
        this.longestStreak = longestStreak;
        this.lastUpdate = lastUpdate;
    }

    /**
     * Current game state, stop responding to emotes if game has finished
     *
     * @return game status
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Attempts to locate emotes named win & loss, required for game
     *
     * @return Presence of emotes
     */
    private boolean checkEmotes() {
        List<Emote> victory = server.getEmotesByName("victory", true);
        List<Emote> defeat = server.getEmotesByName("defeat", true);
        List<Emote> stop = server.getEmotesByName("stop", true);
        List<Emote> undo = server.getEmotesByName("undo", true);

        if(victory.size() > 0 && defeat.size() > 0 && stop.size() > 0 && undo.size() > 0) {
            this.win = victory.get(0);
            this.loss = defeat.get(0);
            this.stop = stop.get(0);
            this.undo = undo.get(0);
            return true;
        }
        return false;
    }

    /**
     * Builds the game message. Called to begin game and build new messages as score is added.
     *
     * @return Game message
     */
    private MessageEmbed buildGameMessage(String streak) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(65280);
        String title = "GUNFIGHT";
        if(rank > 0) {
            title += " RANK " + rank;
        }
        builder.setTitle(title);
        builder.setDescription(createDesc());
        builder.setThumbnail(thumb);
        builder.setImage("https://i.imgur.com/24Xf03H.png");
        builder.addField("**WIN**", String.valueOf(wins), true);
        builder.addBlankField(true);
        builder.addField("**LOSS**", String.valueOf(losses), true);
        builder.addField("**STREAK**", streak, false);
        String footer;
        String suffix = " -- Checkout 'gunfight help!' for instructions";

        if(lastUpdate == 0) {
            footer = "Game started at " + formatTime(startTime);
        }
        else {
            footer = "Last update at " + formatTime(lastUpdate);
        }

        builder.setFooter(footer + suffix, "https://i.imgur.com/rVhdoRs.gif");
        return builder.build();
    }


    /**
     * Format the last update time to display on the game message
     *
     * @return Current time
     */
    private String formatTime(long time) {
        return new SimpleDateFormat("HH:mm:ss").format(time);
    }

    /**
     * Send the game message to the channel to begin playing
     */
    private void startGame() {
        startTime = System.currentTimeMillis();
        sendGameMessage(buildGameMessage(String.valueOf(streak)));
    }

    /**
     * Remove the game message
     */
    public void deleteGame() {
        getGameMessage().delete().complete();
    }

    /**
     * Check if reaction is a win or loss & update score appropriately
     *
     * @param reaction Emote reaction on game message (may be invalid)
     */
    public void reactionAdded(MessageReaction reaction) {
        Emote emote = reaction.getReactionEmote().getEmote();
        long currentTime = System.currentTimeMillis();

        if((emote != win && emote != loss && emote != stop && emote != undo)) {
            return;
        }

        if(emote == stop) {
            stopGame();
            return;
        }

        if(emote == undo) {
            undoLast();
            return;
        }
        matchHistory.push(new Gunfight(wins, losses, streak, lastUpdate, rank, longestStreak));
        lastUpdate = currentTime;

        if(emote == win) {
            addWin();
        }
        else {
            addLoss();
        }
        ArrayList<Session> leaderboard = Session.getHistory();
        Session current = new Session(startTime, lastUpdate, wins, losses, longestStreak);
        leaderboard.add(current);
        Session.sortSessions(leaderboard);
        rank = (leaderboard.indexOf(current)) + 1;
        updateMessage();
    }

    /**
     * Move the game back to the most recent message
     */
    public void relocate() {
        if(!gameFocused()) {
            deleteGame();
            sendGameMessage(createUpdateMessage());
        }
    }

    /**
     * Create the updated game message displaying new values
     *
     * @return Updated game message
     */
    private MessageEmbed createUpdateMessage() {

        // Which form of win/wins, loss/losses to use if more than 1
        String win = this.streak == 1 ? " WIN" : " WINS";
        String loss = this.streak == -1 ? " LOSS" : " LOSSES";

        // Streak message to display, a negative streak should show as 2 losses not -2 losses
        String streak = this.streak < 0 ? Math.abs(this.streak) + loss : this.streak + win;

        return buildGameMessage(streak);
    }

    /**
     * Update the game message to display new score
     */
    private void updateMessage() {
        Message gameMessage = getGameMessage();
        MessageEmbed updateMessage = createUpdateMessage();

        if(gameFocused()) {
            gameMessage.editMessage(updateMessage).queue();
        }
        else {
            deleteGame();
            sendGameMessage(updateMessage);
        }
    }

    /**
     * Find the game message
     *
     * @return Game message
     */
    private Message getGameMessage() {
        return channel.retrieveMessageById(gameID).complete();
    }

    /**
     * Game message is the most recent channel message
     *
     * @return Game message most recent channel message
     */
    private boolean gameFocused() {
        //Message latest = channel.getHistory().retrievePast(1).complete().get(0);
        return channel.getLatestMessageIdLong() == gameID; //|| latest.getContentDisplay().isEmpty();
    }

    /**
     * Sends the game message and adds the win/loss emotes so the user can click to add score
     *
     * @param gameMessage Interactive game message
     */
    private void sendGameMessage(MessageEmbed gameMessage) {

        // Callback to add reactions and save message id
        Consumer<Message> addReactionCallback = (response) -> {
            gameID = response.getIdLong();
            response.addReaction(win).queue();
            response.addReaction(loss).queue();
            response.addReaction(undo).queue();
            response.addReaction(stop).queue();
        };
        channel.sendMessage(gameMessage).queue(addReactionCallback);
    }

    /**
     * Add a win to the scoreboard, reset streak if on a loss streak.
     */
    private void addWin() {
        if(streak < 0) {
            streak = 0;
        }
        wins++;
        streak++;

        // Keep track of the largest win streak of the session
        if(streak > longestStreak) {
            longestStreak = streak;
        }

        System.out.println("\nWin reaction added: " + formatTime(lastUpdate) + " " + wins + "/" + losses);
    }

    /**
     * Add a loss to the scoreboard, reset streak if on a win streak.
     */
    private void addLoss() {
        if(streak > 0) {
            streak = 0;
        }
        losses++;
        streak--;
        System.out.println("\nLoss reaction added: " + formatTime(lastUpdate) + " " + wins + "/" + losses);
    }

    /**
     * Get id of the game message
     *
     * @return Game message id
     */
    public long getGameId() {
        return gameID;
    }

    /**
     * Get the user who started the game
     *
     * @return User who started the game
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Get the thumbnail used in the game message
     *
     * @return URL of thumbnail
     */
    static String getThumb() {
        return thumb;
    }

    /**
     * Finish the game and commit the score to the database
     */
    private void stopGame() {
        active = false;
        deleteGame();

        // Don't submit empty games
        if(wins == 0 && losses == 0) {
            return;
        }
        Session session = new Session(startTime, lastUpdate, wins, losses, longestStreak);
        session.submitGame();
        channel.sendMessage(buildGameSummaryMessage(session)).queue();
    }

    /**
     * Builds the game summary message, display the final results of the game
     *
     * @return Game summary message
     */
    private MessageEmbed buildGameSummaryMessage(Session session) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(15655767);
        builder.setTitle("GUNFIGHT RESULTS #" + (Session.getTotalMatches()));
        builder.setThumbnail(thumb);
        builder.setDescription(getRanking(session));
        builder.setFooter("Check out the leaderboard!", null);
        builder.addField("**DURATION**", session.getDuration(), false);
        builder.addField("**WINS**", String.valueOf(wins), true);
        builder.addField("**LOSSES**", String.valueOf(losses), true);
        builder.addField("**RATIO**", String.valueOf(session.formatRatio()), true);
        builder.addField("**LONGEST STREAK**", session.formatStreak(), false);
        return builder.build();
    }

    private String getRanking(Session session) {
        String result;
        int rank = session.getRank();
        if(rank == 1) {
            result = "That's a new personal best, nice work cunts!";
        }
        else if(rank < 5) {
            result = "Rank " + rank + ", you're now in the top 5!";
        }
        else {
            result = "Rank " + rank + ", terrible job cunts";
        }
        return result;
    }

    /**
     * Generate a message to be shown on the game based on the current score.
     *
     * @return An encouraging message to be displayed on the game
     */
    private String createDesc() {
        Random rand = new Random();
        String[] messages;

        // Beginning of game
        if(wins == 0 && losses == 0) {
            messages = new String[]{
                    "Let's do this!",
                    "Good news, the cheque cleared... It's go time!",
                    "Shut up, cock in and load up.",
                    "Lock 'em and load 'em ladies it's go time.",
                    "Keep your head up and your ass down, it's go time."
            };
        }

        // Score is even
        else if(wins == losses) {
            messages = new String[]{
                    "Neck and neck, pick up your game cunts!",
                    "You better not fall behind",
                    "Get that lead back!"
            };
        }

        // last match won
        else if(streak >= 1) {

            // just taken lead and it isn't first match
            if(wins - losses == 1 && losses != 0) {
                messages = new String[]{
                        "Now Maintain that lead cunts!",
                        "Nice work, you're back in the game!",
                        "What a cum back!"
                };
            }

            // first win
            else if(wins - losses == 1) {
                messages = new String[]{
                        "Off to a good start, how long before you fuck it up?",
                        "Nice job spastics!",
                        "Bon boulot, get ready for the next round",
                        "Let's get a streak going!",
                };
            }

            // win streak
            else if(wins - losses > 1) {
                String mvp = (owner.getName().charAt(owner.getName().length() - 1)) == 's' ? owner.getName() + "'" : owner.getName() + "'s";
                messages = new String[]{
                        "Nice streak!",
                        "What a lead!",
                        "Unstoppable!",
                        "They never stood a chance!",
                        "Take it easy on them!",
                        "On a roll!",
                        "HERE COMES JOE",
                        "Beautiful, I especially enjoyed " + mvp + " performance, it was incredible!"
                };
            }

            // still behind
            else {
                messages = new String[]{
                        "You're still behind cunts, not good enough",
                        "You still need " + Math.abs(wins - losses) + " " + (Math.abs(wins - losses) == 1 ? "win" : "wins") + " to pass them cunts"
                };
            }
        }

        // last match lost
        else {
            // just fallen behind
            if(losses - wins == 1) {
                messages = new String[]{
                        "You're behind cunts",
                        "You're behind, you better win the fucking next one",
                        "They were terrible, now you're behind, that final killcam was pathetic cunts, pick up your fucking game"
                };
            }
            // loss streak
            else if(losses - wins >= 2) {
                messages = new String[]{
                        "What the fuck was that?",
                        "How didn't you kill him?",
                        "Is this is the fucking special olympics?",
                        "Fucking pistol rounds",
                        "Fucking JOE",
                        "That was pathetic",
                        "This is embarrassing",
                        "You fucking useless cunts",
                        "Fuck that's embarrassing",
                        "Time to give up?",
                        "Give up now lmao",
                        "How the fuck are you " + (losses - wins) + " games behind?"
                };
            }
            // still ahead
            else {
                messages = new String[]{
                        "Pathetic, but at least you're " + (wins - losses) + (wins - losses == 1 ? " win" : " wins") + " ahead cunts",
                        "Don't you dare fall behind cunts",
                        "We lost that round but it's not over yet, you get ready for the next one"
                };
            }
        }

        String message = null;

        while(message == null || message.equals(lastMessage)) {
            message = messages[rand.nextInt(messages.length)];
        }

        lastMessage = message;

        return lastMessage;
    }

    /**
     * Undo the last game update
     */
    private void undoLast() {
        if(matchHistory.size() == 0) {
            return;
        }
        Gunfight prev = matchHistory.pop();
        this.wins = prev.getWins();
        this.losses = prev.getLosses();
        this.streak = prev.getStreak();
        this.lastUpdate = prev.getLastUpdate();
        this.rank = prev.getRank();
        updateMessage();
    }

    public int getRank(){
        return rank;
    }

    /**
     * Get current wins
     *
     * @return Current wins
     */
    public int getWins() {
        return wins;
    }

    /**
     * Get current losses
     *
     * @return Current losses
     */
    public int getLosses() {
        return losses;
    }

    /**
     * Get time of last update
     *
     * @return Time of last update
     */
    public int getStreak() {
        return streak;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Generate a message showing how the gunfight game is played
     *
     * @return Ready to send/edit help message
     */
    private EmbedBuilder buildHelpMessage() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(65280);
        builder.setTitle("GUNFIGHT HELP");
        builder.addField("BASICS", "Call **gunfight!** to begin a gunfight session.\n\nThe session will run until it is submitted to the **leaderboard!**\n\nOnly the user who called **gunfight!** can control the session.", false);
        builder.addField("HOW TO USE", "CLICK THE EMOTES", false);
        builder.setThumbnail(thumb);
        return builder;
    }

    /**
     * Send the gunfight help message to the channel
     */
    private void showHelpMessage() {
        sendGameMessage(buildHelpMessage().build());
    }

    /**
     * Update the help message to display info on the pressed emote
     */
    public void updateHelpMessage(MessageReaction reaction) {
        Emote react = reaction.getReactionEmote().getEmote();
        String purpose = getEmoteFunction(react);
        Message helpMessage = getGameMessage();
        EmbedBuilder update = buildHelpMessage();
        update.addBlankField(false);
        update.setFooter(purpose, react.getImageUrl());
        helpMessage.editMessage(update.build()).complete();
    }

    /**
     * Get a description of what the emote does to the gunfight session
     *
     * @param e Emote to check
     * @return Description of what the emote does
     */
    private String getEmoteFunction(Emote e) {
        String desc;
        if(e == win) {
            desc = "Add a win to the gunfight session.";
        }
        else if(e == loss) {
            desc = "Add a loss to the gunfight session.";

        }
        else if(e == undo) {
            desc = "Undo the last update to the gunfight session.";

        }
        else if(e == stop) {
            desc = "End the gunfight session and submit the score to the leaderboard.";
        }
        else {
            desc = "Nothing you fucking idiot, that's why it isn't on there.";
        }
        return desc;
    }
}
