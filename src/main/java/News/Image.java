package News;

import org.jetbrains.annotations.Nullable;

/**
 * Image from a news article
 */
public class Image {
    private final String url, caption;

    /**
     * Create a news article image
     *
     * @param url     URL to the image
     * @param caption Optional caption for the image
     */
    public Image(String url, @Nullable String caption) {
        this.url = url;
        this.caption = caption;
    }

    /**
     * Get the URL to the image
     *
     * @return URL to image
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the caption for the image
     *
     * @return Image caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Check if the image has a caption
     *
     * @return Image has a caption
     */
    public boolean hasCaption() {
        return caption != null;
    }
}
