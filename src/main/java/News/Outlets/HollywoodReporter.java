package News.Outlets;

import Command.Structure.EmbedHelper;
import Command.Structure.HTMLUtils;
import Network.NetworkRequest;
import News.Article;
import News.Author;
import News.Image;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Hollywood Reporter article parsing using their API
 *
 * @see <a href="https://www.hollywoodreporter.com/wp-json">API reference</a>
 */
public class HollywoodReporter extends NewsOutlet {
    private static final String
            BASE_URL = "https://www.hollywoodreporter.com/",
            BASE_API_URL = BASE_URL + "wp-json/wp/v2/",
            MEDIA_API_URL = BASE_API_URL + "media/",
            RENDERED_KEY = "rendered",
            IMAGE_TYPE = "image";

    /**
     * Initialise Hollywood Reporter values
     */
    public HollywoodReporter() {
        super(
                "Hollywood Reporter",
                "https://i.imgur.com/Xc1s0x0.jpg",
                BASE_URL + "[a-z-\\d/]+(-\\d+)(/)?(.+)?",
                EmbedHelper.RED
        );
    }

    @Override
    protected @Nullable Article parseArticleByUrl(String articleUrl) {
        articleUrl = articleUrl.split("\\?")[0]; // Strip parameters
        final long articleId = getArticleIdFromUrl(articleUrl);

        JSONObject articleData = getArticleDataById(articleId);

        // Issue with API
        if(articleData == null) {
            return null;
        }

        final String introKey = "excerpt";
        String intro = null;
        if(articleData.has(introKey)) {
            intro = HTMLUtils.stripHtml(articleData.getJSONObject(introKey).getString(RENDERED_KEY));
        }

        final String authorKey = "author";
        ArrayList<Author> authors = new ArrayList<>();
        if(articleData.has(authorKey)) {
            Author author = getAuthorById(articleData.getLong(authorKey));
            if(author != null) {
                authors.add(author);
            }
        }

        // Article images (excluding the 'featured' image)
        ArrayList<Image> images = getArticleImages(articleId);

        final String mainImageKey = "featured_media";
        if(articleData.has(mainImageKey)) {
            JSONObject featuredMediaData = getMediaDataById(articleData.getLong(mainImageKey));

            // Featured media may not be an image, could be video etc
            if(featuredMediaData != null) {
                Image image = parseImage(featuredMediaData);
                if(image != null) {
                    images.add(image);
                }
            }
        }

        return new Article(
                articleUrl,
                getArticleDataUrl(articleId),
                authors,
                HTMLUtils.stripHtml(articleData.getJSONObject("title").getString(RENDERED_KEY)),
                intro,
                parseDate(articleData.getString("date")),
                images
        );
    }

    /**
     * Get the JSON data of an article from its unique ID
     *
     * @param articleId Unique ID of article - e.g 1234965092
     * @return JSON data of article or null (if the API doesn't respond)
     */
    @Nullable
    private JSONObject getArticleDataById(long articleId) {
        try {
            return new JSONObject(new NetworkRequest(getArticleDataUrl(articleId), false).get().body);
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the JSON data of an article's media by the unique article ID
     *
     * @param articleId Unique ID of article - e.g 1234965092
     * @return JSON data of article media or null (if the API doesn't respond)
     */
    @Nullable
    private JSONArray getArticleMediaDataById(long articleId) {
        try {
            final String url = MEDIA_API_URL + "?parent=" + articleId + "&media_type=" + IMAGE_TYPE;
            return new JSONArray(new NetworkRequest(url, false).get().body);
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the JSON data of a media asset from its unique ID
     *
     * @param mediaId Unique ID of the media - e.g 1234978945
     * @return JSON data of media or null (if the API doesn't respond)
     */
    private JSONObject getMediaDataById(long mediaId) {
        try {
            return new JSONObject(new NetworkRequest(MEDIA_API_URL + mediaId, false).get().body);
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the JSON data of an author from their unique ID
     *
     * @param authorId Unique ID of author - e.g 1223253
     * @return JSON data of author or null (if the API doesn't respond)
     */
    @Nullable
    private JSONObject getAuthorDataById(long authorId) {
        try {
            return new JSONObject(new NetworkRequest(BASE_API_URL + "users/" + authorId, false).get().body);
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the URL required to retrieve the JSON data of an article
     *
     * @param articleId Unique ID of article - e.g 1234965092
     * @return URL to JSON data of article
     */
    private String getArticleDataUrl(long articleId) {
        return BASE_API_URL + "posts/" + articleId;
    }

    /**
     * Attempt to parse the given media data in to an image.
     * If the media data does not represent an image (e.g video), the result will be null.
     *
     * @param mediaData JSON data of media
     * @return Image or null (if not an image)
     */
    private Image parseImage(JSONObject mediaData) {

        // Not an image, could be video/text/audio etc
        if(!mediaData.getString("media_type").equals(IMAGE_TYPE)) {
            return null;
        }

        // May be empty
        final String caption = HTMLUtils.stripHtml(mediaData.getJSONObject("caption").getString(RENDERED_KEY));

        return new Image(
                mediaData.getJSONObject("guid").getString(RENDERED_KEY),
                caption.isEmpty() ? null : caption
        );
    }

    /**
     * Get the ID from an article URL. The ID is used to make requests to their API.
     *
     * @param articleUrl URL to an article
     * @return Unique ID of article - e.g 1234965092
     */
    private long getArticleIdFromUrl(String articleUrl) {

        // Remove trailing slash
        if(articleUrl.endsWith("/")) {
            articleUrl = articleUrl.substring(0, articleUrl.length() - 1);
        }

        String[] urlArgs = articleUrl.split("/");

        // ID is within the title slug - e.g "title-of-article-1234965092"
        String[] slugArgs = urlArgs[urlArgs.length - 1].split("-");

        return Long.parseLong(slugArgs[slugArgs.length - 1]);
    }


    /**
     * Get a list of images in an article by the unique article ID.
     *
     * @param articleId Unique ID of article - e.g 1234965092
     * @return List of images in the article
     */
    private ArrayList<Image> getArticleImages(long articleId) {
        ArrayList<Image> images = new ArrayList<>();

        JSONArray mediaData = getArticleMediaDataById(articleId);

        // No response
        if(mediaData == null) {
            return images;
        }

        for(int i = 0; i < mediaData.length(); i++) {
            Image image = parseImage(mediaData.getJSONObject(i));

            // Media may not be an image
            if(image == null) {
                continue;
            }

            images.add(image);
        }
        return images;
    }

    /**
     * Get an author by their unique ID.
     *
     * @param authorId Unique ID of author - e.g 1223253
     * @return Author or null (if the API doesn't respond)
     */
    @Nullable
    private Author getAuthorById(long authorId) {
        JSONObject authorData = getAuthorDataById(authorId);

        // No response
        if(authorData == null) {
            return null;
        }

        return new Author(
                authorData.getString("name"),
                authorData.getJSONObject("avatar_urls").getString("96"),
                authorData.getString("link")
        );
    }
}
