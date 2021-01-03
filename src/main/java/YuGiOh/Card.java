package YuGiOh;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Yu-Gi-Oh card
 */
public class Card {
    private final String name;
    private String url;
    private final long id;
    private final CardType type;
    private final CardStats stats;
    private final String[] images;
    private int imageIndex;

    /**
     * Create a Yu-Gi-Oh card
     *
     * @param name   Card name
     * @param id     Unique card id
     * @param type   Card type
     * @param stats  Card popularity stats
     * @param images Array of card artwork images
     */
    public Card(String name, long id, CardType type, CardStats stats, String[] images) {
        this.name = name;
        this.id = id;
        this.url = "https://db.ygoprodeck.com/card/?search=" + id;
        this.type = type;
        this.stats = stats;
        this.images = images;
        this.imageIndex = 0;
    }

    /**
     * Set the URL
     *
     * @param url url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get the card stats - Website view/popularity information
     *
     * @return Card stats
     */
    public CardStats getStats() {
        return stats;
    }

    /**
     * Get the unique id of the card
     *
     * @return Unique id
     */
    public long getId() {
        return id;
    }

    /**
     * Get the URL to the web page of the card
     *
     * @return URL to web page
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the image at the current index
     *
     * @return Image at current index
     */
    public String getCurrentImage() {
        return images[imageIndex];
    }

    /**
     * Get the current image index
     *
     * @return Current image index
     */
    public int getImageIndex() {
        return imageIndex;
    }

    /**
     * Get the total number of available images for the card
     *
     * @return Total card images
     */
    public int getTotalImages() {
        return images.length;
    }

    /**
     * Update the current image index and return the new image
     *
     * @return Image after index update
     */
    public String updateImage() {
        imageIndex = (imageIndex + 1) % images.length;
        return getCurrentImage();
    }

    /**
     * Get the name of the card
     *
     * @return Card name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the card type
     *
     * @return Card type
     */
    public CardType getType() {
        return type;
    }

    /**
     * Get the array of card artwork images
     *
     * @return Artwork images
     */
    public String[] getImages() {
        return images;
    }

    /**
     * Reset the image index to 0
     */
    public void resetImageIndex() {
        this.imageIndex = 0;
    }
}
