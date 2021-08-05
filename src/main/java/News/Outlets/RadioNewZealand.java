package News.Outlets;

import Command.Structure.EmbedHelper;
import Command.Structure.HTMLUtils;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Network.Secret;
import News.Article;
import News.Author;
import News.Image;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * RNZ article parsing using their mobile app API
 */
public class RadioNewZealand extends NewsOutlet {
    private final HashMap<String, String> headers;
    private static final String
            BASE_URL = "https://www.rnz.co.nz",
            API_HOST = "api.rnz.co.nz",
            SUMMARY_KEY = "synopsis",
            RELATIVE_PATH_KEY = "publicWebPath",
            IMAGE_KEY = "image";

    /**
     * Initialise RNZ values
     */
    public RadioNewZealand() {
        super(
                "RNZ",
                "https://i.imgur.com/WM2pBTw.png",
                BASE_URL + "/news/.+/\\d+/.+",
                EmbedHelper.RED
        );
        this.headers = createAuthenticationHeaders();
    }

    /**
     * Create a map of header key -> header value. These are the headers required to authenticate with the RNZ API.
     * Without the {@code Devicetype} header, only articles previously requested with the header will be available,
     * it must be included to request new articles.
     *
     * @return Map of authentication headers
     */
    private HashMap<String, String> createAuthenticationHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("X-Api-Key", Secret.RNZ_KEY);
        headers.put("X-Rnz-Devicetype", "androidphone");
        return headers;
    }

    @Override
    protected @Nullable Article parseArticleByUrl(String articleUrl) {
        final String uid = getArticleUidByUrl(articleUrl);

        // Unable to get UID
        if(uid == null) {
            return null;
        }

        JSONObject articleData = fetchArticleDataByUid(uid);

        // Issue with RNZ API
        if(articleData == null) {
            return null;
        }

        // Parse authors
        ArrayList<Author> authors = new ArrayList<>();
        JSONArray authorData = articleData.getJSONArray("authors");

        for(int i = 0; i < authorData.length(); i++) {
            JSONObject author = authorData.getJSONObject(i);
            authors.add(
                    new Author(
                            author.getString("name"),
                            author.has(IMAGE_KEY) ? author.getString(IMAGE_KEY) : null,
                            author.has(RELATIVE_PATH_KEY)
                                    ? BASE_URL + author.getString(RELATIVE_PATH_KEY)
                                    : null
                    )
            );
        }

        return new Article(
                articleUrl,
                getArticleDataUrl(uid),
                authors,
                articleData.getString("title"),
                articleData.has(SUMMARY_KEY) ? articleData.getString(SUMMARY_KEY) : null,
                parseDate(articleData.getString("publishedAt")),
                parseImages(articleData)
        );
    }

    /**
     * Parse a list of images from the given JSON data of an article.
     * The hero image is provided explicitly, but additional images are not.
     * The additional images (if any) can be found in the HTML of the article, this is returned as part of the
     * article JSON.
     *
     * @param articleData Article JSON data
     * @return List of images from the article
     */
    private ArrayList<Image> parseImages(JSONObject articleData) {
        ArrayList<Image> images = new ArrayList<>();

        String heroImageUrl = articleData.has(IMAGE_KEY) ? articleData.getString(IMAGE_KEY) : null;

        try {
            Document articleDocument = Jsoup.parse(articleData.getString("body"));
            Elements imageElements = articleDocument.select(".photo-captioned-full img");

            for(Element image : imageElements) {
                final String caption = HTMLUtils.stripHtml(image.attr("alt"));
                final String url = BASE_URL + image.attr("src");

                // Mark hero image as seen if found (as a caption may be provided here but is never provided otherwise)
                if(url.equals(heroImageUrl)) {
                    heroImageUrl = null;
                }

                images.add(
                        new Image(
                                url,
                                caption.equalsIgnoreCase("no caption") ? null : caption
                        )
                );
            }

            // The hero image is null if not provided or was in the HTML (as the HTML version may have had a caption)
            if(heroImageUrl != null) {
                images.add(new Image(heroImageUrl));
            }
        }
        catch(Exception e) {
            System.out.println("Failed to parse images from article.");
        }

        return images;
    }

    /**
     * Fetch the JSON data of an article by its UID using the RNZ API.
     *
     * @param uid Article UID - e.g "1781f30f-c694-4c7e-ab50-14901fd3f3cf"
     * @return Article JSON data or null (if an API issue occurs)
     */
    @Nullable
    private JSONObject fetchArticleDataByUid(String uid) {
        try {
            NetworkResponse response = new NetworkRequest(getArticleDataUrl(uid), false).get(headers);
            return new JSONObject(response.body).getJSONObject("article");
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the URL used to retrieve the JSON data of an article by its UID.
     *
     * @param uid Article UID - e.g "1781f30f-c694-4c7e-ab50-14901fd3f3cf"
     * @return URL to article JSON data
     */
    private String getArticleDataUrl(String uid) {
        return "https://" + API_HOST + "/mobile/v1/article/" + uid;
    }

    /**
     * Attempt to get the UID of an article via its URL. This can be found in the HTML for the article as part of a
     * shortened link to the article. There is an ID present in the standard URL however this cannot be used with
     * the API.
     *
     * @param articleUrl URL to an article - e.g "https://www.rnz.co.nz/news/{category}/{id}/{article-title-slug}"
     * @return Article UID - e.g "1781f30f-c694-4c7e-ab50-14901fd3f3cf" or null (if unable to retrieve UID)
     */
    @Nullable
    private String getArticleUidByUrl(String articleUrl) {
        try {
            Document articleDocument = Jsoup.connect(articleUrl).get();
            String[] shortUrlArgs = articleDocument

                    // <link rel="amphtml" href="https://amp.rnz.co.nz/article/1781f30f-c694-4c7e-ab50-14901fd3f3cf">
                    .getElementsByAttributeValue("rel", "amphtml")
                    .first()

                    // https://amp.rnz.co.nz/article/1781f30f-c694-4c7e-ab50-14901fd3f3cf
                    .attr("href")
                    .split("/");

            // 1781f30f-c694-4c7e-ab50-14901fd3f3cf
            return shortUrlArgs[shortUrlArgs.length - 1];
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Parse the given article date String in to a Date.
     *
     * @param dateString Date String to parse - e.g "2021-07-19T14:38:19+12:00"
     * @return Date from date String (or today's date if unable to parse)
     */
    @Override
    public Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(dateString);
        }
        catch(ParseException e) {
            return new Date();
        }
    }
}
