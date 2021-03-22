package Reddit;

/**
 * Reddit subreddit
 */
public class Subreddit {
    private final String name, url, imageUrl;

    /**
     * Create the subreddit
     *
     * @param name     Name of the subreddit
     * @param url      URL to the subreddit
     * @param imageUrl URL to the subreddit icon
     */
    public Subreddit(String name, String url, String imageUrl) {
        this.name = name;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    /**
     * Get the URL to the subreddit
     *
     * @return Subreddit URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the name of the subreddit
     *
     * @return Subreddit name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the URL to the subreddit icon
     *
     * @return URL to subreddit icon
     */
    public String getImageUrl() {
        return imageUrl;
    }
}
