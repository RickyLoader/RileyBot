package News;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;

/**
 * News article
 */
public class Article {
    private final String browserUrl, dataUrl, title, intro, author;
    private final Date date;
    private final ArrayList<String> images;

    /**
     * Create the news article
     *
     * @param browserUrl URL to view the article in a browser
     * @param dataUrl    URL to view the article API data
     * @param author     Article author
     * @param title      Article title
     * @param intro      Intro text for article
     * @param date       Date article was posted
     * @param images     List of images
     */
    public Article(String browserUrl, String dataUrl, @Nullable String author, String title, String intro, Date date, ArrayList<String> images) {
        this.browserUrl = browserUrl;
        this.dataUrl = dataUrl;
        this.author = author;
        this.title = title;
        this.intro = intro;
        this.date = date;
        this.images = images;
    }

    /**
     * Get the title/headline of the article
     *
     * @return Article title/headline
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the author of the article
     *
     * @return Article author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Get the date that the article was posted
     *
     * @return Date article was posted
     */
    public Date getDate() {
        return date;
    }

    /**
     * Get the list of images for the article
     *
     * @return List of images
     */
    public ArrayList<String> getImages() {
        return images;
    }

    /**
     * Get the URL to view the article in a browser
     *
     * @return Article browser URL
     */
    public String getBrowserUrl() {
        return browserUrl;
    }

    /**
     * Get the URL to view the article API data
     *
     * @return Article API URL
     */
    public String getDataUrl() {
        return dataUrl;
    }

    /**
     * Get the intro text of the article.
     * This is a brief summary of the article contents.
     *
     * @return Article intro text
     */
    public String getIntro() {
        return intro;
    }

    /**
     * Check if the article has any images
     *
     * @return Article has images
     */
    public boolean hasImages() {
        return !images.isEmpty();
    }

    /**
     * Check if the article has an author
     *
     * @return Article has author
     */
    public boolean hasAuthor() {
        return author != null;
    }
}
