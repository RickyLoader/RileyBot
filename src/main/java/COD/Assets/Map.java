package COD.Assets;

import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

/**
 * Hold map name and image info
 */
public class Map extends CODAsset {
    private final String imageUrl;

    /**
     * Create a map
     *
     * @param codename Map codename - as named in files
     * @param name     Map name - as named in-game
     * @param image    Map loading screen image
     * @param imageUrl URL to map loading screen image
     */
    public Map(String codename, String name, @Nullable BufferedImage image, @Nullable String imageUrl) {
        super(codename, name, image);
        this.imageUrl = imageUrl;
    }

    /**
     * Create a missing map
     *
     * @param codename Map codename e.g - "mp_euphrates"
     */
    public Map(String codename) {
        this(codename, "MISSING: " + codename, null, null);
    }

    /**
     * Check if there is an image URL available for the map image
     *
     * @return Image URL available
     */
    public boolean hasImageUrl() {
        return imageUrl != null;
    }

    /**
     * Get the URL to an image displaying the map loading screen
     *
     * @return Loading image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }
}