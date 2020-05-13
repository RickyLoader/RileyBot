package COD;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Interactive win/loss tracker - use emotes to track score
 */
public class Gunfight {

    // ID of the game message, used to find the message in the channel
    private MessageChannel channel;
    private Emote win, loss, stop;
    private int wins, losses, streak, longestStreak = 0;
    private Guild server;
    private long lastUpdate, startTime = 0, gameID;
    private String lastMessage, endPoint;
    private static String thumb = "https://bit.ly/2YTzfTQ";
    private boolean active;

    // User who started game, only user allowed to register score
    private User owner;

    public Gunfight(MessageChannel channel, Guild server, User owner) {
        this.channel = channel;
        this.server = server;
        this.owner = owner;
        this.endPoint = "gunfight";
        this.active = true;
        if(checkEmotes()) {
            startGame();
        }
        else {
            channel.sendMessage("This server needs emotes named \"victory\", \"defeat\", and \"stop\" to play gunfight cunt.").queue();
        }
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

        if(victory.size() > 0 && defeat.size() > 0 && stop.size() > 0) {
            this.win = victory.get(0);
            this.loss = defeat.get(0);
            this.stop = stop.get(0);
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
        builder.setColor(15655767);
        builder.setTitle("GUNFIGHT");
        builder.setDescription(createDesc());
        builder.setThumbnail(thumb);
        builder.addField("**WIN**", String.valueOf(wins), true);
        builder.addField("**LOSS**", String.valueOf(losses), true);
        builder.addField("**STREAK**", streak, false);

        if(lastUpdate != 0) {
            builder.setFooter("Last update at " + formatUpdateTime(), null);
        }

        return builder.build();
    }

    /**
     * Pad out smaller description messages to prevent drastic shifts in game message width
     *
     * @param message The message to be padded
     * @return Message with invisible unicode padding
     */
    private String getPadding(String message) {
        for(int i = message.length(); i < 35; i++) {
            message += "\u2800";
        }
        return message;
    }

    /**
     * Format the last update time to display on the game message
     *
     * @return Current time
     */
    private String formatUpdateTime() {
        return new SimpleDateFormat("HH:mm:ss").format(lastUpdate);
    }

    /**
     * Send the game message to the channel to begin playing
     */
    private void startGame() {
        sendGameMessage(buildGameMessage(String.valueOf(streak)));
        startTime = System.currentTimeMillis();
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
        Emote emote = server.getEmotesByName(reaction.getReactionEmote().getName(), true).get(0);
        long currentTime = System.currentTimeMillis();

        if((emote != win && emote != loss && emote != stop)/* || (currentTime - lastUpdate < 1000)*/) {
            return;
        }
        if(emote == stop) {
            stopGame();
            return;
        }
        lastUpdate = currentTime;

        if(emote == win) {
            addWin();
        }
        else {
            addLoss();
        }
        updateMessage();
    }

    /**
     * Update the game message to display new score
     */
    private void updateMessage() {
        Message message = getGameMessage();

        // Which form of win/wins, loss/losses to use if more than 1
        String win = this.streak == 1 ? " WIN" : " WINS";
        String loss = this.streak == -1 ? " LOSS" : " LOSSES";

        // Streak message to display, a negative streak should show as 2 losses not -2 losses
        String streak = this.streak < 0 ? Math.abs(this.streak) + loss : this.streak + win;

        MessageEmbed update = buildGameMessage(streak);

        if(gameFocused()) {
            message.editMessage(update).complete();
        }
        else {
            deleteGame();
            sendGameMessage(update);
        }
    }

    /**
     * Find the game message
     *
     * @return Game message
     */
    private Message getGameMessage() {
        return channel.getMessageById(gameID).complete();
    }

    /**
     * Game message is the most recent channel message
     *
     * @return Game message most recent channel message
     */
    private boolean gameFocused() {
        return channel.getLatestMessageIdLong() == gameID;
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
        builder.addField("**RATIO**", String.valueOf(session.getRatio()), true);
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

        return getPadding(lastMessage);
    }
}
