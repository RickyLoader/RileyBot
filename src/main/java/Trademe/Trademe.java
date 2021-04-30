package Trademe;

import Network.NetworkRequest;
import Network.Secret;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetch Trademe listing details
 */
public class Trademe {
    private final HashMap<String, String> headers = new HashMap<>();
    public final static String
            BASE_URL = "https://www.trademe.co.nz/",
            TRADEME_LOGO = "https://i.imgur.com/tTVElnt.png",
            LISTING_ID = "listingid",
            TRADEME_URL = BASE_URL + "(.+)/(Listing\\.aspx\\?id=)?(listing-)?(?<" + LISTING_ID + ">\\d+)(.+)?",
            API_URL = "https://api.trademe.co.nz/v1/";

    public Trademe() {
        headers.put(
                "Authorization",
                "OAuth oauth_consumer_key=\"" + Secret.TRADEME_CONSUMER_KEY + "\""
                        + ", oauth_signature_method=\"PLAINTEXT\""
                        + ", oauth_signature=\"" + Secret.TRADEME_CONSUMER_SECRET + "&\""
        );

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
        return getListingById(matcher.group(LISTING_ID));
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
    public Listing getListingById(String id) {
        JSONObject listingDetails = apiRequest("Listings/" + id + ".json?return_member_profile=true");

        ArrayList<String> images = new ArrayList<>();
        String PHOTOS = "Photos";

        if(!listingDetails.isNull(PHOTOS)) {
            JSONArray photos = listingDetails.getJSONArray(PHOTOS);
            for(int i = 0; i < photos.length(); i++) {
                JSONObject photo = photos.getJSONObject(i);
                images.add(photo.getJSONObject("Value").getString("FullSize"));
            }
        }
        String BIDDERS = "BiddersAndWatchers";
        return new Listing(
                new Listing.ListingOverview(
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
