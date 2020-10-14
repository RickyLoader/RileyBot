package Command.Structure;

import Bot.ResourceHandler;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;

public abstract class ImageBuilder {
    private final EmoteHelper emoteHelper;
    private final MessageChannel channel;
    private final String resourcePath;
    private final ResourceHandler handler;
    private Font gameFont;

    public ImageBuilder(MessageChannel channel, EmoteHelper emoteHelper, String resourcePath, String fontName) {
        this.channel = channel;
        this.emoteHelper = emoteHelper;
        this.resourcePath = resourcePath;
        this.handler = new ResourceHandler();
        this.gameFont = registerFont(resourcePath + fontName, handler);
        if(gameFont == null) {
            System.out.println("Error loading font at: " + resourcePath + fontName);
        }
    }

    /**
     * Get the resource handler
     *
     * @return Resource handler
     */
    public ResourceHandler getResourceHandler() {
        return handler;
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
     * Get the Emote helper
     *
     * @return Emote helper
     */
    public EmoteHelper getEmoteHelper() {
        return emoteHelper;
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
     * @param nameQuery   Player name
     * @param helpMessage Help message to display in loading message
     * @param args        Other arguments
     */
    public abstract void buildImage(String nameQuery, String helpMessage, String... args);

    /**
     * Register the font with the graphics environment
     *
     * @param fontPath Path to font relative to resource directory
     * @param handler  Resource handler
     */
    public static Font registerFont(String fontPath, ResourceHandler handler) {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font gameFont = Font.createFont(Font.TRUETYPE_FONT, handler.getResourceFileAsStream(fontPath));
            ge.registerFont(gameFont);
            return gameFont;
        }
        catch(Exception e) {
            return null;
        }
    }
}
