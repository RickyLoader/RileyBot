package Instagram;

import Command.Structure.EmbedHelper;
import Facebook.SocialResponse;
import Instagram.Post.MEDIA_TYPE;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Network.Secret;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Instagram mobile app API functions
 */
public class Instagram {
    private String authKey;
    public static final String
            USER_REGEX = "[A-Za-z0-9._]+",
            LOGO = "https://i.imgur.com/CjkEgbt.png",
            HOSTNAME = "instagram.com",
            BASE_URL = "https://www." + HOSTNAME + "/";

    private static final String
            TYPE_KEY = "media_type",
            COMMENT_KEY = "comment_count",
            LIKE_KEY = "like_count",
            USER_KEY = "user",
            URL_KEY = "url",
            ID_KEY = "pk",
            FEED_ENDPOINT = "feed/",
            LOCATION_KEY = "location",
            ITEMS_KEY = "items",
            CAPTION_KEY = "caption",
            USER_AGENT = "Instagram 195.0.0.31.123 Android (30/11; 420dpi; 1080x1794; Google/google; sdk_gphone_x86_arm; generic_x86_arm; ranchu; en_US; 302733750)",
            BASE_API_URL = "https://i." + HOSTNAME + "/api/v1/",
            BASE_POST_URL = BASE_URL + "p/",
            USER_URL = BASE_URL + USER_REGEX + "(/)?(\\?.*)?",
            POST_URL = BASE_POST_URL + ".+";

    public static final String BASE_EXPLORE_URL = BASE_URL + "explore/";

    /**
     * Login to Instagram and set the auth key required when making further requests
     */
    public Instagram() {
        login();
    }

    /**
     * Log in to Instagram & set the {@code authKey} value
     */
    private void login() {
        final String url = BASE_API_URL + "accounts/login/";
        final String contentType = "application/x-www-form-urlencoded; charset=UTF-8";

        HashMap<String, String> headers = getDefaultHeaders();
        headers.put("Content-Type", contentType);

        JSONObject arguments = new JSONObject()

                // TODO generate this
                .put("enc_password", "#PWD_INSTAGRAM:4:1627541238:" + Secret.ENCODED_INSTAGRAM_PASSWORD)

                .put("username", Secret.INSTAGRAM_EMAIL)
                .put("device_id", "a") // Doesn't seem to be checked
                .put("login_attempt_count", 0);


        final String argumentString = "signed_body=SIGNATURE." + EmbedHelper.urlEncode(arguments.toString());
        RequestBody body = RequestBody.create(MediaType.parse(contentType), argumentString);

        NetworkResponse loginResponse = new NetworkRequest(url, false).post(body, headers, false);
        this.authKey = loginResponse.code == 200 ? loginResponse.headers.get("ig-set-authorization") : null;
    }

    /**
     * Get an Instagram post by a URL to the post
     *
     * @param postUrl URL to an Instagram post - e.g "https://www.instagram.com/p/CSQLSBdgsaQ/"
     * @return Instagram post or null (if unable to retrieve)
     */
    @Nullable
    public Post getPostByUrl(String postUrl) {
        final String postId = getPostIdFromUrl(postUrl);

        // Issue retrieving post ID
        if(postId == null) {
            return null;
        }

        return getPostById(postId);
    }

    /**
     * Get an Instagram post by its unique ID.
     * This is the long form ID, not the short form ID.
     *
     * @param postId Long form post ID - e.g "2634655399042795153_9698694"
     * @return Instagram post or null (if unable to retrieve)
     */
    @Nullable
    private Post getPostById(String postId) {
        JSONObject data = fetchAuthenticatedData("media/" + postId + "/info/");

        // Issue getting post data
        if(data == null) {
            return null;
        }

        ArrayList<Post> posts = parsePosts(data.getJSONArray(ITEMS_KEY));

        // Response does not contain post data
        if(posts.isEmpty()) {
            return null;
        }

        return posts.get(0);
    }

    /**
     * Parse a list of posts from the given JSON array
     *
     * @param postsArray JSON array containing posts
     * @return List of posts
     */
    private ArrayList<Post> parsePosts(JSONArray postsArray) {
        ArrayList<Post> posts = new ArrayList<>();

        for(int i = 0; i < postsArray.length(); i++) {
            posts.add(parsePost(postsArray.getJSONObject(i)));
        }

        return posts;
    }

    /**
     * Parse an Instagram post from the given JSON data of a post.
     *
     * @param postData JSON data of Instagram post
     * @return Instagram post
     */
    private Post parsePost(JSONObject postData) {
        final MEDIA_TYPE type = MEDIA_TYPE.fromCode(postData.getInt(TYPE_KEY));
        ArrayList<Media> media = new ArrayList<>();

        switch(type) {
            case VIDEO:
                media.add(parseVideoMedia(postData));
                break;
            case IMAGE:
                media.add(parseImageMedia(postData));
                break;
            case CAROUSEL:
                media.addAll(parseCarouselMedia(postData));
                break;

            // Unknown post type
            default:
                return null;
        }

        final String caption = postData.isNull(CAPTION_KEY)
                ? null
                : postData.getJSONObject(CAPTION_KEY).getString("text");

        return new Post(
                parseUser(postData.getJSONObject(USER_KEY)),
                caption,
                BASE_POST_URL + postData.getString("code"),
                type,
                postData.has(LOCATION_KEY) ? parseLocation(postData.getJSONObject(LOCATION_KEY)) : null,
                new SocialResponse(
                        postData.has(LIKE_KEY) ? postData.getInt(LIKE_KEY) : 0,
                        postData.has(COMMENT_KEY) ? postData.getInt(COMMENT_KEY) : 0
                ),
                new Date(postData.getLong("taken_at") * 1000),
                caption == null ? new ArrayList<>() : parseMentionedUsers(caption),
                media
        );
    }

    /**
     * Parse a location from the given JSON data of a tagged location within a post.
     *
     * @param locationData JSON data of tagged location
     * @return Location
     */
    private Location parseLocation(JSONObject locationData) {
        return new Location(

                // Depending on whether parsing from a post or a location by ID API request
                locationData.has(ID_KEY) ? locationData.getLong(ID_KEY) : locationData.getLong("location_id"),

                locationData.getString("name")
        );
    }

    /**
     * Parse image media from the given JSON data of an image post.
     *
     * @param imagePostData JSON data of an image post
     * @return Image media
     */
    private ImageMedia parseImageMedia(JSONObject imagePostData) {
        return new ImageMedia(getImageUrl(imagePostData));
    }

    /**
     * Parse video media from the given JSON data of a video post.
     *
     * @param videoPostData JSON data of a video post
     * @return Video media
     */
    private VideoMedia parseVideoMedia(JSONObject videoPostData) {
        return new VideoMedia(
                videoPostData.getJSONArray("video_versions").getJSONObject(0).getString(URL_KEY),
                getImageUrl(videoPostData)
        );
    }

    /**
     * Parse a list of unique mentioned usernames from the given caption of a post.
     *
     * @param caption Caption of a post
     * @return List of mentioned usernames
     */
    private ArrayList<String> parseMentionedUsers(String caption) {
        ArrayList<String> mentioned = new ArrayList<>();
        HashSet<String> usernameSeen = new HashSet<>();

        Matcher matcher = Pattern.compile("@" + USER_REGEX).matcher(caption);

        // Loop through mentioned e.g "@dave"
        while(matcher.find()) {
            final String username = matcher.group().replaceFirst("@", "");

            // Skip duplicates
            if(usernameSeen.contains(username)) {
                continue;
            }

            usernameSeen.add(username);
            mentioned.add(username);
        }
        return mentioned;
    }


    /**
     * Get the URL to an image from the given JSON media data.
     * The media data is the root of an image/video post or an object within the media list of a carousel post.
     *
     * @param mediaData Post media JSON data
     * @return Image URL for media data
     */
    private String getImageUrl(JSONObject mediaData) {
        return mediaData
                .getJSONObject("image_versions2")
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getString(URL_KEY);
    }

    /**
     * Parse the list of media from the given JSON data of a carousel type post.
     * This can contain a mixture of images & videos.
     *
     * @param postData Carousel post JSON data
     * @return List of carousel media
     */
    private ArrayList<Media> parseCarouselMedia(JSONObject postData) {
        ArrayList<Media> media = new ArrayList<>();
        JSONArray carouselData = postData.getJSONArray("carousel_media");

        for(int i = 0; i < carouselData.length(); i++) {
            JSONObject mediaData = carouselData.getJSONObject(i);
            MEDIA_TYPE type = MEDIA_TYPE.fromCode(mediaData.getInt(TYPE_KEY));
            media.add(type == MEDIA_TYPE.VIDEO ? parseVideoMedia(mediaData) : parseImageMedia(mediaData));
        }
        return media;
    }

    /**
     * Parse an Instagram user from the given JSON data of a user.
     *
     * @param userData JSON data of Instagram user
     * @return Instagram user
     */
    private User parseUser(JSONObject userData) {
        final String username = userData.getString("username");
        return new User(
                userData.getLong(ID_KEY),
                userData.getString("profile_pic_url"),
                userData.getString("full_name"),
                username
        );
    }

    /**
     * Attempt to get the ID of a post from a URL to the post.
     * Instagram uses a short form ID - e.g "CSQLSBdgsaQ" and a long form ID - e.g "2625237892992202124_9698691".
     * The short form ID is included in a post URL - e.g "https://www.instagram.com/p/CSQLSBdgsaQ/" however the long
     * form is required when making requests to the API.
     *
     * @param postUrl URL to an Instagram post - e.g "https://www.instagram.com/p/CSQLSBdgsaQ/"
     * @return Post ID or null (if unable to retrieve)
     */
    @Nullable
    private String getPostIdFromUrl(String postUrl) {
        final String url = BASE_API_URL + "oembed/?url=" + postUrl;
        NetworkResponse response = new NetworkRequest(url, false).get(getDefaultHeaders());

        // Issue with the URL
        if(response.code != 200) {
            return null;
        }

        return new JSONObject(response.body).getString("media_id");
    }

    /**
     * Attempt to get an Instagram user from a URL to their profile
     *
     * @param userUrl URL to Instagram user profile - e.g "https://www.instagram.com/username/"
     * @return Instagram user or null (if unable to find)
     */
    @Nullable
    public User getUserFromUrl(String userUrl) {

        // https://www.instagram.com/username/?param&param -> https://www.instagram.com/username/
        userUrl = userUrl.split("\\?")[0];

        // Remove trailing slash
        if(userUrl.endsWith("/")) {
            userUrl = userUrl.substring(0, userUrl.length() - 1);
        }

        final String[] urlArgs = userUrl.split("/");

        // https://www.instagram.com/username -> username
        return getUserByUsername(urlArgs[urlArgs.length - 1]);
    }

    /**
     * Attempt to get an Instagram location from a URL to the location explore page.
     * E.g "https://www.instagram.com/explore/locations/{location_id}/{location_name}"
     * The location name parameter is optional.
     *
     * @param locationExploreUrl URL to Instagram location explore page
     * @return Instagram location or null (if unable to find)
     */
    @Nullable
    public Location getLocationByUrl(String locationExploreUrl) {

        // Remove trailing slash
        if(locationExploreUrl.endsWith("/")) {
            locationExploreUrl = locationExploreUrl.substring(0, locationExploreUrl.length() - 1);
        }

        String[] urlArgs = locationExploreUrl.split("\\?")[0].split("/");

        // May be location ID or location name
        final String lastParam = urlArgs[urlArgs.length - 1];
        final long id = Long.parseLong(lastParam.matches("\\d+") ? lastParam : urlArgs[urlArgs.length - 2]);

        return getLocationById(id);
    }

    /**
     * Get a location by its unique ID
     *
     * @param locationId Unique location ID
     * @return Location or null (if location doesn't exist)
     */
    @Nullable
    private Location getLocationById(long locationId) {
        JSONObject response = fetchAuthenticatedData("locations/" + locationId + "/location_info");

        // Error fetching location
        if(response == null) {
            return null;
        }

        return parseLocation(response);
    }

    /**
     * Get a list of recent posts for the given location
     *
     * @param location Location to get recent posts for
     * @return List of posts made in the location recently
     */
    public ArrayList<Post> getRecentLocationPosts(Location location) {
        return getRecentLocationPostsById(location.getId());
    }

    /**
     * Get a list of recent posts for the given location
     *
     * @param locationId ID of location to get recent posts for
     * @return List of posts made in the location recently
     */
    private ArrayList<Post> getRecentLocationPostsById(long locationId) {
        JSONObject response = fetchAuthenticatedData(FEED_ENDPOINT + "location/" + locationId);

        // Error fetching posts
        if(response == null) {
            return new ArrayList<>();
        }

        return parsePosts(response.getJSONArray("ranked_items"));
    }


    /**
     * Get a list of recent posts from the given user, this is the beginning of their current feed.
     *
     * @param user User to get recent posts for
     * @return List of posts the user has made recently
     */
    public ArrayList<Post> getRecentUserPosts(User user) {
        return getRecentUserPostsById(user.getId());
    }

    /**
     * Get a list of recent posts from the given user, this is the beginning of their current feed.
     *
     * @param userId ID of user to get recent posts for
     * @return List of posts the user has made recently
     */
    private ArrayList<Post> getRecentUserPostsById(long userId) {
        JSONObject response = fetchAuthenticatedData(FEED_ENDPOINT + "user/" + userId);

        // Error fetching posts
        if(response == null) {
            return new ArrayList<>();
        }

        return parsePosts(response.getJSONArray(ITEMS_KEY));
    }

    /**
     * Attempt to find a user by their username.
     *
     * @param username Username - This is the @username of the user e.g "@davedobbyn" -> "davedobbyn"
     * @return User or null (if unable to locate)
     */
    @Nullable
    private User getUserByUsername(String username) {
        JSONObject response = fetchAuthenticatedData(
                "users/search/?search_surface=user_search_page&q=" + EmbedHelper.urlEncode(username)
        );

        // No response
        if(response == null) {
            return null;
        }

        JSONArray results = response.getJSONArray("users");

        // Search through results to find a match
        for(int i = 0; i < results.length(); i++) {
            User user = parseUser(results.getJSONObject(i));
            if(user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }

        return null;
    }

    /**
     * Make an authenticated request for data to the API and return the JSON response.
     * If a not logged in response is received, attempt to login.
     *
     * @param endpoint API endpoint e.g "media/{post_id}/info/"
     * @return JSON response or null (no response/auth error)
     */
    @Nullable
    private JSONObject fetchAuthenticatedData(String endpoint) {
        NetworkRequest request = new NetworkRequest(BASE_API_URL + endpoint, false);
        NetworkResponse response = request.get(getAuthenticationHeaders());

        // Been logged out
        if(response.code == 403) {
            login();

            // Failed to login
            if(authKey == null) {
                return null;
            }

            // Redo the request
            response = request.get(getAuthenticationHeaders());
        }

        // No response - may be invalid endpoint or auth error
        if(response.code != 200) {
            return null;
        }

        return new JSONObject(response.body);
    }

    /**
     * Get a map of header key -> header value.
     * These are the headers required to make authenticated requests to the Instagram API,
     * this includes the current login session key.
     *
     * @return Authentication headers
     */
    private HashMap<String, String> getAuthenticationHeaders() {
        HashMap<String, String> headers = getDefaultHeaders();
        headers.put("Authorization", authKey);
        return headers;
    }

    /**
     * Get a map of header key -> header value.
     * These are the default required headers (user agent etc)
     *
     * @return Default headers
     */
    private HashMap<String, String> getDefaultHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", USER_AGENT);
        return headers;
    }

    /**
     * Check if the given URL is an Instagram user URL.
     * E.g "https://www.instagram.com/username/"
     *
     * @param url URL to check
     * @return URL is an Instagram user URL
     */
    public static boolean isUserUrl(String url) {
        return url.matches(USER_URL);
    }

    /**
     * Check if the given URL is an Instagram post URL.
     * E.g "https://www.instagram.com/p/CSadM2wMoOa/"
     *
     * @param url URL to check
     * @return URL is an Instagram post URL
     */
    public static boolean isPostUrl(String url) {
        return url.matches(POST_URL);
    }

    /**
     * Check if the given URL is an Instagram location explore URL.
     * The location name parameter is optional.
     * E.g "https://www.instagram.com/explore/locations/{location_id}/{location_name}"
     *
     * @param url URL to check
     * @return URL is an Instagram location explore URL
     */
    public static boolean isLocationUrl(String url) {
        return url.matches(Location.BASE_EXPLORE_URL + ".+");
    }

    /**
     * Check if the given URL is an Instagram URL (either a user profile or post URL).
     *
     * @param url URL to check
     * @return URL is an Instagram URL
     */
    public static boolean isInstagramUrl(String url) {
        return isUserUrl(url) || isPostUrl(url) || isLocationUrl(url);
    }
}
