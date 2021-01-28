package COD.Assets;

/**
 * Hold map name and image info
 */
public class Map {
    private final String codename, name, imageURL;

    /**
     * Create a map
     *
     * @param codename Map codename e.g "mp_euphrates"
     * @param name     Map real name e.g "Euphrates Bridge"
     * @param imageURL URL to map image
     */
    public Map(String codename, String name, String imageURL) {
        this.codename = codename;
        this.name = name;
        this.imageURL = imageURL;
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