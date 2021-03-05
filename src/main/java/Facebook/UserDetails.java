package Facebook;

/**
 * Facebook user details
 */
public class UserDetails {
    private final String name, url, thumbnailUrl;

    /**
     * Create Facebook user details
     *
     * @param name         Page name
     * @param url          URL to page
     * @param thumbnailUrl URL to page thumbnail image
     */
    public UserDetails(String name, String url, String thumbnailUrl) {
        this.name = name;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * Get the name of the page
     *
     * @return Page name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the URL to the page
     *
     * @return URL to page
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the URL to the page thumbnail image
     *
     * @return URL to page thumbnail image
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
