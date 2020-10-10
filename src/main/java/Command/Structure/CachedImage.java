package Command.Structure;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Read in the given path to an image
 */
public class CachedImage {
    private final BufferedImage image;

    /**
     * Create a cached image
     *
     * @param path Path to image
     */
    public CachedImage(String path) {
        System.out.println("Caching: " + path);
        this.image = readImage(path);
    }

    /**
     * Read in the path to a BufferedImage
     *
     * @param path Path to image
     * @return Buffered image from path
     */
    private BufferedImage readImage(String path) {
        try {
            return ImageIO.read(new File(path));
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the image
     *
     * @return Image
     */
    public BufferedImage getImage() {
        return image;
    }
}
