package COD;


/**
 * Hold map name and image info
 */
public class Map {
    private final String name, imageURL;

    /**
     * Create a map
     *
     * @param name     Map name
     * @param imageURL URL to map image
     */
    public Map(String name, String imageURL) {
        this.name = name;
        this.imageURL = imageURL;
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