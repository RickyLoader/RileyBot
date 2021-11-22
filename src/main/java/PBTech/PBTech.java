package PBTech;

import Command.Structure.HTMLUtils;
import PBTech.Product.StarRating;
import Steam.Price;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 * www.pbtech.co.nz functions
 */
public class PBTech {
    private static final String
            BASE_URL = "https://www.pbtech.co.nz/",
            PRODUCT_REGEX = BASE_URL + "product/.+/.+";

    public static final String LOGO = "https://i.imgur.com/G6K6mpv.png";

    /**
     * Check if the given String is a URL to a product
     *
     * @param url URL to check
     * @return URL is a product URL
     */
    public static boolean isProductUrl(String url) {
        return url.matches(PRODUCT_REGEX);
    }

    /**
     * Get the details of a product by the URL to the product
     *
     * @param productUrl URL to the product
     * @return Product details or null
     */
    @Nullable
    public static Product getProductByUrl(String productUrl) {
        try {
            if(!isProductUrl(productUrl)) {
                throw new Exception(productUrl + " is not a valid PBTech product URL");
            }

            // Embedded in product page
            JSONArray productJson = HTMLUtils.getEmbeddedJsonArray(Jsoup.connect(productUrl).get());

            if(productJson.isEmpty()) {
                throw new Exception("No JSON found on product page: " + productUrl);
            }

            return parseProduct(productJson.getJSONArray(0).getJSONObject(1), productUrl);
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Attempt to parse the given JSON representing a product in to a product.
     *
     * @param productJson Product JSON embedded in product page
     * @param url         URL to the product page
     * @return Product or null
     */
    @Nullable
    private static Product parseProduct(JSONObject productJson, String url) {
        try {

            // Parse images
            JSONArray imageJson = productJson.getJSONArray("image");
            String[] images = new String[imageJson.length()];
            for(int i = 0; i < imageJson.length(); i++) {
                images[i] = imageJson.getString(i);
            }

            JSONObject offer = productJson.getJSONArray("offers").getJSONObject(0);

            // Rating not always present
            final String ratingKey = "aggregateRating";
            StarRating rating = null;

            if(productJson.has(ratingKey)) {
                JSONObject ratingInfo = productJson.getJSONObject(ratingKey);
                rating = new StarRating(
                        ratingInfo.getDouble("ratingValue"),
                        ratingInfo.getInt("reviewCount")
                );
            }

            return new Product(
                    productJson.getString("name"),
                    productJson.getString("description"),
                    url,
                    new Price(
                            offer.getDouble("price"),
                            offer.getString("priceCurrency")
                    ),
                    rating,
                    images
            );
        }
        catch(Exception e) {
            return null;
        }
    }
}
