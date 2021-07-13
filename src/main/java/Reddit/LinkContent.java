package Reddit;

/**
 * Reddit link post
 */
public class LinkContent extends PostContent {
    private final String url;

    /**
     * Create the link post content
     *
     * @param url Post content URL - e.g twitter, website URL, etc
     */
    public LinkContent(String url) {
        super(TYPE.LINK);
        this.url = url;
    }

    /**
     * Get the post content URL - e.g twitter, website URL, etc
     *
     * @return Post content URL
     */
    public String getUrl() {
        return url;
    }
}
