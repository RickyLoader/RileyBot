package Instagram;

/**
 * Location tag on post
 */
public class Location {
    private final long id;
    private final String name, exploreUrl;
    public static final String BASE_EXPLORE_URL = Instagram.BASE_EXPLORE_URL + "locations/";

    /**
     * Create a location
     *
     * @param id   Unique ID of the location
     * @param name Location name
     */
    public Location(long id, String name) {
        this.id = id;
        this.name = name;
        this.exploreUrl = BASE_EXPLORE_URL + id;
    }

    /**
     * Get the unique ID of the location
     *
     * @return Location Id
     */
    public long getId() {
        return id;
    }

    /**
     * Get the name of the location
     *
     * @return Location name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the URL to this location's explore page on Instagram - a feed of images marked with this location.
     *
     * @return URL to explore page
     */
    public String getExploreUrl() {
        return exploreUrl;
    }
}
