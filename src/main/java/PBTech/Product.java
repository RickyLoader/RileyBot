package PBTech;

import Steam.Price;
import org.jetbrains.annotations.Nullable;

/**
 * Product from PBTech
 */
public class Product {
    private final String name, description, url;
    private final String[] images;
    private final Price price;
    private final StarRating starRating;

    /**
     * Create a product
     *
     * @param name        Product name
     * @param description Product description
     * @param url         URL to the product
     * @param price       Price of the product
     * @param starRating  Optional product rating
     * @param images      Array of product image URLs
     */
    public Product(String name, String description, String url, Price price, @Nullable StarRating starRating, String[] images) {
        this.name = name;
        this.description = description;
        this.url = url;
        this.price = price;
        this.images = images;
        this.starRating = starRating;
    }

    /**
     * Get the star rating of the product (out of 5 stars w/ x reviews).
     * This is null if unavailable.
     *
     * @return Star rating
     */
    @Nullable
    public StarRating getStarRating() {
        return starRating;
    }

    /**
     * Get the price of the product
     *
     * @return Product price
     */
    public Price getPrice() {
        return price;
    }

    /**
     * Get an array of product image URLs
     *
     * @return Array of product image URLs
     */
    public String[] getImages() {
        return images;
    }

    /**
     * Get the URL to the product
     *
     * @return URL to product
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the name of the product
     *
     * @return Product name
     */
    public String getName() {
        return name;
    }

    /**
     * Get a short description of the product
     *
     * @return Product description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Overall rating of the product
     */
    public static class StarRating {
        private final double stars;
        private final int reviews;
        public static final double MAX_STARS = 5.0;

        /**
         * Create the star rating for the product.
         *
         * @param stars   Average number of stars for the reviews (out of 5)
         * @param reviews Total number of reviews averaged to get the stars
         */
        public StarRating(double stars, int reviews) {
            this.stars = stars;
            this.reviews = reviews;
        }

        /**
         * Get the average number of stars for the reviews (out of 5)
         *
         * @return Average stars
         */
        public double getStars() {
            return stars;
        }

        /**
         * Get the total number of reviews averaged to get the stars
         *
         * @return Number of reviews
         */
        public int getReviews() {
            return reviews;
        }
    }
}
