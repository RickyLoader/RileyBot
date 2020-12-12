package Hangman;

import java.awt.image.BufferedImage;

/**
 * Hold data on a Hangman Gallows
 */
public class Gallows {
    private final BufferedImage[] images;
    private final String imagePreview;
    public final int MAX_STAGES; // First image should be an empty gallows so it is not counted as a stage
    private int index;

    /**
     * Create a Hangman Gallows defined by an array of incremental images.
     * 0 -> empty gallows, 1 -> gallows with character section etc
     *
     * @param images       Array of images to be parsed through as the game progresses
     * @param imagePreview Image URL preview of gallows
     */
    public Gallows(BufferedImage[] images, String imagePreview) {
        this.images = images;
        this.imagePreview = imagePreview;
        this.MAX_STAGES = images.length - 1;
        this.index = 0;
    }

    /**
     * Get the image at the current index
     */
    public BufferedImage getCurrentImage() {
        return images[index];
    }

    /**
     * Reset the current index/game stage
     */
    public void resetStages() {
        this.index = 0;
    }

    /**
     * Get the current index
     *
     * @return Current index/game stage
     */
    public int getStage() {
        return index;
    }

    /**
     * Increment the index
     * Throw an exception if the index would exceed the number of images when incremented
     */
    public void incrementStage() {
        int index = this.index + 1;
        if(index > images.length - 1) {
            throw new IndexOutOfBoundsException("No more images to display!");
        }
        this.index = index;
    }

    /**
     * Get the image preview URL of the gallows
     *
     * @return Image URL
     */
    public String getImagePreview() {
        return imagePreview;
    }
}
