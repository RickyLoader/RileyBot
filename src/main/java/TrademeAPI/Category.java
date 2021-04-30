package TrademeAPI;

/**
 * Listing category
 */
public class Category {
    public final String name, number;

    /**
     * Create a category
     * @param name Category name - e.g "Cars"
     * @param number Category number - e.g "0001-"
     */
    public Category(String name, String number){
        this.name = name;
        this.number = number;
    }

    /**
     * Get the name of the category - e.g "Cars"
     * @return Category name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the category number - e.g "0001-"
     * @return Category number
     */
    public String getNumber() {
        return number;
    }
}
