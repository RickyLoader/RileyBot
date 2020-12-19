package XKCD;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * XKCD comic
 */
public class Comic {
    private final int issue;
    private final Date date;
    private final String image, title, desc;

    /**
     * Create a comic
     *
     * @param issue Comic issue number
     * @param image URL to comic image
     * @param title Title of comic
     * @param date  Release date
     */
    public Comic(int issue, String image, String title, String desc, Date date) {
        this.issue = issue;
        this.image = image;
        this.title = title;
        this.desc = desc;
        this.date = date;
    }

    /**
     * Get the description of the comic
     *
     * @return Comic description
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Get the release date of the comic formatted in a String
     *
     * @return Release date String
     */
    public String getFormattedDate() {
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    /**
     * Get the title of the comic
     *
     * @return Comic title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the issue number of the comic
     *
     * @return Issue number
     */
    public int getIssue() {
        return issue;
    }

    /**
     * Get the image URL of the comic
     *
     * @return Image URL
     */
    public String getImage() {
        return image;
    }
}
