package Reddit;

import java.util.ArrayList;

/**
 * Reddit post content
 */
public class PostContent {
    private final String content;
    private final TYPE type;
    private final ArrayList<String> gallery;

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
        this.gallery = new ArrayList<>();
    }

    /**
     * Add the given image URL to the list of gallery image URLs
     *
     * @param url URL to add to gallery
     */
    public void addImageToGallery(String url) {
        this.gallery.add(url);
    }

    /**
     * Check if the content has a gallery
     *
     * @return Content has gallery
     */
    public boolean hasGallery() {
        return !gallery.isEmpty();
    }

    /**
     * Get the list of image URLs in the gallery
     *
     * @return List of gallery image URLs
     */
    public ArrayList<String> getGallery() {
        return gallery;
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
