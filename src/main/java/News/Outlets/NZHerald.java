package News.Outlets;

import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Network.Secret;
import News.Article;
import News.Author;
import News.Image;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.pkcs.RSAPrivateKey;
import org.spongycastle.util.io.pem.PemReader;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * NZ Herald news parsing using their mobile app API
 */
public class NZHerald extends NewsOutlet {
    private static final String
            BASE_URL = "https://www.nzherald.co.nz/",
            API_URL = "https://appapi.nzherald.co.nz",
            ARTICLE_CONTENT_TYPE = "article",
            IMAGE_TYPE = "image",
            VIDEO_TYPE = "video",
            TYPE_KEY = "type";

    private final HashMap<String, Integer> resolutionOrderMap;

    /**
     * Initialise NZ Herald values
     */
    public NZHerald() {
        super(
                "NZ Herald",
                "https://i.imgur.com/hszDKPY.png",
                BASE_URL + "(.+)/(.+)(/)?(.+)?",
                EmbedHelper.RED
        );
        this.resolutionOrderMap = getResolutionOrderMap();
    }

    /**
     * Image resolutions are referred to in the API with a key in the format {name}_{size}
     * e.g "original_medium" or "square_large"
     * Create a map of size -> index of size (smallest to largest).
     * This can then be used to sort a list of resolution keys by comparing the index of the size.
     *
     * @return Map of resolution size to size index (smallest to largest)
     */
    private HashMap<String, Integer> getResolutionOrderMap() {
        HashMap<String, Integer> resolutionOrderMap = new HashMap<>();
        resolutionOrderMap.put("medium", 0);
        resolutionOrderMap.put("large", 1);
        resolutionOrderMap.put("xlarge", 2);
        return resolutionOrderMap;
    }

    /**
     * Image resolutions are referred to in the API with a key in the format {name}_{size}
     * e.g "original_medium" or "square_large".
     * Return the size of the given resolution key - e.g "original_medium" -> "medium"
     *
     * @param resolutionKey Resolution key - e.g "original_medium"
     * @return Resolution size - e.g "medium"
     */
    private String getResolutionSizeFromKey(String resolutionKey) {
        return resolutionKey.split("_")[1];
    }

    /**
     * Image resolutions are referred to in the API with a key in the format {name}_{size}
     * e.g "original_medium" or "square_large".
     * Return the index of the given resolution size - e.g "medium" -> 0.
     * This index is in order of smallest to largest.
     * If the resolution key is not present, -1 is returned.
     *
     * @param resolutionSize Resolution size - e.g "medium"
     * @return Resolution size index - e.g 0 (or -1 if the size is not found)
     */
    private int getResolutionSizeIndex(String resolutionSize) {
        return resolutionOrderMap.getOrDefault(resolutionSize, -1);
    }

    @Override
    protected @Nullable Article parseArticleByUrl(String articleUrl) {
        try {

            // Sometimes URL looks like -> [article URL]#Echobox=1631416616-1 & the "echobox" breaks the API response
            articleUrl = articleUrl.split("#")[0];
            JSONObject articleData = fetchArticleDataByUrl(articleUrl);

            // URL is not an article/doesn't exist
            if(articleData == null) {
                throw new Exception("Unable to locate article data for URL: " + articleUrl);
            }

            return new Article(
                    articleUrl,
                    API_URL + getArticleDataUrlEndpoint(articleUrl),
                    parseAuthors(articleData),
                    articleData.getString("headline"),
                    articleData.getString("quickread"),
                    parseDate(articleData.getString("publish_date")),
                    parseImages(articleData)
            );
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Perform an article search for the given query.
     * Return the JSON response or null (if a response isn't received)
     *
     * @param query Query to search
     * @return Return the JSON response or null (if a response isn't received)
     */
    @Nullable
    private JSONArray searchArticles(String query) {
        JSONObject response = apiRequest("/v2/api/search/?q=" + EmbedHelper.urlEncode(query));

        // Issue with request
        if(response == null) {
            return null;
        }

        return response.getJSONArray("results");
    }

    /**
     * Parse a list of images from the given JSON data of an article
     *
     * @param articleData JSON data of article to get images from
     * @return List of images in article JSON (may be empty)
     */
    private ArrayList<Image> parseImages(JSONObject articleData) {
        ArrayList<Image> images = new ArrayList<>();
        final String contentKey = "content";

        JSONObject hero = articleData.getJSONObject("hero");
        final String heroType = hero.getString(TYPE_KEY);

        // Hero section may be image/video/etc
        if(heroType.equals(IMAGE_TYPE) || heroType.equals(VIDEO_TYPE)) {
            images.add(parseImageOrVideo(hero.getJSONObject(contentKey), heroType.equals(IMAGE_TYPE)));
        }

        // Video articles may not have further images after the video preview
        if(!articleData.has(contentKey)) {
            return images;
        }

        // The content list is an array of various pieces of the article - e.g text, images, html, etc
        JSONArray contentList = articleData.getJSONArray(contentKey);

        for(int i = 0; i < contentList.length(); i++) {
            JSONObject content = contentList.getJSONObject(i);
            final String contentType = content.getString(TYPE_KEY);
            if(!contentType.equals(IMAGE_TYPE) && !contentType.equals(VIDEO_TYPE)) {
                continue;
            }
            images.add(parseImageOrVideo(content.getJSONObject(contentKey), contentType.equals(IMAGE_TYPE)));
        }
        return images;
    }

    /**
     * Parse an image or video from the given image JSON.
     * This JSON contains the caption and various resolutions of the image.
     *
     * @param content Image/video JSON
     * @param image   Content is an image
     * @return Image/video thumbnail
     */
    private Image parseImageOrVideo(JSONObject content, boolean image) {
        final String resolutionsKey = "resolutions";
        JSONObject resolutions = image
                ? content.getJSONObject(resolutionsKey)
                : content.getJSONObject(IMAGE_TYPE).getJSONObject(resolutionsKey);

        ArrayList<String> resolutionKeys = new ArrayList<>(resolutions.keySet());

        // Sort the provided resolution keys in order of smallest to largest
        resolutionKeys.sort(Comparator.comparingInt(o -> getResolutionSizeIndex(getResolutionSizeFromKey(o))));

        final String largestResolution = resolutionKeys.get(resolutionKeys.size() - 1);

        final String captionKey = "caption";
        return new Image(
                resolutions.getJSONObject(largestResolution).getString("url"),
                content.has(captionKey) ? content.getString(captionKey) : null
        );
    }

    /**
     * Parse a list of authors from the given JSON data of an article
     *
     * @param articleData JSON data of article to get authors from
     * @return List of the article authors (may be empty)
     */
    private ArrayList<Author> parseAuthors(JSONObject articleData) {
        ArrayList<Author> authors = new ArrayList<>();
        final String authorsKey = "authors", imageKey = "image";

        JSONObject authorSummary = articleData.getJSONObject("author");

        // No authors
        if(!authorSummary.has("authors")) {
            return authors;
        }

        JSONArray authorData = authorSummary.getJSONArray(authorsKey);

        for(int i = 0; i < authorData.length(); i++) {
            JSONObject author = authorData.getJSONObject(i);
            String name = author.getString("name");

            authors.add(
                    new Author(
                            author.getString("name"),
                            author.has(imageKey) ? author.getString(imageKey) : null,
                            BASE_URL + "author/" + name.toLowerCase().replaceAll(" ", "-")

                    )
            );
        }
        return authors;
    }

    /**
     * Attempt to fetch the JSON of an article using its ID via the NZ Herald mobile app API.
     * This takes the ID of an article (found only in API responses).
     * The ID found in an article URL is the UID, which will not return a response from the API.
     *
     * @param articleId ID of the article (NOT UID)
     * @return Article JSON or null (if the given ID doesn't exist/isn't an article)
     */
    @Nullable
    private JSONObject fetchArticleDataById(String articleId) {
        return fetchArticleData(getArticleDataIdEndpoint(articleId));
    }

    /**
     * Attempt to fetch the JSON of an article using its URL via the NZ Herald mobile app API.
     *
     * @param articleUrl URL to an article - e.g "https://www.nzherald.co.nz/{CATEGORY}/frank-died/{ARTICLE_UID}"
     * @return Article JSON or null (if the given URL doesn't exist/isn't an article)
     */
    @Nullable
    private JSONObject fetchArticleDataByUrl(String articleUrl) {
        return fetchArticleData(getArticleDataUrlEndpoint(articleUrl));
    }

    /**
     * Attempt to fetch the JSON of an article using the given article endpoint.
     *
     * @param articleEndpoint Article API endpoint being requested - e.g "/v2/api/content/{ARTICLE_ID}"
     * @return Article JSON or null (if the article isn't found/result isn't an article)
     */
    @Nullable
    private JSONObject fetchArticleData(String articleEndpoint) {
        JSONObject response = apiRequest(articleEndpoint);

        // ID doesn't exist
        if(response == null) {
            return null;
        }

        final String type = response.getString(TYPE_KEY);

        // ID may not be for an article/video (a video has similar data to an article)
        return type.equals(ARTICLE_CONTENT_TYPE) || type.equals(VIDEO_TYPE) ? response : null;
    }

    /**
     * Get the API endpoint required to make a request for the data of an article by its ID.
     *
     * @param articleId Unique ID of the article - e.g "V6O6KF2MP5EG7ANINGF337QVP4"
     * @return API endpoint for requesting article data by ID - e.g "/v2/api/content/"V6O6KF2MP5EG7ANINGF337QVP4"
     */
    private String getArticleDataIdEndpoint(String articleId) {
        return "/v2/api/content/" + EmbedHelper.urlEncode(articleId);
    }

    /**
     * Get the API endpoint required to make a request for the data of an article by its URL.
     * A URL must end in a '/' to return a response. Parameters will be stripped and a '/' added if required.
     *
     * @param articleUrl URL to an article - e.g "https://www.nzherald.co.nz/{CATEGORY}/frank-died/{ARTICLE_UID}"
     * @return API endpoint for requesting article data by URL - e.g "/v2/api/link?url={ENCODED_URL}"
     */
    private String getArticleDataUrlEndpoint(String articleUrl) {
        final String end = "/";
        articleUrl = articleUrl.split("\\?")[0]; // Strip parameters

        // Add '/' if required
        if(!articleUrl.endsWith(end)) {
            articleUrl += end;
        }

        return "/v2/api/content/link/?url=" + EmbedHelper.urlEncode(articleUrl);
    }

    /**
     * Make a request to the NZ Herald mobile app API.
     *
     * @param endpoint API endpoint being requested - e.g "/v2/api/content/{ARTICLE_ID}"
     * @return JSON response or null (if request failed)
     */
    @Nullable
    private JSONObject apiRequest(String endpoint) {
        final String url = API_URL + endpoint;
        NetworkResponse response = new NetworkRequest(url, false).get(generateRequestHeaders(endpoint));

        // Request failed - could be auth/etc
        if(response.code != 200) {
            return null;
        }

        return new JSONObject(response.body);
    }

    /**
     * Generate the required headers to authenticate with the NZ Herald mobile app API.
     *
     * @param endpoint API endpoint being requested - e.g "/v2/api/content/{ARTICLE_ID}"
     * @return Map of header name -> header value
     */
    private HashMap<String, String> generateRequestHeaders(String endpoint) {
        final String dateString = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
                .format(new Date());

        HashMap<String, String> headers = new HashMap<>();
        headers.put("date", dateString);
        headers.put("authorization", generateAuthorizationHeader(endpoint, dateString));
        return headers;
    }

    /**
     * Generate the required authorization header for authenticating with the NZ Herald mobile app API.
     * This involves signing the request URL & date of request with a private key.
     *
     * @param endpoint   API endpoint being requested - e.g "/v2/api/content/{ARTICLE_ID}"
     * @param dateString Formatted date of request - e.g "Wed, 14 Jul 2021 08:12:32 GMT"
     * @return Authorization header value
     */
    private String generateAuthorizationHeader(String endpoint, String dateString) {
        final String keyId = "nzh_app";
        final String algorithm = "rsa-sha256";

        final String intro = "Signature keyId=\"" + keyId + "\""
                + ","
                + "algorithm=\"" + algorithm + "\""
                + ","
                + "headers=\""
                + "(request-target) date\""
                + ","
                + "signature=";

        return intro + "\"" + generateSignature(endpoint, dateString) + "\"";
    }

    /**
     * Generate the signature for authenticating with the NZ Herald mobile app API.
     *
     * @param endpoint   API endpoint being requested - e.g "/v2/api/content/{ARTICLE_ID}"
     * @param dateString Formatted date of request - e.g "Wed, 14 Jul 2021 08:12:32 GMT"
     * @return Signature required to authenticate or null (if unable to generate)
     */
    @Nullable
    private String generateSignature(String endpoint, String dateString) {
        try {
            final String valueToSign = "(request-target): get " + endpoint + "\ndate: " + dateString;
            final byte[] bytes = valueToSign.getBytes(StandardCharsets.UTF_8);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(getPrivateKey());
            signature.update(bytes);
            return Base64.getEncoder().encodeToString(signature.sign());
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the private key to use when making requests to the NZ Herald API
     *
     * @return Private key
     */
    private PrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        final StringReader keyStringReader = new StringReader(Secret.NZ_HERALD_PRIVATE_KEY);
        final byte[] keyData = new PemReader(keyStringReader).readPemObject().getContent();

        RSAPrivateKey instance = RSAPrivateKey.getInstance(new ASN1InputStream(keyData).readObject());
        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(
                instance.getModulus(),
                instance.getPrivateExponent()
        );

        return KeyFactory
                .getInstance("RSA")
                .generatePrivate(keySpec);
    }

    @Override
    public Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(dateString);
        }
        catch(ParseException e) {
            return new Date();
        }
    }
}
