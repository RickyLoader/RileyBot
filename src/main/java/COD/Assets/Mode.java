package COD.Assets;

import java.awt.image.BufferedImage;

/**
 * Hold mode info
 */
public class Mode extends CODAsset {
    private final String imageURL;

    /**
     * Create a mode
     *
     * @param codename Mode codename e.g "war"
     * @param name     Mode real name e.g "Team Deathmatch"
     * @param image    Mode icon image
     * @param imageURL Mode icon URL
     */
    public Mode(String codename, String name, BufferedImage image, String imageURL) {
        super(codename, name, image);
        this.imageURL = imageURL;
    }

    /**
     * Get the URL to an image displaying the mode icon
     *
     * @return Icon image URL
     */
    public String getImageURL() {
        return imageURL;
    }
}