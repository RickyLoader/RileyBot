package Steam;

/**
 * Steam store app info
 */
public class AppInfo {
    private final int id;
    private final String name, storeUrl;

    /**
     * Create the steam store application info
     *
     * @param id   Unique id of the application
     * @param name Application name
     */
    public AppInfo(int id, String name) {
        this.id = id;
        this.name = name;
        this.storeUrl = SteamStore.STEAM_STORE_WEB_BASE_URL + id;
    }

    /**
     * Get the URL to the application on the Steam store
     *
     * @return Application store URL
     */
    public String getStoreUrl() {
        return storeUrl;
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
