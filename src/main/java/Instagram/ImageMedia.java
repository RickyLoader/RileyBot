package Instagram;

/**
 * Image media type
 */
public class ImageMedia extends Media {
    private final String imageUrl;

    /**
     * Create the image media
     *
     * @param imageUrl URL to image
     */
    public ImageMedia(String imageUrl) {
        super(Post.MEDIA_TYPE.IMAGE);
        this.imageUrl = imageUrl;
    }

    /**
     * Get the URL to the image
     *
     * @return URL to image
     */
    public String getImageUrl() {
        return imageUrl;
    }
}
