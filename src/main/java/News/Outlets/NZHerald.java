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
        super("NZ Herald", "https://i.imgur.com/hszDKPY.png", BASE_URL + "(.+)/(.+)(/)?(.+)?");
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
     * Return the size of the given resolution key - e.g "original_medium" -> medium
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
            final String articleId = getArticleIdByUrl(articleUrl);

            // Unable to make headline slug -> ID transform
            if(articleId == null) {
                throw new Exception("Unable to locate ID from headline slug: " + getHeadlineSlugFromUrl(articleUrl));
            }

            JSONObject articleData = fetchArticleDataById(articleId);

            // ID is not an article/doesn't exist
            if(articleData == null) {
                throw new Exception("Unable to locate article data for ID: " + articleId);
            }

            return new Article(
                    articleUrl,
                    API_URL + getArticleDataEndpoint(articleId),
                    parseAuthors(articleData),
                    articleData.getString("headline"),
                    articleData.getString("quickread"),
                    parseDate(articleData.getString("publish_date")),
                    parseImages(articleData)
            );
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Get the headline slug from an article URL.
     * This is the headline of the article separated by dashes.
     * e.g "Frank Died" -> "frank-died"
     *
     * @param articleUrl URL to an article - e.g "https://www.nzherald.co.nz/{CATEGORY}/frank-died/{ARTICLE_UID}"
     * @return Headline slug - e.g "frank-died"
     */
    private String getHeadlineSlugFromUrl(String articleUrl) {
        String[] urlArgs = articleUrl
                .split("\\?")[0] // Remove parameters
                .split("/");

        return urlArgs[urlArgs.length - 2]; // Last section is UID, second last is headline slug
    }

    /**
     * Get the ID of an article from a URL to the article.
     * The last section in an article URL (after removing parameters) is the article's UID, whereas the required
     * identifier to get article data is the article's ID.
     * Attempt to locate this ID by performing a search for the headline slug.
     *
     * @param articleUrl URL to an article - e.g "https://www.nzherald.co.nz/{CATEGORY}/{HEADLINE-SLUG}/{ARTICLE_UID}"
     * @return Article ID - e.g "V6O6KF2MP5EG7ANINGF337QVP4" or null (if unable to locate)
     */
    @Nullable
    private String getArticleIdByUrl(String articleUrl) {
        final String query = EmbedHelper.urlEncode(getHeadlineSlugFromUrl(articleUrl));
        JSONObject response = apiRequest("/v2/api/search/?q=" + query);

        // Issue with request
        if(response == null) {
            return null;
        }

        JSONArray results = response.getJSONArray("results");
        return results.isEmpty() ? null : results.getJSONObject(0).getString("id");
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

        // The content list is an array of various pieces of the article - e.g text, images, html, etc
        JSONArray contentList = articleData.getJSONArray("content");

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
     * Attempt to fetch the JSON of an article via the NZ Herald mobile app API.
     *
     * @param articleId Unique ID of the article
     * @return Article JSON or null (if the given ID doesn't exist/isn't an article)
     */
    @Nullable
    private JSONObject fetchArticleDataById(String articleId) {
        JSONObject response = apiRequest(getArticleDataEndpoint(articleId));

        // ID doesn't exist
        if(response == null) {
            return null;
        }

        // ID may not be for an article
        return response.getString("type").equals(ARTICLE_CONTENT_TYPE) ? response : null;
    }

    /**
     * Get the API endpoint required to make a request for the data of an article with the given ID.
     *
     * @param articleId Unique ID of the article - e.g "V6O6KF2MP5EG7ANINGF337QVP4"
     * @return API endpoint for requesting article data - e.g "/v2/api/content/"V6O6KF2MP5EG7ANINGF337QVP4"
     */
    private String getArticleDataEndpoint(String articleId) {
        return "/v2/api/content/" + articleId;
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
