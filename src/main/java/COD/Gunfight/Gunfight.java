package COD.Gunfight;

import Bot.DiscordBot;
import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

import java.text.SimpleDateFormat;
import java.util.*;

import COD.Gunfight.GameStatus.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

/**
 * Interactive win/loss tracker for Modern Warfare - use buttons to track score and submit to leaderboard
 */
public class Gunfight {
    public static final String THUMBNAIL = "https://bit.ly/2YTzfTQ";
    private final MessageChannel channel;
    private long ownerUserId, ownerMemberId;
    private final Button win, loss, stop, undo;
    private final GunfightScore score;
    private final LinkedList<GunfightScore> matchUpdateHistory;
    private final ArrayList<Session> leaderboard;
    private final JDA jda;
    private final GunfightCallback callback;
    private String lastMessage;
    private long startTime = 0, gameID;
    private boolean active;

    /**
     * Constructor to begin gunfight session
     *
     * @param channel     Text channel to play in
     * @param member      Member who started the game
     * @param emoteHelper Emote helper
     * @param jda         JDA for resolving owner
     * @param callback    Interface for callback events
     */
    public Gunfight(MessageChannel channel, Member member, EmoteHelper emoteHelper, JDA jda, GunfightCallback callback) {
        this.channel = channel;
        setOwner(member);
        this.score = new GunfightScore();
        this.matchUpdateHistory = new LinkedList<>();
        this.leaderboard = Session.getHistory();
        this.jda = jda;
        this.callback = callback;
        this.win = Button.success("win", "Victory");
        this.loss = Button.danger("loss", "Defeat");
        this.stop = Button.danger("stop", Emoji.fromEmote(emoteHelper.getStopWhite()));
        this.undo = Button.primary("undo", Emoji.fromEmote(emoteHelper.getUndo()));
    }

    /**
     * Builds the game message. Called to begin game and build new messages as score is added.
     *
     * @return Game message
     */
    private MessageEmbed buildGameMessage(String streak, String longestStreak) {
        String footer = score.getLastUpdate() == 0
                ? "Game started at " + formatTime(startTime)
                : "Last update at " + formatTime(score.getLastUpdate());
        return new EmbedBuilder()
                .setColor(score.getColour())
                .setTitle(score.getRank() == 0 ? "GUNFIGHT" : "GUNFIGHT RANK " + score.getRank())
                .setDescription(createDesc())
                .setThumbnail(score.getThumbnail())
                .setImage(EmbedHelper.SPACER_IMAGE)
                .addField("**WIN**", String.valueOf(score.getWins()), true)
                .addBlankField(true)
                .addField("**LOSS**", String.valueOf(score.getLosses()), true)
                .addField("**STREAK**", streak, true)
                .addBlankField(true)
                .addField("**LONGEST STREAK**", longestStreak, true)
                .addField("**BUTTON MANAGER**", DiscordBot.getUserMention(jda, ownerUserId), false)
                .setFooter(footer, EmbedHelper.CLOCK_GIF)
                .build();
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
        startTime = System.currentTimeMillis();
        active = true;
        sendGameMessage(createGameMessage());
    }

    /**
     * Check if the gunfight is active (not displaying game summary message)
     *
     * @return Gunfight is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Check if the button clicked is for a win or loss & update score appropriately
     *
     * @param event Button click event from owner
     */
    public void buttonClicked(ButtonClickEvent event) {
        long currentTime = System.currentTimeMillis();
        String buttonId = event.getComponentId();

        if(!isValidButton(buttonId)) {
            return;
        }

        if(buttonId.equals(stop.getId())) {
            stopGame(event);
            return;
        }

        score.setLastUpdate(currentTime);

        if(buttonId.equals(undo.getId())) {
            undoLast(event);
            return;
        }

        // Before adding the win/loss, add to history for undo purposes
        matchUpdateHistory.push(new GunfightScore(score));

        if(buttonId.equals(win.getId())) {
            score.addWin();
        }
        else {
            score.addLoss();
        }
        score.setRank(checkRank());
        updateMessage(event);
    }

    /**
     * Check if the given button ID is a valid button to control the gunfight
     *
     * @param buttonId ID of button
     * @return Button is valid
     */
    private boolean isValidButton(String buttonId) {
        return buttonId.equals(win.getId()) || buttonId.equals(loss.getId()) || buttonId.equals(undo.getId()) || buttonId.equals(stop.getId());
    }

    /**
     * Check the rank of the current session
     */
    private int checkRank() {
        ArrayList<Session> leaderboard = new ArrayList<>(this.leaderboard);
        Session current = new Session(startTime, score);
        leaderboard.add(current);
        Session.sortSessions(leaderboard, true); // Sort exactly as leaderboard does
        return (leaderboard.indexOf(current)) + 1;
    }

    /**
     * Update the game message to display new score
     *
     * @param event Button click event to acknowledge
     */
    private void updateMessage(ButtonClickEvent event) {
        MessageEmbed updateMessage = createGameMessage();
        if(gameFocused()) {
            event.deferEdit().setEmbeds(updateMessage).setActionRows(getButtons()).queue();
        }
        else {
            event.deferEdit().queue();
            channel.deleteMessageById(gameID).queue();
            sendGameMessage(updateMessage);
        }
    }

    /**
     * Move the game back to the most recent message
     */
    public void relocate() {
        channel.deleteMessageById(gameID).queue();
        sendGameMessage(createGameMessage());
    }

    /**
     * Stringify score values and return game message
     *
     * @return Game message
     */
    private MessageEmbed createGameMessage() {
        int currentStreak = score.getCurrentStreak();
        int longestStreak = score.getLongestStreak();

        // Which form of win/wins, loss/losses to use if more than 1
        String win = currentStreak == 1 ? " WIN" : " WINS";
        String loss = currentStreak == -1 ? " LOSS" : " LOSSES";

        // Streak message to display, a negative streak should show as 2 losses not -2 losses
        String streak = currentStreak < 0 ? Math.abs(currentStreak) + loss : currentStreak + win;

        win = longestStreak == 1 ? " WIN" : " WINS";

        return buildGameMessage(streak, longestStreak + win);
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
     * Once the message is sent, pass the session & old message ID to the callback
     *
     * @param gameMessage Interactive game message
     */
    private void sendGameMessage(MessageEmbed gameMessage) {
        channel.sendMessage(gameMessage).setActionRows(getButtons()).queue(message -> {
            long oldId = gameID;
            gameID = message.getIdLong();
            callback.messageIdUpdated(this, oldId);
        });
    }

    /**
     * Get the action row of buttons to use in the message
     *
     * @return Buttons to use
     */
    private ActionRow getButtons() {
        return ActionRow.of(
                win,
                loss,
                matchUpdateHistory.isEmpty() ? undo.asDisabled() : undo,
                stop
        );
    }

    /**
     * Get ID of the game message
     *
     * @return Game message ID
     */
    public long getGameId() {
        return gameID;
    }

    /**
     * Get the user ID of the game owner
     *
     * @return Owner user ID
     */
    public long getOwnerUserId() {
        return ownerUserId;
    }

    /**
     * Get the member ID of the game owner
     *
     * @return Owner member ID
     */
    public long getOwnerMemberId() {
        return ownerMemberId;
    }

    /**
     * Set the owner of the session
     *
     * @param owner Member to control the session
     */
    public void setOwner(Member owner) {
        this.ownerMemberId = owner.getIdLong();
        this.ownerUserId = owner.getUser().getIdLong();
    }

    /**
     * Finish the game and commit the score to the database
     *
     * @param event Button click event
     */
    private void stopGame(ButtonClickEvent event) {
        event.deferEdit().queue();
        this.active = false;
        channel.deleteMessageById(gameID).queue();

        // Don't submit empty games
        if(score.getWins() == 0 && score.getLosses() == 0) {
            return;
        }
        Session session = new Session(startTime, score);
        session.submitGame();
        channel.sendMessage(buildGameSummaryMessage(session)).queue();
    }

    /**
     * Builds the game summary message, display the final results of the game
     *
     * @return Game summary message
     */
    private MessageEmbed buildGameSummaryMessage(Session session) {
        return new EmbedBuilder()
                .setColor(score.getColour())
                .setTitle("GUNFIGHT RESULTS #" + (Session.getTotalMatches()))
                .setThumbnail(score.getThumbnail())
                .setDescription(getRankingMessage())
                .setFooter("Check out the leaderboard!", null)
                .addField("**DURATION**", session.getDuration(), false)
                .addField("**WINS**", String.valueOf(score.getWins()), true)
                .addField("**LOSSES**", String.valueOf(score.getLosses()), true)
                .addField("**RATIO**", String.valueOf(session.getFormattedRatio()), true)
                .addField("**LONGEST STREAK**", session.formatStreak(), false)
                .build();
    }

    /**
     * Get a message to display in the game summary message based on the rank of the finished session
     *
     * @return Congratulatory message based on the rank of the finished session
     */
    private String getRankingMessage() {
        int rank = score.getRank();
        String result;
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
        String[] messages = new String[]{};

        String ownerName = DiscordBot.getUserName(jda, ownerUserId);
        String mvp;
        if(ownerName.equals(DiscordBot.UNKNOWN_USER)) {
            mvp = ownerName;
        }
        else {
            mvp = (ownerName.charAt(ownerName.length() - 1)) == 's'
                    ? ownerName + "'"
                    : ownerName + "'s";
        }

        int wins = score.getWins();
        int losses = score.getLosses();
        GAME_STATUS status = new GameStatus(wins, losses, score.getCurrentStreak()).getGameStatus();

        switch(status) {
            case BEGINNING:
                messages = new String[]{
                        "Let's do this!",
                        "Good news, the cheque cleared... It's go time!",
                        "Shut up, cock in and load up.",
                        "Lock 'em and load 'em ladies, it's go time.",
                        "Keep your head up and your ass down, it's go time."
                };
                break;
            case TIE:
                messages = new String[]{
                        "Neck and neck, pick up your game cunts!",
                        "You better not fall behind",
                        "Get that lead back!"
                };
                break;
            case WON_OPENING_GAME:
                messages = new String[]{
                        "Off to a good start, how long before you fuck it up?",
                        "Nice job spastics!",
                        "Bon boulot, get ready for the next round",
                        "Let's get a streak going!",
                };
                break;
            case WON_TAKEN_LEAD:
                messages = new String[]{
                        "Now Maintain that lead cunts!",
                        "Nice work, you're back in the game!",
                        "What a cum back!"
                };
                break;
            case FIVE_OR_MORE_WIN_AHEAD:
                messages = new String[]{
                        (wins - losses) + " wins ahead!, amazing work!",
                        ownerName + ", you absolutely carried the team that game! Nice job",
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
                        "Keep it pure, fantastic work!",
                        "HERE COMES JOE"
                };
                break;
            case STANDARD_WIN:
                messages = new String[]{
                        "Nice job!",
                        "What a lead!",
                        "Unstoppable!",
                        "They never stood a chance!",
                        "Take it easy on them!",
                        "You clobbered them! Nice job!",
                        "Beautiful job cunts, I loved that bit where that guy did that thing and you won!",
                        "That game was intense, fantastic work!",
                        "Beautiful, I especially enjoyed " + mvp + " performance, it was incredible!"
                };
                break;
            case WON_STILL_BEHIND:
                int behind = (losses - wins) + 1;
                messages = new String[]{
                        "You're still behind cunts, not good enough",
                        "You still need " + behind + " wins to pass them cunts",
                        behind + " more of those and you'll be in the lead, great job!",
                        behind - 1 + " more and you're even, nice work!",
                        "You can do it! You're beginning to believe!"
                };
                break;
            case LOST_OPENING_GAME:
                messages = new String[]{
                        "Off to a terrible start, why don't you just stop now?",
                        "What an amazing start!",
                        "That was only a warm up",
                        "Let's get a negative streak going!",
                };
                break;
            case LOST_LOSING_LEAD:
                messages = new String[]{
                        "You're behind cunts",
                        "You're behind, you better win the fucking next one",
                        "You've fallen behind, pick up your game cunts",
                        "Is this the start of a loss streak?"
                };
                break;
            case FIVE_OR_MORE_LOSS_BEHIND:
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
                break;
            case STANDARD_LOSS:
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
                        "Put the fucking crossbow away and get out your 725",
                        "Just remember, chances are they are sober and you are not, don't be too hard on yourselves",
                        "Time to be tactical cunts, get those broken guns out"
                };
                break;
            case LOST_STILL_AHEAD:
                messages = new String[]{
                        "Pathetic, but at least you're " + (wins - losses) + (wins - losses == 1 ? " win" : " wins") + " ahead cunts",
                        "Don't you dare fall behind cunts",
                        "We lost that round but it's not over yet, you get ready for the next one",
                        "You're still ahead, don't let this be a repeat of last time",
                        "Oh no here we go again",
                        "Get the 725 out and get your lead back up"
                };
                break;
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
     *
     * @param event Button click event
     */
    private void undoLast(ButtonClickEvent event) {
        if(matchUpdateHistory.size() == 0) {
            return;
        }
        score.replaceValues(matchUpdateHistory.pop());
        updateMessage(event);
    }

    /**
     * Get the timestamp of the last update
     *
     * @return Timestamp of last update
     */
    public long getLastUpdate() {
        return score.getLastUpdate();
    }

    /**
     * Interface for Gunfight events
     */
    public interface GunfightCallback {
        /**
         * Called when the game message ID has changed.
         *
         * @param updated      Updated gunfight session - contains new message ID
         * @param oldMessageId Old message ID
         */
        void messageIdUpdated(Gunfight updated, long oldMessageId);
    }
}
