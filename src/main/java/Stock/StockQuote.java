package Stock;

/**
 * Stock info on exchange
 */
public class StockQuote {
    private final double openPrice, highPrice, lowPrice, currentPrice, previousClosePrice, diff, diffPercent;
    private final Company company;

    /**
     * Create the stock quote from the builder
     *
     * @param builder Builder
     */
    private StockQuote(StockQuoteBuilder builder) {
        this.openPrice = builder.openPrice;
        this.highPrice = builder.highPrice;
        this.lowPrice = builder.lowPrice;
        this.currentPrice = builder.currentPrice;
        this.previousClosePrice = builder.previousClosePrice;
        this.company = builder.company;
        this.diff = currentPrice - previousClosePrice;
        this.diffPercent = Math.abs(diff) / (previousClosePrice / 100);
    }

    public static class StockQuoteBuilder {
        private double openPrice, highPrice, lowPrice, currentPrice, previousClosePrice;
        private Company company;

        /**
         * Set the company that the stock quote is for
         *
         * @param company Company
         * @return Builder
         */
        public StockQuoteBuilder setCompany(Company company) {
            this.company = company;
            return this;
        }

        /**
         * Set the previous closing price of the stock
         *
         * @param previousClosePrice Previous closing price
         * @return Builder
         */
        public StockQuoteBuilder setPreviousClosePrice(double previousClosePrice) {
            this.previousClosePrice = previousClosePrice;
            return this;
        }

        /**
         * Set the highest price of the day for the stock
         *
         * @param highPrice Highest price of the day
         * @return Builder
         */
        public StockQuoteBuilder setHighPrice(double highPrice) {
            this.highPrice = highPrice;
            return this;
        }

        /**
         * Set the lowest price of the day for the stock
         *
         * @param lowPrice Lowest price of the day
         * @return Builder
         */
        public StockQuoteBuilder setLowPrice(double lowPrice) {
            this.lowPrice = lowPrice;
            return this;
        }

        /**
         * Set the opening stock price of the day
         *
         * @param openPrice Opening stock price of the day
         * @return Builder
         */
        public StockQuoteBuilder setOpenPrice(double openPrice) {
            this.openPrice = openPrice;
            return this;
        }

        /**
         * Set the current price of the stock
         *
         * @param currentPrice Current price of stock
         * @return Builder
         */
        public StockQuoteBuilder setCurrentPrice(double currentPrice) {
            this.currentPrice = currentPrice;
            return this;
        }

        /**
         * Build the stock quote
         *
         * @return Stock quote from builder values
         */
        public StockQuote build() {
            return new StockQuote(this);
        }
    }

    /**
     * Get the company that the stock belongs to
     *
     * @return Company that stock belongs to
     */
    public Company getCompany() {
        return company;
    }

    /**
     * Get the current price of the stock
     *
     * @return Current price
     */
    public double getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Get the highest price of the day for the stock
     *
     * @return Highest price
     */
    public double getHighPrice() {
        return highPrice;
    }

    /**
     * Get the lowest price of the day for the stock
     *
     * @return Lowest price
     */
    public double getLowPrice() {
        return lowPrice;
    }

    /**
     * Get the opening price of the day for the stock
     *
     * @return Opening price
     */
    public double getOpenPrice() {
        return openPrice;
    }

    /**
     * Get the previous closing price of the stock
     *
     * @return Previous closing price
     */
    public double getPreviousClosePrice() {
        return previousClosePrice;
    }

    /**
     * Get the monetary difference between the previous closing price and the current price
     *
     * @return Difference monetary
     */
    public double getDiff() {
        return diff;
    }

    /**
     * Get the difference between previous closing price and current price as
     * a percentage
     *
     * @return Difference percentage
     */
    public double getDiffPercent() {
        return diffPercent;
    }
}
