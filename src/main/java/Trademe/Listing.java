package Trademe;

import java.util.ArrayList;
import java.util.Date;

/**
 * Trademe listing
 */
public class Listing {
    private final String title, url, price, description;
    private final Date closingDate;
    private final int bidderWatchers;
    private final ArrayList<String> images;
    private final Member member;
    public final static String NO_PHOTOS_IMAGE = "https://i.imgur.com/D75RZCx.png";

    /**
     * Create the Trademe listing
     *
     * @param url            URL to the listing
     * @param title          Listing title
     * @param description    Description of the listing
     * @param closingDate    Closing date
     * @param bidderWatchers Combined number of bidders & watchers
     * @param images         List of image URLs
     * @param member         Member who created listing
     * @param price          Price display - e.g "$260.00"
     */
    public Listing(String url, String title, String description, Date closingDate, int bidderWatchers, ArrayList<String> images, Member member, String price) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.closingDate = closingDate;
        this.bidderWatchers = bidderWatchers;
        this.images = images;
        this.member = member;
        this.price = price;
    }

    /**
     * Get the description of the listing truncated to 100 characters
     *
     * @return Truncated description
     */
    public String getTruncatedDescription() {
        if(description.length() <= 100) {
            return description;
        }
        return description.substring(0, 100) + "...";
    }

    /**
     * Get the price display value - e.g "$260.00"
     *
     * @return Price display
     */
    public String getPriceDisplay() {
        return price;
    }

    /**
     * Get the member who created the listing
     *
     * @return Member who created listing
     */
    public Member getMember() {
        return member;
    }

    /**
     * Get the closing date of the listing
     *
     * @return Closing date
     */
    public Date getClosingDate() {
        return closingDate;
    }

    /**
     * Check if the listing has closed
     *
     * @return Listing has closed
     */
    public boolean isClosed() {
        return new Date().after(closingDate);
    }

    /**
     * Get the combined number of bidders and watchers
     *
     * @return Bidders & watchers
     */
    public int getBidderWatchers() {
        return bidderWatchers;
    }

    /**
     * Get a list of the image URLs featured in the listing
     *
     * @return List of image URLs
     */
    public ArrayList<String> getImages() {
        return images;
    }

    /**
     * Get the title of the listing
     *
     * @return Title of listing
     */
    public String getTitle() {
        return title;
    }

    /**
     * Check if the listing has any images
     *
     * @return Listing has images
     */
    public boolean hasImages() {
        return !images.isEmpty();
    }

    /**
     * Get the listing thumbnail - either the first available image or a placeholder image
     *
     * @return Listing thumbnail
     */
    public String getThumbnail() {
        return hasImages() ? images.get(0) : NO_PHOTOS_IMAGE;
    }

    /**
     * Get the URL to the listing
     *
     * @return URL to listing
     */
    public String getUrl() {
        return url;
    }
}
