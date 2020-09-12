package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * An embed to track the progress of a given task
 */
public class EmbedLoadingMessage {
    private final MessageChannel channel;
    private long id, startTime, currentTime;
    private final String title, thumbnail, helpMessage;
    private String desc;
    private final EmoteHelper emoteHelper;
    private final ArrayList<LoadingStage> stages;
    private int currentStep;
    private boolean finished, failed;

    /**
     * Create a loading message
     *
     * @param channel      Channel to send the message to
     * @param emoteHelper  Emote helper
     * @param title        Embed title
     * @param desc         Embed description
     * @param thumbnail    Embed thumbnail
     * @param helpMessage  Help message to display in embed footer
     * @param loadingSteps List of titles for loading fields
     */
    public EmbedLoadingMessage(MessageChannel channel, EmoteHelper emoteHelper, String title, String desc, String thumbnail, String helpMessage, String[] loadingSteps) {
        this.channel = channel;
        this.emoteHelper = emoteHelper;
        this.title = title;
        this.thumbnail = thumbnail;
        this.desc = desc;
        this.helpMessage = helpMessage;
        this.stages = getStages(loadingSteps);
        this.currentStep = 0;
    }

    /**
     * Create an embed builder with the provided values
     *
     * @return Embed builder
     */
    public EmbedBuilder getEmbedBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(getTitle());
        builder.setDescription(getDesc());
        builder.setThumbnail(getThumbnail());
        builder.setColor(getColour());
        builder.setFooter(helpMessage, thumbnail);
        return builder;
    }

    /**
     * Get the colour to use as the embed border, based on the loading status
     *
     * @return Colour to use
     */
    private int getColour() {
        if(finished) {
            return EmbedHelper.getGreen();
        }
        if(failed) {
            return EmbedHelper.getRed();
        }
        return EmbedHelper.getYellow();
    }

    /**
     * Set the description
     *
     * @param desc New description
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * Create loading stage objects from given values
     *
     * @return Loading stages
     */
    private ArrayList<LoadingStage> getStages(String[] loadingSteps) {
        return Arrays.stream(loadingSteps).map(step -> new LoadingStage(step, emoteHelper)).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get the embed title
     *
     * @return Embed title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the embed description
     *
     * @return Embed description
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Get the loading stages
     *
     * @return Loading stages
     */
    public ArrayList<LoadingStage> getStages() {
        return stages;
    }

    /**
     * Get the embed thumbnail
     *
     * @return Embed thumbnail
     */
    public String getThumbnail() {
        return thumbnail;
    }

    /**
     * Send the loading message to the title and save the starting time and message id
     */
    public void showLoading() {
        Message message = channel.sendMessage(createLoadingMessage()).complete();
        startTime = System.currentTimeMillis();
        currentTime = startTime;
        id = message.getIdLong();
    }

    /**
     * Increment the current step and update the message
     */
    private void nextStage() {
        currentStep++;
        currentTime = System.currentTimeMillis();
        updateLoadingMessage();
    }

    /**
     * Complete the current step with a value to describe what was done
     *
     * @param value Value to describe the step completion - "Player is a normal account"!
     */
    public void completeStage(String value) {
        stages.get(currentStep).complete(value, currentTime);
        nextStage();
    }

    /**
     * Complete the current step without a description
     */
    public void completeStage() {
        stages.get(currentStep).complete(currentTime);
        nextStage();
    }

    /**
     * Update the current progress of a step
     *
     * @param value Value to show updated progress
     */
    public void updateStage(String value) {
        stages.get(currentStep).updateValue(value);
        updateLoadingMessage();
    }

    /**
     * Add a field showing that the loading has completed
     *
     * @param value Value to display - "See player data"
     */
    public void completeLoading(String value) {
        LoadingStage done = new LoadingStage("Done!", emoteHelper);
        if(value == null) {
            done.complete(startTime);
        }
        else {
            done.complete(value, startTime);
        }
        stages.add(done);
        this.finished = true;
        updateLoadingMessage();
    }

    /**
     * Add a field showing that the loading has failed. Fail the current and remaining steps
     *
     * @param reason Reason for failure - "That player doesn't exist"
     */
    public void failLoading(String reason) {
        LoadingStage fail = new LoadingStage("FAIL!", emoteHelper);
        fail.fail(reason, startTime);
        for(int i = currentStep; i < stages.size(); i++) {
            stages.get(i).fail();
        }
        stages.add(fail);
        this.failed = true;
        updateLoadingMessage();
    }

    /**
     * Build the loading message embed
     *
     * @return Message embed
     */
    public MessageEmbed createLoadingMessage() {
        EmbedBuilder builder = getEmbedBuilder();
        for(LoadingStage stage : stages) {
            builder.addField(stage.getTitle(), stage.getValue(), false);
        }
        return builder.build();
    }

    /**
     * Edit the loading message
     */
    void updateLoadingMessage() {
        channel.retrieveMessageById(id).queue(message -> message.editMessage(createLoadingMessage()).queue());
    }

    /**
     * Wrap status emojis for complete, incomplete, and fail.
     * Use the guild emote if it exists or a standard emoji
     */
    public static class Status {
        private final String neutral, fail, complete;

        /**
         * Use the guild emotes for completion status if available otherwise standard emoji
         *
         * @param emoteHelper Emote helper
         */
        public Status(EmoteHelper emoteHelper) {
            this.neutral = EmoteHelper.formatEmote(emoteHelper.getNeutral()) + " ";
            this.fail = EmoteHelper.formatEmote(emoteHelper.getFail()) + " ";
            this.complete = EmoteHelper.formatEmote(emoteHelper.getComplete()) + " ";
        }

        public String getComplete() {
            return complete;
        }

        public String getFail() {
            return fail;
        }

        public String getNeutral() {
            return neutral;
        }
    }

    /**
     * Hold information on loading stages
     */
    static class LoadingStage {

        private final String title;
        private String currentStatus, value;
        private long duration;
        private final Status status;

        /**
         * Create a default incomplete loading stage
         *
         * @param title Title to show in the embed - "Checking account type"
         */
        public LoadingStage(String title, EmoteHelper helper) {
            this.status = new Status(helper);
            this.currentStatus = status.getNeutral();
            this.value = "---";
            this.title = title;
        }

        /**
         * Get the loading stage title
         *
         * @return loading stage title
         */
        public String getTitle() {
            return title;
        }

        /**
         * Complete the loading stage without a reason, change the icon to a checkmark and set the duration to the
         * current time - completion time of the previous step.
         *
         * @param prevStep Completion time of the previous step
         */
        public void complete(long prevStep) {
            this.duration = System.currentTimeMillis() - prevStep;
            this.currentStatus = status.getComplete();
            this.value = "";
        }

        /**
         * Complete the loading stage with a value describing what was done,
         * change the icon to a checkmark and set the duration to the
         * current time - completion time of the previous step.
         *
         * @param prevStep Completion time of the previous step
         * @param value    Reason to be shown by completion - "Player is a normal account!"
         */
        public void complete(String value, long prevStep) {
            this.complete(prevStep);
            this.value = value;
        }

        /**
         * Fail a stage where the previous step was not completed
         */
        public void fail() {
            this.currentStatus = status.getFail();
        }

        /**
         * Fail a step where the previous step was completed
         *
         * @param value    Explanation of failure
         * @param prevStep Completion time of the previous step
         */
        public void fail(String value, long prevStep) {
            this.duration = System.currentTimeMillis() - prevStep;
            this.value = value;
            this.currentStatus = status.getFail();
        }

        /**
         * Get the status to display under the loading stage title. Display the duration if the stage is complete
         *
         * @return Loading stage status
         */
        public String getValue() {
            return duration > 0 ? currentStatus + " " + value + formatTime() : currentStatus + " " + value;
        }

        /**
         * Update the current status of the loading stage
         *
         * @param value Status to update
         */
        public void updateValue(String value) {
            this.value = value;
        }

        /**
         * Format the duration to seconds
         *
         * @return Formatted duration
         */
        private String formatTime() {
            String seconds = String.format("%.2f", (double) duration / 1000);
            return " (" + seconds + " seconds" + ")";
        }
    }
}
