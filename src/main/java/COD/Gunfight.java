package COD;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Interactive win/loss tracker - use emotes to track score and submit to leaderboard
 */
public class Gunfight {

    private MessageChannel channel;
    private Emote win, loss, stop, undo;
    private int wins = 0, losses = 0, currentStreak = 0, rank = 0, longestStreak = 0;
    private Guild server;
    private long lastUpdate = 0, startTime = 0, gameID;
    private String lastMessage;
    private LinkedList<Gunfight> matchUpdateHistory;
    private ArrayList<Session> leaderboard;

    // If !game message has been replaced with game summary message
    private boolean active;

    // User who started game, only user allowed to register score
    private User owner;

    /**
     * Constructor to begin gunfight session
     *
     * @param channel Text channel to play in
     * @param server  Guild to find emotes
     * @param owner   User who started game
     */
    public Gunfight(MessageChannel channel, Guild server, User owner) {
        this.channel = channel;
        this.server = server;
        this.owner = owner;
        this.active = true;
        matchUpdateHistory = new LinkedList<>();
        leaderboard = Session.getHistory();
    }

    /**
     * Constructor for help message
     *
     * @param channel Channel to post in
     * @param server  Guild for emotes
     */
    public Gunfight(MessageChannel channel, Guild server) {
        this.channel = channel;
        this.server = server;

        if(checkEmotes()) {
            showHelpMessage();
        }
    }

    /**
     * Constructor to start a session with given score values
     *
     * @param channel       Text channel to play in
     * @param server        Guild to find emotes
     * @param owner         User who started game
     * @param wins          wins to start with
     * @param losses        losses to start with
     * @param currentStreak current streak to set
     * @param longestStreak longest streak to set
     */
    public Gunfight(MessageChannel channel, Guild server, User owner, int wins, int losses, int currentStreak, int longestStreak) {
        this(channel, server, owner);
        this.wins = wins;
        this.losses = losses;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
    }

    /**
     * Constructor to keep a history of game score for undo purposes
     *
     * @param wins          Current wins
     * @param losses        Current losses
     * @param currentStreak Current streak
     * @param lastUpdate    Time of update
     */
    public Gunfight(int wins, int losses, int currentStreak, long lastUpdate, int rank, int longestStreak) {
        this.wins = wins;
        this.losses = losses;
        this.currentStreak = currentStreak;
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
     * Attempts to locate emotes required for game
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
     * Get the colour to use in the embedded message based on score
     *
     * @return int decimal colour
     */
    private int getColour() {
        if(wins == losses) {
            return 16776960; // YELLOW
        }
        else if(wins < losses) {
            return 16711680; // RED
        }
        else {
            return 65280; // Green
        }
    }

    private String getThumbnail() {

        String[] goodThumb = new String[]{
                "https://bit.ly/2YTzfTQ", // Default price
                "https://bnetcmsus-a.akamaihd.net/cms/blog_header/pv/PV106AQCOXG41591752563326.jpg", // Price in smoke
                "https://i.imgur.com/W3nY6AF.jpg", // Happy price
                "https://vignette.wikia.nocookie.net/callofduty/images/7/71/Pillar3.jpg/revision/latest?cb=20191004172714", // Happy price
                "https://static1.gamerantimages.com/wordpress/wp-content/uploads/2020/05/call-of-duty-modern-warfare-nuke-victory-screen.jpg", // VICTORY
        };

        String[] badThumb = new String[]{
                "https://i.imgur.com/AHtBYyn.jpg", // Sad price
                "https://i.ytimg.com/vi/ONzIHOxtQws/maxresdefault.jpg", // Ghost dying
                "https://vignette.wikia.nocookie.net/callofduty/images/c/c5/Ghost%27s_death_Shepherd_Loose_Ends_MW2.png/revision/latest?cb=20120121101525", // Ghost dying
                "https://i.imgur.com/ZgHmHY2.png" // DEFEAT
        };

        Random rand = new Random();
        if(wins == 0 && losses == 0) {
            return "https://bit.ly/2YTzfTQ";// Going dark cunt
        }
        if(wins > losses) {
            return goodThumb[rand.nextInt(goodThumb.length)];
        }
        return badThumb[rand.nextInt(badThumb.length)];
    }

    /**
     * Builds the game message. Called to begin game and build new messages as score is added.
     *
     * @return Game message
     */
    private MessageEmbed buildGameMessage(String streak, String longestStreak) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(getColour());

        String title = "GUNFIGHT";
        if(rank > 0) {
            title += " RANK " + rank;
        }

        builder.setTitle(title);
        builder.setDescription(createDesc());
        builder.setThumbnail(getThumbnail());
        builder.setImage("https://i.imgur.com/24Xf03H.png");
        builder.addField("**WIN**", String.valueOf(wins), true);
        builder.addBlankField(true);
        builder.addField("**LOSS**", String.valueOf(losses), true);
        builder.addField("**STREAK**", streak, true);
        builder.addBlankField(true);
        builder.addField("**LONGEST STREAK**", longestStreak, true);
        String footer;
        String suffix = " -- Checkout 'gunfight help!' for instructions";

        if(lastUpdate == 0) {
            footer = "Game started at " + formatTime(startTime);
        }
        else {
            footer = "Last update at " + formatTime(lastUpdate);
        }

        builder.setFooter(footer + suffix, "https://i.imgur.com/rVhdoRs.gif"); // Spinning clock gif
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
    public void startGame() {
        if(!checkEmotes()) {
            channel.sendMessage("This server needs emotes named \"victory\", \"defeat\", \"stop\", and \"undo\" to play gunfight cunt.").queue();
            return;
        }
        startTime = System.currentTimeMillis();
        rank = checkRank();
        sendGameMessage(buildGameMessage(String.valueOf(currentStreak), String.valueOf(longestStreak)));
    }

    /**
     * Remove the game message
     */
    public void deleteGame() {
        System.out.println("Deleting game message...");
        channel.retrieveMessageById(gameID).queue(message -> {
            System.out.println("Message has been deleted...");
            message.delete().queue();
        });
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

        // Before adding the win/loss, add to history for undo purposes
        matchUpdateHistory.push(new Gunfight(wins, losses, currentStreak, lastUpdate, rank, longestStreak));

        // Now do update

        lastUpdate = currentTime;

        if(emote == win) {
            addWin();
        }
        else {
            addLoss();
        }
        this.rank = checkRank();
        updateMessage();
    }

    /**
     * Check the rank of the current session
     */
    private int checkRank() {
        ArrayList<Session> leaderboard = new ArrayList<>(this.leaderboard);
        Session current = new Session(startTime, lastUpdate, wins, losses, longestStreak);
        leaderboard.add(current);
        Session.sortSessions(leaderboard, true); // Sort exactly as leaderboard does
        return (leaderboard.indexOf(current)) + 1;
    }

    /**
     * Update the game message to display new score
     */
    private void updateMessage() {
        channel.retrieveMessageById(gameID).queue(message -> {
            MessageEmbed updateMessage = createUpdateMessage();
            if(gameFocused()) {
                message.editMessage(updateMessage).queue();
            }
            else {
                deleteGame();
                sendGameMessage(updateMessage);
            }
        });
    }

    /**
     * Move the game back to the most recent message
     */
    public void relocate() {
        System.out.println("Checking if game is most recent message...");
        if(!gameFocused()) {
            System.out.println("Game is not most recent message...");
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
        String win = this.currentStreak == 1 ? " WIN" : " WINS";
        String loss = this.currentStreak == -1 ? " LOSS" : " LOSSES";

        // Streak message to display, a negative streak should show as 2 losses not -2 losses
        String streak = this.currentStreak < 0 ? Math.abs(this.currentStreak) + loss : this.currentStreak + win;

        win = this.longestStreak == 1 ? " WIN" : " WINS";

        String longestStreak = this.longestStreak + win;

        return buildGameMessage(streak, longestStreak);
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
        System.out.println("Sending game message...");
        channel.sendMessage(gameMessage).queue(message -> {
            System.out.println("Game message has been sent...\n");
            gameID = message.getIdLong();
            message.addReaction(win).queue();
            message.addReaction(loss).queue();
            message.addReaction(undo).queue();
            message.addReaction(stop).queue();
        });
    }

    /**
     * Add a win to the scoreboard, reset streak if on a loss streak.
     */
    private void addWin() {
        if(currentStreak < 0) {
            currentStreak = 0;
        }
        wins++;
        currentStreak++;

        // Keep track of the largest win streak of the session
        if(currentStreak > longestStreak) {
            longestStreak = currentStreak;
        }

        System.out.println("\nWin reaction added: " + formatTime(lastUpdate) + " " + wins + "/" + losses);
    }

    /**
     * Add a loss to the scoreboard, reset streak if on a win streak.
     */
    private void addLoss() {
        if(currentStreak > 0) {
            currentStreak = 0;
        }
        losses++;
        currentStreak--;
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
    public static String getThumb() {
        return "https://bit.ly/2YTzfTQ"; // Default price
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
        builder.setThumbnail(getThumbnail());
        builder.setDescription(getRankingMessage());
        builder.setFooter("Check out the leaderboard!", null);
        builder.addField("**DURATION**", session.getDuration(), false);
        builder.addField("**WINS**", String.valueOf(wins), true);
        builder.addField("**LOSSES**", String.valueOf(losses), true);
        builder.addField("**RATIO**", String.valueOf(session.formatRatio()), true);
        builder.addField("**LONGEST STREAK**", session.formatStreak(), false);
        return builder.build();
    }

    /**
     * Get a message to display in the game summary message based on the rank of the finished session
     *
     * @return Congratulatory message based on the rank of the finished session
     */
    private String getRankingMessage() {
        String result;
        int rank = checkRank();
        if(rank == 1) {
            result = "That's a new personal best, nice work cunts!";
        }
        else if(rank <= 5) {
            result = "Rank " + rank + ", you're now in the top 5!";
        }
        else if(rank <= 10) {
            result = "Rank " + rank + ", you're now in the top 10, who cares!";
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
        String mvp = (owner.getName().charAt(owner.getName().length() - 1)) == 's' ? owner.getName() + "'" : owner.getName() + "'s";

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
        else if(currentStreak >= 1) {

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
            else if(wins - losses >= 5) {
                messages = new String[]{
                        (wins - losses) + " wins ahead!, amazing work!",
                        owner.getName() + ", you absolutely carried the team that game! Nice job",
                        "It's clobbering time!, You clobbered them that game!",
                        "Beautiful, I especially enjoyed " + mvp + " performance, it was incredible!",
                        "The guy who made Downturn would be proud",
                        "The sky is the limit fellas! Great work!",
                        "What an amazing ratio!",
                        "Straight to the top of the leaderboard!",
                        "With these upgrades, they never stood a chance!",
                        "Joe is reaching for the big red button, stay frosty",
                        "It's not camping if you're sniping!",
                        "Riggs would be proud, good job",
                        "Keep it pure, fantastic work!"
                };
            }

            // win streak
            else if(wins - losses > 1) {
                messages = new String[]{
                        "Nice streak!",
                        "What a lead!",
                        "Unstoppable!",
                        "They never stood a chance!",
                        "Take it easy on them!",
                        "You clobbered them! Nice job!",
                        "Beautiful job cunts, I loved that bit where that guy did that thing and you won!",
                        "That game was intense, fantastic work!",
                        "On a roll!",
                        "HERE COMES JOE",
                        "Beautiful, I especially enjoyed " + mvp + " performance, it was incredible!"
                };
            }

            // still behind
            else {
                int behind = (losses - wins) + 1;
                messages = new String[]{
                        "You're still behind cunts, not good enough",
                        "You still need " + behind + " wins to pass them cunts",
                        behind + " more of those and you'll be in the lead, great job!",
                        behind - 1 + " more and you're even, nice work!",
                        "You can do it! You're beginning to believe!"
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
                        "You've fallen behind, pick up your game cunts",
                        "Is this the start of a loss streak?"
                };
            }

            else if(losses - wins >= 5) {
                messages = new String[]{
                        (losses - wins) + " losses behind, holy fuck",
                        "Did you spam click the defeat button by accident?",
                        "Surely you can't be this shit at the game",
                        "This is an unbelievably shit performance cunts",
                        "I've never seen such a terrible job",
                        "Fuck me, aNOTHER one",
                        "ANOTHER ONE",
                        "You're gonna wear out the defeat button at this rate",
                        "Going for the world record?",
                        "You cunts are getting clobbered",
                        "Hold on, Guinness is on the line, you've just broken the world record for being shit at the game"
                };
            }
            // loss streak
            else if(losses - wins > 1) {
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
                        "How the fuck are you " + (losses - wins) + " games behind?",
                        "I fucking hate aim assist",
                        "Beep beep downies coming through, shit effort cunts",
                        "Get your head in the game!",
                        "I miss chester",
                        "The more things change the more they stay the same",
                        "Where's Rory when you need him?",
                        "Sometimes you just need to swallow your pride and gear up in your M4 & 725 loadout",
                        "Stop runecrafting and start winning cunts",
                        "Put the fuckin crossbow away and get out your 725",
                        "Just remember, chances are they are sober and you are not, don't be too hard on yourselves",
                        "Time to be tactical cunts, get those broken guns out"
                };
            }
            // still ahead
            else {
                messages = new String[]{
                        "Pathetic, but at least you're " + (wins - losses) + (wins - losses == 1 ? " win" : " wins") + " ahead cunts",
                        "Don't you dare fall behind cunts",
                        "We lost that round but it's not over yet, you get ready for the next one",
                        "You're still ahead, don't let this be a repeat of last time",
                        "Oh no here we go again",
                        "Get the 725 out and get your lead back up"
                };
            }
        }

        String message = null;

        while(message == null || message.equals(lastMessage)) {
            message = messages[rand.nextInt(messages.length)];
        }

        lastMessage = message;

        return message;
    }

    /**
     * Undo the last game update
     */
    private void undoLast() {
        if(matchUpdateHistory.size() == 0) {
            return;
        }
        Gunfight prev = matchUpdateHistory.pop();
        this.wins = prev.getWins();
        this.losses = prev.getLosses();
        this.currentStreak = prev.getCurrentStreak();
        this.longestStreak = prev.getLongestStreak();
        this.lastUpdate = prev.getLastUpdate();
        this.rank = prev.getRank();
        updateMessage();
    }

    public int getRank() {
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
    public int getCurrentStreak() {
        return currentStreak;
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
        builder.addField("CUSTOM SCORE", "Call **gunfight! [wins] [losses] [current streak] [longest streak]** to begin a gunfight session with a custom score.", false);
        builder.addField("HOW TO USE", "CLICK THE EMOTES", false);
        builder.setThumbnail(getThumb());
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
        channel.retrieveMessageById(gameID).queue(message -> {
            EmbedBuilder update = buildHelpMessage();
            update.addBlankField(false);
            update.setFooter(purpose, react.getImageUrl());
            message.editMessage(update.build()).queue();
        });
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

    public int getLongestStreak() {
        return longestStreak;
    }
}
