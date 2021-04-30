package TrademeAPI;

import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Network.Secret;
import TrademeAPI.Listing.ListingOverview;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetch Trademe listing details
 */
public class Trademe {
    private final HashMap<String, String> headers;
    private final HashMap<String, Category> categories;
    public final static String
            BASE_URL = "https://www.trademe.co.nz/",
            TRADEME_LOGO = "https://i.imgur.com/tTVElnt.png",
            LISTING_ID = "listingid",
            TRADEME_URL = BASE_URL + "(.+)/(Listing\\.aspx\\?id=)?(listing-)?(?<" + LISTING_ID + ">\\d+)(.+)?",
            API_URL = "https://api.trademe.co.nz/v1/",
            NO_SEARCH_RESULTS_IMAGE = "https://i.imgur.com/VJVbVZu.png";

    /**
     * Initialise authentication headers & listing categories
     */
    public Trademe() {
        this.headers = getHeaders();
        this.categories = fetchCategories();
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
     * Get a listing category by name
     *
     * @param name Name of category to retrieve
     * @return Category matching name or null
     */
    public Category getCategoryByName(String name) {
        return categories.get(name.toLowerCase());
    }

    /**
     * Get a list of listing categories matching/containing the given name
     *
     * @return List of matching categories
     */
    public ArrayList<Category> getCategoriesByName(String name) {
        ArrayList<Category> results = new ArrayList<>();
        for(String categoryName : categories.keySet()) {
            if(!categoryName.contains(name.toLowerCase()) && !categoryName.equalsIgnoreCase(name)) {
                continue;
            }
            results.add(categories.get(categoryName));
        }
        return results;
    }

    /**
     * Get a list of all listing categories
     *
     * @return List of listing categories
     */
    public ArrayList<Category> getCategories() {
        return new ArrayList<>(categories.values());
    }

    /**
     * Get a map of listing categories.
     * Map from category name -> category
     *
     * @return Map of listing categories
     */
    private HashMap<String, Category> fetchCategories() {
        String key = "Subcategories";
        HashMap<String, Category> categories = new HashMap<>();
        JSONArray generalCategoryList = apiRequest("Categories.json").getJSONArray(key);
        for(int i = 0; i < generalCategoryList.length(); i++) {
            JSONObject generalCategory = generalCategoryList.getJSONObject(i);
            if(!generalCategory.has(key)) {
                continue;
            }
            JSONArray subCategories = generalCategory
                    .getJSONArray(key);
            for(int j = 0; j < subCategories.length(); j++) {
                JSONObject categoryData = subCategories.getJSONObject(j);
                Category category = new Category(
                        categoryData.getString("Name"),
                        categoryData.getString("Number")
                );
                categories.put(category.getName().toLowerCase(), category);
            }
        }
        return categories;
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
     * If a match is not found, return an array of listing overviews containing the given query in the title.
     *
     * @param titleQuery Query to search for in listing titles
     * @param category   Category to search in (null = all categories)
     * @return Listing overviews containing/matching query
     */
    public ListingOverview[] getListingOverviewsByTitle(String titleQuery, Category category) {
        String endpoint = "Search/General.json?search_string="
                + EmbedHelper.urlEncode(titleQuery);
        if(category != null) {
            endpoint += "&category=" + category.getNumber();
        }
        JSONObject response = apiRequest(endpoint);
        JSONArray results = response.getJSONArray("List");
        ListingOverview[] listings = new ListingOverview[results.length()];

        for(int i = 0; i < listings.length; i++) {
            JSONObject listing = results.getJSONObject(i);
            ListingOverview overview = new ListingOverview(
                    listing.getString("Title"),
                    listing.getLong("ListingId"),
                    listing.getString("PriceDisplay")
            );
            listings[i] = overview;
        }

        ListingOverview[] matching = Arrays.stream(listings)
                .filter(overview -> overview.getTitle().equalsIgnoreCase(titleQuery))
                .toArray(ListingOverview[]::new);
        return matching.length == 1 ? matching : listings;
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
                new ListingOverview(
                        listingDetails.getString("Title"),
                        id,
                        listingDetails.getString("PriceDisplay")
                ),
                listingDetails.getString("Body"),
                parseDate(listingDetails.getString("EndDate")),
                listingDetails.has(BIDDERS) ? listingDetails.getInt(BIDDERS) : 0,
                images,
                parseMemberDetails(listingDetails)
        );
    }
}
