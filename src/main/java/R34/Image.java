package R34;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Date;

/**
 * R34 image
 */
public class Image {
    private final String imageUrl, postUrl, uploader;
    private final String[] tags;
    private final int score; // upvotes
    private final Date datePosted;

    /**
     * Create the R34 image
     *
     * @param imageUrl   URL to the raw image
     * @param postUrl    URL to the post
     * @param uploader   Name of the uploader - e.g "dave123"
     * @param tags       Array of tags - e.g ["cool_tag", "other_tag"]
     * @param score      Number of upvotes
     * @param datePosted Date posted
     */
    public Image(String imageUrl, String postUrl, String uploader, String[] tags, int score, Date datePosted) {
        this.imageUrl = imageUrl;
        this.postUrl = postUrl;
        this.uploader = uploader;
        this.tags = tags;
        this.score = score;
        this.datePosted = datePosted;
    }

    /**
     * Get the URL to the raw image
     *
     * @return URL to image
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Get the URL to view the post in a browser
     *
     * @return URL to view image post
     */
    public String getPostUrl() {
        return postUrl;
    }

    /**
     * Get the date that the image was posted
     *
     * @return Image posted date
     */
    public Date getDatePosted() {
        return datePosted;
    }

    /**
     * Get a summary String of the image tags.
     * Create a String containing {@code number} comma separated tags with details about remaining tags
     * appended on the end.
     * e.g for an image with 4 tags, requesting 2 would result in "tag1, tag2, (and 2 more)"
     *
     * @param number Number of tags to retrieve
     * @return Summary of image tags
     */
    public String getTagSummary(int number) {
        if(number <= 0 || number > tags.length) {
            number = tags.length;
        }

        final String tagSummary = StringUtils.join(Arrays.copyOfRange(tags, 0, number), ", ");
        return number == tags.length
                ? tagSummary
                : tagSummary + ", (and " + (tags.length - number) + " more)";
    }

    /**
     * Get the number of upvotes the image has
     *
     * @return Number of upvotes
     */
    public int getScore() {
        return score;
    }

    /**
     * Get the name of the image uploader
     *
     * @return Image uploader - e.g "dave123"
     */
    public String getUploader() {
        return uploader;
    }
}
