package COD.Assets;

import COD.CODManager.GAME;

import java.awt.image.BufferedImage;

/**
 * Hold map name and image info
 */
public class Map {
    private final String codename, name, loadingImageURL, compassImageURL;
    private final BufferedImage loadingImage, compassImage;

    public static class MapBuilder {
        private final String codename, name;
        private String loadingImageURL, compassImageURL;
        private BufferedImage loadingImage, compassImage;
        private final GAME game;

        /**
         * Create the MapBuilder
         *
         * @param codename Map codename e.g "mp_euphrates"
         * @param name     Map real name e.g "Euphrates Bridge"
         * @param game     COD game
         */
        public MapBuilder(String codename, String name, GAME game) {
            this.codename = codename;
            this.name = name;
            this.game = game;
        }

        /**
         * Set the URL to the loading screen image
         *
         * @param loadingImageURL URL to loading screen image
         * @return Builder
         */
        public MapBuilder setLoadingImageURL(String loadingImageURL) {
            this.loadingImageURL = loadingImageURL;
            return this;
        }

        /**
         * Set the URL to the compass image
         *
         * @param compassImageURL ImageURL URL to compass image
         * @return Builder
         */
        public MapBuilder setCompassImageURL(String compassImageURL) {
            this.compassImageURL = compassImageURL;
            return this;
        }

        /**
         * Set the loading screen image
         *
         * @param loadingImage Loading screen image
         * @return Builder
         */
        public MapBuilder setLoadingImage(BufferedImage loadingImage) {
            this.loadingImage = loadingImage;
            return this;
        }

        /**
         * Set the compass image (as seen in game)
         *
         * @param compassImage Compass image
         * @return Builder
         */
        public MapBuilder setCompassImage(BufferedImage compassImage) {
            this.compassImage = compassImage;
            return this;
        }

        /**
         * Build the map from the builder values
         *
         * @return Map from builder values
         */
        public Map build() {
            if(loadingImageURL == null) {
                loadingImageURL = fetchLoadingImageUrl(codename, game);
            }
            if(compassImageURL == null) {
                compassImageURL = fetchCompassImageUrl(codename, game);
            }
            return new Map(this);
        }
    }

    /**
     * Create a map from the given builder values
     *
     * @param builder Map builder
     */
    private Map(MapBuilder builder) {
        this.codename = builder.codename;
        this.name = builder.name;
        this.loadingImageURL = builder.loadingImageURL;
        this.compassImageURL = builder.compassImageURL;
        this.loadingImage = builder.loadingImage;
        this.compassImage = builder.compassImage;
    }

    /**
     * Create a missing map
     *
     * @param codename Map codename e.g - "mp_euphrates"
     * @param game     COD game
     */
    public Map(String codename, GAME game) {
        this(new MapBuilder(codename, "MISSING: " + codename, game));
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
     * Get the URL to an image displaying the map compass
     *
     * @return Compass image URL
     */
    public String getCompassImageURL() {
        return compassImageURL;
    }

    /**
     * Get the map image
     *
     * @return Map image
     */
    public BufferedImage getLoadingImage() {
        return loadingImage;
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
     * @return Loading image URL
     */
    public String getLoadingImageURL() {
        return loadingImageURL;
    }

    /**
     * Get the image of the map compass
     *
     * @return Compass image
     */
    public BufferedImage getCompassImage() {
        return compassImage;
    }
}