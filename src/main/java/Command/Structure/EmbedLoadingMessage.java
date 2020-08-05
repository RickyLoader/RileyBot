package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;

/**
 * An embed to track the progress of a given task
 */
public class EmbedLoadingMessage {
    private final MessageChannel channel;
    private long id, startTime, currentTime;
    private final String title, thumbnail, desc;
    private final ArrayList<LoadingStage> stages = new ArrayList<>();
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
    public EmbedLoadingMessage(MessageChannel channel, String title, String desc, String thumbnail, String[] loadingSteps) {
        this.channel = channel;
        this.title = title;
        this.thumbnail = thumbnail;
        this.desc = desc;
        for(String key : loadingSteps) {
            stages.add(new LoadingStage(key));
        }
        this.currentStep = 0;
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
        LoadingStage done = new LoadingStage("Done!");
        done.complete(startTime);
        stages.add(done);
        updateLoadingMessage();
    }

    /**
     * Add a field showing that the loading has failed. Fail the current and remaining steps
     *
     * @param reason Reason for failure - "That player doesn't exist"
     */
    public void failLoading(String reason) {
        LoadingStage fail = new LoadingStage("FAIL!");
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
    private MessageEmbed createLoadingMessage() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(this.title);
        builder.setDescription(this.desc);
        builder.setThumbnail(this.thumbnail);
        builder.setColor(65280);
        for(LoadingStage stage : stages) {
            builder.addField(stage.getTitle(), stage.getValue(), false);
        }
        return builder.build();
    }

    /**
     * Edit the loading message
     */
    private void updateLoadingMessage() {
        channel.retrieveMessageById(id).queue(message -> message.editMessage(createLoadingMessage()).queue());
    }

    /**
     * Hold information on loading stages
     */
    private static class LoadingStage {

        private final String title;
        private String status, value;
        private long duration;

        /**
         * Create a default incomplete loading stage
         *
         * @param title Title to show in the embed - "Checking account type"
         */
        public LoadingStage(String title) {
            this.status = "☐";
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
            this.status = "\uD83D\uDDF9";
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
            this.status = "\uD83D\uDDF9";
            this.value = value;
        }

        /**
         * Fail a stage where the previous step was not completed
         */
        public void fail() {
            this.status = "☒";
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
            this.status = "☒";
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
