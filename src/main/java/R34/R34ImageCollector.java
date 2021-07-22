package R34;

import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * Grab cool images
 */
public class R34ImageCollector {
    public static final String THUMBNAIL = "https://i.imgur.com/IPRigRp.png";
    private final static String
            BASE_URL = "https://rule34.xxx/index.php?page=",
            BASE_VIEW_URL = BASE_URL + "post&s=view&id=",
            BASE_API_SEARCH_URL = BASE_URL + "dapi&q=index&json=1&s=",
            BASE_IMAGE_SEARCH_BY_TAGS_URL = BASE_API_SEARCH_URL + "post&tags=",
            BASE_IMAGE_SEARCH_BY_ID_URL = BASE_API_SEARCH_URL + "post&id=",
            BASE_TAG_SEARCH_URL = BASE_URL + "tags&s=list&sort=asc&order_by=updated&tags=";

    /**
     * Search for images with the given tag query.
     * Tags are whitespace separated e.g "garen lol" is two tags.
     * Search operators found in the cheat sheet may be used e.g "( tag1 ~ tag2 )" to search for images that have either
     * "tag1" or "tag2" (note the whitespace).
     *
     * @param tagQuery Tag search query e.g "garen"
     * @return List of images found for the given tag query or null (if the API doesn't respond)
     * @see <a href="https://rule34.xxx/index.php?page=help&topic=cheatsheet">For cheat sheet</a>
     */
    @Nullable
    public static ArrayList<Image> searchImagesByTag(String tagQuery) {
        return searchImages(BASE_IMAGE_SEARCH_BY_TAGS_URL + EmbedHelper.urlEncode(tagQuery));
    }

    /**
     * Search for an image with the given URL.
     *
     * @param imageUrl URL to an image
     * @return Image from URL or null (if the API doesn't respond or the image is not found)
     */
    @Nullable
    public static Image searchImagesByUrl(String imageUrl) {
        if(!isImageUrl(imageUrl)) {
            return null;
        }
        final String id = imageUrl.replace(BASE_VIEW_URL, "").trim();
        return searchImagesById(id);
    }

    /**
     * Check if the given query is the URL to an image
     *
     * @param query Query to check
     * @return Query is an image URL
     */
    public static boolean isImageUrl(String query) {
        return query.matches(Pattern.quote(BASE_VIEW_URL) + "(\\d+)");
    }

    /**
     * Search for an image with the given ID.
     *
     * @param id Image ID - e.g "4921679"
     * @return Image found for the given ID or null (if the API doesn't respond or the ID doesn't exist)
     */
    @Nullable
    private static Image searchImagesById(String id) {
        ArrayList<Image> results = searchImages(BASE_IMAGE_SEARCH_BY_ID_URL + id);

        // API error or no results found
        if(results == null || results.isEmpty()) {
            return null;
        }

        return results.get(0);
    }

    /**
     * Parse the given image JSON in to an object.
     *
     * @param imageData JSON data for an image
     * @return Image or null (if not an image)
     */
    @Nullable
    private static Image parseImage(JSONObject imageData) {
        final String imageUrl = imageData.getString("file_url");
        if(imageUrl.endsWith(".mp4")) {
            return null;
        }
        return new Image(
                imageUrl,
                BASE_VIEW_URL + imageData.getLong("id"),
                imageData.getString("owner"),
                getUniqueTags(imageData.getString("tags").split(" ")),
                imageData.getInt("score"),
                new Date(imageData.getLong("change") * 1000)
        );
    }

    /**
     * Get an array of unique tags from the given array of tags from the API.
     * Image tags from the API often contain duplicates.
     *
     * @param tags Array of tags to remove duplicates from
     * @return Array of unique tags
     */
    private static String[] getUniqueTags(String[] tags) {
        HashSet<String> seen = new HashSet<>();
        ArrayList<String> uniqueTags = new ArrayList<>();

        for(String tag : tags) {
            if(seen.contains(tag)) {
                continue;
            }
            uniqueTags.add(tag);
            seen.add(tag);
        }

        return uniqueTags.toArray(new String[0]);
    }

    /**
     * Make a request to the given image search API URL and return the JSON array response.
     * If no response is received, return null.
     * When no results are received they send an empty response body, return an empty JSON array in this case.
     *
     * @param searchUrl API search URL to get response from
     * @return JSON array results or null (if an error occurs)
     */
    @Nullable
    private static JSONArray getImageSearchResponse(String searchUrl) {
        final String responseBody = new NetworkRequest(searchUrl, false).get().body;

        // Issue with API - rarely happens
        if(responseBody == null) {
            return null;
        }

        return responseBody.isEmpty() ? new JSONArray() : new JSONArray(responseBody);
    }

    /**
     * Make a request to the given image search API URL and parse the response in to images.
     *
     * @param searchUrl API search URL to get response from
     * @return List of images found for the given search URL or null (if the API doesn't respond)
     */
    @Nullable
    private static ArrayList<Image> searchImages(String searchUrl) {
        JSONArray resultData = getImageSearchResponse(searchUrl);

        // Error getting results
        if(resultData == null) {
            return null;
        }

        // Parse response data in to images
        ArrayList<Image> results = new ArrayList<>();
        for(int i = 0; i < resultData.length(); i++) {
            Image image = parseImage(resultData.getJSONObject(i));

            // Not an image
            if(image == null) {
                continue;
            }

            results.add(image);
        }

        return results;
    }

    /**
     * Search for image tags on the website by name.
     * The website search is exact, and will not return similar tags.
     * e.g "shrek" will not return any tags, as the shrek tag is actually named "shrek_(character)".
     * To get around this, attach wildcards to the tag - e.g "*shrek*" to get all results containing "shrek".
     *
     * @param tagName Tag name - e.g "shrek"
     * @return List of tags found for the given query or null (if too many results are found they do not display them)
     */
    @Nullable
    public static ArrayList<Tag> searchTags(String tagName) {
        try {
            // Attach wildcards "*" to get all similar results
            final String url = BASE_TAG_SEARCH_URL + "*" + tagName.toLowerCase() + "*";

            Document document = Jsoup.connect(url).get();
            Elements resultRows = document
                    .getElementById("content")
                    .getElementsByClass("highlightable")
                    .first()
                    .select(".highlightable tr:not(:first-child)");

            // Errors are stored as the first row in the result table
            String firstRowText = resultRows.get(0).child(1).text().toLowerCase();

            // Either too many results or no results - return appropriately
            if(firstRowText.contains("refine your search")) {
                return firstRowText.contains("no results found") ? new ArrayList<>() : null;
            }

            ArrayList<Tag> tags = new ArrayList<>();

            // Parse results
            for(Element result : resultRows) {
                tags.add(
                        new Tag(
                                result.child(1).text(),
                                result.child(2).text().replace("(edit)", "").trim(),
                                Integer.parseInt(result.child(0).text())
                        )
                );
            }
            return tags;
        }
        catch(Exception e) {
            return null;
        }
    }
}
