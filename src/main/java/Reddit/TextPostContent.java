package Reddit;

/**
 * Reddit text post
 */
public class TextPostContent extends PostContent {
    private final String text;

    /**
     * Create the text post content
     *
     * @param text Post text
     */
    public TextPostContent(String text) {
        super(TYPE.TEXT);
        this.text = text;
    }

    /**
     * Get the post text
     *
     * @return Post text
     */
    public String getText() {
        return text;
    }
}
