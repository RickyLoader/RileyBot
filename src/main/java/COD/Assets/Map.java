package COD.Assets;

import COD.API.CODManager.GAME;
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
     * @param game     Game map belongs to
     * @param image    Map loading screen image
     * @param imageUrl URL to map loading screen image
     */
    public Map(String codename, String name, GAME game, @Nullable BufferedImage image, @Nullable String imageUrl) {
        super(codename, name, image);
        this.imageUrl = imageUrl == null ? fetchLoadingImageUrl(codename, game) : imageUrl;
    }

    /**
     * Create a missing map
     *
     * @param codename Map codename e.g - "mp_euphrates"
     * @param game     COD game
     */
    public Map(String codename, GAME game) {
        this(codename, "MISSING: " + codename, game, null, null);
    }

    /**
     * Get the image URL for a map given the codename
     *
     * @param codename Codename to retrieve
     * @param game     COD game
     * @return Image URL for map
     */
    private static String fetchLoadingImageUrl(String codename, GAME game) {
        return "https://www.callofduty.com/cdn/app/base-maps/"
                + game.name().toLowerCase() + "/" + codename + ".jpg";
    }

    /**
     * Get the image URL for a map compass given the codename
     *
     * @param codename Codename to retrieve
     * @param game     COD game
     * @return Image URL for map compass
     */
    private static String fetchCompassImageUrl(String codename, GAME game) {
        return "https://www.callofduty.com/cdn/app/maps/"
                + game.name().toLowerCase() + "/compass_map_" + codename + ".jpg";
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