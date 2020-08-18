package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class ImageLoadingMessage extends EmbedLoadingMessage {
    private String url;

    /**
     * Create a loading message
     * Create a loading message
     *
     * @param channel      Channel to send the message to
     * @param title        Embed title
     * @param desc         Embed description
     * @param thumbnail    Embed thumbnail
     * @param loadingSteps List of titles for loading fields
     */
    public ImageLoadingMessage(MessageChannel channel, Guild guild, String title, String desc, String thumbnail, String[] loadingSteps) {
        super(channel, guild, title, desc, thumbnail, loadingSteps);
    }

    /**
     * Complete the loading embed with an image URL to be used
     *
     * @param url     URL to be used as the image of the embed
     * @param message Message to display under the "Done!" loading step
     */
    public void completeLoading(String url, String message) {
        this.url = url;
        super.completeLoading(message);
    }

    /**
     * Complete the loading embed with an image URL to be used
     *
     * @param url URL to be used as the image of the embed
     */
    public void completeLoading(String url) {
        this.url = url;
        super.completeLoading(null);
    }

    @Override
    public MessageEmbed createLoadingMessage() {
        EmbedBuilder builder = getEmbedBuilder();
        for(LoadingStage stage : getStages()) {
            builder.addField(stage.getTitle(), stage.getValue(), false);
        }
        if(url != null) {
            builder.setImage(url);
        }
        return builder.build();
    }
}
