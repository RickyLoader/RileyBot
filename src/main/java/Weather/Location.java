package Weather;

/**
 * Location for weather forecast
 */
public class Location {
    private final String name, dataUrl, webUrl, matchDetails;

    /**
     * Create a location for a weather forecast
     *
     * @param name         Location name
     * @param dataUrl      URL to fetch the location's weather forecast from the MetService API
     * @param webUrl       URL to the location's forecast on the MetService website
     * @param matchDetails Details on why this location was returned for a given search query
     *                     e.g "**Matched on**: **Toato**a, Ōpōtiki"
     */
    public Location(String name, String dataUrl, String webUrl, String matchDetails) {
        this.name = name;
        this.dataUrl = dataUrl;
        this.webUrl = webUrl;
        this.matchDetails = matchDetails;
    }

    /**
     * Get the details on why this location was returned for a given search query
     * e.g "**Matched on**: **Toato**a, Ōpōtiki"
     *
     * @return Search match details
     */
    public String getMatchDetails() {
        return matchDetails;
    }

    /**
     * Get the location name
     *
     * @return Location name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the URL to fetch the location's weather forecast from the MetService API
     *
     * @return Location data URL
     */
    public String getDataUrl() {
        return dataUrl;
    }

    /**
     * Get the URL to the location's weather forecast on the MetService website
     *
     * @return Location forecast URL
     */
    public String getWebURL() {
        return webUrl;
    }
}
