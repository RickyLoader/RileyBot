package Poll;

import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Command.Structure.EmoteListener;
import Command.Structure.ProgressBar;
import Countdown.Countdown;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Poll which can be voted on via emotes - runs for 2 minutes
 */
public class Poll {
    private final MessageChannel channel;
    private final LinkedHashMap<Emote, PollAnswer> answers;
    private final TimerTask pollTimer;
    private final EmoteListener voteListener;
    private final ProgressBar yellowBar, redBar, greenBar;
    private final String title;
    private long messageId, startTime, endTime, lastRefresh;
    private int totalVotes, highestVotes;
    private boolean running;


    /**
     * Create the poll
     *
     * @param channel     Channel where poll will take place
     * @param items       Poll items
     * @param title       Title of the poll
     * @param jda         JDA for registering emote listener
     * @param emoteHelper Emote helper for voting emotes
     */
    public Poll(MessageChannel channel, String[] items, String title, JDA jda, EmoteHelper emoteHelper) {
        this.channel = channel;
        this.answers = createPollAnswers(items, emoteHelper);
        this.title = title;
        this.voteListener = createVoteListener();
        this.pollTimer = new TimerTask() {
            @Override
            public void run() {
                running = false;
                stop(jda);
            }
        };
        this.totalVotes = 0;
        this.highestVotes = 0;
        this.redBar = createProgressBar(emoteHelper.getRedProgressBar());
        this.yellowBar = createProgressBar(emoteHelper.getYellowProgressBar());
        this.greenBar = createProgressBar(emoteHelper.getGreenProgressBar());
        jda.addEventListener(voteListener);
    }

    /**
     * Create a progress bar with the given array of emotes
     *
     * @param emotes Emotes -> start, section, end
     * @return Progress bar using provided emotes
     */
    private ProgressBar createProgressBar(Emote[] emotes) {
        return new ProgressBar(emotes[0], emotes[1], emotes[2]);
    }

    /**
     * Start the poll and initialise the end timer
     */
    public void start() {
        if(running) {
            return;
        }
        running = true;
        startTime = System.currentTimeMillis();
        Timer timer = new Timer();
        timer.schedule(
                pollTimer,
                120000
        );
        refreshPollMessage();
    }

    /**
     * Stop the poll
     * Remove the emote listener and send the results
     *
     * @param jda JDA to remove emote listener
     */
    public void stop(JDA jda) {
        if(running) {
            running = false;
            pollTimer.cancel();
        }
        endTime = System.currentTimeMillis();
        jda.removeEventListener(voteListener);
        refreshPollMessage();
    }

    /**
     * Create the emote listener used for voting in the poll
     *
     * @return Emote listener
     */
    private EmoteListener createVoteListener() {
        return new EmoteListener() {
            @Override
            public void handleReaction(MessageReaction reaction, User user, Guild guild) {
                Emote selected = reaction.getReactionEmote().getEmote();
                long currentTime = System.currentTimeMillis();
                if(!running || !answers.containsKey(selected) || reaction.getMessageIdLong() != messageId) {
                    return;
                }
                totalVotes++;
                int votes = answers.get(selected).incrementVotes();
                if(votes > highestVotes) {
                    highestVotes = votes;
                }
                if(lastRefresh != 0 && currentTime - lastRefresh < 2000) {
                    return;
                }
                lastRefresh = currentTime;
                refreshPollMessage();
            }
        };
    }

    /**
     * Create a map of emote -> answer used for voting in the poll
     *
     * @param items       Items to map to emotes
     * @param emoteHelper EmoteHelper for retrieving emotes
     * @return Ordered map of emote -> answer
     */
    private LinkedHashMap<Emote, PollAnswer> createPollAnswers(String[] items, EmoteHelper emoteHelper) {
        LinkedHashMap<Emote, PollAnswer> answers = new LinkedHashMap<>();
        Emote[] emotes = new Emote[]{
                emoteHelper.getPollOptionA(),
                emoteHelper.getPollOptionB(),
                emoteHelper.getPollOptionC(),
                emoteHelper.getPollOptionD()
        };

        for(int i = 0; i < items.length; i++) {
            answers.put(
                    emotes[i],
                    new PollAnswer(items[i].trim())
            );
        }
        return answers;
    }

    /**
     * Get the time remaining of the poll in mm:ss
     *
     * @return Time remaining
     */
    public String getTimeRemaining() {
        return formatCountdown(Countdown.from(System.currentTimeMillis(), pollTimer.scheduledExecutionTime()));
    }

    /**
     * Get the duration of the poll in mm:ss
     *
     * @return Running time
     */
    public String getDuration() {
        return formatCountdown(Countdown.from(startTime, endTime));
    }

    /**
     * Format the given countdown in to a String showing mm:ss
     *
     * @param countdown Countdown to format
     * @return Countdown formatted in String
     */
    private String formatCountdown(Countdown countdown) {
        DecimalFormat timeFormat = new DecimalFormat("00");
        return timeFormat.format(countdown.getMinutes()) + ":" + timeFormat.format(countdown.getSeconds());
    }

    /**
     * Format the given time to HH:mm:ss
     *
     * @param time Time to format (in ms)
     * @return Time formatted in String
     */
    private String formatTime(long time) {
        return new SimpleDateFormat("HH:mm:ss").format(time);
    }

    /**
     * Get the end time of the poll formatted as a String
     *
     * @return Poll end time
     */
    public String getEndTime() {
        return formatTime(pollTimer.scheduledExecutionTime());
    }

    /**
     * Check whether the poll is running
     *
     * @return Poll is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Refresh the poll message
     * Delete or edit the current message based on whether it is the most recent message
     */
    public void refreshPollMessage() {
        MessageEmbed pollMessage = buildPollMessage();
        if(messageId == 0 || channel.getLatestMessageIdLong() != messageId || !running) {
            channel.deleteMessageById(messageId)
                    .queue(sent -> sendMessage(pollMessage), err -> sendMessage(pollMessage));
            return;
        }
        channel.editMessageById(messageId, pollMessage).queue();
    }

    /**
     * Send the poll message and add the voting emotes
     *
     * @param pollMessage Poll message embed to send
     */
    private void sendMessage(MessageEmbed pollMessage) {
        channel.sendMessage(pollMessage).queue(message -> {
            if(!running) {
                return;
            }
            this.messageId = message.getIdLong();
            for(Emote emote : answers.keySet()) {
                message.addReaction(emote).queue();
            }
        });
    }

    /**
     * Build the poll message
     *
     * @return Poll message
     */
    private MessageEmbed buildPollMessage() {
        String clock = running ? EmbedHelper.CLOCK_GIF : EmbedHelper.CLOCK_STOPPED;
        final int totalEmotes = 8;

        EmbedBuilder builder = new EmbedBuilder()
                .setFooter(
                        getFooter(),
                        clock
                )
                .setColor(running ? EmbedHelper.YELLOW : EmbedHelper.GREEN)
                .setThumbnail("https://i.imgur.com/Vsjp0Og.png")
                .setTitle("Poll | " + title + " - " + totalVotes + " votes counted")
                .setImage(EmbedHelper.SPACER_IMAGE);

        for(Emote e : answers.keySet()) {
            PollAnswer option = answers.get(e);
            int votes = option.getVotes();
            ProgressBar bar = running ? yellowBar : (votes == highestVotes && highestVotes > 0 ? greenBar : redBar);
            int displayVotes = Math.min(totalEmotes, option.getVotes());
            String pollImage = EmoteHelper.formatEmote(e)
                    + " "
                    + bar.build(displayVotes, !running || displayVotes == totalEmotes)
                    + " "
                    + votes;

            if(votes > 0) {
                double percent = votes / (totalVotes / 100.0);
                pollImage += " (" + new DecimalFormat("#.##").format(percent) + "%)";
            }

            builder.addField(option.getTitle(), pollImage, false);
        }
        return builder.build();
    }

    /**
     * Get the footer text to use in the poll embed
     *
     * @return Footer text
     */
    private String getFooter() {
        String suffix = "Type: poll for help";
        if(running) {
            return "Ends at: " + getEndTime() + " | " + getTimeRemaining() + " remaining | " + suffix;
        }
        return "Ended at: " + formatTime(endTime) + " | Ran for: " + getDuration() + " | " + suffix;
    }
}
