package News.Outlets;

import Command.Structure.EmbedHelper;
import Command.Structure.HTMLUtils;
import Network.NetworkRequest;
import Network.NetworkResponse;
import News.Article;
import News.Author;
import News.Image;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;

/**
 * One News article parsing using their mobile app API
 */
public class OneNews extends NewsOutlet {
    private static final String
            BASE_URL = "https://www.tvnz.co.nz/",
            BASE_ARTICLE_URL = BASE_URL + "one-news/",
            API_URL = "https://api.tvnz.co.nz/api/content/tvnz/news/story.androidphone.v3.json/";

    /**
     * Initialise One News values
     */
    public OneNews() {
        super(
                "One News",
                "https://i.imgur.com/4Gp3qnH.png",
                BASE_ARTICLE_URL + "(.+)/(.+)(/)?(.+)?",
                EmbedHelper.RED
        );
    }

    @Override
    protected @Nullable Article parseArticleByUrl(String articleUrl) {
        articleUrl = articleUrl.split("\\?")[0]; // Strip parameters
        final String relativeMobilePath = getRelativeMobilePath(articleUrl);

        // Unable to find mobile path, can't use web URL with API
        if(relativeMobilePath == null) {
            return null;
        }

        JSONObject articleData = getArticleDataByRelativeMobilePath(relativeMobilePath);

        // Issue retrieving article data
        if(articleData == null) {
            return null;
        }

        try {
            JSONArray bodyList = articleData.getJSONArray("body");
            final String textKey = "text";
            JSONObject firstElement = bodyList.getJSONObject(0);

            return new Article(
                    articleUrl,
                    getArticleDataUrl(relativeMobilePath),
                    parseAuthors(articleData),
                    articleData.getString("heading"),
                    firstElement.has(textKey) ? HTMLUtils.stripHtml(firstElement.getString(textKey)) : null,
                    parseDate(articleData.getString("updatedTime")),
                    parseImages(bodyList)
            );
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse a list of authors from the given JSON data of an article
     *
     * @param articleData JSON data of article to get authors from
     * @return List of the article authors (may be empty)
     */
    private ArrayList<Author> parseAuthors(JSONObject articleData) {
        final String sourcesKey = "sources";
        ArrayList<Author> authors = new ArrayList<>();

        // No authors available
        if(!articleData.has(sourcesKey)) {
            return authors;
        }

        JSONArray sources = articleData.getJSONArray(sourcesKey);
        for(int i = 0; i < sources.length(); i++) {
            authors.add(new Author(sources.getString(i)));
        }
        return authors;
    }


    /**
     * Parse a list of images from the given JSON data of an article's page body.
     * This list contains the various elements of the article - images, text, etc.
     *
     * @param bodyList JSON list of body elements of an article to get images from
     * @return List of images in article JSON (may be empty)
     */
    private ArrayList<Image> parseImages(JSONArray bodyList) {
        final String imageType = "image", videoType = "video";
        ArrayList<Image> images = new ArrayList<>();

        for(int i = 0; i < bodyList.length(); i++) {
            JSONObject bodyItem = bodyList.getJSONObject(i);
            final String itemType = bodyItem.getString("type");

            if(!itemType.equals(imageType) && !itemType.equals(videoType)) {
                continue;
            }

            String targetKey, captionKey, imageKey;

            // Set image/caption keys and target JSON to retrieve values from
            if(itemType.equals(imageType)) {
                targetKey = "gallery"; // Image values nested inside gallery
                captionKey = "posterImageCaption";
                imageKey = "posterImageUrl";
            }
            else {
                targetKey = "video"; // Video image values nested inside video
                captionKey = "caption";
                imageKey = "imageUrl";
            }

            JSONObject target = bodyItem.getJSONObject(targetKey);
            images.add(
                    new Image(

                            // Given as "/path/to/image" -> remove first slash to append to base URL
                            BASE_URL + target.getString(imageKey).replaceFirst("/", ""),
                            target.has(captionKey) ? target.getString(captionKey) : null
                    )
            );
        }
        return images;
    }

    /**
     * Make a request to the given article URL and attempt to scrape the relative mobile URL path from the HTML.
     * This is the parameter required by {@link #getArticleDataByRelativeMobilePath(String)} to retrieve data about an
     * article.
     * It is found in a meta tag that is used to open an article within the IOS app and is in the
     * format "app-id={APP_ID}, app-argument=tvnz-one-news://story/{RELATIVE_PATH}"
     *
     * @param articleUrl URL to article - e.g "https://www.tvnz.co.nz/one-news/{CATEGORY}/{TITLE_SLUG}"
     * @return Relative mobile path or null (if unable to locate) e.g "content/onenews/story/{CATEGORY}/{TITLE_SLUG}"
     */
    @Nullable
    private String getRelativeMobilePath(String articleUrl) {
        try {
            Document articleDocument = Jsoup.connect(articleUrl).get();

            // "app-id={APP_ID}, app-argument=tvnz-one-news://story/{RELATIVE_PATH}"
            final String mobileArguments = HTMLUtils.getMetaTagValue(
                    articleDocument,
                    "name",
                    "apple-itunes-app"
            );

            // Mobile arguments not found
            if(mobileArguments == null) {
                throw new Exception("Unable to locate mobile URL!");
            }

            return mobileArguments
                    .split(",")[1].trim() // "app-argument=tvnz-one-news://story/{RELATIVE_PATH}"
                    .replaceFirst("app-argument=tvnz-one-news://story/", ""); // {RELATIVE_PATH}
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the URL required to fetch data for an article with the given relative mobile path.
     *
     * @param relativeMobilePath Relative path to article in mobile app - e.g "content/onenews/story/{CATEGORY}/{TITLE_SLUG}"
     * @return URL to fetch article data for given path
     */
    private String getArticleDataUrl(String relativeMobilePath) {
        return API_URL + EmbedHelper.urlEncode(relativeMobilePath);
    }

    /**
     * Make a request to the One News mobile app API with a relative path to an article. If the article is found,
     * the API will return the JSON data of the article.
     * The relative path from a web article URL cannot be used, as it is different to the mobile path:
     * Relative web: /one-news/new-zealand/dunedin-farmer-protest-gets-heated-woman-has-sign-ripped-her-hands
     * Relative mobile: /content/tvnz/onenews/story/2021/07/16/dunedin-farmers-protest-gets-heated-as-environmental-supporter-h
     *
     * @param relativeMobilePath Relative path to article in mobile app - e.g "content/onenews/story/{CATEGORY}/{TITLE_SLUG}"
     * @return Article JSON data or null (if unable to retrieve)
     */
    @Nullable
    private JSONObject getArticleDataByRelativeMobilePath(String relativeMobilePath) {
        NetworkResponse response = new NetworkRequest(getArticleDataUrl(relativeMobilePath), false).get();

        // No response for path
        if(response.code != 200) {
            return null;
        }
        return new JSONObject(response.body);
    }
}
