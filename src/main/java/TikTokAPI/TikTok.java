package TikTokAPI;

import Command.Structure.EmbedHelper;
import Command.Structure.ImageLoadingMessage;
import Network.NetworkRequest;
import Network.NetworkResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;

/**
 * TikTok API functions for retrieving user/video details
 */
public class TikTok {
    public static final String
            LOGO = "https://i.imgur.com/yVw8Llj.png",
            BASE_WEB_URL = "https://www.tiktok.com/";

    private static final String
            BASE_SHORT_URL = "https://vm.tiktok.com/",
            BASE_MOBILE_URL = "https://m.tiktok.com/",
            WEB_VIDEO_URL = BASE_WEB_URL + "@.+/video/[0-9]+(/)?(\\?.+)?",
            SHORT_VIDEO_URL = BASE_SHORT_URL + "(.+)+(/)?(\\?.+)?",
            MOBILE_VIDEO_URL = BASE_MOBILE_URL + "v/[0-9]+(/)?(\\?.+)?",
            BASE_API_URL = BASE_MOBILE_URL + "api/",
            USER_AGENT = "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36";

    private final Signer signer;
    private final HashMap<String, String> headers;

    /**
     * Initialise the API URL signer & request headers
     */
    public TikTok() {
        this.signer = new Signer(USER_AGENT);
        this.headers = getHeaders();
    }

    /**
     * Check if the given URL is a TikTok video URL
     * These come in various formats.
     *
     * @param query Query to check
     * @return Query is a TikTok video URL
     */
    public static boolean isVideoUrl(String query) {
        return isShortVideoUrl(query) || isWebVideoUrl(query) || isMobileVideoUrl(query);
    }

    /**
     * Check if the given URL is a mobile TikTok video URL
     * e.g https://m.tiktok.com/v/6977559793368124674
     *
     * @param query Query to check
     * @return Query is a mobile TikTok video URL
     */
    public static boolean isMobileVideoUrl(String query) {
        return query.matches(MOBILE_VIDEO_URL);
    }

    /**
     * Check if the given URL is a web (normal) TikTok video URL
     * e.g https://www.tiktok.com/@daviddobrik/video/6982679574333213957
     *
     * @param query Query to check
     * @return Query is a web TikTok video URL
     */
    public static boolean isWebVideoUrl(String query) {
        return query.matches(WEB_VIDEO_URL);
    }

    /**
     * Check if the given URL is a shortened TikTok video URL
     * e.g https://vm.tiktok.com/ZSJ4XccKG/
     *
     * @param query Query to check
     * @return Query is a shortened TikTok video URL
     */
    private static boolean isShortVideoUrl(String query) {
        return query.matches(SHORT_VIDEO_URL);
    }

    /**
     * Generate the required headers for making requests to the API.
     * These are only used to track your request chain and their values aren't actually validated.
     * e.g Video download URLs provided in a {@link #getVideoById(String)} response will return a 403 Forbidden code
     * if the headers don't match the request made to retrieve them.
     *
     * @return Required API headers
     */
    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("referer", BASE_WEB_URL);
        headers.put("User-Agent", signer.getUserAgent());
        headers.put("Cookie", "tt_webid_v2=6920769581394165602;"); // TODO this value could be randomised
        return headers;
    }

    /**
     * Get a TikTok video by its URL.
     *
     * @param url URL to TikTok video
     * @return TikTok video from URL or null (on API error/not a valid URL)
     */
    @Nullable
    public TikTokPost getVideoByUrl(String url) {
        if(!isVideoUrl(url)) {
            return null;
        }

        // Shortened video URL requires an extra request to resolve the original URL
        String[] urlArgs = (isShortVideoUrl(url) ? getFullVideoUrl(url) : url)
                .split("\\?")[0] // Remove any parameters
                .split("/");

        return getVideoById(urlArgs[urlArgs.length - 1]); // Video ID is always the final argument
    }

    /**
     * Make a request to TikTok with a shortened TikTok post URL.
     * TikTok responds with a 301 moved & provides the full URL to the post.
     * Return the full URL.
     *
     * @param shortVideoUrl Shortened TikTok URL
     * @return Full TikTok URL
     */
    private String getFullVideoUrl(String shortVideoUrl) {
        if(!isShortVideoUrl(shortVideoUrl)) {
            return shortVideoUrl;
        }

        // Ignore redirects
        NetworkResponse response = new NetworkRequest(shortVideoUrl, false, false).get();

        // Issue with URL
        if(response.code != 301) {
            return shortVideoUrl;
        }

        String fullUrl = response.headers.get("Location");

        // No redirect provided
        if(fullUrl == null) {
            return shortVideoUrl;
        }

        // URL is given in mobile format: https://m.tiktok.com/v/6950804474768264450.html?arg1&arg2...
        return fullUrl.split("\\?")[0].replaceFirst(".html", "");
    }

    /**
     * Get a TikTok video by its unique ID.
     *
     * @param videoId Unique video ID - e.g "6982679574333213957"
     * @return TikTok video from ID or null (on API error/invalid ID)
     */
    @Nullable
    private TikTokPost getVideoById(@NotNull String videoId) {
        final String url = generateApiVideoUrl(videoId);
        final String itemKey = "itemInfo";

        JSONObject responseData = apiRequest(url);

        // Request failed
        if(responseData == null || !responseData.has(itemKey)) {
            return null;
        }

        // Contains info on the author & video
        JSONObject itemData = responseData.getJSONObject(itemKey).getJSONObject("itemStruct");

        JSONObject videoData = itemData.getJSONObject("video");

        Creator creator = parseCreator(itemData.getJSONObject("author"));
        String description = itemData.getString("desc");

        return new TikTokPost(
                downloadVideo(videoData.getString("downloadAddr")),
                description.isEmpty() ? null : description,
                BASE_WEB_URL + "@" + creator.getId() + "/video/" + videoData.getString("id"),
                downloadImage(videoData.getString("cover")),
                parseSocialResponse(itemData.getJSONObject("stats")),
                creator,
                parseMusic(itemData.getJSONObject("music")),
                new Date(itemData.getLong("createTime") * 1000)
        );
    }

    /**
     * Parse the social response to a TikTok post from the given API social stats JSON
     *
     * @param socialData API social stats data
     * @return Social response from data
     */
    private SocialResponse parseSocialResponse(JSONObject socialData) {
        return new SocialResponse(
                socialData.getInt("diggCount"), // Likes are called diggs for some reason
                socialData.getInt("commentCount"),
                socialData.getInt("shareCount"),
                socialData.getInt("playCount")
        );
    }

    /**
     * Parse a TikTok music track from the given API music JSON
     *
     * @param musicData API music data
     * @return Music from data
     */
    private Music parseMusic(JSONObject musicData) {
        return new Music(
                musicData.getString("title"),
                musicData.getString("authorName"),
                downloadImage(musicData.getString("coverThumb"))
        );
    }

    /**
     * Parse a TikTok creator from the given API creator JSON
     *
     * @param creatorData API creator data
     * @return Creator from data
     */
    private Creator parseCreator(JSONObject creatorData) {
        final String uniqueId = creatorData.getString("uniqueId");
        final String signature = creatorData.getString("signature");
        return new Creator(
                uniqueId,
                creatorData.getString("nickname"),
                signature.isEmpty() ? null : signature,
                BASE_WEB_URL + "@" + uniqueId,
                downloadImage(creatorData.getString("avatarThumb"))
        );
    }

    /**
     * Initialise a URL connection to a TikTok download URL (image/video address from the API).
     * Attach the required headers.
     *
     * @param downloadUrl Download URL to initialise a connection to
     * @return URL connection or null (if error occurs)
     */
    @Nullable
    private URLConnection initialiseDownloadConnection(String downloadUrl) {
        try {
            URLConnection connection = new URL(downloadUrl).openConnection();
            for(String header : headers.keySet()) {
                connection.setRequestProperty(header, headers.get(header));
            }
            return connection;
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Attempt to download a TikTok image from the given URL.
     * This is not a browser URL but a download address returned from the API.
     *
     * @param imageUrl Image download address
     * @return Image byte array or null
     */
    private byte[] downloadImage(String imageUrl) {
        URLConnection connection = initialiseDownloadConnection(imageUrl);

        // Issue downloading image
        if(connection == null) {
            return null;
        }

        return ImageLoadingMessage.imageToByteArray(EmbedHelper.downloadImage(connection));
    }

    /**
     * Attempt to download a TikTok video from the given URL.
     * This is not a browser URL but a download address returned from {@link #getVideoById(String)}.
     * Returns null if the video size exceeds 8MB or an error occurs.
     *
     * @param videoUrl Video download address
     * @return Video byte array or null
     */
    private byte[] downloadVideo(String videoUrl) {
        URLConnection connection = initialiseDownloadConnection(videoUrl);
        return connection == null ? null : EmbedHelper.downloadVideo(connection);
    }

    /**
     * Make a request to the TikTok API and return the response JSON.
     * The response JSON may be invalid if there was an issue with the request, but it will still
     * return a 200 OK response code.
     * The JSON should be checked for the expected values before attempting to use.
     *
     * @param url TikTok API URL - must NOT be signed
     * @return JSON response from API or null (if no response)
     */
    @Nullable
    private JSONObject apiRequest(@NotNull String url) {
        final String signedUrl = signer.signUrl(url);

        // Issue signing
        if(signedUrl == null) {
            return null;
        }

        NetworkResponse response = new NetworkRequest(signedUrl, false).get(headers);

        // No response - still could be an invalid response (they always return 200 OK)
        if(response.code != 200 || response.body.isEmpty()) {
            return null;
        }
        return new JSONObject(response.body);
    }

    /**
     * Generate the API URL required to fetch the details of the TikTok video from the given video ID.
     *
     * @param videoId Unique video ID - e.g "6982679574333213957"
     * @return API URL required to fetch video details
     */
    private String generateApiVideoUrl(@NotNull String videoId) {
        return BASE_API_URL + "item/detail/?itemId=" + videoId;
    }
}
