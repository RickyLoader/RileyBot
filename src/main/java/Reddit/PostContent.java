package Reddit;

/**
 * Reddit post content
 */
public class PostContent {
    private final String content;
    private final TYPE type;

    public enum TYPE {
        TEXT,
        IMAGE,
        LINK,
        VIDEO
    }

    /**
     * Create the post content
     *
     * @param content Text/URL
     * @param type    Content type
     */
    public PostContent(String content, TYPE type) {
        this.content = content;
        this.type = type;
    }

    /**
     * Get the content type
     *
     * @return Content type
     */
    public TYPE getType() {
        return type;
    }

    /**
     * Get the content - text/URL
     *
     * @return Content
     */
    public String getContent() {
        return content;
    }
}
