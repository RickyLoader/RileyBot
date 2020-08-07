package Command.Structure;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;
import java.io.File;

public abstract class ImageBuilder {
    private final Guild guild;
    private final MessageChannel channel;
    private final String resourcePath;
    private Font gameFont;

    public ImageBuilder(MessageChannel channel, Guild guild, String resourcePath, String fontName) {
        this.channel = channel;
        this.guild = guild;
        this.resourcePath = resourcePath;
        this.gameFont = registerFont(resourcePath + fontName);
        if(gameFont == null) {
            System.out.println("Error loading font at: " + resourcePath + fontName);
        }
    }

    /**
     * Get game font
     *
     * @return Game font
     */
    public Font getGameFont() {
        return gameFont;
    }

    /**
     * Set the game font
     *
     * @param gameFont Font to set
     */
    public void setGameFont(Font gameFont) {
        this.gameFont = gameFont;
    }

    /**
     * Get the path to the applicable resource folder
     *
     * @return Resource folder path
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Get the guild
     *
     * @return Guild
     */
    public Guild getGuild() {
        return guild;
    }

    /**
     * Get the channel
     *
     * @return Channel
     */
    public MessageChannel getChannel() {
        return channel;
    }

    /**
     * Build the various sections of the image and draw them each on to the background image
     *
     * @param nameQuery Player name
     */
    public abstract void buildImage(String nameQuery);

    /**
     * Register the font with the graphics environment
     */
    private Font registerFont(String fontPath) {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font gameFont = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath));
            ge.registerFont(gameFont);
            return gameFont;
        }
        catch(Exception e) {
            return null;
        }
    }
}
