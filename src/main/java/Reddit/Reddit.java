package Reddit;

import COD.Assets.Ratio;
import Command.Commands.GIFCommand;
import Network.ImgurManager;
import Network.NetworkRequest;
import Network.NetworkResponse;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.Date;

/**
 * Reddit API functions
 */
public class Reddit {
    public static final String LOGO = "https://i.imgur.com/zSNgbNA.png";
    private static final String
            BASE_URL = "https://www.reddit.com",
            SUBREDDIT_REGEX = "https://(www.|old.)?reddit.com/r/[a-zA-Z0-9_]+/?",
            STANDARD_URL_REGEX = SUBREDDIT_REGEX + ".+/comments/.+/.+/?",
            VIDEO_URL_REGEX = BASE_URL + "/video/.+",
            SHORTENED_VIDEO_URL_REGEX = "https://v.redd.it/.+",
            GALLERY_POST_URL_REGEX = BASE_URL + "/gallery/.+",
            POLL_KEY = "poll_data",
            DATA_KEY = "data",
            HINT_KEY = "post_hint",
            JSON = ".json?raw_json=1",
            CHILDREN_KEY = "children",
            URL_KEY = "url",
            THUMBNAIL_KEY = "thumbnail",
            MEDIA_KEY = "media",
            VOTE_COUNT_KEY = "vote_count",
            CROSSPOST_KEY = "crosspost_parent_list";

    public enum URL_TYPE {
        SUBREDDIT, // https://www.reddit.com/r/Eyebleach/
        STANDARD, // https://www.reddit.com/r/CrazyFuckingVideos/comments/ov5lty/looks_fresh
        SHORTENED_VIDEO, // https://v.redd.it/1li3tukkbje71
        STANDARD_VIDEO, // https://reddit.com/video/1li3tukkbje71
        GALLERY_POST, // https://www.reddit.com/gallery/hrrh23
        NONE
    }

    /**
     * Get the info for a subreddit from the given URL to a subreddit
     *
     * @param url URL to a subreddit
     * @return Subreddit or null (if unable to retrieve)
     */
    @Nullable
    public Subreddit getSubredditByUrl(String url) {

        // Not a valid URL
        if(getUrlType(url) != URL_TYPE.SUBREDDIT) {
            return null;
        }

        String[] urlArgs = url.split("/");
        String name = urlArgs[urlArgs.length - 1];

        // Remove trailing slash
        if(name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }

        return getSubredditByName(name);
    }

    /**
     * Get the info for a Reddit post from the given URL to the post
     *
     * @param url URL to Reddit post
     * @return Reddit post or null (if unable to retrieve)
     */
    @Nullable
    public RedditPost getPostByUrl(String url) {

        // Not a valid URL
        if(!isRedditPostUrl(url)) {
            return null;
        }

        // Shortened video post URLs / Direct gallery URLs must have their full URLs resolved to work with API
        if(isShortenedUrl(url)) {
            url = expandShortenedUrl(url);

            // Not a valid short URL
            if(url == null) {
                return null;
            }
        }

        // Strip parameters
        url = url.split("\\?")[0];
        NetworkResponse response = new NetworkRequest(url + JSON, false).get();

        // Failed to retrieve JSON (issue with Reddit/URL)
        if(response.code != 200) {
            return null;
        }

        JSONObject postData = new JSONArray(response.body)
                .getJSONObject(0)
                .getJSONObject(DATA_KEY)
                .getJSONArray(CHILDREN_KEY)
                .getJSONObject(0);

        return parsePost(postData, true);
    }

    /**
     * Parse a Reddit post from the given post JSON data retrieved from the API.
     *
     * @param postData   Reddit post JSON data
     * @param fetchVideo Fetch video download URL (slower)
     * @param subreddit  Optional subreddit (prevents having to make a request to find it)
     * @return Reddit post from data
     */
    private RedditPost parsePost(JSONObject postData, boolean fetchVideo, Subreddit... subreddit) {
        JSONObject info = postData.getJSONObject(DATA_KEY);

        final String url = BASE_URL + info.getString("permalink");

        return new RedditPost(
                info.getString("title"),
                info.getString("author"),
                url,
                getPostContent(info, url, fetchVideo),
                calculateRatio(info.getInt("ups"), info.getDouble("upvote_ratio")),
                info.getInt("num_comments"),
                subreddit.length == 0 ? getSubredditByName(info.getString("subreddit")) : subreddit[0],
                new Date(info.getLong("created_utc") * 1000)
        );
    }

    /**
     * Attempt to locate the full URL for a shortened video URL, how this is done depends on the type of URL.
     *
     * @param url Shortened Reddit URL
     * @return Full URL or null (invalid shortened URL/Reddit issue)
     */
    @Nullable
    private String expandShortenedUrl(String url) {

        // Not a short URL
        if(!isShortenedUrl(url)) {
            return null;
        }

        URL_TYPE type = getUrlType(url);

        // Shortened video URL - e.g "https://v.redd.it/1li3tukkbje71"
        if(type == URL_TYPE.SHORTENED_VIDEO) {
            return expandShortenedVideoUrl(url);
        }

        // Direct URL to a gallery post - e.g "https://www.reddit.com/gallery/hrrh23"
        else {
            return expandGalleryUrl(url);
        }
    }

    /**
     * Make a request to Reddit to resolve the full URL for a shortened video post URL.
     * When requesting the shortened URL, Reddit responds with a 302 Moved response code,
     * with the actual URL in a {@code Location} response header.
     *
     * @param shortVideoUrl Shortened URL to a video post - e.g "https://v.redd.it/1li3tukkbje71"
     * @return Full URL or null (invalid shortened URL/Reddit issue)
     */
    @Nullable
    private String expandShortenedVideoUrl(String shortVideoUrl) {

        // Invalid URL
        if(getUrlType(shortVideoUrl) != URL_TYPE.SHORTENED_VIDEO) {
            return null;
        }

        NetworkResponse response = new NetworkRequest(shortVideoUrl, false, false).get();

        // Unexpected response
        if(response.code != 302) {
            return null;
        }

        return response.headers.get("Location");
    }

    /**
     * Attempt to resolve the Reddit post URL from a direct URL to a post gallery.
     * Request the HTML of the gallery and scrape the URL to the post.
     * Direct URLs to post galleries do not work with the API.
     *
     * @param galleryUrl Direct URL to a gallery post - e.g "https://www.reddit.com/gallery/hrrh23"
     * @return Full URL or null (invalid shortened URL/Reddit issue)
     */
    @Nullable
    private String expandGalleryUrl(String galleryUrl) {

        // Invalid URL
        if(getUrlType(galleryUrl) != URL_TYPE.GALLERY_POST) {
            return null;
        }

        try {
            Document gallery = Jsoup.connect(galleryUrl).get();

            // Timestamp links back to actual post
            org.jsoup.nodes.Element timestamp = gallery
                    .getElementsByAttributeValue("data-click-id", "timestamp")
                    .first();

            return timestamp.attr("href");
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Check the type of the given Reddit URL.
     * If the type is {@code NONE}, the given URL is not a Reddit URL.
     *
     * @param url URL to check
     * @return Reddit URL type
     */
    public static URL_TYPE getUrlType(String url) {
        if(url.matches(STANDARD_URL_REGEX)) {
            return URL_TYPE.STANDARD;
        }
        else if(url.matches(SHORTENED_VIDEO_URL_REGEX)) {
            return URL_TYPE.SHORTENED_VIDEO;
        }
        else if(url.matches(GALLERY_POST_URL_REGEX)) {
            return URL_TYPE.GALLERY_POST;
        }
        else if(url.matches(VIDEO_URL_REGEX)) {
            return URL_TYPE.STANDARD_VIDEO;
        }
        else if(url.matches(SUBREDDIT_REGEX)) {
            return URL_TYPE.SUBREDDIT;
        }
        return URL_TYPE.NONE;
    }

    /**
     * Check if the given Reddit URL is shortened. Shortened URLs do not work with the API and their full URLs
     * must be resolved to do so.
     * Examples:
     * Direct URLs to a gallery inside a gallery post - e.g "https://www.reddit.com/gallery/hrrh23"
     * Direct URLs to a video inside a video post - e.g  "https://v.redd.it/1li3tukkbje71"
     *
     * @param url Reddit URL
     * @return Reddit URL is shortened
     */
    private static boolean isShortenedUrl(String url) {
        URL_TYPE type = getUrlType(url);
        return type == URL_TYPE.SHORTENED_VIDEO || type == URL_TYPE.GALLERY_POST;
    }

    /**
     * Check if the given URL is a Reddit URL
     *
     * @param url URL to check
     * @return URL is a Reddit URL
     */
    public static boolean isRedditUrl(String url) {
        return getUrlType(url) != URL_TYPE.NONE;
    }

    /**
     * Check if the given URL is a Reddit post URL
     *
     * @param url URL to check
     * @return URL is a Reddit post URL
     */
    private boolean isRedditPostUrl(String url) {
        URL_TYPE type = getUrlType(url);
        return type != URL_TYPE.NONE && type != URL_TYPE.SUBREDDIT;
    }


    /**
     * Reddit videos have the video & audio tracks served separately, attempt to
     * use the https://viddit.red/ website to get a download URL for the combined tracks.
     *
     * @param postUrl URL to the Reddit video post
     * @return Video download URL or null (if there is an error acquiring the download URL)
     */
    @Nullable
    private String getVideoDownloadUrl(String postUrl) {
        String url = "https://viddit.red/?url=" + postUrl;
        try {
            return Jsoup
                    .connect(url)
                    .get()
                    .getElementById("dlbutton")
                    .absUrl("href");
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the content of the post - text/URL/video etc
     *
     * @param post       Post JSON object
     * @param postUrl    URL to the Reddit post
     * @param fetchVideo Fetch video download URL (slower)
     * @return Post content
     */
    private PostContent getPostContent(JSONObject post, String postUrl, boolean fetchVideo) {

        // Media is stored in the original post
        if(post.has(CROSSPOST_KEY)) {
            post = post.getJSONArray(CROSSPOST_KEY).getJSONObject(0);
        }

        final String text = post.getString("selftext");

        // Poll post
        if(post.has(POLL_KEY)) {
            return parsePollDetails(post.getJSONObject(POLL_KEY), text);
        }

        // Video post
        else if(post.getBoolean("is_video")) {
            return parseVideoDetails(postUrl, post, fetchVideo);
        }

        // Absence of text indicates either a link post/image post/embedded video post
        else if(text.isEmpty()) {
            final String url = post.getString(URL_KEY);

            // Link post/embedded video
            if(post.has(HINT_KEY)) {
                final String hint = post.getString(HINT_KEY);

                // Link may lead to an image
                switch(hint) {
                    case "rich:video":
                    case "link":
                        if(isImageUrl(url)) {
                            break;
                        }
                        else if(ImgurManager.isAlbumUrl(url)) {
                            GalleryContent content = new GalleryContent();
                            ArrayList<String> images = ImgurManager.getAlbumImagesByUrl(url);

                            if(images != null) {
                                for(String imageUrl : images) {
                                    content.addImageToGallery(imageUrl);
                                }
                            }

                            return content;
                        }
                        else {
                            return new LinkContent(url);
                        }
                }
            }

            // Gallery post (multiple images)
            if(getUrlType(url) == URL_TYPE.GALLERY_POST) {
                return parseGalleryContent(post);
            }

            // Single image post
            return new ImageContent(processImageUrl(url));
        }

        // Standard text post
        return new TextPostContent(text);
    }

    /**
     * Parse gallery post content from the given JSON data for a gallery post.
     *
     * @param galleryData Gallery post JSON data
     * @return Gallery post content
     */
    private GalleryContent parseGalleryContent(JSONObject galleryData) {
        GalleryContent gallery = new GalleryContent();
        JSONObject mediaList = galleryData.getJSONObject("media_metadata");

        for(String key : mediaList.keySet()) {
            JSONArray mediaUrls = mediaList
                    .getJSONObject(key)
                    .getJSONArray("p");

            JSONObject targetMedia = mediaUrls.getJSONObject(mediaUrls.length() - 1);
            gallery.addImageToGallery(targetMedia.getString("u"));
        }

        return gallery;
    }

    /**
     * Parse poll post content from the given JSON data for a poll.
     *
     * @param pollData Poll JSON data
     * @param postText Text from the poll post - usually describes poll
     * @return Poll post content
     */
    private PollContent parsePollDetails(JSONObject pollData, String postText) {
        JSONArray optionsData = pollData.getJSONArray("options");

        PollContent.RedditPoll.Option[] options = new PollContent.RedditPoll.Option[optionsData.length()];

        // Parse poll options
        for(int i = 0; i < optionsData.length(); i++) {
            JSONObject optionData = optionsData.getJSONObject(i);
            final String text = optionData.getString("text");

            // Option votes are not available until poll closes
            options[i] = optionData.has(VOTE_COUNT_KEY)
                    ? new PollContent.RedditPoll.Option(text, optionData.getInt(VOTE_COUNT_KEY))
                    : new PollContent.RedditPoll.Option(text);
        }

        return new PollContent(
                postText,
                new PollContent.RedditPoll(
                        pollData.getInt("total_vote_count"),
                        new Date(pollData.getLong("voting_end_timestamp")),
                        options
                )
        );
    }

    /**
     * Parse video post content from the given video post JSON data.
     * Attempt to get a download URL for the video with sound (if it has sound),
     * otherwise provide a URL to the video without sound.
     *
     * @param postUrl    URL to video post
     * @param fetchVideo Fetch video download URL (slower)
     * @param videoData  Video post JSON data
     * @return Video post content
     */
    private VideoPostContent parseVideoDetails(String postUrl, JSONObject videoData, boolean fetchVideo) {
        JSONObject details = videoData.getJSONObject(MEDIA_KEY).getJSONObject("reddit_video");
        Boolean audioStatus = getAudioStatus(details);

        return new VideoPostContent(
                videoData.getString(THUMBNAIL_KEY),

                /*
                 * Only attempt to get a download URL for the video + audio if the post indicates an audio track is
                 * present or this is unable to be checked (just in case).
                 */
                fetchVideo && (audioStatus == null || audioStatus) ? getVideoDownloadUrl(postUrl) : null,

                details.getString("fallback_url"), // No audio URL
                audioStatus
        );
    }

    /**
     * Check if the video post associated with the given video JSON data has an audio track.
     * This can be done by querying the {@code dash_url} value and checking for the presence of an audio track in
     * the given XML. If this fails, null will be returned meaning the post may or may not have audio.
     *
     * @param videoData JSON data of video from a video post
     * @return Video has audio or null (if unable to determine)
     */
    @Nullable
    private Boolean getAudioStatus(JSONObject videoData) {
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            NodeList tracks = docBuilder.parse(videoData.getString("dash_url"))
                    .getDocumentElement()
                    .getElementsByTagName("Period")
                    .item(0)
                    .getChildNodes();

            // Iterate through tracks and return true if an audio track is found
            for(int i = 0; i < tracks.getLength(); i++) {
                Node trackNode = tracks.item(i);

                // Not an element
                if(trackNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                // Audio track found
                if(((org.w3c.dom.Element) trackNode).getAttribute("contentType").equals("audio")) {
                    return true;
                }
            }
            return false;
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Check if the given URL found in a Reddit post is an image URL.
     * Sometimes posts are marked as a link but that link is an image.
     *
     * @param url URL from Reddit post
     * @return URL is an image
     */
    private boolean isImageUrl(String url) {
        return ImgurManager.isImageUrl(url) || GIFCommand.isGifUrl(url);
    }

    /**
     * Refactor the image/gif URL if it will not embed properly in discord
     *
     * @param url Image/gif URL
     * @return Refactored URL
     */
    private String processImageUrl(String url) {
        if(ImgurManager.isImageUrl(url)) {

            // gifv -> gif
            if(url.endsWith(".gifv")) {
                url = url.substring(0, url.length() - 1);
            }
            return url;
        }

        if(GIFCommand.isGifUrl(url)) {
            GIFCommand.GIF gif = GIFCommand.getGifByUrl(url);
            if(gif != null) {
                url = gif.getUrl();
            }
        }
        return url;
    }

    /**
     * Calculate the ratio of upvotes to downvotes.
     * Reddit API doesn't provide the number of downvotes but provides the number of upvotes and the upvote percentage.
     *
     * @param upvotes     Number of upvotes
     * @param upvoteRatio Ratio of upvotes - e.g 0.97
     * @return Ratio of upvotes/downvotes
     */
    private Ratio calculateRatio(int upvotes, double upvoteRatio) {
        upvoteRatio = upvoteRatio * 100;
        double remaining = 100 - upvoteRatio;

        return new Ratio(
                upvotes,
                (int) (remaining * (upvotes / upvoteRatio))
        );
    }

    /**
     * Get the subreddit info for the given subreddit name
     *
     * @param name Name of subreddit
     * @return Subreddit info or null (invalid subreddit)
     */
    @Nullable
    private Subreddit getSubredditByName(String name) {
        try {
            String json = new NetworkRequest(BASE_URL + "/r/" + name + "/about" + JSON, false).get().body;

            JSONObject info = new JSONObject(json)
                    .getJSONObject(DATA_KEY);

            String icon = info.getString("icon_img");
            String communityIcon = info.getString("community_icon").split("\\?")[0];

            return new Subreddit(
                    info.getString("display_name_prefixed"),
                    BASE_URL + info.getString(URL_KEY),
                    icon.isEmpty() ? (communityIcon.isEmpty() ? LOGO : communityIcon) : icon
            );
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get a list of the current top posts for the current subreddit
     *
     * @param subreddit Subreddit to retrieve posts for
     * @return List of current top posts in the subreddit or null (issue retrieving posts)
     */
    public ArrayList<RedditPost> getPostsBySubreddit(Subreddit subreddit) {
        try {
            ArrayList<RedditPost> posts = new ArrayList<>();

            NetworkResponse response = new NetworkRequest(subreddit.getUrl() + JSON, false).get();
            JSONArray postList = new JSONObject(response.body)
                    .getJSONObject(DATA_KEY)
                    .getJSONArray(CHILDREN_KEY);

            for(int i = 0; i < postList.length(); i++) {
                posts.add(parsePost(postList.getJSONObject(i), false, subreddit));
            }

            return posts;
        }
        catch(Exception e) {
            return new ArrayList<>();
        }
    }
}
