package Command.Structure;

import org.jetbrains.annotations.Nullable;
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
}
