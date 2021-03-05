package Facebook;

/**
 * Facebook post details - text & date
 */
public class PostDetails {
    private final String text, datePublished, url;

    /**
     * Create the post details
     *
     * @param text          Text contained in the post
     * @param datePublished Date the post was published
     * @param url           URL to the post
     */
    public PostDetails(String text, String datePublished, String url) {
        this.text = text;
        this.datePublished = datePublished;
        this.url = url;
    }

    /**
     * Get the text in the post
     *
     * @return Post text
     */
    public String getText() {
        return text;
    }

    /**
     * Get the URL to the post
     *
     * @return URL to post
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the published date of the post
     *
     * @return Post published date
     */
    public String getDatePublished() {
        return datePublished;
    }
}
