package NASA;

import java.time.LocalDate;

/**
 * NASA astronomy picture of the day
 */
public class APOD {
    private final LocalDate date;
    private final String explanation, image, title, url;

    /**
     * Create an astronomy picture of the day
     *
     * @param builder APOD Builder
     */
    private APOD(APODBuilder builder) {
        this.date = builder.date;
        this.explanation = builder.explanation;
        this.image = builder.image;
        this.title = builder.title;
        this.url = builder.url;
    }

    /**
     * Get the URL to view the APOD online
     *
     * @return URL to APOD
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the title of the APOD
     *
     * @return Title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the image of the APOD
     *
     * @return Image
     */
    public String getImage() {
        return image;
    }

    /**
     * Get the date the APOD was featured
     *
     * @return APOD Date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Get the explanation of the image contents
     *
     * @return Explanation of image contents
     */
    public String getTruncatedExplanation() {
        return explanation.substring(0, Math.min(explanation.length(), 150)) + "...";
    }

    public static class APODBuilder {
        private LocalDate date;
        private String explanation, image, title, url;

        /**
         * Set the date that the APOD was featured
         *
         * @param date Date
         * @return Builder
         */
        public APODBuilder setDate(LocalDate date) {
            this.date = date;
            return this;
        }

        /**
         * Set the URL to view the APOD online
         *
         * @param url URL to APOD
         * @return Builder
         */
        public APODBuilder setUrl(String url) {
            this.url = url;
            return this;
        }

        /**
         * Set the explanation of the image contents
         *
         * @param explanation Explanation of image contents
         * @return Builder
         */
        public APODBuilder setExplanation(String explanation) {
            this.explanation = explanation;
            return this;
        }

        /**
         * Set the title of the APOD
         *
         * @param title Title
         * @return Builder
         */
        public APODBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set the image
         *
         * @param image Image
         * @return Builder
         */
        public APODBuilder setImage(String image) {
            this.image = image;
            return this;
        }

        /**
         * Build the APOD
         *
         * @return APOD
         */
        public APOD build() {
            return new APOD(this);
        }
    }
}
