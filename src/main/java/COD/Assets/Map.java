package COD.Assets;

import java.awt.image.BufferedImage;

/**
 * Hold map name and image info
 */
public class Map {
    private final String codename, name, imageURL;
    private final BufferedImage image;

    /**
     * Create a map
     *
     * @param codename Map codename e.g "mp_euphrates"
     * @param name     Map real name e.g "Euphrates Bridge"
     * @param imageURL URL to map image
     * @param image    Map image
     */
    public Map(String codename, String name, String imageURL, BufferedImage image) {
        this.codename = codename;
        this.name = name;
        this.imageURL = imageURL;
        this.image = image;
    }

    /**
     * Get the map image
     *
     * @return Map image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Get the codename of the map e.g "mp_euphrates"
     *
     * @return Codename of map
     */
    public String getCodename() {
        return codename;
    }

    /**
     * Get the map name
     *
     * @return Map name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the URL to an image displaying the map loading screen
     *
     * @return Image URL
     */
    public String getImageURL() {
        return imageURL;
    }
}