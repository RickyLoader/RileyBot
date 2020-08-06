package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An embed to track the progress of a given task
 */
public abstract class EmbedLoadingMessage {
    private final MessageChannel channel;
    private long id, startTime, currentTime;
    private final String title, thumbnail;
    private String desc;
    private final Guild guild;
    private final ArrayList<LoadingStage> stages;
    private int currentStep;

    /**
     * Create a loading message
     *
     * @param channel      Channel to send the message to
     * @param title        Embed title
     * @param desc         Embed description
     * @param thumbnail    Embed thumbnail
     * @param loadingSteps List of titles for loading fields
     */
    public EmbedLoadingMessage(MessageChannel channel, Guild guild, String title, String desc, String thumbnail, String[] loadingSteps) {
        this.channel = channel;
        this.guild = guild;
        this.title = title;
        this.thumbnail = thumbnail;
        this.desc = desc;
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
        builder.setColor(65280);
        return builder;
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
        return Arrays.stream(loadingSteps).map(step -> new LoadingStage(step, guild)).collect(Collectors.toCollection(ArrayList::new));
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
     */
    public void completeLoading() {
        LoadingStage done = new LoadingStage("Done!", guild);
        done.complete(startTime);
        getStages().add(done);
        updateLoadingMessage();
    }

    /**
     * Add a field showing that the loading has failed. Fail the current and remaining steps
     *
     * @param reason Reason for failure - "That player doesn't exist"
     */
    public void failLoading(String reason) {
        LoadingStage fail = new LoadingStage("FAIL!", guild);
        fail.fail(reason, startTime);
        stages.add(fail);
        for(int i = currentStep; i < stages.size(); i++) {
            stages.get(i).fail();
        }
        updateLoadingMessage();
    }

    /**
     * Build the loading message embed
     *
     * @return Message embed
     */
    public abstract MessageEmbed createLoadingMessage();

    /**
     * Edit the loading message
     */
    void updateLoadingMessage() {
        channel.retrieveMessageById(id).queue(message -> message.editMessage(createLoadingMessage()).queue());
    }

    /**
     * Hold information on loading stages
     */
    static class LoadingStage {

        private final String title;
        private String neutral, fail, complete;
        private String status, value;
        private long duration;
        private final Guild guild;

        /**
         * Create a default incomplete loading stage
         *
         * @param title Title to show in the embed - "Checking account type"
         */
        public LoadingStage(String title, Guild guild) {
            this.guild = guild;
            getEmotes();
            this.status = neutral;
            this.value = "---";
            this.title = title;
        }

        /**
         * Use the guild emotes for completion status if available otherwise standard emoji
         */
        private void getEmotes() {
            List<Emote> neutral = guild.getEmotesByName("neutral", true);
            List<Emote> fail = guild.getEmotesByName("fail", true);
            List<Emote> complete = guild.getEmotesByName("complete", true);
            this.neutral = neutral.isEmpty() ? "☐" : formatEmote(neutral.get(0));
            this.fail = fail.isEmpty() ? "☒" : formatEmote(fail.get(0));
            this.complete = complete.isEmpty() ? "\uD83D\uDDF9" : formatEmote(complete.get(0));
        }

        /**
         * Format emote for use in embed
         *
         * @param e Emote to format
         * @return Formatted emote
         */
        private String formatEmote(Emote e) {
            return "<:" + e.getName() + ":" + e.getId() + "> ";
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
            this.status = complete;
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
            this.duration = System.currentTimeMillis() - prevStep;
            this.status = complete;
            this.value = value;
        }

        /**
         * Fail a stage where the previous step was not completed
         */
        public void fail() {
            this.status = fail;
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
            this.status = fail;
        }

        /**
         * Get the status to display under the loading stage title. Display the duration if the stage is complete
         *
         * @return Loading stage status
         */
        public String getValue() {
            return duration > 0 ? status + " " + value + formatTime() : status + " " + value;
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
