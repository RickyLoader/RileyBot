package XKCD;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * XKCD comic
 */
public class Comic {
    private final PublicationDetails publicationDetails;
    private final String image, desc;

    /**
     * Create an XKCD comic
     *
     * @param publicationDetails Comic publication details
     * @param image              URL to comic image
     * @param desc               Comic description
     */
    public Comic(PublicationDetails publicationDetails, String image, String desc) {
        this.publicationDetails = publicationDetails;
        this.image = image;
        this.desc = desc;
    }

    /**
     * Get the publication details of the comic - title, publication date, and issue #
     *
     * @return Comic publication details
     */
    public PublicationDetails getPublicationDetails() {
        return publicationDetails;
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
     * Get the image URL of the comic
     *
     * @return Image URL
     */
    public String getImage() {
        return image;
    }

    /**
     * Comic publication details - Date, issue #, and title
     */
    public static class PublicationDetails {
        private final int issue;
        private final Date date;
        private final String title;

        /**
         * Create the comic publication details
         *
         * @param issue Comic issue number
         * @param title Title of comic
         * @param date  Publication date
         */
        public PublicationDetails(int issue, Date date, String title) {
            this.issue = issue;
            this.date = date;
            this.title = title;
        }

        /**
         * Get the publication date of the comic
         *
         * @return Comic publication date
         */
        public Date getDate() {
            return date;
        }

        /**
         * Get the publication date of the comic formatted in a String
         *
         * @return Publication date String
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
    }
}
