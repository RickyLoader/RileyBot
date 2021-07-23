package Reddit;

/**
 * Reddit post content
 */
public class PostContent {
    private final TYPE type;

    public enum TYPE {
        TEXT,
        IMAGE,
        LINK,
        VIDEO,
        GALLERY,
        POLL
    }

    /**
     * Create the post content
     *
     * @param type Content type
     */
    protected PostContent(TYPE type) {
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
}
