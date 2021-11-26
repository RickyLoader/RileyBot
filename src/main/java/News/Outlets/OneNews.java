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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * One News article parsing using their mobile app API
 */
public class OneNews extends NewsOutlet {
    private static final String
            OLD_DOMAIN = "https://www.tvnz.co.nz/",
            NEW_DOMAIN = "https://www.1news.co.nz/",
            ARTICLE_REGEX = "(.+)/(.+)(/)?(.+)?",
            OLD_ARTICLE_REGEX = OLD_DOMAIN + "one-news/" + ARTICLE_REGEX,
            NEW_ARTICLE_REGEX = NEW_DOMAIN + ARTICLE_REGEX,
            BASE_API_URL = "https://apis.1news.co.nz/mobile/api/",
            IMAGE_TYPE = "image",
            VIDEO_TYPE = "video",
            CONTENT_KEY = "content";

    /**
     * Initialise One News values
     */
    public OneNews() {
        super(
                "One News",
                "https://i.imgur.com/4Gp3qnH.png",
                OLD_ARTICLE_REGEX,
                EmbedHelper.RED
        );
    }

    @Override
    protected @Nullable Article parseArticleByUrl(String articleUrl) {
        final String articleId = getArticleIdFromUrl(articleUrl);
        JSONObject articleData = getArticleDataById(articleId);

        // Issue retrieving article data
        if(articleData == null) {
            return null;
        }

        try {

            // This array contains the various elements of the article - images, text, etc.
            JSONArray content = articleData.getJSONArray(CONTENT_KEY);

            ArrayList<Author> authors = new ArrayList<>();
            ArrayList<Image> images = new ArrayList<>();
            String intro = null;
            Date date = new Date();

            final String captionKey = "caption";

            for(int i = 0; i < content.length(); i++) {
                JSONObject data = content.getJSONObject(i);
                final String type = data.getString("type");

                switch(type) {
                    case VIDEO_TYPE:
                    case IMAGE_TYPE:
                        JSONObject imageData = data.getJSONObject(CONTENT_KEY);
                        images.add(
                                new Image(
                                        type.equals(IMAGE_TYPE)
                                                ? imageData.getString("url")
                                                : imageData.getString("image"),
                                        imageData.has(captionKey) ? imageData.getString(captionKey) : null
                                )
                        );
                        break;
                    case "attribution-item":
                        JSONObject authorData = data.getJSONObject(CONTENT_KEY);
                        authors.add(
                                new Author(
                                        authorData.getString("reporter")
                                )
                        );
                        date = parseDate(authorData.getString("updatedTime"));
                        break;
                    case "intro-text":
                        intro = data.getString(CONTENT_KEY);
                }
            }

            return new Article(
                    articleUrl,
                    getArticleDataUrl(articleId),
                    authors,
                    articleData.getString("heading"),
                    intro,
                    date,
                    images
            );
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Parse the ID of an article from a URL to the article.
     * The ID is simply the relative URL path and is the parameter required
     * by {@link #getArticleDataById(String)}.
     *
     * @param articleUrl URL to article - e.g "https://www.1news.co.nz/{DATE}/{TITLE_SLUG}"
     * @return Article ID - e.g "{DATE}/{TITLE_SLUG}"
     */
    private String getArticleIdFromUrl(String articleUrl) {
        return articleUrl
                .split("\\?")[0] // Strip parameters
                .replaceFirst(articleUrl.startsWith(OLD_DOMAIN) ? OLD_DOMAIN : NEW_DOMAIN, "");
    }

    /**
     * Get the URL required to fetch data for an article by its ID.
     *
     * @param articleId Article ID - e.g "{DATE}/{TITLE_SLUG}"
     * @return URL to fetch article data for given ID - e.g "https://apis.1news.co.nz/mobile/api/story/{ARTICLE_ID}"
     */
    private String getArticleDataUrl(String articleId) {
        return BASE_API_URL + "story/" + EmbedHelper.urlEncode(articleId);
    }

    /**
     * Make a request to the One News mobile app API with the ID of an article. If the article is found,
     * the API will return the JSON data of the article.
     *
     * @param articleId Article ID - e.g "{DATE}/{TITLE_SLUG}"
     * @return Article JSON data or null (if unable to retrieve)
     */
    @Nullable
    private JSONObject getArticleDataById(String articleId) {
        NetworkResponse response = new NetworkRequest(getArticleDataUrl(articleId), false).get();

        // No response for ID
        if(response.code != 200) {
            return null;
        }

        return new JSONObject(response.body);
    }

    @Override
    public Date parseDate(String dateString) {
        try {

            // 2021-11-25T18:36:00.000+13:00
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(dateString);
        }
        catch(ParseException e) {
            return new Date();
        }
    }

    /**
     * Override as One News has changed their domain from "www.tvnz.co.nz" to "www.1news.co.nz", the old domain still
     * works but redirects to the new domain.
     */
    @Override
    public boolean isNewsUrl(String query) {
        return query.matches(OLD_ARTICLE_REGEX) || query.matches(NEW_ARTICLE_REGEX);
    }
}
