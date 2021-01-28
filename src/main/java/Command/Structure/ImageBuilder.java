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

    public ImageBuilder(MessageChannel channel, EmoteHelper emoteHelper, String resourcePath, Font font) {
        this.channel = channel;
        this.emoteHelper = emoteHelper;
        this.resourcePath = resourcePath;
        this.handler = new ResourceHandler();
        this.gameFont = font;
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
}
