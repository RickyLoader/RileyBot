package Poll;

import Command.Structure.*;
import Command.Structure.PageableTableEmbed.IncorrectQuantityException;
import Countdown.Countdown;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.api.interactions.button.Button;
import net.dv8tion.jda.api.interactions.button.ButtonStyle;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Poll which can be voted on via buttons - runs for 2 minutes
 */
public class Poll {
    private static final int MIN_ITEMS = 2, MAX_ITEMS = 5;
    private final MessageChannel channel;
    private final LinkedHashMap<String, PollAnswer> answers; // Button ID -> answer
    private final LinkedHashMap<String, Button> buttons; // Button ID -> Button
    private final TimerTask pollTimer;
    private final ProgressBar yellowBar, redBar, greenBar;
    private final Emoji winning;
    private final String title;
    private long messageId, startTime, endTime;
    private int totalVotes, highestVotes;
    private boolean running;


    /**
     * Create the poll
     *
     * @param channel     Channel where poll will take place
     * @param items       Poll items
     * @param title       Title of the poll
     * @param jda         JDA for registering button listener
     * @param emoteHelper Emote helper to build progress bars and indicate winning answers on the buttons
     * @throws IncorrectQuantityException If too few/many answers are provided
     */
    public Poll(MessageChannel channel, String[] items, String title, JDA jda, EmoteHelper emoteHelper) throws IncorrectQuantityException {
        this.channel = channel;
        this.buttons = new LinkedHashMap<>();
        this.answers = createPollAnswers(items);
        this.title = title;
        this.pollTimer = new TimerTask() {
            @Override
            public void run() {
                running = false;
                stop();
            }
        };
        this.totalVotes = 0;
        this.highestVotes = 0;
        this.redBar = createProgressBar(emoteHelper.getRedProgressBar());
        this.yellowBar = createProgressBar(emoteHelper.getYellowProgressBar());
        this.greenBar = createProgressBar(emoteHelper.getGreenProgressBar());
        this.winning = Emoji.ofEmote(emoteHelper.getTrophy());
        jda.addEventListener(new ButtonListener() {
            @Override
            public void handleButtonClick(@NotNull ButtonClickEvent event) {
                String selected = event.getComponentId();
                if(!running || !answers.containsKey(selected) || event.getMessageIdLong() != messageId) {
                    return;
                }
                totalVotes++;
                int votes = answers.get(selected).incrementVotes();
                if(votes > highestVotes) {
                    highestVotes = votes;
                }
                refreshPollMessage(event);
            }
        });
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
        sendPollMessage();
    }

    /**
     * Stop the poll and send the results
     */
    public void stop() {
        if(running) {
            running = false;
            pollTimer.cancel();
        }
        endTime = System.currentTimeMillis();
        relocateMessage();
    }

    /**
     * Create a map of button ID -> answer used for voting in the poll
     * Also store the created buttons in a global map
     * Duplicate answers are removed
     *
     * @param items Items to map to buttons
     * @return Ordered map of button ID -> answer
     * @throws IncorrectQuantityException If too few/many answers are provided
     */
    private LinkedHashMap<String, PollAnswer> createPollAnswers(String[] items) throws IncorrectQuantityException {
        LinkedHashMap<String, PollAnswer> answers = new LinkedHashMap<>();
        HashSet<String> uniqueAnswers = new HashSet<>();
        for(int i = 0; i < items.length; i++) {
            String item = items[i].trim();
            if(uniqueAnswers.contains(item)) {
                continue;
            }
            uniqueAnswers.add(item);
            PollAnswer answer = new PollAnswer(item);
            Button answerButton = Button.primary(String.valueOf(i), answer.getTitle());

            this.buttons.put(answerButton.getId(), answerButton);
            answers.put(
                    answerButton.getId(),
                    answer
            );
        }

        if(answers.size() > MAX_ITEMS) {
            throw new IncorrectQuantityException("Let's keep it to **" + MAX_ITEMS + "** items at the max bro.");
        }

        if(answers.size() < MIN_ITEMS) {
            String err = "I need at least **" + MIN_ITEMS + "** items to start a poll!";

            // Duplicate answers were cut
            if(uniqueAnswers.size() < items.length) {
                int duplicateAnswers = items.length - uniqueAnswers.size();
                err += " (I removed **" + duplicateAnswers + "** duplicates)";
            }
            throw new IncorrectQuantityException(err);
        }
        return answers;
    }

    /**
     * Get the time remaining of the poll in mm:ss
     *
     * @return Time remaining
     */
    public String getTimeRemaining() {
        return Countdown.from(System.currentTimeMillis(), pollTimer.scheduledExecutionTime()).formatMinutesSeconds();
    }

    /**
     * Get the duration of the poll in mm:ss
     *
     * @return Running time
     */
    public String getDuration() {
        return Countdown.from(startTime, endTime).formatMinutesSeconds();
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
     *
     * @param event Button click event to acknowledge
     */
    public void refreshPollMessage(ButtonClickEvent event) {
        if(channel.getLatestMessageIdLong() != messageId || !running) {
            event.deferEdit().queue();
            relocateMessage();
            return;
        }
        event.deferEdit().setEmbeds(buildPollMessage()).setActionRows(getButtons()).queue();
    }

    /**
     * Delete and resend the poll message
     */
    public void relocateMessage() {
        if(messageId != 0) {
            channel.deleteMessageById(messageId).queue();
        }
        sendPollMessage();
    }

    /**
     * Send the poll message and add the voting buttons
     */
    private void sendPollMessage() {
        MessageAction sendMessage = channel.sendMessage(buildPollMessage());
        if(running) {
            sendMessage = sendMessage.setActionRows(getButtons());
        }
        sendMessage.queue(message -> {
            if(running) {
                this.messageId = message.getIdLong();
            }
        });
    }

    /**
     * Get the row of buttons for the current poll.
     * E.g if there are only 2 questions, the first two buttons will be used
     *
     * @return Row of buttons
     */
    private ActionRow getButtons() {
        ArrayList<Button> buttons = new ArrayList<>();
        for(String buttonId : this.buttons.keySet()) {
            buttons.add(getButtonWithStatus(buttonId));
        }
        return ActionRow.of(buttons);
    }

    /**
     * Get a button by its ID.
     * Return the button unchanged if the associated poll answer is not winning.
     * If the associated poll answer is currently one of the winning answers,
     * return a new button with the old values, but a green colour & trophy emoji attached.
     *
     * @param buttonId ID of button to get
     * @return Standard button or updated button if winning answer
     */
    private Button getButtonWithStatus(String buttonId) {
        PollAnswer answer = answers.get(buttonId);
        Button button = buttons.get(buttonId);
        if(!isWinningAnswer(answer)) {
            return button;
        }
        return new ButtonImpl(
                button.getId(),
                button.getLabel(),
                ButtonStyle.SUCCESS,
                false,
                winning
        );
    }

    /**
     * Check if the given poll answer is currently winning.
     * Multiple answers can be winning and this is denoted by having votes equal to the highest number of votes seen
     *
     * @param answer Answer to check
     * @return Answer is a winning answer
     */
    private boolean isWinningAnswer(PollAnswer answer) {
        return answer.getVotes() == highestVotes && highestVotes > 0;
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

        for(String buttonId : answers.keySet()) {
            PollAnswer option = answers.get(buttonId);
            int votes = option.getVotes();
            ProgressBar bar = running ? yellowBar : (isWinningAnswer(option) ? greenBar : redBar);
            int displayVotes = Math.min(totalEmotes, option.getVotes());
            String pollImage = bar.build(displayVotes, !running || displayVotes == totalEmotes)
                    + " "
                    + votes;

            if(votes > 0) {
                double percent = votes / (totalVotes / 100.0);
                pollImage += " (" + new DecimalFormat("#.##").format(percent) + "%)";
            }
            if(isWinningAnswer(option)) {
                pollImage += " " + winning.getAsMention();
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
