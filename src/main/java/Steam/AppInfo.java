package Steam;

/**
 * Steam store app info
 */
public class AppInfo {
    private final int id;
    private final String name;

    /**
     * Create the steam store application info
     *
     * @param id   Unique id of the application
     * @param name Application name
     */
    public AppInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Get the name of the application
     *
     * @return Application name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the unique id of the application
     *
     * @return Application id
     */
    public int getId() {
        return id;
    }

    /**
     * Get the application info in the format "NAME (ID)"
     *
     * @return Application info summary
     */
    public String getSummary() {
        return name + " (" + id + ")";
    }
}
