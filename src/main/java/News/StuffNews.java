package News;

import Network.NetworkRequest;
import Network.NetworkResponse;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Stuff NZ news parsing using their mobile app API
 */
public class StuffNews {
    private static final String
            BASE_URL = "https://www.stuff.co.nz/",
            ARTICLE_URL = BASE_URL + "[A-Za-z0-9-]+/.+",
            API_ENTRY = "_json/";
    public static final String LOGO = "https://i.imgur.com/zXaHmO1.png";

    /**
     * Check if the given URL is a Stuff news URL
     *
     * @param query Query to check
     * @return Query is a Stuff news Url
     */
    public static boolean isNewsUrl(String query) {
        return query.matches(ARTICLE_URL);
    }

    /**
     * Get the Stuff article info from the given URL.
     *
     * @param articleUrl URL to article
     * @return Article info or null (if any errors occur/URL is not a news article)
     */
    @Nullable
    public static Article getArticleByUrl(String articleUrl) {
        if(!isNewsUrl(articleUrl)) {
            return null;
        }

        String articleLocation = articleUrl.replaceFirst(BASE_URL, ""); // "CATEGORY/ID/PATH-TO-ARTICLE"

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
    private static Article parseArticle(String dataUrl) {
        NetworkResponse response = new NetworkRequest(dataUrl, false).get();
        if(response.code == NetworkResponse.TIMEOUT_CODE || response.code == 404) {
            return null;
        }

        JSONObject data = new JSONObject(response.body);

        JSONArray imageList = data.getJSONArray("images");
        ArrayList<String> images = new ArrayList<>();
        final String sourceKey = "src";

        for(int i = 0; i < imageList.length(); i++) {
            JSONArray variants = imageList.getJSONObject(i).getJSONArray("variants");
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
            images.add(imageSrc);
        }

        String author = data.getString("byline");
        return new Article(
                BASE_URL + data.getString("path").replaceFirst("/", ""),
                dataUrl,
                author.isEmpty() ? null : author,
                data.getString("title"),
                data.getString("intro"),
                parseDate(data.getString("datetime_iso8601")),
                images
        );
    }

    /**
     * Parse the given Stuff API date String in to a Date.
     * The date String should be in ISO 8601 format.
     *
     * @param dateString Date String in ISO 8601 format - e.g "20200320T212450+1300"
     * @return Date from date String (or today's date if unable to parse)
     */
    private static Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyyMMdd'T'HHmmssZZZZZ").parse(dateString);
        }
        catch(ParseException e) {
            return new Date();
        }
    }
}
