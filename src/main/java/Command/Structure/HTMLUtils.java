package Command.Structure;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Utils for Jsoup HTML parsing
 */
public class HTMLUtils {

    /**
     * Get a value from a meta tag in the given HTML document.
     * E.g with the following meta tag: <meta property="og:title" content="Cool title"/>
     * To retrieve "Cool title", {@code attributeKey} = "property" and {@code attributeValue} = "og:title"
     *
     * @param document       HTML document to retrieve tag from
     * @param attributeKey   Key of the meta attribute to retrieve - e.g "name", "property" etc
     * @param attributeValue Value of the key to retrieve e.g "og:type", "description" etc
     * @return Meta tag value or null
     */
    @Nullable
    public static String getMetaTagValue(Document document, String attributeKey, String attributeValue) {
        Elements metaTags = document.getElementsByTag("meta");
        Element desiredTag = null;

        for(Element metaTag : metaTags) {
            if(!metaTag.attr(attributeKey).equals(attributeValue)) {
                continue;
            }
            desiredTag = metaTag;
        }
        return desiredTag == null ? null : desiredTag.attr("content");
    }

    /**
     * Attempt to retrieve a JSON array containing the embedded JSON from the given HTML document.
     * This is JSON-LD which usually contains a basic overview of the page.
     *
     * @param document HTML document to retrieve JSON from
     * @return JSON array or null
     */
    public static JSONArray getEmbeddedJsonArray(Document document) {
        Elements scripts = document
                .getElementsByTag("script")
                .attr("type", "application/ld+json");

        JSONArray results = new JSONArray();

        for(Element script : scripts) {
            String value = script.html();

            try {
                if(value.startsWith("[") && value.endsWith("]")) {
                    results.put(new JSONArray(value));
                }
                else if(value.startsWith("{") && value.endsWith("}")) {
                    results.put(new JSONObject(value));
                }
            }
            catch(Exception e) {
                System.out.println("Failed to parse value as JSON: " + value);
            }
        }
        return results;
    }

    /**
     * Attempt to strip HTML formatting from the given String
     *
     * @param text Text to strip HTML from
     * @return Text stripped of HTML (or unchanged text if unable to parse)
     */
    public static String stripHtml(String text) {
        try {
            return Jsoup.parse(text).text();
        }
        catch(Exception e) {
            return text;
        }
    }
}
