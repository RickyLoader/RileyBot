package Reddit;

/**
 * Reddit image post
 */
public class ImageContent extends PostContent {
    private final String imageUrl;

    /**
     * Create the image post content
     *
     * @param imageUrl URL to image
     */
    public ImageContent(String imageUrl) {
        super(TYPE.IMAGE);
        this.imageUrl = imageUrl;
    }

    /**
     * Get the URL to the image in the post
     *
     * @return URL to post image
     */
    public String getImageUrl() {
        return imageUrl;
    }
}
