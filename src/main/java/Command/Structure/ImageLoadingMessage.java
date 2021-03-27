package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
    @Override
    public void completeLoading(String url) {
        this.url = url;
        super.completeLoading((String) null);
    }

    /**
     * Complete the loading embed with an image to be used
     *
     * @param image   Image to display
     * @param message Message to display
     */
    public void completeLoading(BufferedImage image, String message) {
        this.image = imageToByteArray(image);
        this.url = "attachment://image.png";
        super.completeLoading(message);
    }

    /**
     * Create a byte array from an image
     *
     * @param image Image to create byte array from
     * @return Byte array
     */
    public static byte[] imageToByteArray(BufferedImage image) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            byte[] byteArray = outputStream.toByteArray();
            outputStream.close();
            return byteArray;
        }
        catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Complete the loading embed with an image to be used
     *
     * @param image to display
     */
    public void completeLoading(BufferedImage image) {
        completeLoading(image, null);
    }

    /**
     * Create the loading message with an image URL
     *
     * @return Loading message
     */
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

    /**
     * Update the loading message using a byte array attachment
     */
    @Override
    void updateLoadingMessage() {
        if(image == null) {
            super.updateLoadingMessage();
            return;
        }
        getChannel().sendMessage(createLoadingMessage()).addFile(image, "image.png").queue();
        getChannel().deleteMessageById(getId()).queue();
    }
}
