package News.Outlets;

import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Network.NetworkResponse;
import News.Article;
import News.Author;
import News.Image;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import static Command.Structure.HTMLUtils.stripHtml;

/**
 * The Guardian news parsing using their web API
 */
public class Guardian extends NewsOutlet {
    private final static String BASE_URL = "https://www.theguardian.com/";

    /**
     * Initialise The Guardian values
     */
    public Guardian() {
        super(
                "The Guardian",
                "https://i.imgur.com/2pd59wD.jpg",
                BASE_URL + "(.+)/(.+)(/)?(.+)?",
                EmbedHelper.BLUE
        );
    }

    @Override
    protected @Nullable Article parseArticleByUrl(String articleUrl) {
        final String dataUrl = getDataUrlFromArticleUrl(articleUrl);
        NetworkResponse response = new NetworkRequest(dataUrl, false).get();

        // Unable to retrieve data
        if(response.code != 200) {
            return null;
        }

        try {
            final String contentKey = "lightboxImages", introKey = "standfirst";
            JSONObject articleData = new JSONObject(response.body)
                    .getJSONObject("config")
                    .getJSONObject("page");

            JSONObject contentData = articleData.has(contentKey) ? articleData.getJSONObject(contentKey) : null;

            return new Article(
                    articleUrl,
                    dataUrl,
                    parseAuthors(articleData),
                    articleData.getString("headline"),
                    contentData == null || !contentData.has(introKey)
                            ? null
                            : stripHtml(contentData.getString(introKey)),
                    new Date(articleData.getLong("webPublicationDate")),
                    contentData == null ? null : parseImages(contentData)
            );
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Parse a list of article images from the given article JSON.
     * This list may be empty.
     *
     * @param articleData Article JSON data
     * @return List of images in the article
     */
    private ArrayList<Image> parseImages(JSONObject articleData) {
        final String imagesKey = "images", captionKey = "caption";
        ArrayList<Image> images = new ArrayList<>();

        // No images
        if(!articleData.has(imagesKey)) {
            return images;
        }

        JSONArray imagesList = articleData.getJSONArray(imagesKey);
        for(int i = 0; i < imagesList.length(); i++) {
            JSONObject imageData = imagesList.getJSONObject(i);
            images.add(
                    new Image(
                            imageData.getString("src"),
                            imageData.has(captionKey) ? stripHtml(imageData.getString(captionKey)) : null
                    )
            );
        }
        return images;
    }

    /**
     * Parse a list of article authors from the given article JSON.
     * This list may be empty.
     *
     * @param articleData Article JSON data
     * @return List of article authors
     */
    private ArrayList<Author> parseAuthors(JSONObject articleData) {
        final String authorKey = "author";
        ArrayList<Author> authors = new ArrayList<>();

        if(articleData.has(authorKey)) {
            String[] authorNames = articleData.getString(authorKey).split(",");

            // Authors are provided as comma separated String
            for(String authorName : authorNames) {
                authors.add(new Author(authorName.trim()));
            }
        }

        return authors;
    }

    /**
     * The Guardian gives the article data at {ARTICLE_URL}.json
     * Create this URL from the given article URL.
     *
     * @param articleUrl URL to an article - e.g "https://www.theguardian.com/{CATEGORY}/{DATE}/{HEADLINE-SLUG}/"
     * @return URL to article data - e.g "https://www.theguardian.com/{CATEGORY}/{DATE}/{HEADLINE-SLUG}.json"
     */
    private String getDataUrlFromArticleUrl(String articleUrl) {
        String noParams = articleUrl.split("\\?")[0]; // Strip parameters

        // Trailing slash must be removed
        if(noParams.endsWith("/")) {
            noParams = noParams.substring(0, noParams.length() - 1);
        }
        return noParams + ".json";
    }
}
