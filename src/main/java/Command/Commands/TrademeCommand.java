package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.PageableTrademeListing;
import Network.NetworkRequest;
import Network.Secret;
import Trademe.Listing;
import Trademe.Member;
import net.dv8tion.jda.api.entities.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Take Trademe listing URLs and replace with an embed detailing the listing
 */
public class TrademeCommand extends DiscordCommand {
    private final HashMap<String, String> headers = new HashMap<>();
    public final static String
            BASE_URL = "https://www.trademe.co.nz/",
            TRADEME_LOGO = "https://i.imgur.com/tTVElnt.png";

    private final String
            LISTING_ID = "listingid",
            TRADEME_URL = BASE_URL + "(.+)/(Listing\\.aspx\\?id=)?(listing-)?(?<" + LISTING_ID + ">\\d+)(.+)?";

    public TrademeCommand() {
        super("[trademe url]", "Embed Trademe listings!");
        setSecret(true);
        headers.put(
                "Authorization",
                "OAuth oauth_consumer_key=\"" + Secret.TRADEME_CONSUMER_KEY + "\""
                        + ", oauth_signature_method=\"PLAINTEXT\""
                        + ", oauth_signature=\"" + Secret.TRADEME_CONSUMER_SECRET + "&\""
        );
    }

    @Override
    public void execute(CommandContext context) {
        Listing listing = fetchListing(context.getMessageContent());
        if(listing == null) {
            return;
        }
        context.getMessage().delete().queue(deleted -> new PageableTrademeListing(context, listing).showMessage());
    }

    /**
     * Fetch the details of the listing at the given URL using the Trademe API
     *
     * @param url URL to the listing
     * @return Listing details or null
     */
    private Listing fetchListing(String url) {
        Matcher matcher = Pattern.compile(TRADEME_URL).matcher(url);

        if(!matcher.find()) {
            return null;
        }

        String apiUrl = "https://api.trademe.co.nz/v1/Listings/"
                + matcher.group(LISTING_ID)
                + ".json?return_member_profile=true";

        JSONObject listingDetails = new JSONObject(
                new NetworkRequest(apiUrl, false).get(headers).body
        );

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
                url,
                listingDetails.getString("Title"),
                listingDetails.getString("Body"),
                parseDate(listingDetails.getString("EndDate")),
                listingDetails.has(BIDDERS) ? listingDetails.getInt(BIDDERS) : 0,
                images,
                parseMemberDetails(listingDetails),
                listingDetails.getString("PriceDisplay")
        );
    }

    /**
     * Parse the member details from the JSON of a Trademe listing
     *
     * @param listingDetails Trademe listing JSON
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
                TRADEME_LOGO
        );
    }

    /**
     * Parses the given Trademe date String from the format "/Date(1620091238003)/" to a date
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

    @Override
    public boolean matches(String query, Message message) {
        return message.getContentDisplay().matches(TRADEME_URL);
    }
}
