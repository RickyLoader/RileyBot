package Reddit;

import java.util.ArrayList;

/**
 * Reddit image gallery post
 */
public class GalleryContent extends PostContent {
    private final ArrayList<String> gallery;

    /**
     * Create the image gallery post content
     */
    public GalleryContent() {
        super(TYPE.GALLERY);
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
     * Get the list of image URLs in the gallery
     *
     * @return List of gallery image URLs
     */
    public ArrayList<String> getGallery() {
        return gallery;
    }
}
