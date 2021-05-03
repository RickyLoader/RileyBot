package TrademeAPI;

/**
 * Listing category
 */
public class Category {
    public final String name, number, path;

    /**
     * Create a category
     *
     * @param name   Category name - e.g "Cars"
     * @param number Category number - e.g "0001-"
     * @param path   URL path - e.g "/Trade-Me-Motors/Cars"
     */
    public Category(String name, String number, String path) {
        this.name = name;
        this.number = number;
        this.path = path;
    }

    /**
     * Get the URL path of the category - e.g "/Trade-Me-Motors/Cars"
     *
     * @return Category URL path
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the name of the category - e.g "Cars"
     *
     * @return Category name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the category number - e.g "0001-"
     *
     * @return Category number
     */
    public String getNumber() {
        return number;
    }
}
