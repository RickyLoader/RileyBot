package Steam;

import java.text.DecimalFormat;

/**
 * Steam store application
 */
public class Application {
    private final AppInfo appInfo;
    private final String type, description, thumbnail;
    private final Price price;
    private final long concurrentPlayers;

    /**
     * Create the steam store application
     *
     * @param appInfo           App info - name & unique id
     * @param type              Application type - e.g "Game"
     * @param description       Short description
     * @param price             Price
     * @param concurrentPlayers Concurrent players as of yesterday
     */
    public Application(AppInfo appInfo, String type, String description, Price price, long concurrentPlayers) {
        this.appInfo = appInfo;
        this.type = type;
        this.description = description;
        this.thumbnail = "https://cdn.cloudflare.steamstatic.com/steam/apps/" + appInfo.getId() + "/header.jpg";
        this.price = price;
        this.concurrentPlayers = concurrentPlayers;
    }

    /**
     * Create the steam store application summary using the bare minimum application info
     *
     * @param appInfo           App info - name & unique id
     * @param price             Price
     * @param concurrentPlayers Concurrent players as of yesterday
     */
    public Application(AppInfo appInfo, Price price, long concurrentPlayers) {
        this(appInfo, null, null, price, concurrentPlayers);
    }

    /**
     * Get the URL to the application thumbnail image
     *
     * @return URL to application thumbnail image
     */
    public String getThumbnail() {
        return thumbnail;
    }

    /**
     * Get the type of application - e.g "Game"
     *
     * @return Application type
     */
    public String getType() {
        return type;
    }

    /**
     * Get the application info - name & unique id
     *
     * @return Application info
     */
    public AppInfo getAppInfo() {
        return appInfo;
    }

    /**
     * Get the price of the application
     *
     * @return Application price
     */
    public Price getPrice() {
        return price;
    }

    /**
     * Get the concurrent players playing/using the application as of yesterday
     *
     * @return Concurrent players
     */
    public long getConcurrentPlayers() {
        return concurrentPlayers;
    }


    /**
     * Check if the price is free
     *
     * @return Price is free
     */
    public boolean isFree() {
        return price == null;
    }

    /**
     * Get a short description of the application
     *
     * @return Application store description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the concurrent players playing/using the application as of yesterday
     * formatted as a comma separated String
     *
     * @return Comma separated concurrent players
     */
    public String formatConcurrentPlayers() {
        return new DecimalFormat("#,###").format(concurrentPlayers);
    }
}
