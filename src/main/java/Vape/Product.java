package Vape;

import java.util.ArrayList;
import java.util.Date;

/**
 * Vapourium product
 */
public class Product {
    private final long id;
    private final String name, description, type, url;
    private final Date lastUpdated, created;
    private final ArrayList<String> images;
    private final ArrayList<Option> options;

    /**
     * Create the product
     *
     * @param id          Unique product id
     * @param name        Product name
     * @param url         URL to the product
     * @param description Description of product
     * @param type        Product type
     * @param created     Date of the product creation
     * @param lastUpdated Date of the last update to the product
     * @param images      List of image URLs
     * @param options     List of product options
     */
    public Product(long id, String name, String url, String description, String type, Date created, Date lastUpdated, ArrayList<String> images, ArrayList<Option> options) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.description = description;
        this.type = type.isEmpty() ? "-" : type.toUpperCase();
        this.created = created;
        this.lastUpdated = lastUpdated;
        this.images = images;
        this.options = options;
    }

    /**
     * Get the URL to the product
     *
     * @return URL to the product
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the date that the product was created
     *
     * @return Product creation date
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Get the type of product
     *
     * @return Product type
     */
    public String getType() {
        return type;
    }

    /**
     * Get the name of the product
     *
     * @return Product name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the product description truncated to the given number of characters
     * If the bound is larger than or equal to the length of the description (or <=0),
     * the full description will be returned.
     *
     * @param bound Number of characters to truncate description to
     * @return Truncated description
     */
    public String getTruncatedDescription(int bound) {
        if(bound >= description.length() || bound <= 0) {
            return description;
        }
        return description.substring(0, bound) + "...";
    }

    /**
     * Get the description of the product
     *
     * @return Product description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get a list of product image URLs
     *
     * @return List of product image URLs
     */
    public ArrayList<String> getImages() {
        return images;
    }

    /**
     * Get a list of product options.
     * An option is a name and list of values - e.g "Colour" -> "Blue, Gold, "Black"
     *
     * @return List of product options
     */
    public ArrayList<Option> getOptions() {
        return options;
    }

    /**
     * Get the unique id of the product
     *
     * @return Unique id
     */
    public long getId() {
        return id;
    }

    /**
     * Get the date of the last product update
     *
     * @return Date of last product update
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Check if the product has any options/variants
     *
     * @return Product has options
     */
    public boolean hasOptions() {
        return !options.isEmpty();
    }
}
