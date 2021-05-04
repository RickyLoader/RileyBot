package TrademeAPI;

import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Network.Secret;
import TrademeAPI.Listing.ListingOverview;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Fetch Trademe listing details
 */
public class Trademe {
    private final HashMap<String, String> headers;
    private final HashMap<String, Category> categoriesByNumber;
    private final Category rootCategory, adultCategory;
    public final static String
            BASE_URL = "https://www.trademe.co.nz/",
            TRADEME_LOGO = "https://i.imgur.com/tTVElnt.png",
            LISTING_ID = "listingid",
            TRADEME_URL = BASE_URL + "(.+)/(Listing\\.aspx\\?id=)?(listing-)?(?<" + LISTING_ID + ">\\d+)(.+)?",
            API_URL = "https://api.trademe.co.nz/v1/",
            NO_SEARCH_RESULTS_IMAGE = "https://i.imgur.com/VJVbVZu.png",
            ROOT_CATEGORY_NUMBER = "0000",
            ADULT_CATEGORY_NUMBER = "0004-3267-8324-";

    /**
     * Initialise authentication headers & listing categories
     */
    public Trademe() {
        this.headers = getHeaders();
        this.categoriesByNumber = fetchCategories();
        this.rootCategory = categoriesByNumber.get(ROOT_CATEGORY_NUMBER);
        this.adultCategory = categoriesByNumber.get(ADULT_CATEGORY_NUMBER);
    }

    /**
     * Get the root category (for searching all categories)
     *
     * @return Root category
     */
    public Category getRootCategory() {
        return rootCategory;
    }

    /**
     * Get a map of headers required to authenticate with the Trademe API
     *
     * @return Authentication header map
     */
    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(
                "Authorization",
                "OAuth oauth_consumer_key=\"" + Secret.TRADEME_CONSUMER_KEY + "\""
                        + ", oauth_signature_method=\"PLAINTEXT\""
                        + ", oauth_signature=\"" + Secret.TRADEME_CONSUMER_SECRET + "&\""
        );
        return headers;
    }

    /**
     * Create a map of category number -> category
     *
     * @return Map of category number -> category
     */
    private HashMap<String, Category> fetchCategories() {
        HashMap<String, Category> categoriesByNumber = new HashMap<>();
        JSONObject rootCategory = apiRequest("Categories.json");

        // Change name from Root to All
        rootCategory
                .put("Name", "All")
                .put("Number", ROOT_CATEGORY_NUMBER);
        parseCategory(rootCategory, categoriesByNumber);
        return categoriesByNumber;
    }

    /**
     * Parse a category from its JSON and add to the given map by its unique category number.
     * Categories may hold subcategories, but as the category number is always unique,
     * flatten the tree by recursively visiting all categories.
     *
     * @param categoryData       JSON category
     * @param categoriesByNumber Map to add category to
     */
    private void parseCategory(JSONObject categoryData, HashMap<String, Category> categoriesByNumber) {
        Category category = new Category(
                categoryData.getString("Name"),
                categoryData.getString("Number"),
                categoryData.getString("Path")
        );
        categoriesByNumber.put(category.getNumber(), category);

        String key = "Subcategories";
        if(!categoryData.has(key)) {
            return;
        }

        JSONArray subcategories = categoryData.getJSONArray(key);
        for(int i = 0; i < subcategories.length(); i++) {
            parseCategory(subcategories.getJSONObject(i), categoriesByNumber);
        }
    }

    /**
     * Get a list of categories matching/containing the given name.
     * If any matching categories are found they will be returned,
     * if no matching categories are found, categories containing the given name will be returned.
     *
     * @param name Name to search for
     * @return List of matching categories
     */
    public ArrayList<Category> getCategoriesByName(String name) {
        ArrayList<Category> matching = filterCategories(category -> category.getName().equalsIgnoreCase(name));
        if(!matching.isEmpty()) {
            return matching;
        }
        return filterCategories(category -> category.getName().toLowerCase().contains(name.toLowerCase()));
    }

    /**
     * Filter the categories by the given predicate
     *
     * @param filter Predicate to use to filter categories
     * @return List of categories passing the given filter
     */
    private ArrayList<Category> filterCategories(Predicate<Category> filter) {
        return categoriesByNumber
                .values()
                .stream()
                .filter(filter)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get a list of all listing categories
     *
     * @return List of listing categories
     */
    public ArrayList<Category> getCategories() {
        return new ArrayList<>(categoriesByNumber.values());
    }

    /**
     * Parse the given date String from the format "/Date(1620091238003)/" to a date
     *
     * @param dateString Date String in the format "/Date(1620091238003)/"
     * @return Date of date String
     */
    private Date parseDate(String dateString) {
        String timestamp = "timestamp";
        String dateRegex = "/Date\\((?<" + timestamp + ">\\d+)\\)/";
        Matcher matcher = Pattern.compile(dateRegex).matcher(dateString);
        if(!matcher.find()) {
            return new Date();
        }
        return new Date(Long.parseLong(matcher.group(timestamp)));
    }

    /**
     * Check if the given URL is a Trademe listing URL
     *
     * @param url URL to check
     * @return URL is a Trademe listing URL
     */
    public static boolean isTrademeUrl(String url) {
        return url.matches(TRADEME_URL);
    }

    /**
     * Check whether the given String is a category ID
     *
     * @param categoryString String to check
     * @return String is a category ID
     */
    public static boolean isCategoryId(String categoryString) {
        return categoryString.matches("(\\d+-?)+");
    }

    /**
     * Parse the member details from the JSON of a listing
     *
     * @param listingDetails Listing JSON
     * @return Member of null
     */
    private Member parseMemberDetails(JSONObject listingDetails) {
        String MEMBER = "Member";
        if(listingDetails.isNull(MEMBER)) {
            return null;
        }
        JSONObject member = listingDetails.getJSONObject(MEMBER);
        return new Member(
                member.getString("Nickname"),
                member.getLong("MemberId"),
                Trademe.TRADEME_LOGO
        );
    }

    /**
     * Search for listing overviews with the given query in the title.
     * Make a secondary search for adult results if the given category is the root category.
     * (Adult results aren't returned unless searching the adult category specifically)
     * If a singular matching title is found, the returned list will contain only the match,
     * otherwise the list will contain all listing overviews with the given query in the title.
     *
     * @param titleQuery Query to search for in listing titles
     * @param category   Category to search in
     * @return List of listing overviews containing/matching query
     */
    public ArrayList<ListingOverview> searchListingsByTitle(String titleQuery, Category category) {
        ArrayList<ListingOverview> results = getListingOverviewsByTitle(titleQuery, category);
        if(category == rootCategory) {
            results.addAll(getListingOverviewsByTitle(titleQuery, adultCategory));
        }
        ArrayList<ListingOverview> matching = results.stream()
                .filter(overview -> overview.getTitle().equalsIgnoreCase(titleQuery))
                .collect(Collectors.toCollection(ArrayList::new));
        return matching.size() == 1 ? matching : results;
    }

    /**
     * Search for listing overviews with the given query in the title.
     *
     * @param titleQuery Query to search for in listing titles
     * @param category   Category to search in
     * @return List of listing overviews containing/matching query
     */
    private ArrayList<ListingOverview> getListingOverviewsByTitle(String titleQuery, Category category) {
        String endpoint = "Search/General.json?search_string="
                + EmbedHelper.urlEncode(titleQuery);

        if(category != rootCategory) {
            endpoint += "&category=" + category.getNumber();
        }

        JSONObject response = apiRequest(endpoint);
        JSONArray results = response.getJSONArray("List");
        ArrayList<ListingOverview> listings = new ArrayList<>();

        for(int i = 0; i < results.length(); i++) {
            JSONObject listing = results.getJSONObject(i);
            listings.add(parseListingOverview(listing));
        }
        return listings;
    }

    /**
     * Get a category by its unique number
     *
     * @param categoryNumber Category number
     * @return Category with number or null
     */
    public Category getCategoryByNumber(String categoryNumber) {
        return categoriesByNumber.get(categoryNumber);
    }

    /**
     * Get the details of a listing from the given URL
     *
     * @param url Trademe URL
     * @return Listing details of URL or null
     */
    public Listing getListingByUrl(String url) {
        Matcher matcher = Pattern.compile(TRADEME_URL).matcher(url);
        if(!matcher.find()) {
            return null;
        }
        return getListingById(Long.parseLong(matcher.group(LISTING_ID)));
    }

    /**
     * Make a request to the Trademe API
     *
     * @param endpoint API endpoint - e.g "Listings/{LISTING_ID}.json"
     * @return JSON response
     */
    private JSONObject apiRequest(String endpoint) {
        return new JSONObject(new NetworkRequest(API_URL + endpoint, false).get(headers).body);
    }

    /**
     * Get the details of a listing by a listing id
     *
     * @param id Listing id
     * @return Listing details or null
     */
    public Listing getListingById(long id) {
        JSONObject listingDetails = apiRequest("Listings/" + id + ".json?return_member_profile=true");
        if(listingDetails.has("ErrorDescription")) {
            return null;
        }
        ArrayList<String> images = new ArrayList<>();
        String PHOTOS = "Photos";

        if(!listingDetails.isNull(PHOTOS)) {
            JSONArray photos = listingDetails.getJSONArray(PHOTOS);
            for(int i = 0; i < photos.length(); i++) {
                JSONObject photo = photos.getJSONObject(i);
                images.add(photo.getJSONObject("Value").getString("FullSize"));
            }
        }
        String BIDDERS = "BidderAndWatchers";
        return new Listing(
                parseListingOverview(listingDetails),
                listingDetails.getString("Body"),
                parseDate(listingDetails.getString("EndDate")),
                listingDetails.has(BIDDERS) ? listingDetails.getInt(BIDDERS) : 0,
                images,
                parseMemberDetails(listingDetails)
        );
    }

    /**
     * Parse a listing overview from the given listing JSON.
     * Listing JSON may be from search results or specific listing.
     *
     * @param listingDetails Listing JSON
     * @return Listing overview
     */
    private ListingOverview parseListingOverview(JSONObject listingDetails) {
        return new ListingOverview(
                listingDetails.getString("Title"),
                listingDetails.getLong("ListingId"),
                listingDetails.getString("PriceDisplay"),
                getCategoryByNumber(listingDetails.getString("Category"))
        );
    }
}
