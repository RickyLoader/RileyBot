package Command.Structure;

import Bot.ResourceHandler;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class ImageBuilder {
    private final EmoteHelper emoteHelper;
    private final String resourcePath;
    private final ResourceHandler handler;
    private final Font gameFont;

    /**
     * Create the image builder
     *
     * @param emoteHelper  Emote helper
     * @param resourcePath Base resource path
     * @param font         Font to use in image
     */
    public ImageBuilder(EmoteHelper emoteHelper, String resourcePath, Font font) {
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
     * Create a copy of the given image
     *
     * @param source Source to copy
     * @return Copy of source image
     */
    public static BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
                source.getWidth(),
                source.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics g = copy.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }

    /**
     * Resize the given image (this returns a copy of the image).
     *
     * @param image  Image to resize
     * @param width  Desired width of resized image
     * @param height Desired height of resized image
     * @return Resized image (copy of the original image)
     */
    public static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage resized = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics g = resized.getGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }
}
