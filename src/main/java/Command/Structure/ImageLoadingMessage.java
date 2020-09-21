package Command.Structure;

import Network.ImgurManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class ImageLoadingMessage extends EmbedLoadingMessage {
    private String url;
    private byte[] image;

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
    public ImageLoadingMessage(MessageChannel channel, EmoteHelper emoteHelper, String title, String desc, String thumbnail, String helpMessage, String[] loadingSteps) {
        super(channel, emoteHelper, title, desc, thumbnail, helpMessage, loadingSteps);
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

    /**
     * Complete the loading embed with an image to be used
     *
     * @param image to display
     */
    public void completeLoading(BufferedImage image) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", outputStream);
            this.image = outputStream.toByteArray();
            this.url = "image.png";
            outputStream.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

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

    @Override
    void updateLoadingMessage() {
        if(image == null) {
            super.updateLoadingMessage();
            return;
        }
        getChannel().retrieveMessageById(getId()).queue(message -> message.editMessage(createLoadingMessage()).addFile(image, url).queue());
    }
}
