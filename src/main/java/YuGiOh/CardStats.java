package YuGiOh;

/**
 * Stats on card total & weekly views, upvotes, downvotes, and price
 */
public class CardStats {
    private final long totalViews, weeklyViews, upvotes, downvotes;
    private final double price;

    /**
     * Create the card stats
     *
     * @param builder Card stats builder
     */
    private CardStats(CardStatsBuilder builder) {
        this.totalViews = builder.totalViews;
        this.weeklyViews = builder.weeklyViews;
        this.upvotes = builder.upvotes;
        this.downvotes = builder.downvotes;
        this.price = builder.price;
    }

    /**
     * Get the total views of the card
     *
     * @return Total views
     */
    public long getTotalViews() {
        return totalViews;
    }

    /**
     * Get the current weekly views of the card
     *
     * @return Weekly views
     */
    public long getWeeklyViews() {
        return weeklyViews;
    }

    /**
     * Get the upvotes of the card
     *
     * @return Card upvotes
     */
    public long getUpvotes() {
        return upvotes;
    }

    /**
     * Get the downvotes of the card
     *
     * @return Card downvotes
     */
    public long getDownvotes() {
        return downvotes;
    }

    /**
     * Check if the card has a known lowest price
     *
     * @return Card has price
     */
    public boolean hasPrice() {
        return price > 0;
    }

    /**
     * Get the lowest price of the card formatted with a dollar sign
     *
     * @return Price formatted as String
     */
    public String getFormattedPrice() {
        return "$" + price;
    }

    public static class CardStatsBuilder {
        private long totalViews, weeklyViews, upvotes, downvotes;
        private double price;

        /**
         * Set the total views of the card
         *
         * @param totalViews Total views
         * @return Builder
         */
        public CardStatsBuilder setTotalViews(long totalViews) {
            this.totalViews = totalViews;
            return this;
        }

        /**
         * Set the current weekly views of the card
         *
         * @param weeklyViews Weekly views
         * @return Builder
         */
        public CardStatsBuilder setWeeklyViews(long weeklyViews) {
            this.weeklyViews = weeklyViews;
            return this;
        }

        /**
         * Set the card upvotes
         *
         * @param upvotes Card upvotes
         * @return Builder
         */
        public CardStatsBuilder setUpvotes(long upvotes) {
            this.upvotes = upvotes;
            return this;
        }

        /**
         * Set the card downvotes
         *
         * @param downvotes Card downvotes
         * @return Builder
         */
        public CardStatsBuilder setDownvotes(long downvotes) {
            this.downvotes = downvotes;
            return this;
        }

        /**
         * Set the lowest price of the card in dollars
         *
         * @param price Card price
         * @return Builder
         */
        public CardStatsBuilder setPrice(double price) {
            this.price = price;
            return this;
        }

        /**
         * Build the card stats
         *
         * @return Card stats
         */
        public CardStats build() {
            return new CardStats(this);
        }

    }
}
