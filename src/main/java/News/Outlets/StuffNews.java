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

/**
 * Stuff NZ news parsing using their mobile app API
 */
public class StuffNews extends NewsOutlet {
    private static final String
            DOMAIN = "stuff.co.nz",
            BASE_URL = "https://www." + DOMAIN + "/",
            API_ENTRY = "_json/";

    /**
     * Initialise Stuff news values
     */
    public StuffNews() {
        super(
                "Stuff",
                "https://i.imgur.com/zXaHmO1.png",
                "https://(www|i)." + DOMAIN + "/[A-Za-z0-9-]+/.+",
                EmbedHelper.STUFF_NEWS
        );
    }

    @Override
    public @Nullable Article parseArticleByUrl(String articleUrl) {

        // "CATEGORY/ID/PATH-TO-ARTICLE"
        String articleLocation = articleUrl.split(DOMAIN)[1].replaceFirst("/", "").trim();

        // https://www.stuff.co.nz/_json/CATEGORY/ID/PATH-TO-ARTICLE
        String dataUrl = BASE_URL + API_ENTRY + articleLocation;
        return parseArticle(dataUrl);
    }

    /**
     * Parse an article using the Stuff API from the given article data URL.
     * This is the API URL to the JSON format of the article.
     *
     * @param dataUrl API URL to article JSON
     * @return Article or null (if any errors occur/URL is not a news article)
     */
    @Nullable
    private Article parseArticle(String dataUrl) {
        NetworkResponse response = new NetworkRequest(dataUrl, false).get();
        if(response.code == NetworkResponse.TIMEOUT_CODE || response.code == 404) {
            return null;
        }

        JSONObject data = new JSONObject(response.body);

        JSONArray imageList = data.getJSONArray("images");
        ArrayList<Image> images = new ArrayList<>();
        final String sourceKey = "src", captionKey = "caption";

        for(int i = 0; i < imageList.length(); i++) {
            JSONObject imageSummary = imageList.getJSONObject(i);
            JSONArray variants = imageSummary.getJSONArray("variants");
            final String caption = imageSummary.has(captionKey) ? imageSummary.getString(captionKey) : null;
            String imageSrc = null;

            for(int j = 0; j < variants.length(); j++) {
                JSONObject variant = variants.getJSONObject(j);

                // Look for the largest image
                if(!variant.getString("layout").equals("Standard Image")) {
                    continue;
                }
                imageSrc = variant.getString(sourceKey);
            }

            // If not present for whatever reason take the first image
            if(imageSrc == null) {
                imageSrc = variants.getJSONObject(0).getString(sourceKey);
            }
            images.add(new Image(imageSrc, caption));
        }

        ArrayList<Author> authors = new ArrayList<>();
        String author = data.getString("byline");

        if(author != null) {
            authors.add(new Author(author, null, null));
        }

        return new Article(
                BASE_URL + data.getString("path").replaceFirst("/", ""),
                dataUrl,
                authors,
                data.getString("title"),
                data.getString("intro"),
                parseDate(data.getString("datetime_iso8601")),
                images
        );
    }
}
