package StarCitizen;

import Network.NetworkRequest;
import StarCitizen.Ship.Manufacturer;
import StarCitizen.Ship.Measurements;
import StarCitizen.Ship.StructuralDetails;
import Steam.Price;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Star Citizen ship info manager
 */
public class ShipManager {
    private static ShipManager instance = null;
    private static final String
            BASE_URL = "https://robertsspaceindustries.com",
            BASE_API_URL = BASE_URL + "/ship-matrix/index",
            MAIN_IMAGE_KEY = "main_image";
    private long lastUpdate;
    private ArrayList<Ship> ships;

    /**
     * Initialise ship cache
     */
    private ShipManager() {
        updateShips();
    }

    /**
     * Get an instance of the ShipManager
     *
     * @return Instance
     */
    public static ShipManager getInstance() {
        if(instance == null) {
            instance = new ShipManager();
        }
        return instance;
    }

    /**
     * Check if the given ship has had store details applied (price/images).
     * If not, scrape and apply these details to the ship.
     * These details are not returned by the API which is why they must be added later.
     *
     * @param ship Ship to apply store details to
     */
    public static void applyStoreDetails(Ship ship) {
        Price price = ship.getPrice();
        ArrayList<String> images = ship.getImages();

        // Store details already applied (API does return a single image)
        if(price != null && images.size() > 1) {
            return;
        }

        try {
            Document storePage = Jsoup.connect(ship.getStoreUrl()).get();

            // Scrape price of the ship
            if(price == null) {
                ship.setPrice(scrapeShipPrice(storePage));
            }

            // Scrape ship images
            if(images.size() <= 1) {
                Element gallery = storePage.selectFirst(".ship-slideshow");

                // May not have any images
                if(gallery != null) {
                    Elements imageElements = gallery
                            .selectFirst(".placeholder") // Gallery
                            .select(".download_source a"); // Images in gallery

                    final String mainImageUrl = ship.getMainImageUrl();

                    // Add images to list
                    for(Element image : imageElements) {
                        final String imageUrl = image.absUrl("href");

                        // Don't add the main image URL (as it was already returned from API)
                        if(mainImageUrl != null && mainImageUrl.equals(imageUrl)) {
                            continue;
                        }

                        images.add(imageUrl);
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Attempt to retrieve the store price of a ship by scraping the given store page.
     *
     * @param shipPage HTML document of ship store page to scrape price from
     * @return Price or null
     */
    @Nullable
    public static Price scrapeShipPrice(Document shipPage) {
        try {
            final Element priceElement = shipPage
                    .selectFirst(".final-price");

            return new Price(
                    Double.parseDouble(priceElement.attr("data-value")) / 100, // It's in cents
                    priceElement.attr("data-currency")
            );
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Update the cached list of ships, only if it has been an hour or more.
     *
     * @param force Update regardless of how much time has passed
     */
    public void updateShips(boolean force) {
        final long now = System.currentTimeMillis();

        // Only update if it has been an hour or more OR asked to force update
        if(!force && ships != null && now - lastUpdate < 3600000) {
            return;
        }

        ships = fetchAllShips();
        lastUpdate = now;
    }

    /**
     * Update the cached list of ships if it has been an hour or more.
     */
    public void updateShips() {
        updateShips(false);
    }

    /**
     * Get a list of Star Citizen ships. If no filter is provided, all ships will be returned.
     *
     * @param filter Optional filter for ships
     * @return Ships matching filter/all ships if none is provided
     */
    public ArrayList<Ship> getShips(@Nullable Predicate<Ship> filter) {

        // Check for updates
        updateShips();

        // Return all ships
        if(filter == null) {
            return ships;
        }

        // Filter by provided predicate
        return ships
                .stream()
                .filter(filter)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get all Star Citizen ships
     *
     * @return List of all ships
     */
    public ArrayList<Ship> getShips() {
        return getShips(null);
    }

    /**
     * Fetch a list of ships from the Star Citizen API by the given URL.
     * This may fail if the API is down.
     *
     * @param apiUrl API URL for requesting ship(s) - e.g "https://robertsspaceindustries.com/ship-matrix/index?chassis_id=21"
     * @return List of ships found at URL or null
     */
    @Nullable
    private ArrayList<Ship> fetchShips(String apiUrl) {
        try {
            JSONArray shipArr = new JSONObject(
                    new NetworkRequest(apiUrl, false).get().body
            ).getJSONArray("data");

            ArrayList<Ship> ships = new ArrayList<>();
            for(int i = 0; i < shipArr.length(); i++) {
                Ship ship = parseShip(shipArr.getJSONObject(i));

                // Issue parsing ship
                if(ship == null) {
                    continue;
                }

                ships.add(ship);
            }
            return ships;
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Attempt to fetch all available ships from the Star Citizen API.
     *
     * @return List of all available ships or null
     */
    @Nullable
    private ArrayList<Ship> fetchAllShips() {
        return fetchShips(BASE_API_URL);
    }

    /**
     * Attempt to parse the given JSON data representing a ship in to an object.
     *
     * @param shipData Ship JSON data from API
     * @return Ship or null
     */
    @Nullable
    private Ship parseShip(JSONObject shipData) {
        try {
            final String
                    sizeKey = getOptionalValue(shipData, "size"),
                    prodKey = shipData.getString("production_status"),
                    nameKey = "name";
            final int id = Integer.parseInt(shipData.getString("id"));

            // Parse manufacturer info
            JSONObject manufacturerData = shipData.getJSONObject("manufacturer");

            return new Ship(
                    id,
                    shipData.getString(nameKey),
                    getOptionalValue(shipData, "description"),
                    new Manufacturer(
                            manufacturerData.getString(nameKey),
                            parseImage(manufacturerData)
                    ),
                    new Measurements(
                            shipData.getDouble("length"),
                            shipData.getDouble("beam"),
                            shipData.getDouble("height"),
                            getOptionalInteger(shipData, "mass"),
                            sizeKey == null
                                    ? Measurements.SHIP_SIZE.MISC
                                    : Measurements.SHIP_SIZE.valueOf(sizeKey.toUpperCase())
                    ),
                    new StructuralDetails(
                            getOptionalInteger(shipData, "cargocapacity"),
                            getOptionalInteger(shipData, "min_crew"),
                            getOptionalInteger(shipData, "max_crew")
                    ),
                    Ship.STATUS.valueOf(prodKey.toUpperCase().replaceAll("-", "_")),
                    Ship.TYPE.valueOf(shipData.getString("type").toUpperCase()),
                    correctPath(shipData.getString("url")),
                    BASE_API_URL + "?id=" + id,
                    parseImage(shipData)
            );
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Parse the main image from the given JSON data representing a ship/manufacturer.
     * The JSON data for a ship/manufacturer contains an array where each object represents an image and within
     * each object is an array of sizes.
     * For some reason, the image array only ever contains one image (or none). Return the URL to the largest size of
     * this image.
     *
     * @param data JSON ship/manufacturer data
     * @return Image URL or null
     */
    @Nullable
    private String parseImage(JSONObject data) {
        try {
            ArrayList<HashMap<String, String>> images = parseImages(data);

            if(images.isEmpty()) {
                throw new Exception("No images found in given data: " + data);
            }

            return images.get(0).get(MAIN_IMAGE_KEY);
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Parse the list of images from the given JSON data representing a ship/manufacturer.
     * The JSON data for a ship/manufacturer contains an array where each object represents an image and within
     * each object is an array of sizes.
     * Parse each image in to a map of size key to URL - e.g "logo" -> "https://media.robertsspaceindustries.com/t2bky2nbdg0ms/logo.jpg".
     * Store the maps in a list.
     *
     * @param data JSON ship/manufacturer data
     * @return List of images (may be empty)
     */
    private ArrayList<HashMap<String, String>> parseImages(JSONObject data) {
        try {
            JSONArray mediaArr = data.getJSONArray("media");
            ArrayList<HashMap<String, String>> images = new ArrayList<>();

            for(int i = 0; i < mediaArr.length(); i++) {
                JSONObject mediaData = mediaArr.getJSONObject(i);
                HashMap<String, String> sizes = new HashMap<>();

                // Add main URL
                sizes.put(MAIN_IMAGE_KEY, correctPath(mediaData.getString("source_url")));

                JSONObject sizeData = mediaData.getJSONObject("images");

                // Add other sizes
                for(String sizeKey : sizeData.keySet()) {
                    sizes.put(sizeKey, sizeData.getString(sizeKey));
                }

                // Add map of sizes for image to list of images
                images.add(sizes);
            }

            return images;
        }
        catch(Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Correct the given path from the API, which may be either relative OR absolute.
     * Attach the base path to any relative URLs.
     *
     * @param path Path - e.g "/media/xg8d8kyo0bjsmr/source/HullB_landedcompv3b.jpg"
     * @return Corrected path - e.g "https://robertsspaceindustries.com/media/xg8d8kyo0bjsmr/source/HullB_landedcompv3b.jpg"
     */
    private String correctPath(String path) {
        return path.startsWith("/") ? (BASE_URL + path) : path;
    }

    /**
     * Attempt to retrieve the optional value stored at the given key in the JSON response.
     * Most values included in the API response for a ship are optional.
     * They are provided as either null or a String, regardless of actual type.
     *
     * @param data JSON ship data
     * @param key  Key to return value for
     * @return Value at key or null
     */
    @Nullable
    private String getOptionalValue(JSONObject data, String key) {
        return data.isNull(key) ? null : data.getString(key);
    }

    /**
     * Attempt to retrieve the optional Integer value stored at the given key in the JSON response.
     *
     * @param data JSON ship data
     * @param key  Key to return value for
     * @return Integer value at key or null
     */
    @Nullable
    private Integer getOptionalInteger(JSONObject data, String key) {
        final String value = getOptionalValue(data, key);
        return value == null ? null : Integer.parseInt(value);
    }

    /**
     * Get a ship by a URL to either its store or API page.
     *
     * @param shipUrl URL to the ship (e.g store or API URL).
     * @return Ship or null (doesn't exist or API error)
     */
    @Nullable
    public Ship getShipByUrl(String shipUrl) {

        // Not a URL for a ship
        if(!isShipUrl(shipUrl)) {
            return null;
        }

        ArrayList<Ship> results;

        // Store URL - search cached ships for the relative URL
        if(isStoreUrl(shipUrl)) {

            // Remove trailing slash as URL returned by API does not contain one
            if(shipUrl.endsWith("/")) {
                shipUrl = shipUrl.substring(0, shipUrl.length() - 1);
            }

            // Strip any parameters and finalise
            final String url = shipUrl.split("\\?")[0];

            results = getShips(ship -> ship.getStoreUrl().equalsIgnoreCase(url));
        }

        // API URL - Navigate to the URL and attempt to parse the result (if it is a singular ship)
        else {
            results = fetchShips(shipUrl);
        }

        // Return either null or the first ship
        return results == null || results.isEmpty() ? null : results.get(0);
    }

    /**
     * Check if the given query is a URL to a ship in the store.
     * Ship URLs are in the format: "https://robertsspaceindustries.com/pledge/ships/[KEY]/[NAME]"
     * e.g "https://robertsspaceindustries.com/pledge/ships/890-jump/890-Jump"
     *
     * @param query Query to check
     * @return Query is a URL to a ship in the store
     */
    public static boolean isStoreUrl(String query) {
        return query.matches(BASE_URL + "/pledge/ships/.+/.+");
    }

    /**
     * Check if the given query is a ship API URL.
     * API URLs are in the format: "https://robertsspaceindustries.com/ship-matrix/index?[QUERY]"
     * e.g "https://robertsspaceindustries.com/ship-matrix/index?id=84"
     *
     * @param query Query to check
     * @return Query is an API URL
     */
    public static boolean isApiUrl(String query) {
        return query.startsWith(BASE_API_URL);
    }

    /**
     * Check if the given query is a URL to a ship (e.g store or API URL).
     *
     * @param query Query to check
     * @return Query is a URL to a ship
     */
    public static boolean isShipUrl(String query) {
        return isApiUrl(query) || isStoreUrl(query);
    }

    /**
     * Get a random ship
     *
     * @return Random ship
     */
    public Ship getRandomShip() {
        ArrayList<Ship> allShips = getShips();
        return allShips.get(new Random().nextInt(allShips.size()));
    }

    /**
     * Get a list of ships matching/containing the given name.
     * If a singular match is found, the returned list will contain only the match.
     *
     * @param query Ship name/query
     * @return List of ship search results
     */
    public ArrayList<Ship> getShipsByName(String query) {
        ArrayList<Ship> results = getShips()
                .stream()
                .filter(ship -> ship.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Ship> matching = results
                .stream()
                .filter(ship -> ship.getName().equalsIgnoreCase(query))
                .collect(Collectors.toCollection(ArrayList::new));
        return matching.size() == 1 ? matching : results;
    }
}
